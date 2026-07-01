package Game.Milionario.Dto;

import Game.Milionario.Enums.DifficultyLevel;
import Game.Milionario.Enums.GameStatus;
import Game.Milionario.Model.GameSession;
import lombok.Data;

@Data
public class GameSessionDto {

    private Long id;
    private int score;
    private boolean finished;
    private DifficultyLevel currentLevel;
    private GameStatus status;
    private int currentQuestionIndex;
    private boolean usedFiftyFifty;
    private boolean usedSkip;
    private boolean usedAskAudience;
    private boolean usedPhoneFriend;

    public static GameSessionDto from(GameSession g) {
        GameSessionDto dto = new GameSessionDto();
        dto.setId(g.getId());
        dto.setScore(g.getScore());
        dto.setFinished(g.isFinished());
        dto.setCurrentLevel(g.getCurrentLevel());
        dto.setStatus(g.getStatus());
        dto.setCurrentQuestionIndex(g.getCurrentQuestionIndex());
        dto.setUsedFiftyFifty(g.isUsedFiftyFifty());
        dto.setUsedSkip(g.isUsedSkip());
        dto.setUsedAskAudience(g.isUsedAskAudience());
        dto.setUsedPhoneFriend(g.isUsedPhoneFriend());
        return dto;
    }
}
