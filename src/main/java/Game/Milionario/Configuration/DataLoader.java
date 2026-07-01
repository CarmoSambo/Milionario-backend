package Game.Milionario.Configuration;

import Game.Milionario.Model.Question;
import Game.Milionario.Model.Answer;
import Game.Milionario.Repository.QuestionRepository;
import Game.Milionario.Enums.DifficultyLevel;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner initDatabase(QuestionRepository questionRepo) {
        return args -> {
            ObjectMapper mapper = new ObjectMapper();
            try {
                List<Map<String, Object>> questionsData = mapper.readValue(
                    new ClassPathResource("questions.json").getInputStream(),
                    new TypeReference<>() {}
                );

                long dbCount = questionRepo.count();
                long jsonCount = questionsData.size();

                if (dbCount == jsonCount) {
                    log.info("Base de dados já contém {} perguntas. Nenhuma acção necessária.", dbCount);
                    return;
                }

                // Se a contagem não corresponde ao JSON (carga parcial anterior ou novas perguntas),
                // apaga tudo e recarrega para garantir consistência.
                log.warn("BD tem {} perguntas mas o JSON tem {}. A recarregar...", dbCount, jsonCount);
                questionRepo.deleteAll();

                for (Map<String, Object> qData : questionsData) {
                    Question question = new Question();
                    question.setQuestion((String) qData.get("question"));
                    question.setDifficulty(DifficultyLevel.valueOf((String) qData.get("difficulty")));

                    List<Map<String, Object>> answersData = (List<Map<String, Object>>) qData.get("answers");
                    List<Answer> answers = answersData.stream().map(aData -> {
                        Answer answer = new Answer();
                        answer.setText((String) aData.get("text"));
                        answer.setCorrect((Boolean) aData.get("correct"));
                        answer.setQuestion(question);
                        return answer;
                    }).toList();

                    question.setAnswers(answers);
                    questionRepo.save(question);
                }

                log.info("{} perguntas carregadas com sucesso.", jsonCount);

            } catch (IOException e) {
                throw new RuntimeException("Falha ao carregar perguntas do ficheiro JSON", e);
            }
        };
    }
}
