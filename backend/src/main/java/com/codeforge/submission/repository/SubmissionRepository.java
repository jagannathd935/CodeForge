package com.codeforge.submission.repository;

import com.codeforge.submission.entity.Submission;
import com.codeforge.submission.entity.SubmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    List<Submission> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Submission> findByProblemIdOrderByCreatedAtDesc(Long problemId);

    List<Submission> findByUserIdAndProblemIdOrderByCreatedAtDesc(Long userId, Long problemId);

    long countByUserId(Long userId);

    long countByUserIdAndStatus(Long userId, SubmissionStatus status);

    @Query("SELECT COUNT(DISTINCT s.problem.id) FROM Submission s WHERE s.user.id = :userId AND s.status = 'ACCEPTED'")
    long countDistinctSolvedProblemsByUserId(@Param("userId") Long userId);

    @Query("SELECT DISTINCT s.problem.id FROM Submission s WHERE s.user.id = :userId AND s.status = 'ACCEPTED'")
    List<Long> findSolvedProblemIdsByUserId(@Param("userId") Long userId);

    List<Submission> findByUserIdAndStatus(Long userId, SubmissionStatus status);

    void deleteByUserId(Long userId);

    List<Submission> findByContestId(Long contestId);
}
