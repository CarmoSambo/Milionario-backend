package Game.Milionario.Service;

import Game.Milionario.Model.GameSession;
import Game.Milionario.Repository.GameRepository;
import Game.Milionario.Repository.QuestionRepository;
import Game.Milionario.Repository.ScoreRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GameServiceTest {

    @Mock
    private GameRepository gameRepo;

    @Mock
    private QuestionRepository questionRepo;

    @Mock
    private ScoreRepository scoreRepo;

    @InjectMocks
    private GameService gameService;

    public GameServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testStartGame() {
        // Arrange
        GameSession mockGame = new GameSession();
        mockGame.setId(1L);
        when(gameRepo.save(any(GameSession.class))).thenReturn(mockGame);

        // Act
        GameSession result = gameService.startGame();

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getCurrentQuestionIndex());
        assertEquals(0, result.getScore());
        assertFalse(result.isFinished());
        verify(gameRepo, times(1)).save(any(GameSession.class));
    }

    // Add more tests as needed
}
