package com.codeforge.submission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RunCodeRequest {

    @NotNull(message = "Problem ID is required")
    private Long problemId;

    @NotBlank(message = "Code cannot be empty")
    private String code;

    private String language = "JAVA";

    private String customInput;
    private String input;

    public RunCodeRequest() {
    }

    public RunCodeRequest(Long problemId, String code, String language, String customInput) {
        this.problemId = problemId;
        this.code = code;
        this.language = language != null ? language : "JAVA";
        this.customInput = customInput;
        this.input = customInput;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Long getProblemId() {
        return problemId;
    }

    public void setProblemId(Long problemId) {
        this.problemId = problemId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCustomInput() {
        if (customInput != null && !customInput.isEmpty()) {
            return customInput;
        }
        return input;
    }

    public void setCustomInput(String customInput) {
        this.customInput = customInput;
        if (this.input == null) {
            this.input = customInput;
        }
    }

    public String getInput() {
        return getCustomInput();
    }

    public void setInput(String input) {
        this.input = input;
        if (this.customInput == null) {
            this.customInput = input;
        }
    }
}
