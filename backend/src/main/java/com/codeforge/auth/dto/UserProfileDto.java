package com.codeforge.auth.dto;

import java.time.LocalDateTime;

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
    private long easySolved;
    private long mediumSolved;
    private long hardSolved;
    private String organization;

    public UserProfileDto() {
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
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
}
