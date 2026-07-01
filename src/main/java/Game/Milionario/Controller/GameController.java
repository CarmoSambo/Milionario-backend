package Game.Milionario.Controller;

import Game.Milionario.Dto.AnswerRequestDto;
import Game.Milionario.Dto.QuestionDto;
import Game.Milionario.Dto.AnswerDto;
import Game.Milionario.Dto.ApiResponse;
import Game.Milionario.Dto.GameSessionDto;
import Game.Milionario.Model.Score;
import Game.Milionario.Service.GameService;
import Game.Milionario.Service.ScoreService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/game")
public class GameController {

    private final GameService gameService;
    private final ScoreService scoreService;

    public GameController(GameService gameService, ScoreService scoreService) {
        this.gameService = gameService;
        this.scoreService = scoreService;
    }

    @PostMapping("/start")
    public GameSessionDto startGame() {
        return GameSessionDto.from(gameService.startGame());
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<GameSessionDto> getGameSession(@PathVariable Long gameId) {
        try {
            return ResponseEntity.ok(GameSessionDto.from(gameService.getGameSession(gameId)));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{gameId}/question")
    public QuestionDto getNextQuestion(@PathVariable Long gameId) {
        return gameService.getNextQuestion(gameId);
    }

    @PostMapping("/{gameId}/answer")
    public ResponseEntity<ApiResponse<Boolean>> answerQuestion(
            @PathVariable Long gameId,
            @Validated @RequestBody AnswerRequestDto request) {

        try {
            boolean correct = gameService.answerQuestion(gameId, request.getAnswerId());
            String message = correct
                    ? "Resposta correta! Pontos adicionados."
                    : "Resposta errada ou tempo esgotado. Jogo terminado.";
            return ResponseEntity.ok(new ApiResponse<>(true, message, correct));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @PostMapping("/{gameId}/lifeline/5050")
    public List<AnswerDto> fiftyFifty(@PathVariable Long gameId) {
        return gameService.useFiftyFifty(gameId);
    }

    @PostMapping("/{gameId}/lifeline/skip")
    public void skip(@PathVariable Long gameId) {
        gameService.skipQuestion(gameId);
    }

    @PostMapping("/{gameId}/lifeline/ask-audience")
    public Map<Long, Integer> askAudience(@PathVariable Long gameId) {
        return gameService.useAskAudience(gameId);
    }

    @PostMapping("/{gameId}/lifeline/phone-friend")
    public Long phoneFriend(@PathVariable Long gameId) {
        return gameService.usePhoneFriend(gameId);
    }

    @PostMapping("/score")
    public void saveScore(
            @RequestParam @NotBlank(message = "O nickname não pode estar vazio") String nickname,
            @RequestParam @PositiveOrZero(message = "Os pontos não podem ser negativos") int points) {
        scoreService.saveScore(nickname, points);
    }

    @GetMapping("/ranking")
    public List<Score> ranking() {
        return scoreService.getTopScores();
    }
}
