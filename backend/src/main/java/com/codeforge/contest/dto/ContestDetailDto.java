package com.codeforge.contest.dto;

import com.codeforge.problem.dto.ProblemListDto;

import java.time.LocalDateTime;
import java.util.List;

public class ContestDetailDto {

    private Long id;
    private String title;
    private String slug;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationMinutes;
    private String status;
    private Boolean isRegistered = false;
    private Long participantCount;
    private String rules;
    private String organizerName;
    private Integer penaltyMinutesPerWrong = 20;
    private List<ContestProblemItemDto> problems;

    public static class ContestProblemItemDto {
        private Long problemId;
        private String title;
        private String slug;
        private String difficulty;
        private Integer points;
        private Integer orderIndex;
        private Boolean isSolved = false;
        private String userStatus = "UNSOLVED"; // SOLVED, ATTEMPTED, UNSOLVED
        private String timeTaken = "—";
        private Integer attempts = 0;

        public ContestProblemItemDto() {
        }

        public ContestProblemItemDto(Long problemId, String title, String slug, String difficulty, Integer points, Integer orderIndex, Boolean isSolved) {
            this.problemId = problemId;
            this.title = title;
            this.slug = slug;
            this.difficulty = difficulty;
            this.points = points;
            this.orderIndex = orderIndex;
            this.isSolved = isSolved;
            this.userStatus = Boolean.TRUE.equals(isSolved) ? "SOLVED" : "UNSOLVED";
        }

        public Long getProblemId() {
            return problemId;
        }

        public void setProblemId(Long problemId) {
            this.problemId = problemId;
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

        public Boolean getIsSolved() {
            return isSolved;
        }

        public void setIsSolved(Boolean isSolved) {
            this.isSolved = isSolved;
        }

        public String getUserStatus() {
            return userStatus != null ? userStatus : (Boolean.TRUE.equals(isSolved) ? "SOLVED" : "UNSOLVED");
        }

        public void setUserStatus(String userStatus) {
            this.userStatus = userStatus;
        }

        public String getTimeTaken() {
            return timeTaken != null ? timeTaken : "—";
        }

        public void setTimeTaken(String timeTaken) {
            this.timeTaken = timeTaken;
        }

        public Integer getAttempts() {
            return attempts != null ? attempts : 0;
        }

        public void setAttempts(Integer attempts) {
            this.attempts = attempts;
        }
    }

    public ContestDetailDto() {
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getIsRegistered() {
        return isRegistered;
    }

    public void setIsRegistered(Boolean isRegistered) {
        this.isRegistered = isRegistered;
    }

    public Long getParticipantCount() {
        return participantCount;
    }

    public void setParticipantCount(Long participantCount) {
        this.participantCount = participantCount;
    }

    public List<ContestProblemItemDto> getProblems() {
        return problems;
    }

    public void setProblems(List<ContestProblemItemDto> problems) {
        this.problems = problems;
    }

    public String getRules() {
        return rules;
    }

    public void setRules(String rules) {
        this.rules = rules;
    }

    public String getOrganizerName() {
        return organizerName;
    }

    public void setOrganizerName(String organizerName) {
        this.organizerName = organizerName;
    }

    public Integer getPenaltyMinutesPerWrong() {
        return penaltyMinutesPerWrong != null ? penaltyMinutesPerWrong : 20;
    }

    public void setPenaltyMinutesPerWrong(Integer penaltyMinutesPerWrong) {
        this.penaltyMinutesPerWrong = penaltyMinutesPerWrong;
    }
}

