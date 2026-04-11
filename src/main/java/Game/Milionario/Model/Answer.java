package Game.Milionario.Model;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Data
public class Answer {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)

    private Long id;

    private String text;

    private boolean correct;
    @ManyToOne
    @JoinColumn(name = "question_id")
    @JsonBackReference // Referência de volta, evita loop
    private Question question;

}
