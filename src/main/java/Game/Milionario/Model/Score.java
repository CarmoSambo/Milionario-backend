package Game.Milionario.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Score {

    @Id
    @GeneratedValue

    private Long id;

    private String nickname;
    private int points;
}
