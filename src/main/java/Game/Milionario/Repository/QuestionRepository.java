package Game.Milionario.Repository;

import Game.Milionario.Model.Question;
import Game.Milionario.Enums.DifficultyLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByDifficulty(DifficultyLevel difficulty);
}
