package Game.Milionario.Service;

import Game.Milionario.Model.Score;
import Game.Milionario.Repository.ScoreRepository;
import org.springframework.stereotype.Service;

import java.util.List;

// CORRIGIDO: adicionada a anotação @Service e implementada a lógica de ranking.
// A classe estava vazia sem qualquer anotação, o que a tornava inútil.
@Service
public class ScoreService {

    private final ScoreRepository scoreRepo;

    public ScoreService(ScoreRepository scoreRepo) {
        this.scoreRepo = scoreRepo;
    }

    public void saveScore(String nickname, int points) {
        Score score = new Score();
        score.setNickname(nickname);
        score.setPoints(points);
        scoreRepo.save(score);
    }

    public List<Score> getTopScores() {
        return scoreRepo.findTop10ByOrderByPointsDesc();
    }
}
