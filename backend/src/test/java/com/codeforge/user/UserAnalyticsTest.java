package com.codeforge.user;

import com.codeforge.auth.dto.TopicStatDto;
import com.codeforge.auth.dto.UserProfileDto;
import com.codeforge.problem.dto.ProblemListDto;
import com.codeforge.problem.entity.Difficulty;
import com.codeforge.problem.entity.Problem;
import com.codeforge.problem.repository.ProblemRepository;
import com.codeforge.submission.entity.Submission;
import com.codeforge.submission.entity.SubmissionStatus;
import com.codeforge.submission.repository.SubmissionRepository;
import com.codeforge.topic.entity.Topic;
import com.codeforge.user.entity.Role;
import com.codeforge.user.entity.User;
import com.codeforge.user.repository.UserRepository;
import com.codeforge.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAnalyticsTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private ProblemRepository problemRepository;

    private UserService userService;
    private User testUser;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, submissionRepository, problemRepository);
        testUser = new User("analytics_dev", "adev@test.com", "pass", Role.ROLE_PARTICIPANT);
        testUser.setId(88L);
    }

    @Test
    void testStreakIsZeroWhenInactive() {
        when(userRepository.findByUsername("analytics_dev")).thenReturn(Optional.of(testUser));

        Problem p = new Problem();
        p.setId(1L);
        p.setDifficulty(Difficulty.EASY);

        // Submission occurred 5 days ago -> streak must be 0
        Submission oldSub = new Submission(testUser, p, "PYTHON", "print(1)");
        oldSub.setStatus(SubmissionStatus.ACCEPTED);
        oldSub.setCreatedAt(LocalDateTime.now().minusDays(5));

        when(submissionRepository.findByUserIdOrderByCreatedAtDesc(88L)).thenReturn(List.of(oldSub));
        when(problemRepository.findAll()).thenReturn(List.of(p));

        UserProfileDto profile = userService.getCurrentUserProfile("analytics_dev");

        assertEquals(0, profile.getStreak(), "Streak must be 0 if no submission was made today or yesterday");
    }

    @Test
    void testStreakActiveConsecutiveDays() {
        when(userRepository.findByUsername("analytics_dev")).thenReturn(Optional.of(testUser));

        Problem p = new Problem();
        p.setId(1L);
        p.setDifficulty(Difficulty.EASY);

        Submission subToday = new Submission(testUser, p, "JAVA", "code");
        subToday.setStatus(SubmissionStatus.ACCEPTED);
        subToday.setCreatedAt(LocalDateTime.now());

        Submission subYesterday = new Submission(testUser, p, "JAVA", "code");
        subYesterday.setStatus(SubmissionStatus.ACCEPTED);
        subYesterday.setCreatedAt(LocalDateTime.now().minusDays(1));

        Submission subDayBefore = new Submission(testUser, p, "JAVA", "code");
        subDayBefore.setStatus(SubmissionStatus.ACCEPTED);
        subDayBefore.setCreatedAt(LocalDateTime.now().minusDays(2));

        when(submissionRepository.findByUserIdOrderByCreatedAtDesc(88L))
                .thenReturn(List.of(subToday, subYesterday, subDayBefore));
        when(problemRepository.findAll()).thenReturn(List.of(p));

        UserProfileDto profile = userService.getCurrentUserProfile("analytics_dev");

        assertEquals(3, profile.getStreak(), "Streak should accurately count 3 consecutive days");
    }

    @Test
    void testWeakTopicIdentificationAndRecommendations() {
        when(userRepository.findByUsername("analytics_dev")).thenReturn(Optional.of(testUser));

        Topic dpTopic = new Topic("Dynamic Programming");
        Topic arrayTopic = new Topic("Arrays");

        Problem p1 = new Problem();
        p1.setId(1L);
        p1.setTitle("Climbing Stairs");
        p1.setDifficulty(Difficulty.EASY);
        p1.setTopics(Set.of(dpTopic));

        Problem p2 = new Problem();
        p2.setId(2L);
        p2.setTitle("Coin Change");
        p2.setDifficulty(Difficulty.MEDIUM);
        p2.setTopics(Set.of(dpTopic));

        Problem p3 = new Problem();
        p3.setId(3L);
        p3.setTitle("Two Sum");
        p3.setDifficulty(Difficulty.EASY);
        p3.setTopics(Set.of(arrayTopic));

        // Submissions: 3 failed attempts on DP problems, 0 accepted
        Submission s1 = new Submission(testUser, p1, "JAVA", "code");
        s1.setStatus(SubmissionStatus.WRONG_ANSWER);
        s1.setCreatedAt(LocalDateTime.now());

        Submission s2 = new Submission(testUser, p1, "JAVA", "code");
        s2.setStatus(SubmissionStatus.TIME_LIMIT_EXCEEDED);
        s2.setCreatedAt(LocalDateTime.now());

        Submission s3 = new Submission(testUser, p2, "JAVA", "code");
        s3.setStatus(SubmissionStatus.WRONG_ANSWER);
        s3.setCreatedAt(LocalDateTime.now());

        // Array problem solved successfully
        Submission s4 = new Submission(testUser, p3, "JAVA", "code");
        s4.setStatus(SubmissionStatus.ACCEPTED);
        s4.setCreatedAt(LocalDateTime.now());

        when(submissionRepository.findByUserIdOrderByCreatedAtDesc(88L))
                .thenReturn(List.of(s1, s2, s3, s4));
        when(problemRepository.findAll()).thenReturn(List.of(p1, p2, p3));

        UserProfileDto profile = userService.getCurrentUserProfile("analytics_dev");

        // Verify weak topic identification
        assertTrue(profile.getWeakTopics().contains("Dynamic Programming"));
        assertFalse(profile.getWeakTopics().contains("Arrays"));

        // Verify recommendations: p3 is solved, so recommended problems must be unsolved p1 or p2
        List<ProblemListDto> recs = profile.getRecommendedProblems();
        assertFalse(recs.isEmpty());
        assertTrue(recs.stream().anyMatch(r -> r.getTitle().equals("Climbing Stairs") || r.getTitle().equals("Coin Change")));
        assertTrue(recs.stream().noneMatch(r -> r.getId().equals(3L)), "Solved problem Two Sum must not be recommended");
    }
}
