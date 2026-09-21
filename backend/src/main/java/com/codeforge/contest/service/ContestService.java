package com.codeforge.contest.service;

import com.codeforge.contest.dto.*;
import com.codeforge.contest.entity.*;
import com.codeforge.contest.repository.*;
import com.codeforge.exception.BadRequestException;
import com.codeforge.exception.ResourceNotFoundException;
import com.codeforge.problem.entity.Problem;
import com.codeforge.problem.repository.ProblemRepository;
import com.codeforge.submission.entity.Submission;
import com.codeforge.submission.entity.SubmissionStatus;
import com.codeforge.submission.repository.SubmissionRepository;
import com.codeforge.user.entity.Role;
import com.codeforge.user.entity.User;
import com.codeforge.user.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ContestService {

    private final ContestRepository contestRepository;
    private final ContestProblemRepository contestProblemRepository;
    private final ContestParticipantRepository contestParticipantRepository;
    private final ContestProblemResultRepository contestProblemResultRepository;
    private final ProblemRepository problemRepository;
    private final UserRepository userRepository;
    private final SubmissionRepository submissionRepository;

    public ContestService(ContestRepository contestRepository,
                          ContestProblemRepository contestProblemRepository,
                          ContestParticipantRepository contestParticipantRepository,
                          ContestProblemResultRepository contestProblemResultRepository,
                          ProblemRepository problemRepository,
                          UserRepository userRepository,
                          SubmissionRepository submissionRepository) {
        this.contestRepository = contestRepository;
        this.contestProblemRepository = contestProblemRepository;
        this.contestParticipantRepository = contestParticipantRepository;
        this.contestProblemResultRepository = contestProblemResultRepository;
        this.problemRepository = problemRepository;
        this.userRepository = userRepository;
        this.submissionRepository = submissionRepository;
    }

    @Transactional(readOnly = true)
    public List<ContestDto> getAllContests() {
        List<Contest> contests = contestRepository.findAllByOrderByStartTimeDesc();
        List<ContestDto> dtos = new ArrayList<>();

        for (Contest c : contests) {
            ContestDto dto = new ContestDto();
            dto.setId(c.getId());
            dto.setTitle(c.getTitle());
            dto.setSlug(c.getSlug());
            dto.setDescription(c.getDescription());
            dto.setRules(c.getRules());
            dto.setOrganizerName(c.getCreatedBy() != null ? c.getCreatedBy().getUsername() : "CodeForge Official");
            dto.setStartTime(c.getStartTime());
            dto.setEndTime(c.getEndTime());
            dto.setDurationMinutes(c.getDurationMinutes());
            dto.setStatus(c.getStatus());
            dto.setParticipantCount(contestParticipantRepository.countByContestId(c.getId()));
            dto.setProblemCount(contestProblemRepository.findByContestIdOrderByOrderIndexAsc(c.getId()).size());
            dtos.add(dto);
        }

        return dtos;
    }

    @Transactional(readOnly = true)
    public List<ContestDto> getContestsByOrganizer(String organizerUsername) {
        List<Contest> contests = contestRepository.findByCreatedByUsernameOrderByStartTimeDesc(organizerUsername);
        List<ContestDto> dtos = new ArrayList<>();

        for (Contest c : contests) {
            ContestDto dto = new ContestDto();
            dto.setId(c.getId());
            dto.setTitle(c.getTitle());
            dto.setSlug(c.getSlug());
            dto.setDescription(c.getDescription());
            dto.setRules(c.getRules());
            dto.setOrganizerName(c.getCreatedBy() != null ? c.getCreatedBy().getUsername() : organizerUsername);
            dto.setStartTime(c.getStartTime());
            dto.setEndTime(c.getEndTime());
            dto.setDurationMinutes(c.getDurationMinutes());
            dto.setStatus(c.getStatus());
            dto.setParticipantCount(contestParticipantRepository.countByContestId(c.getId()));
            dto.setProblemCount(contestProblemRepository.findByContestIdOrderByOrderIndexAsc(c.getId()).size());
            dtos.add(dto);
        }

        return dtos;
    }

    @Transactional(readOnly = true)
    public ContestDetailDto getContestById(Long id, String currentUsername) {
        Contest c = contestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contest", "id", id));
        return mapToDetailDto(c, currentUsername);
    }

    @Transactional(readOnly = true)
    public ContestDetailDto getContestBySlug(String slug, String currentUsername) {
        Contest c = contestRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Contest", "slug", slug));
        return mapToDetailDto(c, currentUsername);
    }

    @Transactional
    public void registerUserForContest(Long contestId, String username) {
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new ResourceNotFoundException("Contest", "id", contestId));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        if (!contestParticipantRepository.existsByContestIdAndUserId(contestId, user.getId())) {
            ContestParticipant participant = new ContestParticipant(contest, user);
            contestParticipantRepository.save(participant);
        }
    }

    /**
     * Automatic Leaderboard with Priority:
     * 1. Problems Solved (descending)
     * 2. Total Solve Time / Penalty (ascending)
     * 3. Tie-breaker: earlier final accepted solution (lastAcceptedAt)
     */
    @Transactional(readOnly = true)
    public List<ContestStandingDto> getContestLeaderboard(Long contestId) {
        List<ContestParticipant> participants = contestParticipantRepository.findByContestId(contestId);

        // Sort by strict ranking rules
        participants.sort((a, b) -> {
            // 1. More problems solved = higher rank
            int solvedComp = Integer.compare(
                    b.getProblemsSolved() != null ? b.getProblemsSolved() : 0,
                    a.getProblemsSolved() != null ? a.getProblemsSolved() : 0
            );
            if (solvedComp != 0) return solvedComp;

            // 2. Lower penalty / solve time = higher rank
            int penaltyA = (a.getPenaltyMinutes() != null ? a.getPenaltyMinutes() : 0);
            int penaltyB = (b.getPenaltyMinutes() != null ? b.getPenaltyMinutes() : 0);
            int penaltyComp = Integer.compare(penaltyA, penaltyB);
            if (penaltyComp != 0) return penaltyComp;

            // 3. Tie-breaker: earlier final accepted result
            if (a.getLastAcceptedAt() != null && b.getLastAcceptedAt() != null) {
                return a.getLastAcceptedAt().compareTo(b.getLastAcceptedAt());
            } else if (a.getLastAcceptedAt() != null) {
                return -1;
            } else if (b.getLastAcceptedAt() != null) {
                return 1;
            }

            return Long.compare(a.getId(), b.getId());
        });

        List<ContestStandingDto> standings = new ArrayList<>();
        int rank = 1;
        for (ContestParticipant p : participants) {
            boolean isWinner = (rank == 1 && (p.getProblemsSolved() != null && p.getProblemsSolved() > 0));
            long totalSeconds = p.getTotalSolveTimeSeconds() != null ? p.getTotalSolveTimeSeconds() : 0L;
            String timeFormatted = ContestProblemResult.formatDuration(totalSeconds);

            standings.add(new ContestStandingDto(
                    rank++,
                    p.getUser().getId(),
                    p.getUser().getUsername(),
                    p.getUser().getAvatarUrl(),
                    p.getProblemsSolved() != null ? p.getProblemsSolved() : 0,
                    timeFormatted,
                    p.getScore(),
                    p.getPenaltyMinutes(),
                    isWinner
            ));
        }

        return standings;
    }

    /**
     * Dedicated Result Section (Section 10)
     * For every participant, calculates per-problem solve time tracking, attempt breakdown,
     * total penalty, and overall ranks.
     */
    @Transactional(readOnly = true)
    public ContestResultDto getContestResults(Long contestId) {
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new ResourceNotFoundException("Contest", "id", contestId));

        List<ContestProblem> cpList = contestProblemRepository.findByContestIdOrderByOrderIndexAsc(contestId);
        List<ContestStandingDto> leaderboard = getContestLeaderboard(contestId);

        ContestResultDto resultDto = new ContestResultDto();
        resultDto.setContestId(contest.getId());
        resultDto.setContestTitle(contest.getTitle());
        resultDto.setContestStatus(contest.getStatus());
        resultDto.setTotalParticipants(leaderboard.size());

        if (!leaderboard.isEmpty() && leaderboard.get(0).getProblemsSolved() > 0) {
            ContestStandingDto top = leaderboard.get(0);
            resultDto.setWinner(new ContestResultDto.WinnerDto(
                    top.getUserId(),
                    top.getUsername(),
                    top.getProblemsSolved(),
                    0L,
                    top.getTotalSolveTime(),
                    top.getScore()
            ));
        }

        List<ContestResultDto.ParticipantResultDto> participantResults = new ArrayList<>();

        for (ContestStandingDto standing : leaderboard) {
            ContestResultDto.ParticipantResultDto pDto = new ContestResultDto.ParticipantResultDto();
            pDto.setRank(standing.getRank());
            pDto.setUserId(standing.getUserId());
            pDto.setUsername(standing.getUsername());
            pDto.setProblemsSolved(standing.getProblemsSolved());
            pDto.setTotalPenalty(standing.getPenaltyMinutes());
            pDto.setTotalScore(standing.getScore());
            pDto.setFormattedTotalTime(standing.getTotalSolveTime());

            List<ContestProblemResult> userResults = contestProblemResultRepository.findByContestIdAndUserId(contestId, standing.getUserId());
            Map<Long, ContestProblemResult> resultMap = userResults.stream()
                    .collect(Collectors.toMap(r -> r.getProblem().getId(), r -> r, (a, b) -> a));

            List<ContestResultDto.ProblemBreakdownDto> breakdowns = new ArrayList<>();
            int attemptedCount = 0;
            int totalSubmissions = 0;
            int acceptedCount = 0;
            int failedCount = 0;
            long totalSolveSecs = 0L;
            Long fastestSec = null;
            String fastestName = "—";
            Long slowestSec = null;
            String slowestName = "—";

            for (ContestProblem cp : cpList) {
                Problem p = cp.getProblem();
                ContestProblemResult r = resultMap.get(p.getId());

                if (r != null) {
                    int attempts = r.getAttemptsCount();
                    totalSubmissions += attempts;
                    if (attempts > 0) attemptedCount++;

                    boolean isAc = "ACCEPTED".equalsIgnoreCase(r.getStatus());
                    if (isAc) {
                        acceptedCount++;
                        failedCount += (attempts - 1);
                        long sec = r.getSolveTimeSeconds() != null ? r.getSolveTimeSeconds() : 0L;
                        totalSolveSecs += sec;
                        if (fastestSec == null || sec < fastestSec) {
                            fastestSec = sec;
                            fastestName = p.getTitle() + " (" + r.getFormattedSolveTime() + ")";
                        }
                        if (slowestSec == null || sec > slowestSec) {
                            slowestSec = sec;
                            slowestName = p.getTitle() + " (" + r.getFormattedSolveTime() + ")";
                        }
                    } else if (attempts > 0) {
                        failedCount += attempts;
                    }

                    breakdowns.add(new ContestResultDto.ProblemBreakdownDto(
                            p.getId(),
                            p.getTitle(),
                            r.getStatus(),
                            r.getSolveTimeSeconds(),
                            r.getFormattedSolveTime(),
                            attempts,
                            r.getScore()
                    ));
                } else {
                    breakdowns.add(new ContestResultDto.ProblemBreakdownDto(
                            p.getId(),
                            p.getTitle(),
                            "UNSOLVED",
                            null,
                            "—",
                            0,
                            0
                    ));
                }
            }

            pDto.setProblemsAttempted(attemptedCount);
            pDto.setTotalSubmissions(totalSubmissions);
            pDto.setAcceptedSubmissions(acceptedCount);
            pDto.setFailedSubmissions(failedCount);
            pDto.setProblemBreakdowns(breakdowns);

            if (acceptedCount > 0) {
                long avgSec = totalSolveSecs / acceptedCount;
                pDto.setAverageSolveTime(ContestProblemResult.formatDuration(avgSec));
            } else {
                pDto.setAverageSolveTime("—");
            }
            pDto.setFastestSolvedProblem(fastestName);
            pDto.setSlowestSolvedProblem(slowestName);

            participantResults.add(pDto);
        }

        resultDto.setResults(participantResults);
        return resultDto;
    }

    /**
     * Contest Result Analytics (Section 12)
     * For organizers and admin to inspect problem solve rates, solve times, difficulty metrics.
     */
    @Transactional(readOnly = true)
    public ContestAnalyticsDto getContestAnalytics(Long contestId) {
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new ResourceNotFoundException("Contest", "id", contestId));

        List<ContestProblem> cpList = contestProblemRepository.findByContestIdOrderByOrderIndexAsc(contestId);
        List<ContestProblemResult> allResults = contestProblemResultRepository.findByContestId(contestId);
        long participantCount = contestParticipantRepository.countByContestId(contestId);

        ContestAnalyticsDto analytics = new ContestAnalyticsDto();
        analytics.setContestId(contest.getId());
        analytics.setContestTitle(contest.getTitle());
        analytics.setTotalParticipants((int) participantCount);

        int totalSubs = 0;
        int totalAc = 0;
        int totalWa = 0;
        long sumSolveSecs = 0L;
        int countSolveSecs = 0;

        List<ContestAnalyticsDto.ProblemMetricDto> metrics = new ArrayList<>();
        String easiestProblem = "—";
        double highestSolveRate = -1.0;
        String hardestProblem = "—";
        double lowestSolveRate = 101.0;
        String fastestSolution = "—";
        long absoluteFastestSec = Long.MAX_VALUE;

        Map<String, Integer> scoreDist = new LinkedHashMap<>();
        scoreDist.put("0 pts", 0);
        scoreDist.put("1 - 100 pts", 0);
        scoreDist.put("101 - 200 pts", 0);
        scoreDist.put("201 - 300 pts", 0);
        scoreDist.put("300+ pts", 0);

        List<ContestParticipant> participants = contestParticipantRepository.findByContestId(contestId);
        for (ContestParticipant p : participants) {
            int score = p.getScore() != null ? p.getScore() : 0;
            if (score == 0) scoreDist.put("0 pts", scoreDist.get("0 pts") + 1);
            else if (score <= 100) scoreDist.put("1 - 100 pts", scoreDist.get("1 - 100 pts") + 1);
            else if (score <= 200) scoreDist.put("101 - 200 pts", scoreDist.get("101 - 200 pts") + 1);
            else if (score <= 300) scoreDist.put("201 - 300 pts", scoreDist.get("201 - 300 pts") + 1);
            else scoreDist.put("300+ pts", scoreDist.get("300+ pts") + 1);
        }

        for (ContestProblem cp : cpList) {
            Problem problem = cp.getProblem();
            List<ContestProblemResult> pResults = allResults.stream()
                    .filter(r -> r.getProblem().getId().equals(problem.getId()))
                    .toList();

            int probAttempts = 0;
            int probAc = 0;
            long probSumSecs = 0L;
            long fastestProbSec = Long.MAX_VALUE;

            for (ContestProblemResult r : pResults) {
                probAttempts += r.getAttemptsCount();
                if ("ACCEPTED".equalsIgnoreCase(r.getStatus())) {
                    probAc++;
                    if (r.getSolveTimeSeconds() != null && r.getSolveTimeSeconds() >= 0) {
                        probSumSecs += r.getSolveTimeSeconds();
                        if (r.getSolveTimeSeconds() < fastestProbSec) {
                            fastestProbSec = r.getSolveTimeSeconds();
                        }
                        if (r.getSolveTimeSeconds() < absoluteFastestSec) {
                            absoluteFastestSec = r.getSolveTimeSeconds();
                            fastestSolution = problem.getTitle() + " (" + r.getFormattedSolveTime() + " by " + r.getUser().getUsername() + ")";
                        }
                    }
                }
            }

            totalSubs += probAttempts;
            totalAc += probAc;
            totalWa += Math.max(0, probAttempts - probAc);

            double solveRate = (participantCount > 0) ? (probAc * 100.0 / participantCount) : 0.0;
            String avgTime = probAc > 0 ? ContestProblemResult.formatDuration(probSumSecs / probAc) : "—";
            String fastestTime = fastestProbSec != Long.MAX_VALUE ? ContestProblemResult.formatDuration(fastestProbSec) : "—";

            if (probAc > 0) {
                sumSolveSecs += probSumSecs;
                countSolveSecs += probAc;
            }

            if (participantCount > 0) {
                if (solveRate > highestSolveRate) {
                    highestSolveRate = solveRate;
                    easiestProblem = problem.getTitle() + " (" + String.format("%.1f", solveRate) + "% solved)";
                }
                if (solveRate < lowestSolveRate) {
                    lowestSolveRate = solveRate;
                    hardestProblem = problem.getTitle() + " (" + String.format("%.1f", solveRate) + "% solved)";
                }
            }

            metrics.add(new ContestAnalyticsDto.ProblemMetricDto(
                    problem.getId(),
                    problem.getTitle(),
                    problem.getDifficulty().name(),
                    probAttempts,
                    probAc,
                    Math.round(solveRate * 10.0) / 10.0,
                    avgTime,
                    fastestTime
            ));
        }

        analytics.setTotalSubmissions(totalSubs);
        analytics.setAcceptedSubmissions(totalAc);
        analytics.setWrongSubmissions(totalWa);
        analytics.setCompilationErrors(0);
        analytics.setAverageSolveTime(countSolveSecs > 0 ? ContestProblemResult.formatDuration(sumSolveSecs / countSolveSecs) : "—");
        analytics.setEasiestProblem(easiestProblem);
        analytics.setMostDifficultProblem(hardestProblem);
        analytics.setFastestSolution(fastestSolution);
        analytics.setProblemMetrics(metrics);
        analytics.setScoreDistribution(scoreDist);

        return analytics;
    }

    @Transactional
    public ContestDetailDto createContest(ContestCreateRequest request, String creatorUsername) {
        String slug = generateSlug(request.getTitle());
        if (contestRepository.existsBySlug(slug)) {
            slug = slug + "-" + System.currentTimeMillis();
        }

        User creator = null;
        if (creatorUsername != null) {
            creator = userRepository.findByUsername(creatorUsername).orElse(null);
        }

        Contest contest = new Contest(
                request.getTitle(),
                slug,
                request.getDescription(),
                request.getStartTime(),
                request.getEndTime(),
                request.getDurationMinutes() != null ? request.getDurationMinutes() : 120
        );
        contest.setRules(request.getRules());
        contest.setPenaltyMinutesPerWrong(request.getPenaltyMinutesPerWrong() != null ? request.getPenaltyMinutesPerWrong() : 20);
        contest.setCreatedBy(creator);

        Contest saved = contestRepository.save(contest);

        if (request.getProblems() != null) {
            for (ContestCreateRequest.ContestProblemItemRequest item : request.getProblems()) {
                Problem p = problemRepository.findById(item.getProblemId()).orElse(null);
                if (p != null) {
                    ContestProblem cp = new ContestProblem(saved, p, item.getPoints(), item.getOrderIndex());
                    contestProblemRepository.save(cp);
                }
            }
        }

        return mapToDetailDto(saved, creatorUsername);
    }

    @Transactional
    public ContestDetailDto updateContest(Long id, ContestCreateRequest request, String username) {
        Contest contest = contestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contest", "id", id));

        // Authorization check: Admin or the organizer who created it
        if (username != null) {
            User current = userRepository.findByUsername(username).orElse(null);
            if (current != null && current.getRole() != Role.ROLE_ADMIN) {
                if (contest.getCreatedBy() != null && !contest.getCreatedBy().getId().equals(current.getId())) {
                    throw new AccessDeniedException("You do not have permission to modify this contest");
                }
            }
        }

        contest.setTitle(request.getTitle());
        contest.setDescription(request.getDescription());
        contest.setRules(request.getRules());
        contest.setStartTime(request.getStartTime());
        contest.setEndTime(request.getEndTime());
        if (request.getDurationMinutes() != null) {
            contest.setDurationMinutes(request.getDurationMinutes());
        }
        if (request.getPenaltyMinutesPerWrong() != null) {
            contest.setPenaltyMinutesPerWrong(request.getPenaltyMinutesPerWrong());
        }

        Contest updated = contestRepository.save(contest);

        if (request.getProblems() != null) {
            contestProblemRepository.deleteByContestId(id);
            for (ContestCreateRequest.ContestProblemItemRequest item : request.getProblems()) {
                Problem p = problemRepository.findById(item.getProblemId()).orElse(null);
                if (p != null) {
                    ContestProblem cp = new ContestProblem(updated, p, item.getPoints(), item.getOrderIndex());
                    contestProblemRepository.save(cp);
                }
            }
        }

        return mapToDetailDto(updated, username);
    }

    @Transactional
    public void deleteContest(Long id, String username) {
        Contest contest = contestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contest", "id", id));

        if (username != null) {
            User current = userRepository.findByUsername(username).orElse(null);
            if (current != null && current.getRole() != Role.ROLE_ADMIN) {
                if (contest.getCreatedBy() != null && !contest.getCreatedBy().getId().equals(current.getId())) {
                    throw new AccessDeniedException("You do not have permission to delete this contest");
                }
            }
        }

        List<com.codeforge.submission.entity.Submission> contestSubmissions = submissionRepository.findByContestId(id);
        for (com.codeforge.submission.entity.Submission s : contestSubmissions) {
            s.setContest(null);
            submissionRepository.save(s);
        }

        contestProblemResultRepository.deleteByContestId(id);
        contestProblemRepository.deleteByContestId(id);
        contestParticipantRepository.findByContestId(id).forEach(contestParticipantRepository::delete);
        contestRepository.deleteById(id);
    }

    @Transactional
    public void updateContestStatus(Long id, String newStatus, String username) {
        Contest contest = contestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contest", "id", id));

        if (username != null) {
            User current = userRepository.findByUsername(username).orElse(null);
            if (current != null && current.getRole() != Role.ROLE_ADMIN) {
                if (contest.getCreatedBy() != null && !contest.getCreatedBy().getId().equals(current.getId())) {
                    throw new AccessDeniedException("You do not have permission to change status of this contest");
                }
            }
        }

        contest.setStatus(newStatus);
        contestRepository.save(contest);
    }

    private ContestDetailDto mapToDetailDto(Contest c, String currentUsername) {
        ContestDetailDto dto = new ContestDetailDto();
        dto.setId(c.getId());
        dto.setTitle(c.getTitle());
        dto.setSlug(c.getSlug());
        dto.setDescription(c.getDescription());
        dto.setRules(c.getRules());
        dto.setOrganizerName(c.getCreatedBy() != null ? c.getCreatedBy().getUsername() : "CodeForge Official");
        dto.setPenaltyMinutesPerWrong(c.getPenaltyMinutesPerWrong());
        dto.setStartTime(c.getStartTime());
        dto.setEndTime(c.getEndTime());
        dto.setDurationMinutes(c.getDurationMinutes());
        dto.setStatus(c.getStatus());
        dto.setParticipantCount(contestParticipantRepository.countByContestId(c.getId()));

        Map<Long, ContestProblemResult> userProblemResults = new HashMap<>();
        if (currentUsername != null) {
            userRepository.findByUsername(currentUsername).ifPresent(u -> {
                boolean isRegistered = contestParticipantRepository.existsByContestIdAndUserId(c.getId(), u.getId());
                dto.setIsRegistered(isRegistered);

                List<ContestProblemResult> results = contestProblemResultRepository.findByContestIdAndUserId(c.getId(), u.getId());
                for (ContestProblemResult r : results) {
                    userProblemResults.put(r.getProblem().getId(), r);
                }
            });
        }

        List<ContestProblem> cpList = contestProblemRepository.findByContestIdOrderByOrderIndexAsc(c.getId());
        List<ContestDetailDto.ContestProblemItemDto> problemDtos = new ArrayList<>();

        for (ContestProblem cp : cpList) {
            Problem p = cp.getProblem();
            ContestProblemResult userResult = userProblemResults.get(p.getId());

            ContestDetailDto.ContestProblemItemDto item = new ContestDetailDto.ContestProblemItemDto();
            item.setProblemId(p.getId());
            item.setTitle(p.getTitle());
            item.setSlug(p.getSlug());
            item.setDifficulty(p.getDifficulty().name());
            item.setPoints(cp.getPoints());
            item.setOrderIndex(cp.getOrderIndex());

            if (userResult != null) {
                boolean isAc = "ACCEPTED".equalsIgnoreCase(userResult.getStatus());
                item.setIsSolved(isAc);
                item.setUserStatus(isAc ? "SOLVED" : (userResult.getAttemptsCount() > 0 ? "ATTEMPTED" : "UNSOLVED"));
                item.setTimeTaken(userResult.getFormattedSolveTime());
                item.setAttempts(userResult.getAttemptsCount());
            } else {
                item.setIsSolved(false);
                item.setUserStatus("UNSOLVED");
                item.setTimeTaken("—");
                item.setAttempts(0);
            }
            problemDtos.add(item);
        }

        boolean privileged = false;
        if (currentUsername != null) {
            Optional<User> uOpt = userRepository.findByUsername(currentUsername);
            if (uOpt.isPresent()) {
                Role role = uOpt.get().getRole();
                privileged = (role == Role.ROLE_ADMIN || (c.getCreatedBy() != null && c.getCreatedBy().getId().equals(uOpt.get().getId())));
            }
        }

        if ("UPCOMING".equalsIgnoreCase(c.getStatus()) && !privileged) {
            dto.setProblems(Collections.emptyList());
        } else {
            dto.setProblems(problemDtos);
        }
        return dto;
    }

    private String generateSlug(String title) {
        if (title == null) return "contest-" + System.currentTimeMillis();
        return title.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();
    }
}
