package com.codeforge.execution;

import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

@Component
public class LocalSandboxExecutor implements CodeExecutor {

    private final boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
    private static final int MAX_OUTPUT_BYTES = 65536; // 64 KB stdout/stderr capture cap

    @Override
    public boolean isAvailable() {
        return true;
    }

    private String resolvePythonCommand() {
        String customPath = "C:\\Users\\Jagannath Dalei\\AppData\\Local\\Programs\\Python\\Python312\\python.exe";
        if (new File(customPath).exists()) {
            return customPath;
        }
        return isWindows ? "python" : "python3";
    }

    private void sanitizeEnvironment(ProcessBuilder pb) {
        // Strip sensitive credentials, keys, and tokens from process environment
        try {
            pb.environment().keySet().removeIf(k -> {
                String upper = k.toUpperCase();
                return upper.contains("SECRET") ||
                        upper.contains("PASSWORD") ||
                        upper.contains("TOKEN") ||
                        upper.contains("KEY") ||
                        upper.contains("DATABASE") ||
                        upper.contains("DATASOURCE") ||
                        upper.startsWith("MYSQL_") ||
                        upper.startsWith("SPRING_");
            });
        } catch (Exception ignored) {}
    }

    private void destroyProcessTree(Process process) {
        try {
            process.descendants().forEach(ProcessHandle::destroyForcibly);
        } catch (Throwable ignored) {}
        try {
            process.destroyForcibly();
        } catch (Throwable ignored) {}
    }

    private String readCappedFile(File file) {
        if (!file.exists()) return "";
        try {
            long len = file.length();
            if (len <= MAX_OUTPUT_BYTES) {
                return Files.readString(file.toPath(), StandardCharsets.UTF_8).trim();
            }
            try (InputStream is = new FileInputStream(file)) {
                byte[] buf = new byte[MAX_OUTPUT_BYTES];
                int read = is.read(buf, 0, MAX_OUTPUT_BYTES);
                String content = new String(buf, 0, read, StandardCharsets.UTF_8);
                return content.trim() + "\n... [Output truncated after " + (MAX_OUTPUT_BYTES / 1024) + " KB]";
            }
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public ExecutionResult compile(File sourceDir, String fileName, String language, long timeoutMs) {
        String lang = language != null ? language.toUpperCase() : "JAVA";

        if ("PYTHON".equals(lang) || "PYTHON3".equals(lang) || "JAVASCRIPT".equals(lang) || "JS".equals(lang) || "NODE".equals(lang) || "NODEJS".equals(lang)) {
            // Interpreted languages; no separate compilation required
            ExecutionResult result = new ExecutionResult();
            result.setExitCode(0);
            result.setRuntimeMs(0);
            return result;
        }

        long startTime = System.currentTimeMillis();
        ExecutionResult result = new ExecutionResult();

        try {
            ProcessBuilder pb;
            String outName = isWindows ? "Solution.exe" : "Solution";
            if ("C".equals(lang)) {
                pb = new ProcessBuilder("gcc", "-O3", fileName, "-o", outName);
            } else if ("CPP".equals(lang) || "C++".equals(lang)) {
                pb = new ProcessBuilder("g++", "-O3", "-std=c++17", fileName, "-o", outName);
            } else {
                // Java
                pb = new ProcessBuilder("javac", "-encoding", "UTF-8", fileName);
            }
            pb.directory(sourceDir);
            sanitizeEnvironment(pb);

            File compOut = new File(sourceDir, "compile_stdout.txt");
            File compErr = new File(sourceDir, "compile_stderr.txt");
            pb.redirectOutput(compOut);
            pb.redirectError(compErr);

            Process process = pb.start();
            boolean completed = process.waitFor(timeoutMs, TimeUnit.MILLISECONDS);

            if (!completed) {
                destroyProcessTree(process);
                result.setTimedOut(true);
                result.setStderr("Compilation timed out after " + timeoutMs + " ms");
                result.setExitCode(1);
                return result;
            }

            result.setExitCode(process.exitValue());
            String stdout = readCappedFile(compOut);
            String stderr = readCappedFile(compErr);
            result.setStdout(stdout);
            result.setStderr(!stderr.isEmpty() ? stderr : stdout);
            result.setRuntimeMs(System.currentTimeMillis() - startTime);

        } catch (Exception e) {
            result.setExitCode(1);
            result.setStderr("Compiler error: " + e.getMessage());
        }

        return result;
    }

    @Override
    public ExecutionResult execute(File workDir, String targetName, String language, String input, long timeoutMs, int memoryLimitMb) {
        String lang = language != null ? language.toUpperCase() : "JAVA";
        long startTime = System.currentTimeMillis();
        ExecutionResult result = new ExecutionResult();

        try {
            ProcessBuilder pb;
            if ("PYTHON".equals(lang) || "PYTHON3".equals(lang)) {
                String pythonCmd = resolvePythonCommand();
                pb = new ProcessBuilder(pythonCmd, "-u", targetName.endsWith(".py") ? targetName : targetName + ".py");
            } else if ("JAVASCRIPT".equals(lang) || "JS".equals(lang) || "NODE".equals(lang) || "NODEJS".equals(lang)) {
                pb = new ProcessBuilder("node", targetName.endsWith(".js") ? targetName : targetName + ".js");
            } else if ("CPP".equals(lang) || "C++".equals(lang) || "C".equals(lang)) {
                File exeFile = new File(workDir, isWindows ? "Solution.exe" : "Solution");
                pb = new ProcessBuilder(exeFile.getAbsolutePath());
            } else {
                // Java
                String maxHeapArg = "-Xmx" + memoryLimitMb + "m";
                pb = new ProcessBuilder("java", maxHeapArg, "-Dfile.encoding=UTF-8", "-cp", ".", targetName);
            }
            pb.directory(workDir);
            sanitizeEnvironment(pb);

            // Redirect stdin (always supply file so child process immediately receives EOF on empty input)
            File inFile = new File(workDir, "stdin.txt");
            Files.writeString(inFile.toPath(), input != null ? input : "", StandardCharsets.UTF_8);
            pb.redirectInput(inFile);

            // Redirect stdout and stderr to prevent OS pipe deadlocks
            File outFile = new File(workDir, "stdout.txt");
            File errFile = new File(workDir, "stderr.txt");
            pb.redirectOutput(outFile);
            pb.redirectError(errFile);

            Process process = pb.start();
            boolean completed = process.waitFor(timeoutMs, TimeUnit.MILLISECONDS);

            if (!completed) {
                destroyProcessTree(process);
                result.setTimedOut(true);
                result.setExitCode(124); // Standard timeout exit code
                result.setStderr("Time Limit Exceeded (" + timeoutMs + " ms)");
                result.setRuntimeMs(timeoutMs);
                return result;
            }

            result.setRuntimeMs(System.currentTimeMillis() - startTime);
            int exitCode = process.exitValue();
            result.setExitCode(exitCode);

            String stdout = readCappedFile(outFile);
            String stderr = readCappedFile(errFile);

            result.setStdout(stdout);

            if (exitCode != 0 && stderr.isEmpty()) {
                // Map common OS crash exit codes to human-readable competitive programming diagnostics
                if (exitCode == -1073741819 || exitCode == 139) {
                    stderr = "Segmentation Fault (Access Violation / Memory Out-of-Bounds)";
                } else if (exitCode == -1073741510 || exitCode == 134) {
                    stderr = "Stack Overflow (Recursion depth limit exceeded)";
                } else if (exitCode == -1073741676 || exitCode == 136) {
                    stderr = "Integer Division by Zero (Floating Point Exception)";
                } else {
                    stderr = "Process terminated with exit code " + exitCode;
                }
            }
            result.setStderr(stderr);
            // In standalone host process mode, memory usage is not fabricated; 0 indicates unmeasured host process.
            result.setMemoryKb(0L);

        } catch (Exception e) {
            result.setExitCode(1);
            result.setStderr("Runtime error executing " + lang + ": " + e.getMessage());
        }

        return result;
    }
}
