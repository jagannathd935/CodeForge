package com.codeforge.contest.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "contests")
public class Contest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, unique = true, length = 150)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes = 120;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private com.codeforge.user.entity.User createdBy;

    @Column(columnDefinition = "TEXT")
    private String rules;

    @Column(name = "status", length = 30)
    private String status; // DRAFT, UPCOMING, LIVE, COMPLETED, CANCELLED

    @Column(name = "penalty_per_wrong", nullable = false)
    private Integer penaltyMinutesPerWrong = 20;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Contest() {
    }

    public Contest(String title, String slug, String description, LocalDateTime startTime, LocalDateTime endTime, Integer durationMinutes) {
        this.title = title;
        this.slug = slug;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationMinutes = durationMinutes;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public String getStatus() {
        if (status != null && (status.equalsIgnoreCase("DRAFT") || status.equalsIgnoreCase("CANCELLED"))) {
            return status.toUpperCase();
        }
        LocalDateTime now = LocalDateTime.now();
        if (startTime != null && now.isBefore(startTime)) {
            return "UPCOMING";
        } else if (endTime != null && now.isAfter(endTime)) {
            return "COMPLETED";
        } else {
            return "LIVE";
        }
    }

    public boolean isLive() {
        String s = getStatus();
        return "LIVE".equals(s) || "ACTIVE".equals(s);
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public com.codeforge.user.entity.User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(com.codeforge.user.entity.User createdBy) {
        this.createdBy = createdBy;
    }

    public String getRules() {
        return rules;
    }

    public void setRules(String rules) {
        this.rules = rules;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getPenaltyMinutesPerWrong() {
        return penaltyMinutesPerWrong != null ? penaltyMinutesPerWrong : 20;
    }

    public void setPenaltyMinutesPerWrong(Integer penaltyMinutesPerWrong) {
        this.penaltyMinutesPerWrong = penaltyMinutesPerWrong;
    }
}

