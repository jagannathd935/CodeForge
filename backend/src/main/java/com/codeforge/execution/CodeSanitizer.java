package com.codeforge.execution;

import java.util.regex.Pattern;

public class CodeSanitizer {

    public static final int MAX_CODE_LENGTH = 65536; // 64 KB limit

    private static final Pattern JAVA_FORBIDDEN = Pattern.compile(
            "\\b(Runtime\\.getRuntime|ProcessBuilder|ProcessHandle|System\\.exit|System\\.load|System\\.loadLibrary"
                    + "|java\\.lang\\.reflect|sun\\.misc|Unsafe|SecurityManager|Class\\.forName"
                    + "|java\\.net\\.(Socket|URL|URI|URLConnection|HttpURLConnection|InetAddress|DatagramSocket)"
                    + "|java\\.nio\\.file\\.Files\\.(delete|write|move|copy)"
                    + "|new\\s+File\\([^)]*\\)\\.delete)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern PYTHON_FORBIDDEN = Pattern.compile(
            "\\b(import\\s+os\\b|from\\s+os\\s+import|import\\s+subprocess\\b|from\\s+subprocess\\s+import"
                    + "|import\\s+socket\\b|from\\s+socket\\s+import|import\\s+pty\\b|import\\s+ctypes\\b"
                    + "|import\\s+shutil\\b|from\\s+shutil\\s+import|import\\s+pathlib\\b|from\\s+pathlib\\s+import"
                    + "|import\\s+signal\\b|import\\s+http\\b|import\\s+urllib\\b|import\\s+requests\\b"
                    + "|import\\s+posix\\b|import\\s+resource\\b|from\\s+sys\\s+import\\s+exit|sys\\.exit"
                    + "|os\\.(system|popen|spawn|remove|unlink|rmdir|mkdir|rename|chmod|chown)"
                    + "|__import__|importlib|__builtins__|__subclasses__)\\b"
                    + "|\\b(eval|exec|compile)\\s*\\(",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern CPP_FORBIDDEN = Pattern.compile(
            "#\\s*include\\s*[<\"]\\s*(windows\\.h|winsock2\\.h|sys/socket\\.h|netinet/in\\.h|arpa/inet\\.h|unistd\\.h|dirent\\.h|sys/types\\.h|sys/stat\\.h)\\s*[>\"]"
                    + "|\\b(system|popen|fork|vfork|execve|execvp|execl|execlp|kill|raise|abort|unlink|remove|rmdir|rename)\\s*\\(",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern JS_FORBIDDEN = Pattern.compile(
            "\\b(child_process|worker_threads|cluster|process\\.exit|process\\.kill|process\\.env"
                    + "|require\\s*\\(\\s*['\"](http|https|net|dgram|child_process|worker_threads|cluster)['\"]\\s*\\)"
                    + "|fetch\\s*\\(|axios|fs\\.(unlink|rm|rmdir|writeFile|writeFileSync|appendFile))\\b"
                    + "|\\b(eval|Function)\\s*\\(",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Inspects submitted code for security violations and code length limits.
     * @param code The source code string.
     * @param language The programming language (JAVA, PYTHON, CPP, C, JAVASCRIPT).
     * @return Error message if prohibited tokens or limits are violated, or null if code is acceptable.
     */
    public static String validate(String code, String language) {
        if (code == null || code.trim().isEmpty()) {
            return "Submitted code cannot be empty.";
        }

        if (code.length() > MAX_CODE_LENGTH) {
            return "Security Policy Violation: Source code exceeds maximum allowed size (" + (MAX_CODE_LENGTH / 1024) + " KB).";
        }

        String lang = language != null ? language.toUpperCase() : "JAVA";

        if ("JAVA".equals(lang)) {
            if (JAVA_FORBIDDEN.matcher(code).find()) {
                return "Security Policy Violation: Invocation of ProcessBuilder, Runtime, Reflection, Network, or System.exit is prohibited.";
            }
        } else if ("PYTHON".equals(lang) || "PYTHON3".equals(lang)) {
            if (PYTHON_FORBIDDEN.matcher(code).find()) {
                return "Security Policy Violation: Usage of os, subprocess, sockets, system calls, or dynamic evaluation is prohibited.";
            }
        } else if ("CPP".equals(lang) || "C++".equals(lang) || "C".equals(lang)) {
            if (CPP_FORBIDDEN.matcher(code).find()) {
                return "Security Policy Violation: Restricted headers or invocation of system(), popen(), fork(), or destructive POSIX calls is prohibited.";
            }
        } else if ("JAVASCRIPT".equals(lang) || "JS".equals(lang) || "NODE".equals(lang) || "NODEJS".equals(lang)) {
            if (JS_FORBIDDEN.matcher(code).find()) {
                return "Security Policy Violation: Child processes, network calls, process.env, or dynamic evaluation is prohibited.";
            }
        }

        return null;
    }
}
