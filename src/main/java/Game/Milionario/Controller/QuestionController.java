package Game.Milionario.Controller;

import Game.Milionario.Model.Question;
import Game.Milionario.Dto.QuestionDto;
import Game.Milionario.Dto.AnswerDto;
import Game.Milionario.Repository.QuestionRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/question")
public class QuestionController {

    private final QuestionRepository questionRepo;

    public QuestionController(QuestionRepository questionRepo){
        this.questionRepo = questionRepo;
    }

    @PostMapping
    public Question create(@RequestBody Question question) {
        return questionRepo.save(question);
    }

    @GetMapping
    public List<QuestionDto> getAll() {
        return questionRepo.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private QuestionDto toDto(Question question) {
        QuestionDto dto = new QuestionDto();
        dto.setQuestion(question.getQuestion());
        List<AnswerDto> answers = question.getAnswers().stream()
                .map(a -> new AnswerDto(a.getId(), a.getText()))
                .collect(Collectors.toList());
        dto.setAnswers(answers);
        return dto;
    }
}
