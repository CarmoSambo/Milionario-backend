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

    // CORRIGIDO: removidos os campos optionA/B/C/D e correctAnswer que nunca foram
    // utilizados. As respostas estão na tabela Answer com o campo "correct" próprio.

    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficulty;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<Answer> answers;
}
