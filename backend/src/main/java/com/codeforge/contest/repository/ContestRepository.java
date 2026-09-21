package com.codeforge.contest.repository;

import com.codeforge.contest.entity.Contest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContestRepository extends JpaRepository<Contest, Long> {
    Optional<Contest> findBySlug(String slug);
    List<Contest> findAllByOrderByStartTimeDesc();
    List<Contest> findByCreatedByUsernameOrderByStartTimeDesc(String username);
    List<Contest> findByCreatedBy(com.codeforge.user.entity.User user);
    boolean existsBySlug(String slug);
}

