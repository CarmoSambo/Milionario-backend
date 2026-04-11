package Game.Milionario.Configuration;

import Game.Milionario.Model.Question;
import Game.Milionario.Model.Answer;
import Game.Milionario.Repository.QuestionRepository;
import Game.Milionario.Enums.DifficultyLevel;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner initDatabase(QuestionRepository questionRepo) {
        return args -> {
            // Verifica se o banco já tem perguntas; se sim, não carrega novamente
            if (questionRepo.count() == 0) { // Only load if empty
                // Cria um ObjectMapper para ler JSON
                ObjectMapper mapper = new ObjectMapper();
                try {
                    // Lê o arquivo questions.json do classpath (resources)
                    List<Map<String, Object>> questionsData = mapper.readValue(
                        new ClassPathResource("questions.json").getInputStream(),
                        new TypeReference<List<Map<String, Object>>>() {}
                    );

                    // Para cada pergunta no JSON
                    for (Map<String, Object> qData : questionsData) {
                        // Cria uma nova entidade Question
                        Question question = new Question();
                        // Define o texto da pergunta
                        question.setQuestion((String) qData.get("question"));
                        // Define a dificuldade baseada no enum
                        question.setDifficulty(DifficultyLevel.valueOf((String) qData.get("difficulty")));

                        // Obtém a lista de respostas do JSON
                        List<Map<String, Object>> answersData = (List<Map<String, Object>>) qData.get("answers");
                        // Mapeia cada resposta para uma entidade Answer
                        List<Answer> answers = answersData.stream().map(aData -> {
                            Answer answer = new Answer();
                            // Define o texto da resposta
                            answer.setText((String) aData.get("text"));
                            // Define se é correta
                            answer.setCorrect((Boolean) aData.get("correct"));
                            // Associa a resposta à pergunta
                            answer.setQuestion(question);
                            return answer;
                        }).toList();

                        // Define as respostas na pergunta
                        question.setAnswers(answers);
                        // Salva a pergunta no banco (as respostas são salvas em cascata)
                        questionRepo.save(question);
                    }
                } catch (IOException e) {
                    // Lança exceção se houver erro ao ler o JSON
                    throw new RuntimeException("Failed to load questions from JSON", e);
                }
            }
        };
    }
}
