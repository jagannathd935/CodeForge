package com.codeforge.execution;

import com.codeforge.problem.entity.Difficulty;
import com.codeforge.problem.entity.Problem;
import com.codeforge.submission.dto.RunCodeRequest;
import com.codeforge.submission.dto.RunCodeResponse;
import com.codeforge.submission.entity.Submission;
import com.codeforge.submission.entity.SubmissionStatus;
import com.codeforge.testcase.entity.TestCase;
import com.codeforge.user.entity.Role;
import com.codeforge.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExecutionSecurityTest {

    @Mock
    private CodeExecutor codeExecutor;

    private JudgeService judgeService;
    private Problem sampleProblem;
    private User sampleUser;

    @BeforeEach
    void setUp() {
        judgeService = new JudgeService(codeExecutor);
        sampleProblem = new Problem();
        sampleProblem.setId(100L);
        sampleProblem.setTitle("Test Problem");
        sampleProblem.setTimeLimit(1000);
        sampleProblem.setMemoryLimit(256);
        sampleProblem.setDifficulty(Difficulty.EASY);

        sampleUser = new User("tester", "test@test.com", "pass", Role.ROLE_PARTICIPANT);
        sampleUser.setId(1L);
    }

    @Test
    void testCompilationFailureVerdict() {
        ExecutionResult compileRes = new ExecutionResult();
        compileRes.setExitCode(1);
        compileRes.setStderr("Syntax error: missing semicolon on line 4");

        when(codeExecutor.compile(any(File.class), anyString(), eq("JAVA"), anyLong()))
                .thenReturn(compileRes);

        TestCase tc = new TestCase(sampleProblem, "1 2", "3", false);
        Submission sub = new Submission(sampleUser, sampleProblem, "JAVA", "public class Solution {}");

        judgeService.evaluateSubmission(sub, sampleProblem, List.of(tc));

        assertEquals(SubmissionStatus.COMPILATION_ERROR, sub.getStatus());
        assertTrue(sub.getErrorMessage().contains("Syntax error"));
        assertEquals(0, sub.getTestsPassed());
    }

    @Test
    void testTimeLimitExceededVerdict() {
        ExecutionResult compileRes = new ExecutionResult();
        compileRes.setExitCode(0);
        when(codeExecutor.compile(any(File.class), anyString(), eq("JAVA"), anyLong()))
                .thenReturn(compileRes);

        ExecutionResult execRes = new ExecutionResult();
        execRes.setTimedOut(true);
        execRes.setExitCode(-1);
        execRes.setRuntimeMs(1050L);

        when(codeExecutor.execute(any(File.class), anyString(), eq("JAVA"), anyString(), anyLong(), anyInt()))
                .thenReturn(execRes);

        TestCase tc = new TestCase(sampleProblem, "1000", "output", false);
        Submission sub = new Submission(sampleUser, sampleProblem, "JAVA", "while(true){}");

        judgeService.evaluateSubmission(sub, sampleProblem, List.of(tc));

        assertEquals(SubmissionStatus.TIME_LIMIT_EXCEEDED, sub.getStatus());
        assertEquals(0, sub.getTestsPassed());
    }

    @Test
    void testRuntimeErrorVerdict() {
        ExecutionResult compileRes = new ExecutionResult();
        compileRes.setExitCode(0);
        when(codeExecutor.compile(any(File.class), anyString(), eq("PYTHON"), anyLong()))
                .thenReturn(compileRes);

        ExecutionResult execRes = new ExecutionResult();
        execRes.setTimedOut(false);
        execRes.setExitCode(1);
        execRes.setStderr("ZeroDivisionError: division by zero");

        when(codeExecutor.execute(any(File.class), anyString(), eq("PYTHON"), anyString(), anyLong(), anyInt()))
                .thenReturn(execRes);

        TestCase tc = new TestCase(sampleProblem, "0", "0", false);
        Submission sub = new Submission(sampleUser, sampleProblem, "PYTHON", "print(1/0)");

        judgeService.evaluateSubmission(sub, sampleProblem, List.of(tc));

        assertEquals(SubmissionStatus.RUNTIME_ERROR, sub.getStatus());
        assertTrue(sub.getErrorMessage().contains("ZeroDivisionError"));
    }

    @Test
    void testAcceptedVerdictWithNormalizedOutput() {
        ExecutionResult compileRes = new ExecutionResult();
        compileRes.setExitCode(0);
        when(codeExecutor.compile(any(File.class), anyString(), eq("PYTHON"), anyLong()))
                .thenReturn(compileRes);

        ExecutionResult execRes = new ExecutionResult();
        execRes.setTimedOut(false);
        execRes.setExitCode(0);
        execRes.setStdout("3 \r\n"); // Trailing whitespace and CRLF should be normalized
        execRes.setRuntimeMs(42L);

        when(codeExecutor.execute(any(File.class), anyString(), eq("PYTHON"), anyString(), anyLong(), anyInt()))
                .thenReturn(execRes);

        TestCase tc = new TestCase(sampleProblem, "1 2", "3", false);
        Submission sub = new Submission(sampleUser, sampleProblem, "PYTHON", "print(3)");

        judgeService.evaluateSubmission(sub, sampleProblem, List.of(tc));

        assertEquals(SubmissionStatus.ACCEPTED, sub.getStatus());
        assertEquals(1, sub.getTestsPassed());
        assertEquals(1, sub.getTotalTests());
    }

    @Test
    void testRunSampleCodeDoesNotExposeHiddenCases() {
        TestCase publicCase = new TestCase(sampleProblem, "1 2", "3", false);

        ExecutionResult compileRes = new ExecutionResult();
        compileRes.setExitCode(0);
        when(codeExecutor.compile(any(File.class), anyString(), eq("JAVA"), anyLong()))
                .thenReturn(compileRes);

        ExecutionResult execRes = new ExecutionResult();
        execRes.setTimedOut(false);
        execRes.setExitCode(0);
        execRes.setStdout("3\n");
        execRes.setRuntimeMs(15L);

        when(codeExecutor.execute(any(File.class), anyString(), eq("JAVA"), anyString(), anyLong(), anyInt()))
                .thenReturn(execRes);

        RunCodeRequest req = new RunCodeRequest(
                sampleProblem.getId(),
                "public class Solution { public static void main(String[] args) { System.out.println(3); } }",
                "JAVA",
                null
        );
        // Only publicCase is passed
        RunCodeResponse resp = judgeService.runSampleCode(req, sampleProblem, List.of(publicCase));

        assertEquals("ACCEPTED", resp.getStatus());
        assertEquals("1 2", resp.getInput());
        assertEquals("3", resp.getExpectedOutput());
        assertEquals("3", resp.getActualOutput().trim());
    }
}
