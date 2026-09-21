package com.codeforge.submission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SubmissionRequest {

    @NotNull(message = "Problem ID is required")
    private Long problemId;

    private Long contestId;

    private String language = "JAVA";

    @NotBlank(message = "Code cannot be empty")
    private String code;

    public SubmissionRequest() {
    }

    public SubmissionRequest(Long problemId, String language, String code) {
        this.problemId = problemId;
        this.language = language;
        this.code = code;
    }

    public SubmissionRequest(Long problemId, Long contestId, String language, String code) {
        this.problemId = problemId;
        this.contestId = contestId;
        this.language = language;
        this.code = code;
    }

    public Long getProblemId() {
        return problemId;
    }

    public void setProblemId(Long problemId) {
        this.problemId = problemId;
    }

    public Long getContestId() {
        return contestId;
    }

    public void setContestId(Long contestId) {
        this.contestId = contestId;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
