package com.codeforge.user.service;

import com.codeforge.auth.dto.UserProfileDto;
import com.codeforge.exception.ResourceNotFoundException;
import com.codeforge.problem.entity.Difficulty;
import com.codeforge.submission.entity.Submission;
import com.codeforge.submission.entity.SubmissionStatus;
import com.codeforge.submission.repository.SubmissionRepository;
import com.codeforge.user.entity.User;
import com.codeforge.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final SubmissionRepository submissionRepository;

    public UserService(UserRepository userRepository, SubmissionRepository submissionRepository) {
        this.userRepository = userRepository;
        this.submissionRepository = submissionRepository;
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

        long totalSubs = submissionRepository.countByUserId(user.getId());
        dto.setTotalSubmissions(totalSubs);

        List<Submission> acceptedSubs = submissionRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .filter(s -> s.getStatus() == SubmissionStatus.ACCEPTED)
                .toList();

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

        // Calculate dynamic streak (consecutive active days up to today/yesterday)
        dto.setStreak(calculateStreak(user.getId()));

        return dto;
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
    public List<com.codeforge.user.dto.LeaderboardDto> getGlobalLeaderboard() {
        List<User> users = userRepository.findAll();
        List<com.codeforge.user.dto.LeaderboardDto> leaderboard = new ArrayList<>();

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

            com.codeforge.user.dto.LeaderboardDto item = new com.codeforge.user.dto.LeaderboardDto(
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
        for (com.codeforge.user.dto.LeaderboardDto item : leaderboard) {
            item.setRank(rank++);
        }

        return leaderboard;
    }

    private int calculateStreak(Long userId) {
        List<Submission> subs = submissionRepository.findByUserIdOrderByCreatedAtDesc(userId);
        if (subs.isEmpty()) return 0;

        Set<java.time.LocalDate> activeDates = new TreeSet<>(Collections.reverseOrder());
        for (Submission s : subs) {
            if (s.getCreatedAt() != null) {
                activeDates.add(s.getCreatedAt().toLocalDate());
            }
        }

        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate checkDate = today;

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

        // Realistic fallback for demo so newly created accounts have a minimum streak of 1 if they submitted today
        return Math.max(streak, 1);
    }
}
