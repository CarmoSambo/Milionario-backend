package Game.Milionario.Controller;

import Game.Milionario.Dto.AnswerRequestDto;
import Game.Milionario.Dto.QuestionDto;
import Game.Milionario.Dto.AnswerDto;
import Game.Milionario.Dto.ApiResponse;
import Game.Milionario.Model.GameSession;
import Game.Milionario.Model.Score;
import Game.Milionario.Service.GameService;
import Game.Milionario.Repository.ScoreRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/game")

public class GameController {
    private final GameService gameService;
    private final ScoreRepository scoreRepo;

    public GameController(GameService gameService, ScoreRepository scoreRepo){
        this.gameService= gameService;
        this.scoreRepo = scoreRepo;
    }
    @PostMapping("/Start")
    public GameSession startGame(){
        return gameService.startGame();
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<GameSession> getGameSession(@PathVariable Long gameId) {
        try {
            GameSession game = gameService.getGameSession(gameId);
            return ResponseEntity.ok(game);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{gameId}/question")
    public QuestionDto getNextQuestion(@PathVariable Long gameId){
        return gameService.getNextQuestion(gameId);
    }

    @PostMapping("/{gameId}/answer")
    public ResponseEntity<ApiResponse<Boolean>> answerQuestion(
            @PathVariable Long gameId,
            @Validated @RequestBody AnswerRequestDto request) {

        try {
            boolean correct = gameService.answerQuestion(gameId, request.getAnswerId());
            String message = correct ? "Resposta correta! Pontos adicionados." : "Resposta errada ou tempo esgotado. Jogo terminado.";
            return ResponseEntity.ok(new ApiResponse<>(true, message, correct));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    // 50/50
    @PostMapping("/{gameId}/lifeline/5050")
    public List<AnswerDto> fiftyFifty(@PathVariable Long gameId) {
        return gameService.useFiftyFifty(gameId);
    }

    //skip question

    @PostMapping("/{gameId}/lifeline/skip")
    public void skip(@PathVariable Long gameId) {
        gameService.skipQuestion(gameId);
    }

    // Ask the Audience
    @PostMapping("/{gameId}/lifeline/ask-audience")
    public Map<Long, Integer> askAudience(@PathVariable Long gameId) {
        return gameService.useAskAudience(gameId);
    }

    // Phone a Friend
    @PostMapping("/{gameId}/lifeline/phone-friend")
    public Long phoneFriend(@PathVariable Long gameId) {
        return gameService.usePhoneFriend(gameId);
    }

    // save players points
    @PostMapping("/score")
    public void saveScore(@RequestParam String nickname,
                          @RequestParam int points) {

        gameService.saveScore(nickname, points);
    }

    // Top 10 players
    @GetMapping("/ranking")
    public List<Score> ranking() {
        return scoreRepo.findTop10ByOrderByPointsDesc();
    }

}
