package Game.Milionario.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class UserAnswer {

    @Id
    @GeneratedValue

    private Long id;

    private Long questionId;
    private Long answerId;
    private boolean correct;

    @ManyToOne
    private GameSession game;
}
