package com.codeforge.execution;

import java.io.File;

public interface CodeExecutor {

    default ExecutionResult compile(File sourceDir, String fileName, long timeoutMs) {
        return compile(sourceDir, fileName, "JAVA", timeoutMs);
    }

    default ExecutionResult execute(File workDir, String className, String input, long timeoutMs, int memoryLimitMb) {
        return execute(workDir, className, "JAVA", input, timeoutMs, memoryLimitMb);
    }

    ExecutionResult compile(File sourceDir, String fileName, String language, long timeoutMs);

    ExecutionResult execute(File workDir, String targetName, String language, String input, long timeoutMs, int memoryLimitMb);

    boolean isAvailable();
}
