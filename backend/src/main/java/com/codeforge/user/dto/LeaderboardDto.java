package com.codeforge.user.dto;

public class LeaderboardDto {

    private Integer rank;
    private Long userId;
    private String username;
    private String avatarUrl;
    private Long solvedCount;
    private Long easySolved;
    private Long mediumSolved;
    private Long hardSolved;
    private Long totalSubmissions;
    private Double acceptanceRate;
    private Long score;
    private String organization;

    public LeaderboardDto() {
    }

    public LeaderboardDto(Integer rank, Long userId, String username, String avatarUrl,
                          Long solvedCount, Long easySolved, Long mediumSolved, Long hardSolved,
                          Long totalSubmissions, Double acceptanceRate, Long score, String organization) {
        this.rank = rank;
        this.userId = userId;
        this.username = username;
        this.avatarUrl = avatarUrl;
        this.solvedCount = solvedCount;
        this.easySolved = easySolved;
        this.mediumSolved = mediumSolved;
        this.hardSolved = hardSolved;
        this.totalSubmissions = totalSubmissions;
        this.acceptanceRate = acceptanceRate;
        this.score = score;
        this.organization = organization;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public Long getSolvedCount() {
        return solvedCount;
    }

    public void setSolvedCount(Long solvedCount) {
        this.solvedCount = solvedCount;
    }

    public Long getEasySolved() {
        return easySolved;
    }

    public void setEasySolved(Long easySolved) {
        this.easySolved = easySolved;
    }

    public Long getMediumSolved() {
        return mediumSolved;
    }

    public void setMediumSolved(Long mediumSolved) {
        this.mediumSolved = mediumSolved;
    }

    public Long getHardSolved() {
        return hardSolved;
    }

    public void setHardSolved(Long hardSolved) {
        this.hardSolved = hardSolved;
    }

    public Long getTotalSubmissions() {
        return totalSubmissions;
    }

    public void setTotalSubmissions(Long totalSubmissions) {
        this.totalSubmissions = totalSubmissions;
    }

    public Double getAcceptanceRate() {
        return acceptanceRate;
    }

    public void setAcceptanceRate(Double acceptanceRate) {
        this.acceptanceRate = acceptanceRate;
    }

    public Long getScore() {
        return score;
    }

    public void setScore(Long score) {
        this.score = score;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }
}
