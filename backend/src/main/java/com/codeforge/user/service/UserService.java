package com.codeforge.user.service;

import com.codeforge.auth.dto.TopicStatDto;
import com.codeforge.auth.dto.UserProfileDto;
import com.codeforge.exception.ResourceNotFoundException;
import com.codeforge.problem.dto.ProblemListDto;
import com.codeforge.problem.entity.Difficulty;
import com.codeforge.problem.entity.Problem;
import com.codeforge.topic.entity.Topic;
import com.codeforge.problem.repository.ProblemRepository;
import com.codeforge.submission.entity.Submission;
import com.codeforge.submission.entity.SubmissionStatus;
import com.codeforge.submission.repository.SubmissionRepository;
import com.codeforge.user.dto.LeaderboardDto;
import com.codeforge.user.entity.User;
import com.codeforge.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final SubmissionRepository submissionRepository;
    private final ProblemRepository problemRepository;

    public UserService(UserRepository userRepository,
                       SubmissionRepository submissionRepository,
                       ProblemRepository problemRepository) {
        this.userRepository = userRepository;
        this.submissionRepository = submissionRepository;
        this.problemRepository = problemRepository;
    }

    @Transactional(readOnly = true)
    public UserProfileDto getCurrentUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        UserProfileDto dto = new UserProfileDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole().name());
        dto.setOrganization(user.getOrganization() != null ? user.getOrganization() : "CodeForge Tech");
        dto.setCreatedAt(user.getCreatedAt());

        List<Submission> userSubs = submissionRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        long totalSubs = userSubs.size();
        dto.setTotalSubmissions(totalSubs);

        List<Submission> acceptedSubs = userSubs.stream()
                .filter(s -> s.getStatus() == SubmissionStatus.ACCEPTED)
                .toList();

        dto.setAcceptedSubmissions(acceptedSubs.size());
        double accRate = totalSubs > 0 ? Math.round(((double) acceptedSubs.size() / totalSubs * 100.0) * 10.0) / 10.0 : 0.0;
        dto.setAcceptanceRate(accRate);

        Set<Long> solvedProblemIds = new HashSet<>();
        long easy = 0;
        long medium = 0;
        long hard = 0;

        for (Submission sub : acceptedSubs) {
            Long problemId = sub.getProblem().getId();
            if (!solvedProblemIds.contains(problemId)) {
                solvedProblemIds.add(problemId);
                Difficulty diff = sub.getProblem().getDifficulty();
                if (diff == Difficulty.EASY) easy++;
                else if (diff == Difficulty.MEDIUM) medium++;
                else if (diff == Difficulty.HARD) hard++;
            }
        }

        dto.setSolvedCount(solvedProblemIds.size());
        dto.setEasySolved(easy);
        dto.setMediumSolved(medium);
        dto.setHardSolved(hard);
        dto.setAvatarUrl(user.getAvatarUrl());

        // Dynamic streak (consecutive active days ending today or yesterday)
        dto.setStreak(calculateStreak(userSubs));

        // Daily activity heatmap (date YYYY-MM-DD -> count of submissions)
        Map<String, Long> dailyActivity = new TreeMap<>();
        for (Submission s : userSubs) {
            if (s.getCreatedAt() != null) {
                String dateKey = s.getCreatedAt().toLocalDate().toString();
                dailyActivity.put(dateKey, dailyActivity.getOrDefault(dateKey, 0L) + 1L);
            }
        }
        dto.setDailyActivity(dailyActivity);

        // Topic-wise accuracy & weak topic calculation
        Map<String, long[]> topicCounts = new HashMap<>(); // topic -> [totalSubmissions, acceptedSubmissions]
        Map<String, Set<Long>> topicSolvedProblems = new HashMap<>();

        for (Submission s : userSubs) {
            if (s.getProblem() != null && s.getProblem().getTopics() != null) {
                for (Topic t : s.getProblem().getTopics()) {
                    String tName = t.getName();
                    topicCounts.putIfAbsent(tName, new long[]{0, 0});
                    long[] counts = topicCounts.get(tName);
                    counts[0]++; // total submissions
                    if (s.getStatus() == SubmissionStatus.ACCEPTED) {
                        counts[1]++; // accepted submissions
                        topicSolvedProblems.computeIfAbsent(tName, k -> new HashSet<>()).add(s.getProblem().getId());
                    }
                }
            }
        }

        List<TopicStatDto> topicStats = new ArrayList<>();
        List<String> weakTopics = new ArrayList<>();

        for (Map.Entry<String, long[]> entry : topicCounts.entrySet()) {
            String topicName = entry.getKey();
            long total = entry.getValue()[0];
            long accepted = entry.getValue()[1];
            long uniqueSolved = topicSolvedProblems.getOrDefault(topicName, Collections.emptySet()).size();
            double accuracy = total > 0 ? Math.round(((double) accepted / total * 100.0) * 10.0) / 10.0 : 0.0;

            topicStats.add(new TopicStatDto(topicName, total, accepted, uniqueSolved, accuracy));

            // A topic is deemed "weak" if accuracy is under 50%
            if (total > 0 && accuracy < 50.0) {
                weakTopics.add(topicName);
            }
        }

        // Sort topicStats by total submissions descending
        topicStats.sort((a, b) -> Long.compare(b.getTotalSubmissions(), a.getTotalSubmissions()));
        // Sort weak topics by lowest accuracy first
        weakTopics.sort(Comparator.comparingDouble(t -> {
            for (TopicStatDto ts : topicStats) {
                if (ts.getTopic().equals(t)) return ts.getAccuracy();
            }
            return 100.0;
        }));

        dto.setTopicStats(topicStats);
        dto.setWeakTopics(weakTopics);

        // 3-5 Rule-Based Problem Recommendations
        dto.setRecommendedProblems(generateRecommendations(solvedProblemIds, weakTopics, easy, medium, hard));

        return dto;
    }

    private List<ProblemListDto> generateRecommendations(Set<Long> solvedProblemIds,
                                                         List<String> weakTopics,
                                                         long easy, long medium, long hard) {
        List<Problem> allProblems = problemRepository.findAll();
        List<Problem> unsolved = allProblems.stream()
                .filter(p -> !solvedProblemIds.contains(p.getId()))
                .toList();

        if (unsolved.isEmpty()) {
            return Collections.emptyList();
        }

        // Determine user's target difficulty
        Difficulty targetDifficulty;
        if (easy < 3) {
            targetDifficulty = Difficulty.EASY;
        } else if (medium < 5) {
            targetDifficulty = Difficulty.MEDIUM;
        } else {
            targetDifficulty = Difficulty.HARD;
        }

        Set<String> weakTopicSet = new HashSet<>(weakTopics);

        // Rank unsolved problems based on weak topics and difficulty
        List<Problem> scored = new ArrayList<>(unsolved);
        scored.sort((p1, p2) -> {
            int score1 = 0;
            int score2 = 0;

            boolean p1Weak = p1.getTopics().stream().anyMatch(t -> weakTopicSet.contains(t.getName()));
            boolean p2Weak = p2.getTopics().stream().anyMatch(t -> weakTopicSet.contains(t.getName()));
            if (p1Weak) score1 += 10;
            if (p2Weak) score2 += 10;

            if (p1.getDifficulty() == targetDifficulty) score1 += 5;
            if (p2.getDifficulty() == targetDifficulty) score2 += 5;

            int cmp = Integer.compare(score2, score1);
            if (cmp != 0) return cmp;
            return Long.compare(p1.getId(), p2.getId());
        });

        int limit = Math.min(5, scored.size());
        List<Problem> picked = scored.subList(0, limit);

        return picked.stream().map(p -> {
            ProblemListDto d = new ProblemListDto();
            d.setId(p.getId());
            d.setTitle(p.getTitle());
            d.setSlug(p.getSlug());
            d.setDifficulty(p.getDifficulty().name());
            d.setTopics(p.getTopics().stream().map(Topic::getName).toList());
            d.setIsSolved(false);

            List<Submission> pSubs = submissionRepository.findByProblemIdOrderByCreatedAtDesc(p.getId());
            if (pSubs.isEmpty()) {
                d.setAcceptanceRate(0.0);
            } else {
                long acc = pSubs.stream().filter(s -> s.getStatus() == SubmissionStatus.ACCEPTED).count();
                d.setAcceptanceRate(Math.round(((double) acc / pSubs.size() * 100.0) * 10.0) / 10.0);
            }
            return d;
        }).toList();
    }

    @Transactional
    public UserProfileDto updateUserProfile(String username, String avatarUrl) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        if (avatarUrl != null) {
            user.setAvatarUrl(avatarUrl);
            userRepository.save(user);
        }

        return getCurrentUserProfile(username);
    }

    @Transactional(readOnly = true)
    public List<LeaderboardDto> getGlobalLeaderboard() {
        List<User> users = userRepository.findAll();
        List<LeaderboardDto> leaderboard = new ArrayList<>();

        for (User u : users) {
            long totalSubs = submissionRepository.countByUserId(u.getId());
            List<Submission> acceptedSubs = submissionRepository.findByUserIdOrderByCreatedAtDesc(u.getId())
                    .stream()
                    .filter(s -> s.getStatus() == SubmissionStatus.ACCEPTED)
                    .toList();

            Set<Long> solvedProblemIds = new HashSet<>();
            long easy = 0, medium = 0, hard = 0;

            for (Submission sub : acceptedSubs) {
                Long problemId = sub.getProblem().getId();
                if (!solvedProblemIds.contains(problemId)) {
                    solvedProblemIds.add(problemId);
                    Difficulty diff = sub.getProblem().getDifficulty();
                    if (diff == Difficulty.EASY) easy++;
                    else if (diff == Difficulty.MEDIUM) medium++;
                    else if (diff == Difficulty.HARD) hard++;
                }
            }

            if (solvedProblemIds.isEmpty()) {
                continue; // Only users who have solved problems appear on the leaderboard
            }

            long score = (easy * 100) + (medium * 200) + (hard * 300);
            double accRate = totalSubs > 0 ? Math.round(((double) acceptedSubs.size() / totalSubs) * 1000.0) / 10.0 : 0.0;

            LeaderboardDto item = new LeaderboardDto(
                    0,
                    u.getId(),
                    u.getUsername(),
                    u.getAvatarUrl(),
                    (long) solvedProblemIds.size(),
                    easy,
                    medium,
                    hard,
                    totalSubs,
                    accRate,
                    score,
                    u.getOrganization() != null ? u.getOrganization() : "CodeForge Tech"
            );
            leaderboard.add(item);
        }

        // Sort descending: score first, then solvedCount, then lower submissions
        leaderboard.sort((a, b) -> {
            int cmp = Long.compare(b.getScore(), a.getScore());
            if (cmp != 0) return cmp;
            return Long.compare(b.getSolvedCount(), a.getSolvedCount());
        });

        int rank = 1;
        for (LeaderboardDto item : leaderboard) {
            item.setRank(rank++);
        }

        return leaderboard;
    }

    private int calculateStreak(List<Submission> subs) {
        if (subs == null || subs.isEmpty()) return 0;

        Set<LocalDate> activeDates = new TreeSet<>(Collections.reverseOrder());
        for (Submission s : subs) {
            if (s.getCreatedAt() != null) {
                activeDates.add(s.getCreatedAt().toLocalDate());
            }
        }

        LocalDate today = LocalDate.now();
        LocalDate checkDate = today;

        // If no submission today, check if streak started yesterday
        if (!activeDates.contains(today)) {
            checkDate = today.minusDays(1);
            if (!activeDates.contains(checkDate)) {
                return 0;
            }
        }

        int streak = 0;
        while (activeDates.contains(checkDate)) {
            streak++;
            checkDate = checkDate.minusDays(1);
        }

        return streak;
    }
}
