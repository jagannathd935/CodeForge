package com.codeforge.contest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

public class ContestCreateRequest {

    @NotBlank(message = "Contest title is required")
    private String title;

    private String description;

    private String rules;

    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    private LocalDateTime endTime;

    private Integer durationMinutes = 120;

    private Integer penaltyMinutesPerWrong = 20;

    private List<ContestProblemItemRequest> problems;

    public static class ContestProblemItemRequest {
        private Long problemId;
        private Integer points = 100;
        private Integer orderIndex = 1;

        public ContestProblemItemRequest() {
        }

        public ContestProblemItemRequest(Long problemId, Integer points, Integer orderIndex) {
            this.problemId = problemId;
            this.points = points;
            this.orderIndex = orderIndex;
        }

        public Long getProblemId() {
            return problemId;
        }

        public void setProblemId(Long problemId) {
            this.problemId = problemId;
        }

        public Integer getPoints() {
            return points;
        }

        public void setPoints(Integer points) {
            this.points = points;
        }

        public Integer getOrderIndex() {
            return orderIndex;
        }

        public void setOrderIndex(Integer orderIndex) {
            this.orderIndex = orderIndex;
        }
    }

    public ContestCreateRequest() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRules() {
        return rules;
    }

    public void setRules(String rules) {
        this.rules = rules;
    }

    public Integer getPenaltyMinutesPerWrong() {
        return penaltyMinutesPerWrong != null ? penaltyMinutesPerWrong : 20;
    }

    public void setPenaltyMinutesPerWrong(Integer penaltyMinutesPerWrong) {
        this.penaltyMinutesPerWrong = penaltyMinutesPerWrong;
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

    public List<ContestProblemItemRequest> getProblems() {
        return problems;
    }

    public void setProblems(List<ContestProblemItemRequest> problems) {
        this.problems = problems;
    }
}
