package com.codeforge.problem.repository;

import com.codeforge.problem.entity.Difficulty;
import com.codeforge.problem.entity.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long> {

    Optional<Problem> findBySlug(String slug);

    Boolean existsBySlug(String slug);

    List<Problem> findByDifficulty(Difficulty difficulty);

    List<Problem> findByTitleContainingIgnoreCase(String title);

    @Query("SELECT DISTINCT p FROM Problem p JOIN p.topics t WHERE t.name = :topicName")
    List<Problem> findByTopicName(@Param("topicName") String topicName);
}
