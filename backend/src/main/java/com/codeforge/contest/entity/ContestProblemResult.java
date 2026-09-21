package com.codeforge.contest.entity;

import com.codeforge.problem.entity.Problem;
import com.codeforge.user.entity.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "contest_problem_results", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"contest_id", "user_id", "problem_id"})
})
public class ContestProblemResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contest_id", nullable = false)
    private Contest contest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Column(nullable = false, length = 30)
    private String status = "UNSOLVED"; // ACCEPTED, WRONG_ANSWER, UNSOLVED

    @Column(name = "attempts_count", nullable = false)
    private Integer attemptsCount = 0;

    @Column(name = "wrong_attempts_count", nullable = false)
    private Integer wrongAttemptsCount = 0;

    @Column(name = "first_submission_time")
    private LocalDateTime firstSubmissionTime;

    @Column(name = "accepted_submission_time")
    private LocalDateTime acceptedSubmissionTime;

    @Column(name = "solve_time_seconds")
    private Long solveTimeSeconds;

    @Column(name = "formatted_solve_time", length = 30)
    private String formattedSolveTime = "—";

    @Column(nullable = false)
    private Integer score = 0;

    @Column(name = "penalty_minutes", nullable = false)
    private Integer penaltyMinutes = 0;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public ContestProblemResult() {
    }

    public ContestProblemResult(Contest contest, User user, Problem problem) {
        this.contest = contest;
        this.user = user;
        this.problem = problem;
        this.status = "UNSOLVED";
        this.attemptsCount = 0;
        this.wrongAttemptsCount = 0;
        this.formattedSolveTime = "—";
        this.score = 0;
        this.penaltyMinutes = 0;
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    @PrePersist
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public static String formatDuration(long seconds) {
        if (seconds < 0) return "—";
        long minutes = seconds / 60;
        long remainingSec = seconds % 60;
        return String.format("%dm %02ds", minutes, remainingSec);
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

    public Problem getProblem() {
        return problem;
    }

    public void setProblem(Problem problem) {
        this.problem = problem;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getAttemptsCount() {
        return attemptsCount != null ? attemptsCount : 0;
    }

    public void setAttemptsCount(Integer attemptsCount) {
        this.attemptsCount = attemptsCount;
    }

    public Integer getWrongAttemptsCount() {
        return wrongAttemptsCount != null ? wrongAttemptsCount : 0;
    }

    public void setWrongAttemptsCount(Integer wrongAttemptsCount) {
        this.wrongAttemptsCount = wrongAttemptsCount;
    }

    public LocalDateTime getFirstSubmissionTime() {
        return firstSubmissionTime;
    }

    public void setFirstSubmissionTime(LocalDateTime firstSubmissionTime) {
        this.firstSubmissionTime = firstSubmissionTime;
    }

    public LocalDateTime getAcceptedSubmissionTime() {
        return acceptedSubmissionTime;
    }

    public void setAcceptedSubmissionTime(LocalDateTime acceptedSubmissionTime) {
        this.acceptedSubmissionTime = acceptedSubmissionTime;
    }

    public Long getSolveTimeSeconds() {
        return solveTimeSeconds;
    }

    public void setSolveTimeSeconds(Long solveTimeSeconds) {
        this.solveTimeSeconds = solveTimeSeconds;
        if (solveTimeSeconds != null && solveTimeSeconds >= 0) {
            this.formattedSolveTime = formatDuration(solveTimeSeconds);
        } else {
            this.formattedSolveTime = "—";
        }
    }

    public String getFormattedSolveTime() {
        return formattedSolveTime != null ? formattedSolveTime : "—";
    }

    public String getSolveTimeString() {
        return getFormattedSolveTime();
    }

    public void setFormattedSolveTime(String formattedSolveTime) {
        this.formattedSolveTime = formattedSolveTime;
    }

    public Integer getScore() {
        return score != null ? score : 0;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getPenaltyMinutes() {
        return penaltyMinutes != null ? penaltyMinutes : 0;
    }

    public void setPenaltyMinutes(Integer penaltyMinutes) {
        this.penaltyMinutes = penaltyMinutes;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
