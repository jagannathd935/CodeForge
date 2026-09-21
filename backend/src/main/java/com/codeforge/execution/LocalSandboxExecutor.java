package com.codeforge.execution;

import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

@Component
public class LocalSandboxExecutor implements CodeExecutor {

    private final boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");

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
                // Default: Java
                pb = new ProcessBuilder("javac", "-encoding", "UTF-8", fileName);
            }
            pb.directory(sourceDir);

            File compOut = new File(sourceDir, "compile_stdout.txt");
            File compErr = new File(sourceDir, "compile_stderr.txt");
            pb.redirectOutput(compOut);
            pb.redirectError(compErr);

            Process process = pb.start();
            boolean completed = process.waitFor(timeoutMs, TimeUnit.MILLISECONDS);

            if (!completed) {
                process.destroyForcibly();
                result.setTimedOut(true);
                result.setStderr("Compilation timed out after " + timeoutMs + " ms");
                result.setExitCode(1);
                return result;
            }

            result.setExitCode(process.exitValue());
            result.setStdout(compOut.exists() ? Files.readString(compOut.toPath(), StandardCharsets.UTF_8).trim() : "");
            result.setStderr(compErr.exists() ? Files.readString(compErr.toPath(), StandardCharsets.UTF_8).trim() : "");
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
                pb = new ProcessBuilder(pythonCmd, targetName.endsWith(".py") ? targetName : targetName + ".py");
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

            // Redirect stdin
            File inFile = new File(workDir, "stdin.txt");
            if (input != null && !input.isEmpty()) {
                Files.writeString(inFile.toPath(), input, StandardCharsets.UTF_8);
                pb.redirectInput(inFile);
            }

            // Redirect stdout and stderr to prevent OS pipe deadlocks
            File outFile = new File(workDir, "stdout.txt");
            File errFile = new File(workDir, "stderr.txt");
            pb.redirectOutput(outFile);
            pb.redirectError(errFile);

            Process process = pb.start();
            boolean completed = process.waitFor(timeoutMs, TimeUnit.MILLISECONDS);

            if (!completed) {
                process.destroyForcibly();
                result.setTimedOut(true);
                result.setExitCode(124); // Standard timeout exit code
                result.setStderr("Time Limit Exceeded (" + timeoutMs + " ms)");
                result.setRuntimeMs(timeoutMs);
                return result;
            }

            result.setRuntimeMs(System.currentTimeMillis() - startTime);
            result.setExitCode(process.exitValue());

            String stdout = outFile.exists() ? Files.readString(outFile.toPath(), StandardCharsets.UTF_8).trim() : "";
            String stderr = errFile.exists() ? Files.readString(errFile.toPath(), StandardCharsets.UTF_8).trim() : "";

            result.setStdout(stdout);
            result.setStderr(stderr);

            long approxMemory = Math.min((long) memoryLimitMb * 1024, 38400 + (result.getRuntimeMs() * 2));
            result.setMemoryKb(approxMemory);

        } catch (Exception e) {
            result.setExitCode(1);
            result.setStderr("Runtime error executing " + lang + ": " + e.getMessage());
        }

        return result;
    }
}
