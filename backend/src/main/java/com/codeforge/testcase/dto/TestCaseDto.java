package com.codeforge.testcase.dto;

public class TestCaseDto {

    private Long id;
    private String input;
    private String expectedOutput;
    private Boolean isHidden;

    public TestCaseDto() {
    }

    public TestCaseDto(Long id, String input, String expectedOutput, Boolean isHidden) {
        this.id = id;
        this.input = input;
        this.expectedOutput = expectedOutput;
        this.isHidden = isHidden;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getExpectedOutput() {
        return expectedOutput;
    }

    public void setExpectedOutput(String expectedOutput) {
        this.expectedOutput = expectedOutput;
    }

    public Boolean getIsHidden() {
        return isHidden;
    }

    public void setIsHidden(Boolean isHidden) {
        this.isHidden = isHidden;
    }
}
