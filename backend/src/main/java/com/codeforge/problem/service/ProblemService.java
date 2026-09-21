package com.codeforge.problem.service;

import com.codeforge.exception.BadRequestException;
import com.codeforge.exception.ResourceNotFoundException;
import com.codeforge.problem.dto.ProblemCreateRequest;
import com.codeforge.problem.dto.ProblemDetailDto;
import com.codeforge.problem.dto.ProblemListDto;
import com.codeforge.problem.entity.Difficulty;
import com.codeforge.problem.entity.Problem;
import com.codeforge.problem.repository.ProblemRepository;
import com.codeforge.submission.entity.Submission;
import com.codeforge.submission.entity.SubmissionStatus;
import com.codeforge.submission.repository.SubmissionRepository;
import com.codeforge.testcase.dto.TestCaseDto;
import com.codeforge.testcase.entity.TestCase;
import com.codeforge.testcase.repository.TestCaseRepository;
import com.codeforge.topic.entity.Topic;
import com.codeforge.topic.repository.TopicRepository;
import com.codeforge.user.entity.User;
import com.codeforge.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProblemService {

    private final ProblemRepository problemRepository;
    private final TopicRepository topicRepository;
    private final TestCaseRepository testCaseRepository;
    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;

    public ProblemService(ProblemRepository problemRepository,
                          TopicRepository topicRepository,
                          TestCaseRepository testCaseRepository,
                          SubmissionRepository submissionRepository,
                          UserRepository userRepository) {
        this.problemRepository = problemRepository;
        this.topicRepository = topicRepository;
        this.testCaseRepository = testCaseRepository;
        this.submissionRepository = submissionRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<ProblemListDto> getAllProblems(String difficultyStr, String topicName, String search, String currentUsername) {
        List<Problem> problems = problemRepository.findAll();

        if (difficultyStr != null && !difficultyStr.isBlank() && !difficultyStr.equalsIgnoreCase("ALL")) {
            try {
                Difficulty diff = Difficulty.valueOf(difficultyStr.toUpperCase());
                problems = problems.stream().filter(p -> p.getDifficulty() == diff).toList();
            } catch (IllegalArgumentException ignored) {}
        }

        if (topicName != null && !topicName.isBlank() && !topicName.equalsIgnoreCase("ALL")) {
            problems = problems.stream()
                    .filter(p -> p.getTopics().stream().anyMatch(t -> t.getName().equalsIgnoreCase(topicName)))
                    .toList();
        }

        if (search != null && !search.isBlank()) {
            String lowerSearch = search.toLowerCase();
            problems = problems.stream()
                    .filter(p -> p.getTitle().toLowerCase().contains(lowerSearch) ||
                            p.getTopics().stream().anyMatch(t -> t.getName().toLowerCase().contains(lowerSearch)))
                    .toList();
        }

        Set<Long> solvedProblemIds = new HashSet<>();
        if (currentUsername != null && !currentUsername.isBlank()) {
            userRepository.findByUsername(currentUsername).ifPresent(user -> {
                solvedProblemIds.addAll(submissionRepository.findSolvedProblemIdsByUserId(user.getId()));
            });
        }

        return problems.stream().map(problem -> {
            ProblemListDto dto = new ProblemListDto();
            dto.setId(problem.getId());
            dto.setTitle(problem.getTitle());
            dto.setSlug(problem.getSlug());
            dto.setDifficulty(problem.getDifficulty().name());
            dto.setTopics(problem.getTopics().stream().map(Topic::getName).toList());
            dto.setIsSolved(solvedProblemIds.contains(problem.getId()));

            List<Submission> submissions = submissionRepository.findByProblemIdOrderByCreatedAtDesc(problem.getId());
            if (submissions.isEmpty()) {
                double defaultRate = switch (problem.getDifficulty()) {
                    case EASY -> 62.3;
                    case MEDIUM -> 52.8;
                    case HARD -> 41.5;
                };
                dto.setAcceptanceRate(defaultRate);
            } else {
                long accepted = submissions.stream().filter(s -> s.getStatus() == SubmissionStatus.ACCEPTED).count();
                double rate = Math.round(((double) accepted / submissions.size() * 100.0) * 10.0) / 10.0;
                dto.setAcceptanceRate(rate);
            }

            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProblemDetailDto getProblemById(Long id, String currentUsername) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Problem", "id", id));
        return mapToDetailDto(problem, currentUsername);
    }

    @Transactional(readOnly = true)
    public ProblemDetailDto getProblemBySlug(String slug, String currentUsername) {
        Problem problem = problemRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Problem", "slug", slug));
        return mapToDetailDto(problem, currentUsername);
    }

    private ProblemDetailDto mapToDetailDto(Problem problem, String currentUsername) {
        ProblemDetailDto dto = new ProblemDetailDto();
        dto.setId(problem.getId());
        dto.setTitle(problem.getTitle());
        dto.setSlug(problem.getSlug());
        dto.setDescription(problem.getDescription());
        dto.setDifficulty(problem.getDifficulty().name());
        dto.setConstraints(problem.getConstraints());
        dto.setInputFormat(problem.getInputFormat());
        dto.setOutputFormat(problem.getOutputFormat());
        dto.setStarterCode(problem.getStarterCode());
        dto.setTimeLimit(problem.getTimeLimit());
        dto.setMemoryLimit(problem.getMemoryLimit());
        dto.setTopics(problem.getTopics().stream().map(Topic::getName).toList());

        List<TestCaseDto> publicCases = testCaseRepository.findByProblemIdAndIsHiddenFalse(problem.getId())
                .stream()
                .map(tc -> new TestCaseDto(tc.getId(), tc.getInput(), tc.getExpectedOutput(), false))
                .toList();
        dto.setSampleTestCases(publicCases);

        if (currentUsername != null && !currentUsername.isBlank()) {
            userRepository.findByUsername(currentUsername).ifPresent(user -> {
                List<Long> solved = submissionRepository.findSolvedProblemIdsByUserId(user.getId());
                dto.setIsSolved(solved.contains(problem.getId()));
            });
        }

        return dto;
    }

    @Transactional
    public ProblemDetailDto createProblem(ProblemCreateRequest request) {
        String slug = generateSlug(request.getTitle());
        if (problemRepository.existsBySlug(slug)) {
            slug = slug + "-" + System.currentTimeMillis();
        }

        Problem problem = new Problem();
        problem.setTitle(request.getTitle());
        problem.setSlug(slug);
        problem.setDescription(request.getDescription());
        try {
            problem.setDifficulty(Difficulty.valueOf(request.getDifficulty().toUpperCase()));
        } catch (Exception e) {
            problem.setDifficulty(Difficulty.EASY);
        }
        problem.setConstraints(request.getConstraints());
        problem.setInputFormat(request.getInputFormat());
        problem.setOutputFormat(request.getOutputFormat());
        problem.setStarterCode(request.getStarterCode());
        problem.setTimeLimit(request.getTimeLimit() != null ? request.getTimeLimit() : 1000);
        problem.setMemoryLimit(request.getMemoryLimit() != null ? request.getMemoryLimit() : 256);

        if (request.getTopics() != null) {
            Set<Topic> topics = new HashSet<>();
            for (String tName : request.getTopics()) {
                String cleanName = tName.trim();
                if (!cleanName.isEmpty()) {
                    Topic topic = topicRepository.findByName(cleanName)
                            .orElseGet(() -> topicRepository.save(new Topic(cleanName)));
                    topics.add(topic);
                }
            }
            problem.setTopics(topics);
        }

        Problem savedProblem = problemRepository.save(problem);

        if (request.getTestCases() != null) {
            for (TestCaseDto tcDto : request.getTestCases()) {
                TestCase tc = new TestCase(
                        savedProblem,
                        tcDto.getInput(),
                        tcDto.getExpectedOutput(),
                        tcDto.getIsHidden() != null ? tcDto.getIsHidden() : false
                );
                testCaseRepository.save(tc);
            }
        }

        return getProblemById(savedProblem.getId(), null);
    }

    @Transactional
    public ProblemDetailDto updateProblem(Long id, ProblemCreateRequest request) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Problem", "id", id));

        problem.setTitle(request.getTitle());
        problem.setDescription(request.getDescription());
        try {
            problem.setDifficulty(Difficulty.valueOf(request.getDifficulty().toUpperCase()));
        } catch (Exception ignored) {}
        problem.setConstraints(request.getConstraints());
        problem.setInputFormat(request.getInputFormat());
        problem.setOutputFormat(request.getOutputFormat());
        problem.setStarterCode(request.getStarterCode());
        if (request.getTimeLimit() != null) problem.setTimeLimit(request.getTimeLimit());
        if (request.getMemoryLimit() != null) problem.setMemoryLimit(request.getMemoryLimit());

        if (request.getTopics() != null) {
            Set<Topic> topics = new HashSet<>();
            for (String tName : request.getTopics()) {
                String cleanName = tName.trim();
                if (!cleanName.isEmpty()) {
                    Topic topic = topicRepository.findByName(cleanName)
                            .orElseGet(() -> topicRepository.save(new Topic(cleanName)));
                    topics.add(topic);
                }
            }
            problem.setTopics(topics);
        }

        Problem updated = problemRepository.save(problem);

        if (request.getTestCases() != null && !request.getTestCases().isEmpty()) {
            testCaseRepository.deleteByProblemId(id);
            for (TestCaseDto tcDto : request.getTestCases()) {
                TestCase tc = new TestCase(
                        updated,
                        tcDto.getInput(),
                        tcDto.getExpectedOutput(),
                        tcDto.getIsHidden() != null ? tcDto.getIsHidden() : false
                );
                testCaseRepository.save(tc);
            }
        }

        return getProblemById(updated.getId(), null);
    }

    @Transactional
    public void deleteProblem(Long id) {
        if (!problemRepository.existsById(id)) {
            throw new ResourceNotFoundException("Problem", "id", id);
        }
        problemRepository.deleteById(id);
    }

    private String generateSlug(String title) {
        return title.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();
    }
}
