package Game.Milionario.Service;

import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import Game.Milionario.Model.Answer;
import Game.Milionario.Model.Score;
import Game.Milionario.Dto.QuestionDto;
import Game.Milionario.Dto.AnswerDto;
import Game.Milionario.Enums.GameStatus;
import Game.Milionario.Model.GameSession;
import Game.Milionario.Model.Question;
import Game.Milionario.Repository.GameRepository;
import Game.Milionario.Repository.QuestionRepository;
import Game.Milionario.Repository.ScoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import Game.Milionario.Enums.DifficultyLevel;

@Service
@Transactional
public class GameService {

    // Tempo limite por pergunta em segundos
    private static final int QUESTION_TIME_LIMIT_SECONDS = 30;

    private final GameRepository gameRepo;
    private final QuestionRepository questionRepo;
    private final ScoreRepository scoreRepo;

    public GameService(GameRepository gameRepo, QuestionRepository questionRepo, ScoreRepository scoreRepo) {
        this.gameRepo = gameRepo;
        this.questionRepo = questionRepo;
        this.scoreRepo = scoreRepo;
    }

    // Inicia um novo jogo
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
        System.out.println("Jogo iniciado com ID: " + savedGame.getId());
        return savedGame;
    }

    // Devolve a próxima pergunta e inicia o timer de 30 segundos
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

        System.out.println("Pergunta enviada: [" + question.getId() + "] " + question.getQuestion());
        return toDTO(question, game);
    }

    // Valida a resposta do jogador
    public boolean answerQuestion(Long gameId, Long answerId) {
        GameSession game = getGame(gameId);
        checkGameStatus(game);

        // CORRIGIDO: o timer agora termina o jogo correctamente (status LOST).
        // Antes, finishGame(game) sem parâmetro não actualizava o status,
        // por isso o jogo continuava a aceitar perguntas mesmo após timeout.
        if (game.getQuestionStartTime() == null ||
                LocalDateTime.now().isAfter(game.getQuestionStartTime().plusSeconds(QUESTION_TIME_LIMIT_SECONDS))) {
            finishGame(game, false);
            return false;
        }

        // CORRIGIDO: carrega a pergunta pelo ID guardado em getNextQuestion(),
        // eliminando o problema do shuffle que gerava uma pergunta diferente aqui.
        // Antes, getQuestionsByLevel() era chamado novamente e o shuffle produzia
        // uma lista em ordem diferente, por isso o answerId nunca coincidia.
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

    // Ajuda 50/50: remove duas respostas erradas
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

    // Ajuda Saltar: avança para a próxima pergunta sem responder
    public void skipQuestion(Long gameId) {
        GameSession game = getGame(gameId);
        checkGameStatus(game);

        if (game.isUsedSkip()) {
            throw new RuntimeException("Ajuda Saltar já utilizada");
        }

        game.setUsedSkip(true);
        nextQuestion(game);
    }

    // Ajuda Pergunta ao Público: simula votação da audiência
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

        // Resposta certa recebe entre 60% e 80% dos votos
        int correctVotes = 60 + (int) (Math.random() * 21);
        votes.put(correct.getId(), correctVotes);

        // Restante dividido pelas respostas erradas
        List<Answer> incorrect = answers.stream().filter(a -> !a.isCorrect()).toList();
        int votesPerIncorrect = (100 - correctVotes) / incorrect.size();
        incorrect.forEach(a -> votes.put(a.getId(), votesPerIncorrect));

        game.setUsedAskAudience(true);
        gameRepo.save(game);

        return votes;
    }

    // Ajuda Ligar a um Amigo: 70% de chance de sugerir a resposta certa
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

    // Retorna a sessão de jogo pelo ID
    public GameSession getGameSession(Long gameId) {
        return getGame(gameId);
    }

    // Guarda a pontuação do jogador no ranking
    public void saveScore(String nickname, int score) {
        Score s = new Score();
        s.setNickname(nickname);
        s.setPoints(score);
        scoreRepo.save(s);
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

    // Selecciona 5 perguntas aleatórias de cada nível directamente na BD (ORDER BY RANDOM())
    private String selectRandomQuestionIds() {
        List<Question> easy   = questionRepo.findRandomByDifficulty("EASY",   5);
        List<Question> medium = questionRepo.findRandomByDifficulty("MEDIUM", 5);
        List<Question> hard   = questionRepo.findRandomByDifficulty("HARD",   5);

        List<Long> ids = new ArrayList<>();
        easy.forEach(q   -> ids.add(q.getId()));
        medium.forEach(q -> ids.add(q.getId()));
        hard.forEach(q   -> ids.add(q.getId()));

        String csv = ids.stream().map(String::valueOf).collect(Collectors.joining(","));
        System.out.println("IDs das perguntas seleccionadas: " + csv);
        return csv;
    }

    // CORRIGIDO: carrega a pergunta activa directamente pelo ID guardado,
    // em vez de re-fazer o shuffle. Usado pelas ajudas (50/50, audiência, amigo).
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

        // Promove o nível a cada 5 perguntas correctas
        if (game.getCurrentQuestionIndex() == 5) {
            game.setCurrentLevel(DifficultyLevel.MEDIUM);
        } else if (game.getCurrentQuestionIndex() == 10) {
            game.setCurrentLevel(DifficultyLevel.HARD);
        }

        gameRepo.save(game);
    }

    // CORRIGIDO: unificado num único método que sempre define o status correcto.
    // Antes existia um finishGame(game) sem parâmetro que apenas colocava finished=true
    // mas deixava o status como IN_PROCESS, o que impedia o timer de terminar o jogo.
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

        // Calcula o tempo restante com base no momento em que a pergunta foi enviada
        int timeRemaining = QUESTION_TIME_LIMIT_SECONDS;
        if (game.getQuestionStartTime() != null) {
            long elapsed = java.time.Duration.between(game.getQuestionStartTime(), LocalDateTime.now()).getSeconds();
            timeRemaining = Math.max(0, QUESTION_TIME_LIMIT_SECONDS - (int) elapsed);
        }

        return new QuestionDto(question.getQuestion(), answers, timeRemaining);
    }
}
