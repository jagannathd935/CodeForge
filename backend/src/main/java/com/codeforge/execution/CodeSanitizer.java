package com.codeforge.execution;

import java.util.regex.Pattern;

public class CodeSanitizer {

    private static final Pattern JAVA_FORBIDDEN = Pattern.compile(
            "\\b(Runtime\\.getRuntime|ProcessBuilder|System\\.exit|java\\.lang\\.reflect|sun\\.misc|java\\.net\\.Socket|java\\.net\\.URL)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern PYTHON_FORBIDDEN = Pattern.compile(
            "\\b(import\\s+os|from\\s+os\\s+import|import\\s+subprocess|from\\s+subprocess\\s+import|from\\s+sys\\s+import\\s+exit|sys\\.exit|os\\.system|__import__|eval\\s*\\(|exec\\s*\\(|shutil\\.rmtree)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern CPP_FORBIDDEN = Pattern.compile(
            "\\b(system\\s*\\(|popen\\s*\\(|fork\\s*\\(|execvp\\s*\\(|kill\\s*\\(|remove\\s*\\(|unlink\\s*\\()\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern JS_FORBIDDEN = Pattern.compile(
            "\\b(child_process|cluster|process\\.exit|process\\.kill|eval\\s*\\()\\b",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Inspects submitted code for potentially harmful system calls or malicious operations.
     * @param code The source code string.
     * @param language The programming language (JAVA, PYTHON, CPP, C, JAVASCRIPT).
     * @return Error message if prohibited tokens are found, or null if code is acceptable.
     */
    public static String validate(String code, String language) {
        if (code == null || code.trim().isEmpty()) {
            return "Submitted code cannot be empty";
        }

        String lang = language != null ? language.toUpperCase() : "JAVA";

        if ("JAVA".equals(lang)) {
            if (JAVA_FORBIDDEN.matcher(code).find()) {
                return "Security Policy Violation: Invocation of ProcessBuilder, Runtime, Reflection, or System.exit is prohibited.";
            }
        } else if ("PYTHON".equals(lang) || "PYTHON3".equals(lang)) {
            if (PYTHON_FORBIDDEN.matcher(code).find()) {
                return "Security Policy Violation: Usage of os, subprocess, system calls, or dynamic evaluation is prohibited.";
            }
        } else if ("CPP".equals(lang) || "C++".equals(lang) || "C".equals(lang)) {
            if (CPP_FORBIDDEN.matcher(code).find()) {
                return "Security Policy Violation: Invocation of system(), popen(), fork(), or destructive POSIX calls is prohibited.";
            }
        } else if ("JAVASCRIPT".equals(lang) || "JS".equals(lang) || "NODE".equals(lang) || "NODEJS".equals(lang)) {
            if (JS_FORBIDDEN.matcher(code).find()) {
                return "Security Policy Violation: Child processes, process.exit, or dynamic evaluation is prohibited.";
            }
        }

        return null;
    }
}
