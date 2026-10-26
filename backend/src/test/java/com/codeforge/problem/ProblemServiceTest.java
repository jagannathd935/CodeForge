package com.codeforge.problem;

import com.codeforge.exception.ResourceNotFoundException;
import com.codeforge.problem.dto.ProblemDetailDto;
import com.codeforge.problem.dto.ProblemListDto;
import com.codeforge.problem.entity.Difficulty;
import com.codeforge.problem.entity.Problem;
import com.codeforge.problem.repository.ProblemRepository;
import com.codeforge.problem.service.ProblemService;
import com.codeforge.submission.repository.SubmissionRepository;
import com.codeforge.testcase.entity.TestCase;
import com.codeforge.testcase.repository.TestCaseRepository;
import com.codeforge.topic.entity.Topic;
import com.codeforge.topic.repository.TopicRepository;
import com.codeforge.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProblemServiceTest {

    @Mock
    private ProblemRepository problemRepository;

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private TestCaseRepository testCaseRepository;

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProblemService problemService;

    private Problem sampleProblem;

    @BeforeEach
    void setUp() {
        sampleProblem = new Problem();
        sampleProblem.setId(1L);
        sampleProblem.setTitle("Two Sum");
        sampleProblem.setSlug("two-sum");
        sampleProblem.setDescription("Given an array...");
        sampleProblem.setDifficulty(Difficulty.EASY);
        sampleProblem.setTimeLimit(1000);
        sampleProblem.setMemoryLimit(256);

        Topic t = new Topic("Arrays");
        t.setId(10L);
        sampleProblem.setTopics(Set.of(t));
    }

    @Test
    void testGetProblemByIdSuccess() {
        when(problemRepository.findById(1L)).thenReturn(Optional.of(sampleProblem));
        TestCase publicCase = new TestCase(sampleProblem, "2\n1 2\n3", "0 1", false);
        when(testCaseRepository.findByProblemIdAndIsHiddenFalse(1L)).thenReturn(List.of(publicCase));

        ProblemDetailDto dto = problemService.getProblemById(1L, null);

        assertNotNull(dto);
        assertEquals("Two Sum", dto.getTitle());
        assertEquals("EASY", dto.getDifficulty());
        assertEquals(1, dto.getSampleTestCases().size());
    }

    @Test
    void testGetProblemByIdNotFoundThrowsException() {
        when(problemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> problemService.getProblemById(99L, null));
    }

    @Test
    void testGetAllProblems() {
        when(problemRepository.findAll()).thenReturn(List.of(sampleProblem));
        when(submissionRepository.findByProblemIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());

        List<ProblemListDto> list = problemService.getAllProblems(null, null, null, null);

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("Two Sum", list.get(0).getTitle());
        assertEquals("EASY", list.get(0).getDifficulty());
    }
}
