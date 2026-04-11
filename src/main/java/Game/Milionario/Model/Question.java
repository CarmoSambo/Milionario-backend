package Game.Milionario.Model;

import Game.Milionario.Enums.DifficultyLevel;
import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.util.List;

@Entity
@Data
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String question;

    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;

    private String correctAnswer;
    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficulty;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL)
    @JsonManagedReference // Gerencia a referência para evitar loop
    private List<Answer> answers;

}
