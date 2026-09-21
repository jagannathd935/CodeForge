package com.codeforge.contest.repository;

import com.codeforge.contest.entity.ContestParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContestParticipantRepository extends JpaRepository<ContestParticipant, Long> {
    List<ContestParticipant> findByContestIdOrderByScoreDescPenaltyMinutesAsc(Long contestId);
    List<ContestParticipant> findByContestId(Long contestId);
    List<ContestParticipant> findByUserId(Long userId);
    Optional<ContestParticipant> findByContestIdAndUserId(Long contestId, Long userId);
    boolean existsByContestIdAndUserId(Long contestId, Long userId);
    long countByContestId(Long contestId);
    void deleteByUserId(Long userId);
}

