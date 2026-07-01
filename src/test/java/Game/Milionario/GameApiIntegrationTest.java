package Game.Milionario;

import Game.Milionario.Dto.AnswerRequestDto;
import Game.Milionario.Dto.ApiResponse;
import Game.Milionario.Dto.GameSessionDto;
import Game.Milionario.Dto.QuestionDto;
import Game.Milionario.Model.Score;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de integração — requerem a aplicação em execução com a base de dados PostgreSQL.
 * Executar manualmente: mvn test -Dtest=GameApiIntegrationTest
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GameApiIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testStartGame() {
        ResponseEntity<GameSessionDto> response = restTemplate.postForEntity(
                "/api/game/start", null, GameSessionDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
    }

    @Test
    public void testGetNextQuestion() {
        GameSessionDto game = restTemplate.postForObject("/api/game/start", null, GameSessionDto.class);
        assertNotNull(game);

        ResponseEntity<QuestionDto> response = restTemplate.getForEntity(
                "/api/game/" + game.getId() + "/question", QuestionDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getQuestion());
        assertFalse(response.getBody().getAnswers().isEmpty());
    }

    @Test
    public void testAnswerQuestion() {
        GameSessionDto game = restTemplate.postForObject("/api/game/start", null, GameSessionDto.class);
        assertNotNull(game);

        QuestionDto question = restTemplate.getForObject(
                "/api/game/" + game.getId() + "/question", QuestionDto.class);
        assertNotNull(question);

        AnswerRequestDto answerRequest = new AnswerRequestDto();
        answerRequest.setAnswerId(question.getAnswers().get(0).getId());

        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
                "/api/game/" + game.getId() + "/answer", answerRequest, ApiResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
    }

    @Test
    public void testUseFiftyFifty() {
        GameSessionDto game = restTemplate.postForObject("/api/game/start", null, GameSessionDto.class);
        assertNotNull(game);
        restTemplate.getForObject("/api/game/" + game.getId() + "/question", QuestionDto.class);

        ResponseEntity<Object[]> response = restTemplate.postForEntity(
                "/api/game/" + game.getId() + "/lifeline/5050", null, Object[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().length);
    }

    @Test
    public void testSaveScore() {
        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/api/game/score?nickname=TestPlayer&points=1000", null, Void.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testGetRanking() {
        ResponseEntity<Score[]> response = restTemplate.getForEntity("/api/game/ranking", Score[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}
