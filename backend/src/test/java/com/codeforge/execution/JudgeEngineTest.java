package com.codeforge.execution;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JudgeEngineTest {

    @Test
    void testCodeSanitizerDetectsDangerousJavaTokens() {
        String safeCode = "public class Solution { public static void main(String[] args) { System.out.println(\"Hello\"); } }";
        assertNull(CodeSanitizer.validate(safeCode, "JAVA"));

        String dangerousProcess = "class S { void run() { Runtime.getRuntime().exec(\"cmd\"); } }";
        assertNotNull(CodeSanitizer.validate(dangerousProcess, "JAVA"));

        String dangerousProcessBuilder = "class S { void run() { new ProcessBuilder(\"notepad\").start(); } }";
        assertNotNull(CodeSanitizer.validate(dangerousProcessBuilder, "JAVA"));

        String dangerousExit = "class S { void run() { System.exit(0); } }";
        assertNotNull(CodeSanitizer.validate(dangerousExit, "JAVA"));

        String dangerousSocket = "class S { void run() { java.net.Socket s = new java.net.Socket(\"example.com\", 80); } }";
        assertNotNull(CodeSanitizer.validate(dangerousSocket, "JAVA"));

        String dangerousProcessHandle = "class S { void run() { ProcessHandle.current().destroy(); } }";
        assertNotNull(CodeSanitizer.validate(dangerousProcessHandle, "JAVA"));

        String dangerousReflection = "class S { void run() throws Exception { Class.forName(\"java.lang.Runtime\"); } }";
        assertNotNull(CodeSanitizer.validate(dangerousReflection, "JAVA"));
    }

    @Test
    void testCodeSanitizerDetectsDangerousPythonTokens() {
        String safePy = "import sys\ninput_data = sys.stdin.read().strip()\nprint(sum([1, 2, 3]))";
        assertNull(CodeSanitizer.validate(safePy, "PYTHON"));

        String osImport = "import os\nos.system('dir')";
        assertNotNull(CodeSanitizer.validate(osImport, "PYTHON"));

        String subprocessImport = "import subprocess\nsubprocess.run(['ls'])";
        assertNotNull(CodeSanitizer.validate(subprocessImport, "PYTHON"));

        String evalUsage = "eval('__import__(\"os\").remove(\"file\")')";
        assertNotNull(CodeSanitizer.validate(evalUsage, "PYTHON"));

        String socketImport = "import socket\ns = socket.socket()";
        assertNotNull(CodeSanitizer.validate(socketImport, "PYTHON"));

        String shutilRmtree = "import shutil\nshutil.rmtree('/')";
        assertNotNull(CodeSanitizer.validate(shutilRmtree, "PYTHON"));
    }

    @Test
    void testCodeSanitizerDetectsDangerousCppTokens() {
        String safeCpp = "#include <iostream>\n#include <vector>\nint main() { std::cout << 42; return 0; }";
        assertNull(CodeSanitizer.validate(safeCpp, "CPP"));

        String systemCall = "#include <stdlib.h>\nint main() { system(\"calc\"); return 0; }";
        assertNotNull(CodeSanitizer.validate(systemCall, "CPP"));

        String forkCall = "#include <unistd.h>\nint main() { fork(); return 0; }";
        assertNotNull(CodeSanitizer.validate(forkCall, "CPP"));

        String windowsHeader = "#include <windows.h>\nint main() { return 0; }";
        assertNotNull(CodeSanitizer.validate(windowsHeader, "CPP"));

        String windowsHeaderQuotes = "#include \"windows.h\"\nint main() { return 0; }";
        assertNotNull(CodeSanitizer.validate(windowsHeaderQuotes, "CPP"));

        String socketHeader = "#include <sys/socket.h>\nint main() { return 0; }";
        assertNotNull(CodeSanitizer.validate(socketHeader, "CPP"));
    }

    @Test
    void testCodeSanitizerDetectsDangerousJsTokens() {
        String safeJs = "const fs = require('fs');\nconst data = fs.readFileSync(0, 'utf-8');\nconsole.log(data);";
        assertNull(CodeSanitizer.validate(safeJs, "JAVASCRIPT"));

        String childProcess = "const cp = require('child_process');\ncp.execSync('calc');";
        assertNotNull(CodeSanitizer.validate(childProcess, "JAVASCRIPT"));

        String processEnv = "console.log(process.env.JWT_SECRET);";
        assertNotNull(CodeSanitizer.validate(processEnv, "JAVASCRIPT"));

        String evalCall = "eval('console.log(1)');";
        assertNotNull(CodeSanitizer.validate(evalCall, "JAVASCRIPT"));
    }

    @Test
    void testCodeSanitizerLimits() {
        assertNotNull(CodeSanitizer.validate("", "JAVA"));
        assertNotNull(CodeSanitizer.validate(null, "JAVA"));

        // Test max code length limit (exceeding 64 KB)
        String massiveCode = "a".repeat(CodeSanitizer.MAX_CODE_LENGTH + 1);
        String result = CodeSanitizer.validate(massiveCode, "JAVA");
        assertNotNull(result);
        assertTrue(result.contains("exceeds maximum"));
    }

    @Test
    void testAccurateSolutionComparison() {
        JudgeService judge = new JudgeService(null);

        // 1. Direct match with Windows CRLF vs Linux LF
        assertTrue(judge.isOutputCorrect("1 2 3\r\n", "1 2 3\n"));

        // 2. Trailing spaces on lines
        assertTrue(judge.isOutputCorrect("1 2 3   \n4 5 6  ", "1 2 3\n4 5 6"));

        // 3. Space separated vs newline separated token stream
        assertTrue(judge.isOutputCorrect("1 2 3 4 5", "1\n2\n3\n4\n5"));

        // 4. Floating point precision within 1e-6 tolerance
        assertTrue(judge.isOutputCorrect("3.1415926", "3.141593"));

        // 5. Boolean & affirmation case tolerance
        assertTrue(judge.isOutputCorrect("True", "true"));
        assertTrue(judge.isOutputCorrect("YES", "yes"));
        assertTrue(judge.isOutputCorrect("False", "false"));

        // 6. Wrong answer detection
        assertFalse(judge.isOutputCorrect("1 2 3", "1 2 4"));
        assertFalse(judge.isOutputCorrect("false", "true"));
        assertFalse(judge.isOutputCorrect("hello", "world"));
    }
}
