package Game.Milionario.Repository;

import Game.Milionario.Model.GameSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<GameSession, Long> {
}
