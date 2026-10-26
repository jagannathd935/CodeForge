package com.codeforge.contest;

import com.codeforge.contest.dto.ContestStandingDto;
import com.codeforge.contest.entity.Contest;
import com.codeforge.contest.entity.ContestParticipant;
import com.codeforge.contest.repository.ContestParticipantRepository;
import com.codeforge.contest.repository.ContestProblemRepository;
import com.codeforge.contest.repository.ContestProblemResultRepository;
import com.codeforge.contest.repository.ContestRepository;
import com.codeforge.contest.service.ContestService;
import com.codeforge.problem.repository.ProblemRepository;
import com.codeforge.submission.repository.SubmissionRepository;
import com.codeforge.user.entity.Role;
import com.codeforge.user.entity.User;
import com.codeforge.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContestScoringTest {

    @Mock
    private ContestRepository contestRepository;

    @Mock
    private ContestProblemRepository contestProblemRepository;

    @Mock
    private ContestParticipantRepository contestParticipantRepository;

    @Mock
    private ContestProblemResultRepository contestProblemResultRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProblemRepository problemRepository;

    @Mock
    private SubmissionRepository submissionRepository;

    private ContestService contestService;
    private Contest sampleContest;
    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        contestService = new ContestService(
                contestRepository,
                contestProblemRepository,
                contestParticipantRepository,
                contestProblemResultRepository,
                problemRepository,
                userRepository,
                submissionRepository
        );

        sampleContest = new Contest();
        sampleContest.setId(1L);
        sampleContest.setTitle("Weekly Contest 1");
        sampleContest.setSlug("weekly-contest-1");
        sampleContest.setStatus("LIVE");
        sampleContest.setStartTime(LocalDateTime.now().minusHours(1));
        sampleContest.setEndTime(LocalDateTime.now().plusHours(1));

        user1 = new User("coder1", "c1@test.com", "pass", Role.ROLE_PARTICIPANT);
        user1.setId(101L);

        user2 = new User("coder2", "c2@test.com", "pass", Role.ROLE_PARTICIPANT);
        user2.setId(102L);
    }

    @Test
    void testLeaderboardDeterministicRanking() {
        ContestParticipant p1 = new ContestParticipant(sampleContest, user1);
        p1.setProblemsSolved(2);
        p1.setScore(300);
        p1.setPenaltyMinutes(40);
        p1.setTotalSolveTimeSeconds(1800L);

        ContestParticipant p2 = new ContestParticipant(sampleContest, user2);
        p2.setProblemsSolved(2);
        p2.setScore(300);
        p2.setPenaltyMinutes(20); // Lower penalty -> should rank higher
        p2.setTotalSolveTimeSeconds(1500L);

        List<ContestParticipant> list = new ArrayList<>(List.of(p1, p2));
        when(contestParticipantRepository.findByContestId(1L)).thenReturn(list);

        List<ContestStandingDto> leaderboard = contestService.getContestLeaderboard(1L);

        assertEquals(2, leaderboard.size());
        assertEquals("coder2", leaderboard.get(0).getUsername(), "coder2 should be rank 1 due to lower penalty");
        assertEquals(1, leaderboard.get(0).getRank());
        assertEquals("coder1", leaderboard.get(1).getUsername());
        assertEquals(2, leaderboard.get(1).getRank());
    }

    @Test
    void testTieBreakByLastAcceptedAtWhenPenaltyEqual() {
        LocalDateTime now = LocalDateTime.now();

        ContestParticipant p1 = new ContestParticipant(sampleContest, user1);
        p1.setProblemsSolved(1);
        p1.setScore(100);
        p1.setPenaltyMinutes(0);
        p1.setLastAcceptedAt(now.minusMinutes(10)); // Solved earlier

        ContestParticipant p2 = new ContestParticipant(sampleContest, user2);
        p2.setProblemsSolved(1);
        p2.setScore(100);
        p2.setPenaltyMinutes(0);
        p2.setLastAcceptedAt(now.minusMinutes(5)); // Solved later

        List<ContestParticipant> list = new ArrayList<>(List.of(p2, p1));
        when(contestParticipantRepository.findByContestId(1L)).thenReturn(list);

        List<ContestStandingDto> leaderboard = contestService.getContestLeaderboard(1L);

        assertEquals(2, leaderboard.size());
        assertEquals("coder1", leaderboard.get(0).getUsername(), "coder1 solved earlier and should break the tie");
        assertEquals(1, leaderboard.get(0).getRank());
    }
}
