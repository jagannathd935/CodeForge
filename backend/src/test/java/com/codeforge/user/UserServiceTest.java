package com.codeforge.user;

import com.codeforge.auth.dto.UserProfileDto;
import com.codeforge.problem.entity.Difficulty;
import com.codeforge.problem.entity.Problem;
import com.codeforge.problem.repository.ProblemRepository;
import com.codeforge.submission.entity.Submission;
import com.codeforge.submission.entity.SubmissionStatus;
import com.codeforge.submission.repository.SubmissionRepository;
import com.codeforge.user.entity.Role;
import com.codeforge.user.entity.User;
import com.codeforge.user.repository.UserRepository;
import com.codeforge.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private ProblemRepository problemRepository;

    private UserService userService;
    private User sampleUser;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, submissionRepository, problemRepository);
        sampleUser = new User("dev_user", "dev@test.com", "pass", Role.ROLE_PARTICIPANT);
        sampleUser.setId(5L);
        sampleUser.setOrganization("IIT Bombay");
    }

    @Test
    void testGetCurrentUserProfileWithStats() {
        when(userRepository.findByUsername("dev_user")).thenReturn(Optional.of(sampleUser));

        Problem p1 = new Problem();
        p1.setId(1L);
        p1.setTitle("Two Sum");
        p1.setSlug("two-sum");
        p1.setDifficulty(Difficulty.EASY);

        Problem p2 = new Problem();
        p2.setId(2L);
        p2.setTitle("Add Two Numbers");
        p2.setSlug("add-two-numbers");
        p2.setDifficulty(Difficulty.MEDIUM);

        Submission s1 = new Submission(sampleUser, p1, "JAVA", "code");
        s1.setStatus(SubmissionStatus.ACCEPTED);

        Submission s2 = new Submission(sampleUser, p2, "PYTHON", "code");
        s2.setStatus(SubmissionStatus.ACCEPTED);

        Submission s3 = new Submission(sampleUser, p1, "JAVA", "code");
        s3.setStatus(SubmissionStatus.WRONG_ANSWER);

        Submission s4 = new Submission(sampleUser, p2, "PYTHON", "code");
        s4.setStatus(SubmissionStatus.WRONG_ANSWER);

        when(submissionRepository.findByUserIdOrderByCreatedAtDesc(5L)).thenReturn(List.of(s1, s2, s3, s4));
        when(problemRepository.findAll()).thenReturn(List.of(p1, p2));

        UserProfileDto profile = userService.getCurrentUserProfile("dev_user");

        assertNotNull(profile);
        assertEquals("dev_user", profile.getUsername());
        assertEquals("IIT Bombay", profile.getOrganization());
        assertEquals(4L, profile.getTotalSubmissions());
        assertEquals(2L, profile.getAcceptedSubmissions());
        assertEquals(50.0, profile.getAcceptanceRate());
        assertEquals(2L, profile.getSolvedCount());
        assertEquals(1L, profile.getEasySolved());
        assertEquals(1L, profile.getMediumSolved());
        assertEquals(0L, profile.getHardSolved());
    }
}
