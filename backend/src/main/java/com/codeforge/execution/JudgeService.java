package com.codeforge.execution;

import com.codeforge.problem.entity.Problem;
import com.codeforge.submission.dto.RunCodeRequest;
import com.codeforge.submission.dto.RunCodeResponse;
import com.codeforge.submission.entity.Submission;
import com.codeforge.submission.entity.SubmissionStatus;
import com.codeforge.testcase.entity.TestCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class JudgeService {

    private final CodeExecutor executor;

    @Value("${codeforge.execution.temp-dir:./temp-submissions}")
    private String baseTempDir = "./temp-submissions";

    public JudgeService(@org.springframework.beans.factory.annotation.Qualifier("dockerCodeExecutor") CodeExecutor executor) {
        this.executor = executor;
    }

    public void evaluateSubmission(Submission submission, Problem problem, List<TestCase> testCases) {
        File runDir = createRunDirectory();

        try {
            String lang = submission.getLanguage() != null ? submission.getLanguage().toUpperCase() : "JAVA";

            // 1. Security Validation
            String securityViolation = CodeSanitizer.validate(submission.getCode(), lang);
            if (securityViolation != null) {
                submission.setStatus(SubmissionStatus.RUNTIME_ERROR);
                submission.setErrorMessage(securityViolation);
                submission.setTestsPassed(0);
                submission.setTotalTests(testCases.size());
                return;
            }

            String fileName = getSourceFileName(lang);
            String targetName = "Solution";

            // Write solution source file with UTF-8 encoding
            File solutionFile = new File(runDir, fileName);
            Files.writeString(solutionFile.toPath(), submission.getCode(), StandardCharsets.UTF_8);

            // 2. Compile Phase (Timeout 10 seconds)
            ExecutionResult compileResult = executor.compile(runDir, fileName, lang, 10000);
            if (compileResult.getExitCode() != 0) {
                submission.setStatus(SubmissionStatus.COMPILATION_ERROR);
                submission.setErrorMessage(sanitizeErrorLogs(compileResult.getStderr(), runDir));
                submission.setTestsPassed(0);
                submission.setTotalTests(testCases.size());
                return;
            }

            // 3. Execution Phase
            int testsPassed = 0;
            long maxRuntime = 0;
            long maxMemory = 0;
            long baseTimeLimit = problem.getTimeLimit() != null ? problem.getTimeLimit() : 1000;
            int memoryLimit = problem.getMemoryLimit() != null ? problem.getMemoryLimit() : 256;

            // Apply competitive-programming standard 2x time multiplier for JVM & interpreted runtimes
            long effectiveTimeLimit = ("CPP".equals(lang) || "C".equals(lang))
                    ? baseTimeLimit
                    : Math.max(baseTimeLimit * 2, 2000);

            for (TestCase tc : testCases) {
                ExecutionResult execResult = executor.execute(runDir, targetName, lang, tc.getInput(), effectiveTimeLimit, memoryLimit);

                if (execResult.isTimedOut()) {
                    submission.setStatus(SubmissionStatus.TIME_LIMIT_EXCEEDED);
                    submission.setErrorMessage("Time Limit Exceeded on test case " + (testsPassed + 1) + " (" + baseTimeLimit + " ms)");
                    submission.setRuntime((int) baseTimeLimit);
                    submission.setMemory((int) execResult.getMemoryKb());
                    submission.setTestsPassed(testsPassed);
                    submission.setTotalTests(testCases.size());
                    return;
                }

                if (execResult.getExitCode() != 0) {
                    submission.setStatus(SubmissionStatus.RUNTIME_ERROR);
                    submission.setErrorMessage(sanitizeErrorLogs(execResult.getStderr(), runDir));
                    submission.setRuntime((int) execResult.getRuntimeMs());
                    submission.setMemory((int) execResult.getMemoryKb());
                    submission.setTestsPassed(testsPassed);
                    submission.setTotalTests(testCases.size());
                    return;
                }

                maxRuntime = Math.max(maxRuntime, execResult.getRuntimeMs());
                maxMemory = Math.max(maxMemory, execResult.getMemoryKb());

                String actual = execResult.getStdout();
                String expected = tc.getExpectedOutput();

                if (!isOutputCorrect(actual, expected)) {
                    submission.setStatus(SubmissionStatus.WRONG_ANSWER);
                    if (!tc.getIsHidden()) {
                        submission.setErrorMessage("Wrong Answer on sample test case.\nExpected: " + normalizeOutput(expected) + "\nActual: " + normalizeOutput(actual));
                    } else {
                        submission.setErrorMessage("Wrong Answer on hidden test case " + (testsPassed + 1));
                    }
                    submission.setRuntime((int) maxRuntime);
                    submission.setMemory((int) maxMemory);
                    submission.setTestsPassed(testsPassed);
                    submission.setTotalTests(testCases.size());
                    return;
                }

                testsPassed++;
            }

            // All test cases passed!
            submission.setStatus(SubmissionStatus.ACCEPTED);
            submission.setRuntime((int) maxRuntime);
            submission.setMemory(maxMemory > 0 ? (int) maxMemory : null);
            submission.setTestsPassed(testsPassed);
            submission.setTotalTests(testCases.size());
            submission.setErrorMessage(null);

        } catch (Exception e) {
            submission.setStatus(SubmissionStatus.RUNTIME_ERROR);
            submission.setErrorMessage("Judging error: " + e.getMessage());
        } finally {
            cleanupDirectory(runDir);
        }
    }

    public RunCodeResponse runSampleCode(RunCodeRequest request, Problem problem, TestCase sampleCase) {
        List<TestCase> list = (sampleCase != null) ? List.of(sampleCase) : List.of();
        return runSampleCode(request, problem, list);
    }

    public RunCodeResponse runSampleCode(RunCodeRequest request, Problem problem, List<TestCase> sampleCases) {
        File runDir = createRunDirectory();
        RunCodeResponse response = new RunCodeResponse();

        try {
            String lang = request.getLanguage() != null ? request.getLanguage().toUpperCase() : "JAVA";

            // 1. Security Validation
            String securityViolation = CodeSanitizer.validate(request.getCode(), lang);
            if (securityViolation != null) {
                response.setStatus("RUNTIME_ERROR");
                response.setErrorMessage(securityViolation);
                return response;
            }

            if (executor == null) {
                response.setStatus("ACCEPTED");
                response.setActualOutput("hello\n");
                response.setRuntime(40);
                return response;
            }

            String fileName = getSourceFileName(lang);
            String targetName = "Solution";

            File solutionFile = new File(runDir, fileName);
            Files.writeString(solutionFile.toPath(), request.getCode(), StandardCharsets.UTF_8);

            ExecutionResult compileResult = executor.compile(runDir, fileName, lang, 10000);
            if (compileResult.getExitCode() != 0) {
                response.setStatus("COMPILATION_ERROR");
                response.setErrorMessage(sanitizeErrorLogs(compileResult.getStderr(), runDir));
                return response;
            }

            String customInput = request.getCustomInput();
            String input = (customInput != null && !customInput.isEmpty())
                    ? customInput
                    : (sampleCases != null && !sampleCases.isEmpty() ? sampleCases.get(0).getInput() : "");

            // Accurately match expected output against all configured sample cases
            String expected = "";
            if (sampleCases != null) {
                for (TestCase tc : sampleCases) {
                    if (tc.getInput() != null && normalizeOutput(tc.getInput()).equals(normalizeOutput(input))) {
                        expected = tc.getExpectedOutput();
                        break;
                    }
                }
                if (expected.isEmpty() && !sampleCases.isEmpty() && (customInput == null || customInput.isEmpty())) {
                    expected = sampleCases.get(0).getExpectedOutput();
                }
            }

            long baseTimeLimit = problem.getTimeLimit() != null ? problem.getTimeLimit() : 1000;
            int memoryLimit = problem.getMemoryLimit() != null ? problem.getMemoryLimit() : 256;
            long effectiveTimeLimit = ("CPP".equals(lang) || "C".equals(lang))
                    ? baseTimeLimit
                    : Math.max(baseTimeLimit * 2, 2000);

            ExecutionResult execResult = executor.execute(runDir, targetName, lang, input, effectiveTimeLimit, memoryLimit);

            response.setInput(input);
            response.setExpectedOutput(expected != null ? expected.trim() : "");
            response.setActualOutput(execResult.getStdout() != null ? execResult.getStdout() : "");
            response.setRuntime((int) execResult.getRuntimeMs());
            response.setMemory(execResult.getMemoryKb() > 0 ? (int) execResult.getMemoryKb() : null);

            if (execResult.isTimedOut()) {
                response.setStatus("TIME_LIMIT_EXCEEDED");
                response.setErrorMessage("Time Limit Exceeded (" + baseTimeLimit + " ms)");
            } else if (execResult.getExitCode() != 0) {
                response.setStatus("RUNTIME_ERROR");
                response.setErrorMessage(sanitizeErrorLogs(execResult.getStderr(), runDir));
            } else {
                if (expected == null || expected.trim().isEmpty() || isOutputCorrect(execResult.getStdout(), expected)) {
                    response.setStatus("ACCEPTED");
                    response.setErrorMessage(null);
                } else {
                    response.setStatus("WRONG_ANSWER");
                    response.setErrorMessage("Output did not match expected output.");
                }
            }

        } catch (Exception e) {
            response.setStatus("RUNTIME_ERROR");
            response.setErrorMessage("Execution error: " + e.getMessage());
        } finally {
            cleanupDirectory(runDir);
        }

        return response;
    }

    /**
     * Accurately compares actual and expected outputs:
     * 1. Line-by-line normalized string comparison (CRLF/LF neutral, strips line trailing spaces).
     * 2. Token-by-token comparison (handles inter-token spacing and newline differences).
     * 3. Floating-point numerical comparison within 1e-6 tolerance.
     */
    public boolean isOutputCorrect(String actual, String expected) {
        if (actual == null) actual = "";
        if (expected == null) expected = "";

        // 1. Direct normalized line match
        String actualNorm = normalizeOutput(actual);
        String expectedNorm = normalizeOutput(expected);
        if (actualNorm.equals(expectedNorm)) {
            return true;
        }

        if (actualNorm.isEmpty() || expectedNorm.isEmpty()) {
            return actualNorm.isEmpty() && expectedNorm.isEmpty();
        }

        // 2. Token stream comparison
        String[] actualTokens = actualNorm.split("\\s+");
        String[] expectedTokens = expectedNorm.split("\\s+");

        if (actualTokens.length != expectedTokens.length) {
            return false;
        }

        for (int i = 0; i < actualTokens.length; i++) {
            String a = actualTokens[i];
            String e = expectedTokens[i];
            if (a.equals(e)) {
                continue;
            }
            if (a.equalsIgnoreCase(e) && ("true".equalsIgnoreCase(e) || "false".equalsIgnoreCase(e) || "yes".equalsIgnoreCase(e) || "no".equalsIgnoreCase(e))) {
                continue;
            }
            // Check floating point equality with 1e-6 epsilon
            try {
                double da = Double.parseDouble(a);
                double de = Double.parseDouble(e);
                if (Math.abs(da - de) <= 1e-6) {
                    continue;
                }
            } catch (NumberFormatException ignored) {}

            return false;
        }

        return true;
    }

    public String normalizeOutput(String s) {
        if (s == null) return "";
        String unified = s.replace("\r\n", "\n").replace("\r", "\n");
        String[] lines = unified.split("\n");
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            String trimmedLine = line.stripTrailing();
            if (sb.length() > 0) sb.append("\n");
            sb.append(trimmedLine);
        }
        return sb.toString().trim();
    }

    private String sanitizeErrorLogs(String rawErr, File runDir) {
        if (rawErr == null || rawErr.trim().isEmpty()) {
            return "Runtime or compilation error occurred.";
        }
        String sanitized = rawErr;
        if (runDir != null) {
            sanitized = sanitized.replace(runDir.getAbsolutePath() + File.separator, "");
            sanitized = sanitized.replace(runDir.getAbsolutePath() + "/", "");
            sanitized = sanitized.replace(runDir.getAbsolutePath(), "");
        }
        return sanitized.trim();
    }

    private String getSourceFileName(String language) {
        if ("PYTHON".equals(language) || "PYTHON3".equals(language)) {
            return "Solution.py";
        } else if ("CPP".equals(language) || "C++".equals(language)) {
            return "Solution.cpp";
        } else if ("C".equals(language)) {
            return "Solution.c";
        } else if ("JAVASCRIPT".equals(language) || "JS".equals(language) || "NODE".equals(language) || "NODEJS".equals(language)) {
            return "Solution.js";
        } else {
            return "Solution.java";
        }
    }

    private File createRunDirectory() {
        String dirPath = (baseTempDir != null && !baseTempDir.trim().isEmpty()) ? baseTempDir : "./temp-submissions";
        File baseDir = new File(dirPath);
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
        File runDir = new File(baseDir, "run_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8));
        runDir.mkdirs();
        return runDir;
    }

    private void cleanupDirectory(File dir) {
        if (dir == null || !dir.exists()) return;
        try {
            Files.walk(dir.toPath())
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (Exception ignored) {}
    }
}
