package com.codeforge.contest.repository;

import com.codeforge.contest.entity.ContestProblemResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContestProblemResultRepository extends JpaRepository<ContestProblemResult, Long> {

    List<ContestProblemResult> findByContestId(Long contestId);

    List<ContestProblemResult> findByContestIdAndUserId(Long contestId, Long userId);

    Optional<ContestProblemResult> findByContestIdAndUserIdAndProblemId(Long contestId, Long userId, Long problemId);

    List<ContestProblemResult> findByContestIdAndProblemId(Long contestId, Long problemId);

    void deleteByContestId(Long contestId);

    void deleteByUserId(Long userId);
}
