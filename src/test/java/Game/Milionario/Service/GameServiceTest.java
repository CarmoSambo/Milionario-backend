package Game.Milionario.Service;

import Game.Milionario.Model.GameSession;
import Game.Milionario.Model.Question;
import Game.Milionario.Enums.DifficultyLevel;
import Game.Milionario.Enums.GameStatus;
import Game.Milionario.Repository.GameRepository;
import Game.Milionario.Repository.QuestionRepository;
import Game.Milionario.Repository.ScoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;

class GameServiceTest {

    @Mock
    private GameRepository gameRepo;

    @Mock
    private QuestionRepository questionRepo;

    @Mock
    private ScoreRepository scoreRepo;

    @InjectMocks
    private GameService gameService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Devolve 5 perguntas mock para cada nível, simulando o que a BD devolveria
        Question mockQuestion = new Question();
        mockQuestion.setId(1L);
        mockQuestion.setDifficulty(DifficultyLevel.EASY);
        mockQuestion.setAnswers(List.of());

        List<Question> fiveQuestions = List.of(
                buildQuestion(1L, DifficultyLevel.EASY),
                buildQuestion(2L, DifficultyLevel.EASY),
                buildQuestion(3L, DifficultyLevel.EASY),
                buildQuestion(4L, DifficultyLevel.EASY),
                buildQuestion(5L, DifficultyLevel.EASY)
        );

        when(questionRepo.findRandomByDifficulty(eq("EASY"),   eq(5))).thenReturn(fiveQuestions);
        when(questionRepo.findRandomByDifficulty(eq("MEDIUM"), eq(5))).thenReturn(fiveQuestions);
        when(questionRepo.findRandomByDifficulty(eq("HARD"),   eq(5))).thenReturn(fiveQuestions);
    }

    @Test
    void testStartGame_inicializaEstadoCorrectamente() {
        // Devolve o próprio argumento passado ao save(), preservando os campos definidos em startGame()
        when(gameRepo.save(any(GameSession.class))).then(returnsFirstArg());

        GameSession result = gameService.startGame();

        assertNotNull(result);
        assertEquals(0, result.getCurrentQuestionIndex());
        assertEquals(0, result.getScore());
        assertFalse(result.isFinished());
        assertEquals(GameStatus.IN_PROCESS, result.getStatus());
        assertEquals(DifficultyLevel.EASY, result.getCurrentLevel());
        assertFalse(result.isUsedFiftyFifty());
        assertFalse(result.isUsedSkip());
        assertFalse(result.isUsedAskAudience());
        assertFalse(result.isUsedPhoneFriend());
        verify(gameRepo, times(1)).save(any(GameSession.class));
    }

    // --- helpers ---

    private Question buildQuestion(Long id, DifficultyLevel difficulty) {
        Question q = new Question();
        q.setId(id);
        q.setDifficulty(difficulty);
        q.setAnswers(List.of());
        return q;
    }
}
