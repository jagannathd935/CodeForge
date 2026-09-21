package com.codeforge.problem.dto;

import java.util.List;

public class ProblemListDto {

    private Long id;
    private String title;
    private String slug;
    private String difficulty;
    private List<String> topics;
    private Double acceptanceRate;
    private Boolean isSolved = false;

    public ProblemListDto() {
    }

    public ProblemListDto(Long id, String title, String slug, String difficulty, List<String> topics, Double acceptanceRate, Boolean isSolved) {
        this.id = id;
        this.title = title;
        this.slug = slug;
        this.difficulty = difficulty;
        this.topics = topics;
        this.acceptanceRate = acceptanceRate;
        this.isSolved = isSolved;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public List<String> getTopics() {
        return topics;
    }

    public void setTopics(List<String> topics) {
        this.topics = topics;
    }

    public Double getAcceptanceRate() {
        return acceptanceRate;
    }

    public void setAcceptanceRate(Double acceptanceRate) {
        this.acceptanceRate = acceptanceRate;
    }

    public Boolean getIsSolved() {
        return isSolved;
    }

    public void setIsSolved(Boolean isSolved) {
        this.isSolved = isSolved;
    }
}
