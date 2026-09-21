package com.codeforge.contest.dto;

public class ContestStandingDto {

    private Integer rank;
    private Long userId;
    private String username;
    private String avatarUrl;
    private Integer problemsSolved = 0;
    private String totalSolveTime = "0m 00s";
    private Integer score;
    private Integer penaltyMinutes;
    private boolean isWinner = false;

    public ContestStandingDto() {
    }

    public ContestStandingDto(Integer rank, Long userId, String username, String avatarUrl, Integer problemsSolved, String totalSolveTime, Integer score, Integer penaltyMinutes, boolean isWinner) {
        this.rank = rank;
        this.userId = userId;
        this.username = username;
        this.avatarUrl = avatarUrl;
        this.problemsSolved = problemsSolved;
        this.totalSolveTime = totalSolveTime;
        this.score = score;
        this.penaltyMinutes = penaltyMinutes;
        this.isWinner = isWinner;
    }

    public ContestStandingDto(Integer rank, Long userId, String username, String avatarUrl, Integer score, Integer penaltyMinutes) {
        this.rank = rank;
        this.userId = userId;
        this.username = username;
        this.avatarUrl = avatarUrl;
        this.score = score;
        this.penaltyMinutes = penaltyMinutes;
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

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getPenaltyMinutes() {
        return penaltyMinutes;
    }

    public void setPenaltyMinutes(Integer penaltyMinutes) {
        this.penaltyMinutes = penaltyMinutes;
    }

    public Integer getProblemsSolved() {
        return problemsSolved != null ? problemsSolved : 0;
    }

    public void setProblemsSolved(Integer problemsSolved) {
        this.problemsSolved = problemsSolved;
    }

    public String getTotalSolveTime() {
        return totalSolveTime != null ? totalSolveTime : "0m 00s";
    }

    public void setTotalSolveTime(String totalSolveTime) {
        this.totalSolveTime = totalSolveTime;
    }

    public boolean isWinner() {
        return isWinner;
    }

    public void setWinner(boolean winner) {
        isWinner = winner;
    }
}

