package com.codeforge.submission;

import com.codeforge.contest.repository.ContestParticipantRepository;
import com.codeforge.contest.repository.ContestProblemRepository;
import com.codeforge.contest.repository.ContestProblemResultRepository;
import com.codeforge.contest.repository.ContestRepository;
import com.codeforge.execution.JudgeService;
import com.codeforge.problem.entity.Difficulty;
import com.codeforge.problem.entity.Problem;
import com.codeforge.problem.repository.ProblemRepository;
import com.codeforge.submission.dto.RunCodeRequest;
import com.codeforge.submission.dto.RunCodeResponse;
import com.codeforge.submission.dto.SubmissionRequest;
import com.codeforge.submission.dto.SubmissionResponse;
import com.codeforge.submission.entity.Submission;
import com.codeforge.submission.entity.SubmissionStatus;
import com.codeforge.submission.repository.SubmissionRepository;
import com.codeforge.submission.service.SubmissionService;
import com.codeforge.testcase.entity.TestCase;
import com.codeforge.testcase.repository.TestCaseRepository;
import com.codeforge.user.entity.Role;
import com.codeforge.user.entity.User;
import com.codeforge.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubmissionServiceTest {

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private ProblemRepository problemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TestCaseRepository testCaseRepository;

    @Mock
    private ContestRepository contestRepository;

    @Mock
    private ContestProblemRepository contestProblemRepository;

    @Mock
    private ContestParticipantRepository contestParticipantRepository;

    @Mock
    private ContestProblemResultRepository contestProblemResultRepository;

    private JudgeService stubJudgeService;
    private SubmissionService submissionService;

    private User sampleUser;
    private Problem sampleProblem;

    @BeforeEach
    void setUp() {
        stubJudgeService = new JudgeService(null) {
            @Override
            public void evaluateSubmission(Submission submission, Problem problem, List<TestCase> testCases) {
                submission.setStatus(SubmissionStatus.ACCEPTED);
                submission.setRuntime(25);
                submission.setMemory(32000);
                submission.setTestsPassed(testCases.size());
                submission.setTotalTests(testCases.size());
            }

            @Override
            public RunCodeResponse runSampleCode(RunCodeRequest request, Problem problem, TestCase sampleCase) {
                RunCodeResponse resp = new RunCodeResponse();
                resp.setStatus("ACCEPTED");
                resp.setActualOutput("hello\n");
                resp.setRuntime(40);
                return resp;
            }
        };

        submissionService = new SubmissionService(
                submissionRepository,
                problemRepository,
                userRepository,
                testCaseRepository,
                stubJudgeService,
                contestRepository,
                contestProblemRepository,
                contestParticipantRepository,
                contestProblemResultRepository
        );

        sampleUser = new User("coder1", "coder1@test.com", "pass", Role.ROLE_PARTICIPANT);
        sampleUser.setId(10L);

        sampleProblem = new Problem();
        sampleProblem.setId(1L);
        sampleProblem.setTitle("Two Sum");
        sampleProblem.setSlug("two-sum");
        sampleProblem.setDifficulty(Difficulty.EASY);
        sampleProblem.setDescription("Two sum problem");
        sampleProblem.setTimeLimit(1000);
        sampleProblem.setMemoryLimit(256);
    }

    @Test
    void testSubmitCodeSuccess() {
        SubmissionRequest request = new SubmissionRequest();
        request.setProblemId(1L);
        request.setLanguage("JAVA");
        request.setCode("public class Solution { public static void main(String[] args) {} }");

        when(userRepository.findByUsername("coder1")).thenReturn(Optional.of(sampleUser));
        when(problemRepository.findById(1L)).thenReturn(Optional.of(sampleProblem));

        TestCase tc = new TestCase(sampleProblem, "2 3\n5", "5", false);
        when(testCaseRepository.findByProblemId(1L)).thenReturn(List.of(tc));

        when(submissionRepository.save(any(Submission.class))).thenAnswer(invocation -> {
            Submission s = invocation.getArgument(0);
            if (s.getId() == null) {
                s.setId(100L);
            }
            return s;
        });

        SubmissionResponse response = submissionService.submitCode(request, "coder1");

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals("ACCEPTED", response.getStatus());
        assertEquals("Two Sum", response.getProblemTitle());
        assertEquals(1, response.getTestsPassed());
        assertEquals(1, response.getTotalTests());
    }

    @Test
    void testRunSampleCodeSuccess() {
        RunCodeRequest request = new RunCodeRequest();
        request.setProblemId(1L);
        request.setLanguage("PYTHON");
        request.setCode("print('hello')");
        request.setCustomInput("test input");

        when(problemRepository.findById(1L)).thenReturn(Optional.of(sampleProblem));

        TestCase sampleTc = new TestCase(sampleProblem, "2 3\n5", "5", false);
        when(testCaseRepository.findByProblemIdAndIsHiddenFalse(1L)).thenReturn(List.of(sampleTc));

        RunCodeResponse response = submissionService.runSampleCode(request);

        assertNotNull(response);
        assertEquals("ACCEPTED", response.getStatus());
        assertEquals("hello\n", response.getActualOutput());
        assertEquals(40, response.getRuntime());
    }

    @Test
    void testGetUserSubmissions() {
        when(userRepository.findByUsername("coder1")).thenReturn(Optional.of(sampleUser));

        Submission sub = new Submission(sampleUser, sampleProblem, "JAVA", "code");
        sub.setId(55L);
        sub.setStatus(SubmissionStatus.ACCEPTED);

        when(submissionRepository.findByUserIdOrderByCreatedAtDesc(10L)).thenReturn(List.of(sub));

        List<SubmissionResponse> result = submissionService.getUserSubmissions("coder1");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(55L, result.get(0).getId());
    }
}
