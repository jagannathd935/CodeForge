package com.codeforge.contest.dto;

import java.util.List;

public class ContestResultDto {

    private Long contestId;
    private String contestTitle;
    private String contestStatus;
    private Integer totalParticipants;
    private WinnerDto winner;
    private List<ParticipantResultDto> results;

    public ContestResultDto() {
    }

    public static class WinnerDto {
        private Long userId;
        private String username;
        private Integer problemsSolved;
        private Long totalSolveTimeSeconds;
        private String formattedTotalTime;
        private Integer score;

        public WinnerDto() {}

        public WinnerDto(Long userId, String username, Integer problemsSolved, Long totalSolveTimeSeconds, String formattedTotalTime, Integer score) {
            this.userId = userId;
            this.username = username;
            this.problemsSolved = problemsSolved;
            this.totalSolveTimeSeconds = totalSolveTimeSeconds;
            this.formattedTotalTime = formattedTotalTime;
            this.score = score;
        }

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public Integer getProblemsSolved() { return problemsSolved; }
        public void setProblemsSolved(Integer problemsSolved) { this.problemsSolved = problemsSolved; }
        public Long getTotalSolveTimeSeconds() { return totalSolveTimeSeconds; }
        public void setTotalSolveTimeSeconds(Long totalSolveTimeSeconds) { this.totalSolveTimeSeconds = totalSolveTimeSeconds; }
        public String getFormattedTotalTime() { return formattedTotalTime; }
        public void setFormattedTotalTime(String formattedTotalTime) { this.formattedTotalTime = formattedTotalTime; }
        public Integer getScore() { return score; }
        public void setScore(Integer score) { this.score = score; }
    }

    public static class ProblemBreakdownDto {
        private Long problemId;
        private String problemTitle;
        private String status; // ACCEPTED, WRONG_ANSWER, UNSOLVED
        private Long solveTimeSeconds;
        private String formattedSolveTime; // e.g., "18m 42s" or "—"
        private Integer attempts;
        private Integer score;

        public ProblemBreakdownDto() {}

        public ProblemBreakdownDto(Long problemId, String problemTitle, String status, Long solveTimeSeconds, String formattedSolveTime, Integer attempts, Integer score) {
            this.problemId = problemId;
            this.problemTitle = problemTitle;
            this.status = status;
            this.solveTimeSeconds = solveTimeSeconds;
            this.formattedSolveTime = formattedSolveTime;
            this.attempts = attempts;
            this.score = score;
        }

        public Long getProblemId() { return problemId; }
        public void setProblemId(Long problemId) { this.problemId = problemId; }
        public String getProblemTitle() { return problemTitle; }
        public void setProblemTitle(String problemTitle) { this.problemTitle = problemTitle; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Long getSolveTimeSeconds() { return solveTimeSeconds; }
        public void setSolveTimeSeconds(Long solveTimeSeconds) { this.solveTimeSeconds = solveTimeSeconds; }
        public String getFormattedSolveTime() { return formattedSolveTime; }
        public void setFormattedSolveTime(String formattedSolveTime) { this.formattedSolveTime = formattedSolveTime; }
        public Integer getAttempts() { return attempts; }
        public void setAttempts(Integer attempts) { this.attempts = attempts; }
        public Integer getScore() { return score; }
        public void setScore(Integer score) { this.score = score; }
    }

    public static class ParticipantResultDto {
        private Integer rank;
        private Long userId;
        private String username;
        private Integer problemsSolved;
        private Integer problemsAttempted;
        private Long totalSolveTimeSeconds;
        private String formattedTotalTime;
        private Integer totalPenalty;
        private Integer totalScore;
        private Integer totalSubmissions;
        private Integer acceptedSubmissions;
        private Integer failedSubmissions;
        private String averageSolveTime;
        private String fastestSolvedProblem;
        private String slowestSolvedProblem;
        private List<ProblemBreakdownDto> problemBreakdowns;

        public ParticipantResultDto() {}

        public Integer getRank() { return rank; }
        public void setRank(Integer rank) { this.rank = rank; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public Integer getProblemsSolved() { return problemsSolved; }
        public void setProblemsSolved(Integer problemsSolved) { this.problemsSolved = problemsSolved; }
        public Integer getProblemsAttempted() { return problemsAttempted; }
        public void setProblemsAttempted(Integer problemsAttempted) { this.problemsAttempted = problemsAttempted; }
        public Long getTotalSolveTimeSeconds() { return totalSolveTimeSeconds; }
        public void setTotalSolveTimeSeconds(Long totalSolveTimeSeconds) { this.totalSolveTimeSeconds = totalSolveTimeSeconds; }
        public String getFormattedTotalTime() { return formattedTotalTime; }
        public void setFormattedTotalTime(String formattedTotalTime) { this.formattedTotalTime = formattedTotalTime; }
        public Integer getTotalPenalty() { return totalPenalty; }
        public void setTotalPenalty(Integer totalPenalty) { this.totalPenalty = totalPenalty; }
        public Integer getTotalScore() { return totalScore; }
        public void setTotalScore(Integer totalScore) { this.totalScore = totalScore; }
        public Integer getTotalSubmissions() { return totalSubmissions; }
        public void setTotalSubmissions(Integer totalSubmissions) { this.totalSubmissions = totalSubmissions; }
        public Integer getAcceptedSubmissions() { return acceptedSubmissions; }
        public void setAcceptedSubmissions(Integer acceptedSubmissions) { this.acceptedSubmissions = acceptedSubmissions; }
        public Integer getFailedSubmissions() { return failedSubmissions; }
        public void setFailedSubmissions(Integer failedSubmissions) { this.failedSubmissions = failedSubmissions; }
        public String getAverageSolveTime() { return averageSolveTime; }
        public void setAverageSolveTime(String averageSolveTime) { this.averageSolveTime = averageSolveTime; }
        public String getFastestSolvedProblem() { return fastestSolvedProblem; }
        public void setFastestSolvedProblem(String fastestSolvedProblem) { this.fastestSolvedProblem = fastestSolvedProblem; }
        public String getSlowestSolvedProblem() { return slowestSolvedProblem; }
        public void setSlowestSolvedProblem(String slowestSolvedProblem) { this.slowestSolvedProblem = slowestSolvedProblem; }
        public List<ProblemBreakdownDto> getProblemBreakdowns() { return problemBreakdowns; }
        public void setProblemBreakdowns(List<ProblemBreakdownDto> problemBreakdowns) { this.problemBreakdowns = problemBreakdowns; }
        public List<ProblemBreakdownDto> getProblemBreakdown() { return problemBreakdowns; }
        public void setProblemBreakdown(List<ProblemBreakdownDto> problemBreakdown) { this.problemBreakdowns = problemBreakdown; }
    }

    public Long getContestId() { return contestId; }
    public void setContestId(Long contestId) { this.contestId = contestId; }
    public String getContestTitle() { return contestTitle; }
    public void setContestTitle(String contestTitle) { this.contestTitle = contestTitle; }
    public String getContestStatus() { return contestStatus; }
    public void setContestStatus(String contestStatus) { this.contestStatus = contestStatus; }
    public Integer getTotalParticipants() { return totalParticipants; }
    public void setTotalParticipants(Integer totalParticipants) { this.totalParticipants = totalParticipants; }
    public WinnerDto getWinner() { return winner; }
    public void setWinner(WinnerDto winner) { this.winner = winner; }
    public List<ParticipantResultDto> getResults() { return results; }
    public void setResults(List<ParticipantResultDto> results) { this.results = results; }
    public List<ParticipantResultDto> getParticipants() { return results; }
    public void setParticipants(List<ParticipantResultDto> participants) { this.results = participants; }
}
