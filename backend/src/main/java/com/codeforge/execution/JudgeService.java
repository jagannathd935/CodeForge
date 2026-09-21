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
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class JudgeService {

    private final DockerCodeExecutor executor;

    @Value("${codeforge.execution.temp-dir:./temp-submissions}")
    private String baseTempDir;

    public JudgeService(DockerCodeExecutor executor) {
        this.executor = executor;
    }

    public void evaluateSubmission(Submission submission, Problem problem, List<TestCase> testCases) {
        File runDir = createRunDirectory();

        try {
            String lang = submission.getLanguage() != null ? submission.getLanguage().toUpperCase() : "JAVA";

            // Security Validation
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
            Files.writeString(solutionFile.toPath(), submission.getCode(), java.nio.charset.StandardCharsets.UTF_8);

            // 1. Compile Phase (Timeout 10 seconds)
            ExecutionResult compileResult = executor.compile(runDir, fileName, lang, 10000);
            if (compileResult.getExitCode() != 0) {
                submission.setStatus(SubmissionStatus.COMPILATION_ERROR);
                submission.setErrorMessage(compileResult.getStderr());
                submission.setTestsPassed(0);
                submission.setTotalTests(testCases.size());
                return;
            }

            // 2. Execution Phase
            int testsPassed = 0;
            long maxRuntime = 0;
            long maxMemory = 0;
            long timeLimit = problem.getTimeLimit() != null ? problem.getTimeLimit() : 1000;
            int memoryLimit = problem.getMemoryLimit() != null ? problem.getMemoryLimit() : 256;

            for (TestCase tc : testCases) {
                ExecutionResult execResult = executor.execute(runDir, targetName, lang, tc.getInput(), timeLimit, memoryLimit);

                if (execResult.isTimedOut()) {
                    submission.setStatus(SubmissionStatus.TIME_LIMIT_EXCEEDED);
                    submission.setErrorMessage("Time Limit Exceeded on test case " + (testsPassed + 1));
                    submission.setRuntime((int) timeLimit);
                    submission.setMemory((int) execResult.getMemoryKb());
                    submission.setTestsPassed(testsPassed);
                    submission.setTotalTests(testCases.size());
                    return;
                }

                if (execResult.getExitCode() != 0) {
                    submission.setStatus(SubmissionStatus.RUNTIME_ERROR);
                    submission.setErrorMessage(execResult.getStderr());
                    submission.setRuntime((int) execResult.getRuntimeMs());
                    submission.setMemory((int) execResult.getMemoryKb());
                    submission.setTestsPassed(testsPassed);
                    submission.setTotalTests(testCases.size());
                    return;
                }

                maxRuntime = Math.max(maxRuntime, execResult.getRuntimeMs());
                maxMemory = Math.max(maxMemory, execResult.getMemoryKb());

                String actual = normalizeOutput(execResult.getStdout());
                String expected = normalizeOutput(tc.getExpectedOutput());

                if (!actual.equals(expected)) {
                    submission.setStatus(SubmissionStatus.WRONG_ANSWER);
                    if (!tc.getIsHidden()) {
                        submission.setErrorMessage("Wrong Answer on sample test case.\nExpected: " + expected + "\nActual: " + actual);
                    } else {
                        submission.setErrorMessage("Wrong Answer on test case " + (testsPassed + 1));
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
            submission.setRuntime((int) Math.max(maxRuntime, 10));
            submission.setMemory((int) Math.max(maxMemory, 35000));
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
        File runDir = createRunDirectory();
        RunCodeResponse response = new RunCodeResponse();

        try {
            String lang = request.getLanguage() != null ? request.getLanguage().toUpperCase() : "JAVA";

            // Security Validation
            String securityViolation = CodeSanitizer.validate(request.getCode(), lang);
            if (securityViolation != null) {
                response.setStatus("RUNTIME_ERROR");
                response.setErrorMessage(securityViolation);
                return response;
            }

            String fileName = getSourceFileName(lang);
            String targetName = "Solution";

            File solutionFile = new File(runDir, fileName);
            Files.writeString(solutionFile.toPath(), request.getCode(), java.nio.charset.StandardCharsets.UTF_8);

            ExecutionResult compileResult = executor.compile(runDir, fileName, lang, 10000);
            if (compileResult.getExitCode() != 0) {
                response.setStatus("COMPILATION_ERROR");
                response.setErrorMessage(compileResult.getStderr() != null && !compileResult.getStderr().isEmpty()
                        ? compileResult.getStderr()
                        : "Compilation failed.");
                return response;
            }

            String customInput = request.getCustomInput();
            String input = (customInput != null && !customInput.isEmpty())
                    ? customInput
                    : (sampleCase != null ? sampleCase.getInput() : "");

            String expected = "";
            if (sampleCase != null) {
                if (customInput == null || customInput.isEmpty() || customInput.trim().equals(sampleCase.getInput().trim())) {
                    expected = sampleCase.getExpectedOutput();
                }
            }

            long timeLimit = problem.getTimeLimit() != null ? problem.getTimeLimit() : 1000;
            int memoryLimit = problem.getMemoryLimit() != null ? problem.getMemoryLimit() : 256;

            ExecutionResult execResult = executor.execute(runDir, targetName, lang, input, timeLimit, memoryLimit);

            response.setInput(input);
            response.setExpectedOutput(expected);
            response.setActualOutput(execResult.getStdout() != null ? execResult.getStdout() : "");
            response.setRuntime((int) Math.max(execResult.getRuntimeMs(), 8));
            response.setMemory((int) Math.max(execResult.getMemoryKb(), 32000));

            if (execResult.isTimedOut()) {
                response.setStatus("TIME_LIMIT_EXCEEDED");
                response.setErrorMessage("Time Limit Exceeded (" + timeLimit + " ms)");
            } else if (execResult.getExitCode() != 0) {
                response.setStatus("RUNTIME_ERROR");
                response.setErrorMessage(execResult.getStderr() != null && !execResult.getStderr().isEmpty()
                        ? execResult.getStderr()
                        : "Runtime error during execution.");
            } else {
                String actualNorm = normalizeOutput(execResult.getStdout());
                String expectedNorm = normalizeOutput(expected);
                if (expectedNorm.isEmpty() || actualNorm.equals(expectedNorm)) {
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

    private String normalizeOutput(String s) {
        if (s == null) return "";
        String[] lines = s.replace("\r\n", "\n").split("\n");
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            String trimmedLine = line.stripTrailing();
            if (sb.length() > 0) sb.append("\n");
            sb.append(trimmedLine);
        }
        return sb.toString().trim();
    }

    private File createRunDirectory() {
        File baseDir = new File(baseTempDir);
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
