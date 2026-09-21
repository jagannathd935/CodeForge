package com.codeforge.execution;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

@Component
public class DockerCodeExecutor implements CodeExecutor {

    private final LocalSandboxExecutor localSandboxExecutor;

    @Value("${codeforge.execution.docker-enabled:false}")
    private boolean dockerEnabled;

    private Boolean dockerAvailable = null;

    public DockerCodeExecutor(LocalSandboxExecutor localSandboxExecutor) {
        this.localSandboxExecutor = localSandboxExecutor;
    }

    @Override
    public boolean isAvailable() {
        if (!dockerEnabled) {
            return false;
        }
        if (dockerAvailable != null) {
            return dockerAvailable;
        }
        try {
            Process process = new ProcessBuilder("docker", "--version").start();
            boolean completed = process.waitFor(2, java.util.concurrent.TimeUnit.SECONDS);
            dockerAvailable = completed && process.exitValue() == 0;
        } catch (Exception e) {
            dockerAvailable = false;
        }
        return dockerAvailable;
    }

    @Override
    public ExecutionResult compile(File sourceDir, String fileName, String language, long timeoutMs) {
        if (!isAvailable()) {
            return localSandboxExecutor.compile(sourceDir, fileName, language, timeoutMs);
        }

        String lang = language != null ? language.toUpperCase() : "JAVA";
        if ("PYTHON".equals(lang) || "PYTHON3".equals(lang) || "JAVASCRIPT".equals(lang) || "JS".equals(lang) || "NODE".equals(lang) || "NODEJS".equals(lang)) {
            ExecutionResult res = new ExecutionResult();
            res.setExitCode(0);
            return res;
        }

        try {
            ProcessBuilder pb;
            if ("C".equals(lang)) {
                pb = new ProcessBuilder(
                        "docker", "run", "--rm",
                        "--network", "none",
                        "--memory", "256m",
                        "-v", sourceDir.getAbsolutePath() + ":/workspace",
                        "-w", "/workspace",
                        "gcc:latest",
                        "gcc", "-O3", fileName, "-o", "Solution"
                );
            } else if ("CPP".equals(lang) || "C++".equals(lang)) {
                pb = new ProcessBuilder(
                        "docker", "run", "--rm",
                        "--network", "none",
                        "--memory", "256m",
                        "-v", sourceDir.getAbsolutePath() + ":/workspace",
                        "-w", "/workspace",
                        "gcc:latest",
                        "g++", "-O3", fileName, "-o", "Solution"
                );
            } else {
                pb = new ProcessBuilder(
                        "docker", "run", "--rm",
                        "--network", "none",
                        "--memory", "256m",
                        "-v", sourceDir.getAbsolutePath() + ":/workspace",
                        "-w", "/workspace",
                        "openjdk:17-slim",
                        "javac", fileName
                );
            }
            return runProcessWithTimeout(pb, timeoutMs);
        } catch (Exception e) {
            return localSandboxExecutor.compile(sourceDir, fileName, language, timeoutMs);
        }
    }

    @Override
    public ExecutionResult execute(File workDir, String targetName, String language, String input, long timeoutMs, int memoryLimitMb) {
        if (!isAvailable()) {
            return localSandboxExecutor.execute(workDir, targetName, language, input, timeoutMs, memoryLimitMb);
        }

        String lang = language != null ? language.toUpperCase() : "JAVA";

        try {
            ProcessBuilder pb;
            if ("PYTHON".equals(lang) || "PYTHON3".equals(lang)) {
                pb = new ProcessBuilder(
                        "docker", "run", "--rm", "-i",
                        "--network", "none",
                        "--memory", memoryLimitMb + "m",
                        "--cpus", "0.5",
                        "--pids-limit", "64",
                        "-v", workDir.getAbsolutePath() + ":/workspace:ro",
                        "-w", "/workspace",
                        "python:3.10-slim",
                        "python", targetName.endsWith(".py") ? targetName : targetName + ".py"
                );
            } else if ("JAVASCRIPT".equals(lang) || "JS".equals(lang) || "NODE".equals(lang) || "NODEJS".equals(lang)) {
                pb = new ProcessBuilder(
                        "docker", "run", "--rm", "-i",
                        "--network", "none",
                        "--memory", memoryLimitMb + "m",
                        "--cpus", "0.5",
                        "--pids-limit", "64",
                        "-v", workDir.getAbsolutePath() + ":/workspace:ro",
                        "-w", "/workspace",
                        "node:20-slim",
                        "node", targetName.endsWith(".js") ? targetName : targetName + ".js"
                );
            } else if ("CPP".equals(lang) || "C++".equals(lang) || "C".equals(lang)) {
                pb = new ProcessBuilder(
                        "docker", "run", "--rm", "-i",
                        "--network", "none",
                        "--memory", memoryLimitMb + "m",
                        "--cpus", "0.5",
                        "--pids-limit", "64",
                        "-v", workDir.getAbsolutePath() + ":/workspace:ro",
                        "-w", "/workspace",
                        "gcc:latest",
                        "./Solution"
                );
            } else {
                pb = new ProcessBuilder(
                        "docker", "run", "--rm", "-i",
                        "--network", "none",
                        "--memory", memoryLimitMb + "m",
                        "--cpus", "0.5",
                        "--pids-limit", "64",
                        "-v", workDir.getAbsolutePath() + ":/workspace:ro",
                        "-w", "/workspace",
                        "openjdk:17-slim",
                        "java", "-Xmx" + memoryLimitMb + "m", targetName
                );
            }
            return runProcessWithInput(pb, input, timeoutMs);
        } catch (Exception e) {
            return localSandboxExecutor.execute(workDir, targetName, language, input, timeoutMs, memoryLimitMb);
        }
    }

    private ExecutionResult runProcessWithTimeout(ProcessBuilder pb, long timeoutMs) throws Exception {
        long start = System.currentTimeMillis();
        Process p = pb.start();
        boolean finished = p.waitFor(timeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS);
        ExecutionResult result = new ExecutionResult();
        if (!finished) {
            p.destroyForcibly();
            result.setTimedOut(true);
            result.setExitCode(1);
            result.setStderr("Timeout");
        } else {
            result.setExitCode(p.exitValue());
            result.setRuntimeMs(System.currentTimeMillis() - start);
            result.setStdout(readStream(p.getInputStream()));
            result.setStderr(readStream(p.getErrorStream()));
        }
        return result;
    }

    private ExecutionResult runProcessWithInput(ProcessBuilder pb, String input, long timeoutMs) throws Exception {
        long start = System.currentTimeMillis();
        Process p = pb.start();

        if (input != null && !input.isEmpty()) {
            try (OutputStream os = p.getOutputStream()) {
                os.write(input.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }
        } else {
            p.getOutputStream().close();
        }

        boolean finished = p.waitFor(timeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS);
        ExecutionResult result = new ExecutionResult();
        if (!finished) {
            p.destroyForcibly();
            result.setTimedOut(true);
            result.setExitCode(124);
            result.setStderr("Time Limit Exceeded");
        } else {
            result.setExitCode(p.exitValue());
            result.setRuntimeMs(System.currentTimeMillis() - start);
            result.setStdout(readStream(p.getInputStream()));
            result.setStderr(readStream(p.getErrorStream()));
        }
        return result;
    }

    private String readStream(InputStream is) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString().trim();
    }
}
