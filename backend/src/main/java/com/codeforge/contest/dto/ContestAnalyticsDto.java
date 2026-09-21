package com.codeforge.contest.dto;

import java.util.List;
import java.util.Map;

public class ContestAnalyticsDto {

    private Long contestId;
    private String contestTitle;
    private Integer totalParticipants;
    private Integer totalSubmissions;
    private Integer acceptedSubmissions;
    private Integer wrongSubmissions;
    private Integer compilationErrors;
    private String averageSolveTime;
    private String mostDifficultProblem;
    private String easiestProblem;
    private String fastestSolution; // e.g. "Binary Search (2m 14s by john_doe)"
    private List<ProblemMetricDto> problemMetrics;
    private Map<String, Integer> scoreDistribution; // e.g. "0-50": 3, "51-100": 5, ...

    public ContestAnalyticsDto() {}

    public static class ProblemMetricDto {
        private Long problemId;
        private String problemTitle;
        private String difficulty;
        private Integer attempts;
        private Integer successfulSolutions;
        private Double successPercentage;
        private String averageSolveTime;
        private String fastestSolveTime;

        public ProblemMetricDto() {}

        public ProblemMetricDto(Long problemId, String problemTitle, String difficulty, Integer attempts, Integer successfulSolutions, Double successPercentage, String averageSolveTime, String fastestSolveTime) {
            this.problemId = problemId;
            this.problemTitle = problemTitle;
            this.difficulty = difficulty;
            this.attempts = attempts;
            this.successfulSolutions = successfulSolutions;
            this.successPercentage = successPercentage;
            this.averageSolveTime = averageSolveTime;
            this.fastestSolveTime = fastestSolveTime;
        }

        public Long getProblemId() { return problemId; }
        public void setProblemId(Long problemId) { this.problemId = problemId; }
        public String getProblemTitle() { return problemTitle; }
        public void setProblemTitle(String problemTitle) { this.problemTitle = problemTitle; }
        public String getDifficulty() { return difficulty; }
        public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
        public Integer getAttempts() { return attempts; }
        public void setAttempts(Integer attempts) { this.attempts = attempts; }
        public Integer getSuccessfulSolutions() { return successfulSolutions; }
        public void setSuccessfulSolutions(Integer successfulSolutions) { this.successfulSolutions = successfulSolutions; }
        public Double getSuccessPercentage() { return successPercentage; }
        public void setSuccessPercentage(Double successPercentage) { this.successPercentage = successPercentage; }
        public String getAverageSolveTime() { return averageSolveTime; }
        public void setAverageSolveTime(String averageSolveTime) { this.averageSolveTime = averageSolveTime; }
        public String getFastestSolveTime() { return fastestSolveTime; }
        public void setFastestSolveTime(String fastestSolveTime) { this.fastestSolveTime = fastestSolveTime; }
    }

    public Long getContestId() { return contestId; }
    public void setContestId(Long contestId) { this.contestId = contestId; }
    public String getContestTitle() { return contestTitle; }
    public void setContestTitle(String contestTitle) { this.contestTitle = contestTitle; }
    public Integer getTotalParticipants() { return totalParticipants; }
    public void setTotalParticipants(Integer totalParticipants) { this.totalParticipants = totalParticipants; }
    public Integer getTotalSubmissions() { return totalSubmissions; }
    public void setTotalSubmissions(Integer totalSubmissions) { this.totalSubmissions = totalSubmissions; }
    public Integer getAcceptedSubmissions() { return acceptedSubmissions; }
    public void setAcceptedSubmissions(Integer acceptedSubmissions) { this.acceptedSubmissions = acceptedSubmissions; }
    public Integer getWrongSubmissions() { return wrongSubmissions; }
    public void setWrongSubmissions(Integer wrongSubmissions) { this.wrongSubmissions = wrongSubmissions; }
    public Integer getCompilationErrors() { return compilationErrors; }
    public void setCompilationErrors(Integer compilationErrors) { this.compilationErrors = compilationErrors; }
    public String getAverageSolveTime() { return averageSolveTime; }
    public void setAverageSolveTime(String averageSolveTime) { this.averageSolveTime = averageSolveTime; }
    public String getMostDifficultProblem() { return mostDifficultProblem; }
    public void setMostDifficultProblem(String mostDifficultProblem) { this.mostDifficultProblem = mostDifficultProblem; }
    public String getEasiestProblem() { return easiestProblem; }
    public void setEasiestProblem(String easiestProblem) { this.easiestProblem = easiestProblem; }
    public String getFastestSolution() { return fastestSolution; }
    public void setFastestSolution(String fastestSolution) { this.fastestSolution = fastestSolution; }
    public List<ProblemMetricDto> getProblemMetrics() { return problemMetrics; }
    public void setProblemMetrics(List<ProblemMetricDto> problemMetrics) { this.problemMetrics = problemMetrics; }
    public Map<String, Integer> getScoreDistribution() { return scoreDistribution; }
    public void setScoreDistribution(Map<String, Integer> scoreDistribution) { this.scoreDistribution = scoreDistribution; }
}
