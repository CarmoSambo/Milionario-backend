package Game.Milionario.Service;

import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import Game.Milionario.Model.Answer;
import Game.Milionario.Dto.QuestionDto;
import Game.Milionario.Dto.AnswerDto;
import Game.Milionario.Enums.GameStatus;
import Game.Milionario.Model.GameSession;
import Game.Milionario.Model.Question;
import Game.Milionario.Repository.GameRepository;
import Game.Milionario.Repository.QuestionRepository;
import Game.Milionario.Repository.ScoreRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import Game.Milionario.Enums.DifficultyLevel;

@Slf4j
@Service
@Transactional
public class GameService {

    private static final int QUESTION_TIME_LIMIT_SECONDS = 30;

    private final GameRepository gameRepo;
    private final QuestionRepository questionRepo;
    private final ScoreRepository scoreRepo;

    public GameService(GameRepository gameRepo, QuestionRepository questionRepo, ScoreRepository scoreRepo) {
        this.gameRepo = gameRepo;
        this.questionRepo = questionRepo;
        this.scoreRepo = scoreRepo;
    }

    public GameSession startGame() {
        GameSession game = new GameSession();
        game.setCurrentQuestionIndex(0);
        game.setScore(0);
        game.setFinished(false);
        game.setCurrentLevel(DifficultyLevel.EASY);
        game.setStatus(GameStatus.IN_PROCESS);
        game.setUsedFiftyFifty(false);
        game.setUsedSkip(false);
        game.setUsedAskAudience(false);
        game.setUsedPhoneFriend(false);
        game.setQuestionIds(selectRandomQuestionIds());

        GameSession savedGame = gameRepo.save(game);
        log.info("Jogo iniciado com ID: {}", savedGame.getId());
        return savedGame;
    }

    @Transactional(readOnly = true)
    public GameSession getGameSession(Long gameId) {
        return getGame(gameId);
    }

    public QuestionDto getNextQuestion(Long gameId) {
        GameSession game = getGame(gameId);
        checkGameStatus(game);

        if (game.getCurrentQuestionIndex() >= 15) {
            finishGame(game, true);
            throw new RuntimeException("Parabéns! Completou todas as 15 perguntas!");
        }

        String[] ids = game.getQuestionIds().split(",");
        Long questionId = Long.parseLong(ids[game.getCurrentQuestionIndex()]);
        Question question = questionRepo.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Pergunta não encontrada"));

        game.setCurrentQuestionId(question.getId());
        game.setQuestionStartTime(LocalDateTime.now());
        gameRepo.save(game);

        log.info("Pergunta enviada: [{}] {}", question.getId(), question.getQuestion());
        return toDTO(question, game);
    }

    public boolean answerQuestion(Long gameId, Long answerId) {
        GameSession game = getGame(gameId);
        checkGameStatus(game);

        if (game.getQuestionStartTime() == null ||
                LocalDateTime.now().isAfter(game.getQuestionStartTime().plusSeconds(QUESTION_TIME_LIMIT_SECONDS))) {
            finishGame(game, false);
            return false;
        }

        Question current = questionRepo.findById(game.getCurrentQuestionId())
                .orElseThrow(() -> new RuntimeException("Pergunta não encontrada. Chame /question primeiro."));

        boolean correct = current.getAnswers()
                .stream()
                .anyMatch(a -> a.getId().equals(answerId) && a.isCorrect());

        if (correct) {
            updateScore(game);
            nextQuestion(game);
            return true;
        } else {
            finishGame(game, false);
            return false;
        }
    }

    public List<AnswerDto> useFiftyFifty(Long gameId) {
        GameSession game = getGame(gameId);
        checkGameStatus(game);

        if (game.isUsedFiftyFifty()) {
            throw new RuntimeException("Ajuda 50/50 já utilizada");
        }

        Question question = getCurrentQuestion(game);
        List<Answer> answers = question.getAnswers();

        List<Answer> correct = answers.stream().filter(Answer::isCorrect).toList();
        List<Answer> incorrect = answers.stream().filter(a -> !a.isCorrect()).limit(1).toList();

        List<AnswerDto> result = new ArrayList<>();
        correct.forEach(a -> result.add(new AnswerDto(a.getId(), a.getText())));
        incorrect.forEach(a -> result.add(new AnswerDto(a.getId(), a.getText())));

        game.setUsedFiftyFifty(true);
        gameRepo.save(game);

        return result;
    }

    public void skipQuestion(Long gameId) {
        GameSession game = getGame(gameId);
        checkGameStatus(game);

        if (game.isUsedSkip()) {
            throw new RuntimeException("Ajuda Saltar já utilizada");
        }

        game.setUsedSkip(true);
        nextQuestion(game);
    }

    public Map<Long, Integer> useAskAudience(Long gameId) {
        GameSession game = getGame(gameId);
        checkGameStatus(game);

        if (game.isUsedAskAudience()) {
            throw new RuntimeException("Ajuda Pergunta ao Público já utilizada");
        }

        Question question = getCurrentQuestion(game);
        List<Answer> answers = question.getAnswers();

        Map<Long, Integer> votes = new HashMap<>();
        Answer correct = answers.stream().filter(Answer::isCorrect).findFirst().orElseThrow();

        int correctVotes = 60 + (int) (Math.random() * 21);
        votes.put(correct.getId(), correctVotes);

        List<Answer> incorrect = answers.stream().filter(a -> !a.isCorrect()).toList();
        int votesPerIncorrect = (100 - correctVotes) / incorrect.size();
        incorrect.forEach(a -> votes.put(a.getId(), votesPerIncorrect));

        game.setUsedAskAudience(true);
        gameRepo.save(game);

        return votes;
    }

    public Long usePhoneFriend(Long gameId) {
        GameSession game = getGame(gameId);
        checkGameStatus(game);

        if (game.isUsedPhoneFriend()) {
            throw new RuntimeException("Ajuda Ligar a um Amigo já utilizada");
        }

        Question question = getCurrentQuestion(game);
        List<Answer> answers = question.getAnswers();

        Long suggestedId;
        if (Math.random() < 0.7) {
            suggestedId = answers.stream().filter(Answer::isCorrect).findFirst().get().getId();
        } else {
            suggestedId = answers.get((int) (Math.random() * answers.size())).getId();
        }

        game.setUsedPhoneFriend(true);
        gameRepo.save(game);

        return suggestedId;
    }

    // --- Métodos auxiliares privados ---

    private GameSession getGame(Long id) {
        return gameRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Jogo não encontrado com ID: " + id));
    }

    private void checkGameStatus(GameSession game) {
        if (game.getStatus() != GameStatus.IN_PROCESS) {
            throw new RuntimeException("Jogo já terminado com status: " + game.getStatus());
        }
    }

    private String selectRandomQuestionIds() {
        List<Question> easy   = questionRepo.findRandomByDifficulty("EASY",   5);
        List<Question> medium = questionRepo.findRandomByDifficulty("MEDIUM", 5);
        List<Question> hard   = questionRepo.findRandomByDifficulty("HARD",   5);

        List<Long> ids = new ArrayList<>();
        easy.forEach(q   -> ids.add(q.getId()));
        medium.forEach(q -> ids.add(q.getId()));
        hard.forEach(q   -> ids.add(q.getId()));

        String csv = ids.stream().map(String::valueOf).collect(Collectors.joining(","));
        log.info("IDs das perguntas seleccionadas: {}", csv);
        return csv;
    }

    private Question getCurrentQuestion(GameSession game) {
        if (game.getCurrentQuestionId() == null) {
            throw new RuntimeException("Nenhuma pergunta activa. Chame /question primeiro.");
        }
        return questionRepo.findById(game.getCurrentQuestionId())
                .orElseThrow(() -> new RuntimeException("Pergunta activa não encontrada"));
    }

    private void updateScore(GameSession game) {
        int points = switch (game.getCurrentLevel()) {
            case EASY -> 100;
            case MEDIUM -> 200;
            case HARD -> 500;
        };
        game.setScore(game.getScore() + points);
    }

    private void nextQuestion(GameSession game) {
        game.setCurrentQuestionIndex(game.getCurrentQuestionIndex() + 1);

        if (game.getCurrentQuestionIndex() == 5) {
            game.setCurrentLevel(DifficultyLevel.MEDIUM);
        } else if (game.getCurrentQuestionIndex() == 10) {
            game.setCurrentLevel(DifficultyLevel.HARD);
        }

        gameRepo.save(game);
    }

    private void finishGame(GameSession game, boolean won) {
        game.setFinished(true);
        game.setStatus(won ? GameStatus.WON : GameStatus.LOST);
        gameRepo.save(game);
    }

    private QuestionDto toDTO(Question question, GameSession game) {
        List<AnswerDto> answers = new ArrayList<>(question.getAnswers()
                .stream()
                .map(a -> new AnswerDto(a.getId(), a.getText()))
                .toList());
        java.util.Collections.shuffle(answers);

        int timeRemaining = QUESTION_TIME_LIMIT_SECONDS;
        if (game.getQuestionStartTime() != null) {
            long elapsed = java.time.Duration.between(game.getQuestionStartTime(), LocalDateTime.now()).getSeconds();
            timeRemaining = Math.max(0, QUESTION_TIME_LIMIT_SECONDS - (int) elapsed);
        }

        return new QuestionDto(question.getQuestion(), answers, timeRemaining);
    }
}
