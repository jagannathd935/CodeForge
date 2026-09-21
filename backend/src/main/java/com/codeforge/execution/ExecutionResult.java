package com.codeforge.execution;

public class ExecutionResult {

    private int exitCode;
    private String stdout;
    private String stderr;
    private long runtimeMs;
    private long memoryKb;
    private boolean timedOut;

    public ExecutionResult() {
    }

    public ExecutionResult(int exitCode, String stdout, String stderr, long runtimeMs, long memoryKb, boolean timedOut) {
        this.exitCode = exitCode;
        this.stdout = stdout;
        this.stderr = stderr;
        this.runtimeMs = runtimeMs;
        this.memoryKb = memoryKb;
        this.timedOut = timedOut;
    }

    public int getExitCode() {
        return exitCode;
    }

    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    public String getStdout() {
        return stdout;
    }

    public void setStdout(String stdout) {
        this.stdout = stdout;
    }

    public String getStderr() {
        return stderr;
    }

    public void setStderr(String stderr) {
        this.stderr = stderr;
    }

    public long getRuntimeMs() {
        return runtimeMs;
    }

    public void setRuntimeMs(long runtimeMs) {
        this.runtimeMs = runtimeMs;
    }

    public long getMemoryKb() {
        return memoryKb;
    }

    public void setMemoryKb(long memoryKb) {
        this.memoryKb = memoryKb;
    }

    public boolean isTimedOut() {
        return timedOut;
    }

    public void setTimedOut(boolean timedOut) {
        this.timedOut = timedOut;
    }
}
