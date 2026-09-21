package com.codeforge.submission.service;

import com.codeforge.contest.entity.Contest;
import com.codeforge.contest.entity.ContestParticipant;
import com.codeforge.contest.entity.ContestProblem;
import com.codeforge.contest.entity.ContestProblemResult;
import com.codeforge.contest.repository.ContestParticipantRepository;
import com.codeforge.contest.repository.ContestProblemRepository;
import com.codeforge.contest.repository.ContestProblemResultRepository;
import com.codeforge.contest.repository.ContestRepository;
import com.codeforge.exception.BadRequestException;
import com.codeforge.exception.ResourceNotFoundException;
import com.codeforge.execution.JudgeService;
import com.codeforge.problem.entity.Problem;
import com.codeforge.problem.repository.ProblemRepository;
import com.codeforge.submission.dto.RunCodeRequest;
import com.codeforge.submission.dto.RunCodeResponse;
import com.codeforge.submission.dto.SubmissionRequest;
import com.codeforge.submission.dto.SubmissionResponse;
import com.codeforge.submission.entity.Submission;
import com.codeforge.submission.entity.SubmissionStatus;
import com.codeforge.submission.repository.SubmissionRepository;
import com.codeforge.testcase.entity.TestCase;
import com.codeforge.testcase.repository.TestCaseRepository;
import com.codeforge.user.entity.Role;
import com.codeforge.user.entity.User;
import com.codeforge.user.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final ProblemRepository problemRepository;
    private final UserRepository userRepository;
    private final TestCaseRepository testCaseRepository;
    private final JudgeService judgeService;
    private final ContestRepository contestRepository;
    private final ContestProblemRepository contestProblemRepository;
    private final ContestParticipantRepository contestParticipantRepository;
    private final ContestProblemResultRepository contestProblemResultRepository;

    public SubmissionService(SubmissionRepository submissionRepository,
                             ProblemRepository problemRepository,
                             UserRepository userRepository,
                             TestCaseRepository testCaseRepository,
                             JudgeService judgeService,
                             ContestRepository contestRepository,
                             ContestProblemRepository contestProblemRepository,
                             ContestParticipantRepository contestParticipantRepository,
                             ContestProblemResultRepository contestProblemResultRepository) {
        this.submissionRepository = submissionRepository;
        this.problemRepository = problemRepository;
        this.userRepository = userRepository;
        this.testCaseRepository = testCaseRepository;
        this.judgeService = judgeService;
        this.contestRepository = contestRepository;
        this.contestProblemRepository = contestProblemRepository;
        this.contestParticipantRepository = contestParticipantRepository;
        this.contestProblemResultRepository = contestProblemResultRepository;
    }

    @Transactional
    public SubmissionResponse submitCode(SubmissionRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        Problem problem = problemRepository.findById(request.getProblemId())
                .orElseThrow(() -> new ResourceNotFoundException("Problem", "id", request.getProblemId()));

        Contest contest = null;
        if (request.getContestId() != null) {
            contest = contestRepository.findById(request.getContestId())
                    .orElseThrow(() -> new ResourceNotFoundException("Contest", "id", request.getContestId()));

            String contestStatus = contest.getStatus();
            if ("UPCOMING".equalsIgnoreCase(contestStatus)) {
                throw new BadRequestException("Contest has not started yet. Submissions open at " + contest.getStartTime());
            } else if ("COMPLETED".equalsIgnoreCase(contestStatus) || "ENDED".equalsIgnoreCase(contestStatus)) {
                throw new BadRequestException("Contest has concluded. Submissions are closed for this contest.");
            } else if ("CANCELLED".equalsIgnoreCase(contestStatus) || "DRAFT".equalsIgnoreCase(contestStatus)) {
                throw new BadRequestException("Contest is not currently accepting submissions.");
            }

            // Auto-enroll user if not yet registered
            if (!contestParticipantRepository.existsByContestIdAndUserId(contest.getId(), user.getId())) {
                ContestParticipant p = new ContestParticipant(contest, user);
                contestParticipantRepository.save(p);
            }
        }

        List<TestCase> testCases = testCaseRepository.findByProblemId(problem.getId());

        Submission submission = new Submission(user, problem, request.getLanguage(), request.getCode());
        if (contest != null) {
            submission.setContest(contest);
        }
        submission = submissionRepository.save(submission);

        // Perform evaluation synchronously
        judgeService.evaluateSubmission(submission, problem, testCases);
        Submission saved = submissionRepository.save(submission);

        // Update contest per-problem solve times and scoring
        if (contest != null) {
            updateContestProblemResultAndStandings(contest, user, problem, saved);
        } else {
            // Also check if problem belongs to an active contest the user is enrolled in
            checkAutoContestScoring(user, problem, saved);
        }

        return mapToResponse(saved);
    }

    private void updateContestProblemResultAndStandings(Contest contest, User user, Problem problem, Submission submission) {
        ContestProblemResult result = contestProblemResultRepository
                .findByContestIdAndUserIdAndProblemId(contest.getId(), user.getId(), problem.getId())
                .orElseGet(() -> new ContestProblemResult(contest, user, problem));

        if (result.getFirstSubmissionTime() == null) {
            result.setFirstSubmissionTime(submission.getCreatedAt());
        }
        result.setAttemptsCount(result.getAttemptsCount() + 1);

        boolean isAccepted = (submission.getStatus() == SubmissionStatus.ACCEPTED);

        if (isAccepted) {
            // If already accepted previously, keep original first accepted time and score
            if (!"ACCEPTED".equalsIgnoreCase(result.getStatus())) {
                result.setStatus("ACCEPTED");
                result.setAcceptedSubmissionTime(submission.getCreatedAt());

                long solveSeconds = 0L;
                if (contest.getStartTime() != null) {
                    solveSeconds = Math.max(0, Duration.between(contest.getStartTime(), submission.getCreatedAt()).getSeconds());
                }
                result.setSolveTimeSeconds(solveSeconds);

                // Calculate problem points
                int points = 100;
                ContestProblem cp = contestProblemRepository.findByContestIdAndProblemId(contest.getId(), problem.getId()).orElse(null);
                if (cp != null && cp.getPoints() != null) {
                    points = cp.getPoints();
                }
                result.setScore(points);

                // Calculate penalty = solve time in minutes + (wrong attempts * penaltyPerWrong)
                int penaltyWrong = (result.getWrongAttemptsCount() != null ? result.getWrongAttemptsCount() : 0) * contest.getPenaltyMinutesPerWrong();
                int penaltyTime = (int) (solveSeconds / 60);
                result.setPenaltyMinutes(penaltyTime + penaltyWrong);

                contestProblemResultRepository.save(result);

                // Update Participant aggregate standing
                updateParticipantTotals(contest, user, submission.getCreatedAt());
            } else {
                contestProblemResultRepository.save(result);
            }
        } else {
            if (!"ACCEPTED".equalsIgnoreCase(result.getStatus())) {
                result.setStatus("WRONG_ANSWER");
                result.setWrongAttemptsCount(result.getWrongAttemptsCount() + 1);
            }
            contestProblemResultRepository.save(result);
        }
    }

    private void updateParticipantTotals(Contest contest, User user, LocalDateTime acceptedAt) {
        ContestParticipant participant = contestParticipantRepository
                .findByContestIdAndUserId(contest.getId(), user.getId())
                .orElseGet(() -> {
                    ContestParticipant p = new ContestParticipant(contest, user);
                    return contestParticipantRepository.save(p);
                });

        List<ContestProblemResult> allUserResults = contestProblemResultRepository.findByContestIdAndUserId(contest.getId(), user.getId());

        int totalScore = 0;
        int totalPenalty = 0;
        int solvedCount = 0;
        long totalSolveSecs = 0L;

        for (ContestProblemResult r : allUserResults) {
            if ("ACCEPTED".equalsIgnoreCase(r.getStatus())) {
                solvedCount++;
                totalScore += (r.getScore() != null ? r.getScore() : 0);
                totalPenalty += (r.getPenaltyMinutes() != null ? r.getPenaltyMinutes() : 0);
                totalSolveSecs += (r.getSolveTimeSeconds() != null ? r.getSolveTimeSeconds() : 0L);
            }
        }

        participant.setScore(totalScore);
        participant.setPenaltyMinutes(totalPenalty);
        participant.setProblemsSolved(solvedCount);
        participant.setTotalSolveTimeSeconds(totalSolveSecs);
        participant.setLastAcceptedAt(acceptedAt);

        contestParticipantRepository.save(participant);
    }

    private void checkAutoContestScoring(User user, Problem problem, Submission submission) {
        LocalDateTime now = LocalDateTime.now();
        List<ContestProblem> contestProblems = contestProblemRepository.findByProblemId(problem.getId());

        for (ContestProblem cp : contestProblems) {
            Contest contest = cp.getContest();
            if (contest.getStartTime() != null && contest.getEndTime() != null
                    && !now.isBefore(contest.getStartTime()) && !now.isAfter(contest.getEndTime())) {
                // If user is enrolled in this live contest, track it
                if (contestParticipantRepository.existsByContestIdAndUserId(contest.getId(), user.getId())) {
                    submission.setContest(contest);
                    submissionRepository.save(submission);
                    updateContestProblemResultAndStandings(contest, user, problem, submission);
                }
            }
        }
    }

    public RunCodeResponse runSampleCode(RunCodeRequest request) {
        Problem problem = problemRepository.findById(request.getProblemId())
                .orElseThrow(() -> new ResourceNotFoundException("Problem", "id", request.getProblemId()));

        List<TestCase> sampleCases = testCaseRepository.findByProblemIdAndIsHiddenFalse(problem.getId());
        TestCase sampleCase = sampleCases.isEmpty() ? null : sampleCases.get(0);

        return judgeService.runSampleCode(request, problem, sampleCase);
    }

    @Transactional(readOnly = true)
    public List<SubmissionResponse> getUserSubmissions(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        return submissionRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SubmissionResponse> getProblemSubmissions(Long problemId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        return submissionRepository.findByUserIdAndProblemIdOrderByCreatedAtDesc(user.getId(), problemId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SubmissionResponse getSubmissionById(Long id, String currentUsername) {
        Submission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Submission", "id", id));

        if (currentUsername == null) {
            throw new AccessDeniedException("Authentication required to view submission details");
        }

        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", currentUsername));

        boolean isOwner = submission.getUser().getId().equals(currentUser.getId());
        boolean isAdmin = (currentUser.getRole() == Role.ROLE_ADMIN);

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("You do not have permission to view this submission");
        }

        return mapToResponse(submission);
    }

    @Transactional(readOnly = true)
    public List<SubmissionResponse> getAllSubmissions() {
        return submissionRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private SubmissionResponse mapToResponse(Submission s) {
        SubmissionResponse res = new SubmissionResponse();
        res.setId(s.getId());
        res.setProblemId(s.getProblem().getId());
        res.setProblemTitle(s.getProblem().getTitle());
        if (s.getContest() != null) {
            res.setContestId(s.getContest().getId());
            res.setContestTitle(s.getContest().getTitle());
            contestProblemResultRepository
                    .findByContestIdAndUserIdAndProblemId(s.getContest().getId(), s.getUser().getId(), s.getProblem().getId())
                    .ifPresent(cpr -> res.setTimeTaken(cpr.getSolveTimeString()));
        }
        res.setLanguage(s.getLanguage());
        res.setStatus(s.getStatus().name());
        res.setRuntime(s.getRuntime());
        res.setMemory(s.getMemory());
        res.setTestsPassed(s.getTestsPassed());
        res.setTotalTests(s.getTotalTests());
        res.setErrorMessage(s.getErrorMessage());
        res.setCode(s.getCode());
        res.setCreatedAt(s.getCreatedAt());
        return res;
    }
}
