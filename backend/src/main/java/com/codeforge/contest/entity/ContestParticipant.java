package com.codeforge.contest.entity;

import com.codeforge.user.entity.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "contest_participants")
public class ContestParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contest_id", nullable = false)
    private Contest contest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer score = 0;

    @Column(name = "penalty_minutes", nullable = false)
    private Integer penaltyMinutes = 0;

    @Column(name = "problems_solved", nullable = false)
    private Integer problemsSolved = 0;

    @Column(name = "total_solve_time_seconds", nullable = false)
    private Long totalSolveTimeSeconds = 0L;

    @Column(name = "last_accepted_at")
    private LocalDateTime lastAcceptedAt;

    @Column(name = "registered_at", nullable = false, updatable = false)
    private LocalDateTime registeredAt;

    public ContestParticipant() {
    }

    public ContestParticipant(Contest contest, User user) {
        this.contest = contest;
        this.user = user;
        this.score = 0;
        this.penaltyMinutes = 0;
        this.problemsSolved = 0;
        this.totalSolveTimeSeconds = 0L;
    }

    @PrePersist
    protected void onCreate() {
        this.registeredAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Contest getContest() {
        return contest;
    }

    public void setContest(Contest contest) {
        this.contest = contest;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public Long getTotalSolveTimeSeconds() {
        return totalSolveTimeSeconds != null ? totalSolveTimeSeconds : 0L;
    }

    public void setTotalSolveTimeSeconds(Long totalSolveTimeSeconds) {
        this.totalSolveTimeSeconds = totalSolveTimeSeconds;
    }

    public LocalDateTime getLastAcceptedAt() {
        return lastAcceptedAt;
    }

    public void setLastAcceptedAt(LocalDateTime lastAcceptedAt) {
        this.lastAcceptedAt = lastAcceptedAt;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }
}

