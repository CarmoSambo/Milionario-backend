package Game.Milionario.Repository;

import Game.Milionario.Model.Question;
import Game.Milionario.Enums.DifficultyLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByDifficulty(DifficultyLevel difficulty);

    @Query(value = "SELECT * FROM question WHERE difficulty = :difficulty ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<Question> findRandomByDifficulty(@Param("difficulty") String difficulty, @Param("limit") int limit);
}
