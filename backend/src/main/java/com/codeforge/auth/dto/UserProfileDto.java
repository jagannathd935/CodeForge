package com.codeforge.auth.dto;

import com.codeforge.problem.dto.ProblemListDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UserProfileDto {

    private Long id;
    private String username;
    private String email;
    private String role;
    private Boolean active = true;
    private LocalDateTime createdAt;
    private String avatarUrl;
    private int streak = 0;
    private long solvedCount;
    private long totalSubmissions;
    private long acceptedSubmissions;
    private double acceptanceRate;
    private long easySolved;
    private long mediumSolved;
    private long hardSolved;
    private String organization;
    private List<TopicStatDto> topicStats = new ArrayList<>();
    private List<String> weakTopics = new ArrayList<>();
    private List<ProblemListDto> recommendedProblems = new ArrayList<>();
    private Map<String, Long> dailyActivity = new LinkedHashMap<>();

    public UserProfileDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Boolean getActive() {
        return active != null ? active : true;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public int getStreak() {
        return streak;
    }

    public void setStreak(int streak) {
        this.streak = streak;
    }

    public long getSolvedCount() {
        return solvedCount;
    }

    public void setSolvedCount(long solvedCount) {
        this.solvedCount = solvedCount;
    }

    public long getTotalSubmissions() {
        return totalSubmissions;
    }

    public void setTotalSubmissions(long totalSubmissions) {
        this.totalSubmissions = totalSubmissions;
    }

    public long getAcceptedSubmissions() {
        return acceptedSubmissions;
    }

    public void setAcceptedSubmissions(long acceptedSubmissions) {
        this.acceptedSubmissions = acceptedSubmissions;
    }

    public double getAcceptanceRate() {
        return acceptanceRate;
    }

    public void setAcceptanceRate(double acceptanceRate) {
        this.acceptanceRate = acceptanceRate;
    }

    public long getEasySolved() {
        return easySolved;
    }

    public void setEasySolved(long easySolved) {
        this.easySolved = easySolved;
    }

    public long getMediumSolved() {
        return mediumSolved;
    }

    public void setMediumSolved(long mediumSolved) {
        this.mediumSolved = mediumSolved;
    }

    public long getHardSolved() {
        return hardSolved;
    }

    public void setHardSolved(long hardSolved) {
        this.hardSolved = hardSolved;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public List<TopicStatDto> getTopicStats() {
        return topicStats;
    }

    public void setTopicStats(List<TopicStatDto> topicStats) {
        this.topicStats = topicStats;
    }

    public List<String> getWeakTopics() {
        return weakTopics;
    }

    public void setWeakTopics(List<String> weakTopics) {
        this.weakTopics = weakTopics;
    }

    public List<ProblemListDto> getRecommendedProblems() {
        return recommendedProblems;
    }

    public void setRecommendedProblems(List<ProblemListDto> recommendedProblems) {
        this.recommendedProblems = recommendedProblems;
    }

    public Map<String, Long> getDailyActivity() {
        return dailyActivity;
    }

    public void setDailyActivity(Map<String, Long> dailyActivity) {
        this.dailyActivity = dailyActivity;
    }
}
