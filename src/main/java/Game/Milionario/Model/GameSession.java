package Game.Milionario.Model;

import Game.Milionario.Enums.DifficultyLevel;
import Game.Milionario.Enums.GameStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import lombok.Data;

@Entity
@Data
public class GameSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int currentQuestionIndex;
    private int score;
    private boolean finished;

    @Enumerated(EnumType.STRING)
    private DifficultyLevel currentLevel;

    @Enumerated(EnumType.STRING)
    private GameStatus status;

    private boolean usedFiftyFifty;
    private boolean usedSkip;
    private LocalDateTime questionStartTime;
    private boolean usedAskAudience;
    private boolean usedPhoneFriend;

    private Long currentQuestionId;

    // IDs das 15 perguntas do jogo, pré-seleccionadas e embaralhadas no início (separadas por vírgula)
    @Column(length = 1000)
    private String questionIds;
}
