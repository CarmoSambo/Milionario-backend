package Game.Milionario.Service;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import Game.Milionario.Model.Answer;
import Game.Milionario.Model.Score;
import Game.Milionario.Dto.QuestionDto;
import Game.Milionario.Dto.AnswerRequestDto;
import Game.Milionario.Dto.AnswerDto;
import Game.Milionario.Enums.GameStatus;
import Game.Milionario.Model.GameSession;
import Game.Milionario.Model.Question;
import Game.Milionario.Repository.GameRepository;
import Game.Milionario.Repository.QuestionRepository;
import Game.Milionario.Repository.ScoreRepository;
import org.springframework.stereotype.Service;
import Game.Milionario.Enums.DifficultyLevel;

@Service
public class GameService {

    // Constant for question time limit in seconds
    private static final int QUESTION_TIME_LIMIT_SECONDS = 30;

    private final GameRepository gameRepo;
    private final QuestionRepository questionRepo;
    private final ScoreRepository scoreRepo;

    public GameService(GameRepository gameRepo, QuestionRepository questionRepo, ScoreRepository scoreRepo){
        this.gameRepo=gameRepo;
        this.questionRepo=questionRepo;
        this.scoreRepo=scoreRepo;
    }

    //Start game

    public GameSession startGame(){
        GameSession game=new GameSession();
        game.setCurrentQuestionIndex(0);
        game.setScore(0);
        game.setFinished(false);
        game.setCurrentLevel(DifficultyLevel.EASY);
        // Define o status do jogo como EM PROCESSO - IMPORTANTE: sem isso o jogo não funciona!
        game.setStatus(GameStatus.IN_PROCESS);

        return gameRepo.save(game);
    }

    //Search next question

    public QuestionDto getNextQuestion (Long gameId){
        GameSession game = getGame(gameId);

        checkGameStatus(game);

        List<Question> questions = getQuestionsByLevel(game.getCurrentLevel());

        if(game.getCurrentQuestionIndex()>= questions.size()){
            finishGame(game,true);
            throw new RuntimeException("No more Questions");
        }

        Question question = questions.get(game.getCurrentQuestionIndex());

        // Set the start time for the timer
        game.setQuestionStartTime(LocalDateTime.now());
        gameRepo.save(game);

        return toDTO(question);
    }

    // Response

    public boolean answerQuestion(Long gameId, Long answerId){

        GameSession game = getGame(gameId);

        // Check if time limit exceeded
        if (LocalDateTime.now().isAfter(game.getQuestionStartTime().plusSeconds(QUESTION_TIME_LIMIT_SECONDS))) {
            finishGame(game);
            return false; // Time's up, treat as wrong
        }

        List<Question> questions = getQuestionsByLevel (game.getCurrentLevel());

        Question current = questions.get(game.getCurrentQuestionIndex());

        boolean correct = current.getAnswers()
                .stream()
                .anyMatch(a -> a.getId().equals(answerId) && a.isCorrect());

        if(correct) {
            updateScore(game);
            nextQuestion(game);

            return true;
        }else {

            finishGame(game);
            return false;
        }
    }

    //Others methods

    private GameSession getGame(Long id) {
        return gameRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Game not found"));
    }

    private List<Question> getQuestionsByLevel(DifficultyLevel level) {

        List<Question> questions = questionRepo.findByDifficulty(level);

        Collections.shuffle(questions); // aleatório

        return questions;
    }

    // update game status
    private void updateScore(GameSession game) {

        int points;

        switch (game.getCurrentLevel()) {
            case EASY -> points = 100;
            case MEDIUM -> points = 200;
            case HARD -> points = 500;
            default -> points = 0;
        }

        game.setScore(game.getScore() + points);
    }

    private void nextQuestion(GameSession game) {

        game.setCurrentQuestionIndex(game.getCurrentQuestionIndex() + 1);

        // subir nível
        if (game.getCurrentQuestionIndex() == 5) {
            game.setCurrentLevel(DifficultyLevel.MEDIUM);
        } else if (game.getCurrentQuestionIndex() == 10) {
            game.setCurrentLevel(DifficultyLevel.HARD);
        }

        gameRepo.save(game);
    }

    private void finishGame(GameSession game) {
        game.setFinished(true);
        gameRepo.save(game);
    }

    private QuestionDto toDTO(Question question) {

        QuestionDto dto = new QuestionDto();
        dto.setQuestion(question.getQuestion());

        List<AnswerDto> answers = question.getAnswers()
                .stream()
                .map(a -> new AnswerDto(a.getId(), a.getText()))
                .toList();

        dto.setAnswers(answers);

        return dto;
    }

    // 50/50
    public List<AnswerDto> useFiftyFifty(Long gameId) {

        GameSession game = getGame(gameId);

        if (game.isUsedFiftyFifty()) {
            throw new RuntimeException("Lifeline already used");
        }

        Question question = getCurrentQuestion(game);

        List<Answer> answers = question.getAnswers();

        List<Answer> correct = answers.stream()
                .filter(Answer::isCorrect)
                .toList();

        List<Answer> incorrect = answers.stream()
                .filter(a -> !a.isCorrect())
                .limit(1) // mantém só 1 errada
                .toList();

        List<AnswerDto> result = new ArrayList<>();

        correct.forEach(a -> result.add(new AnswerDto(a.getId(), a.getText())));
        incorrect.forEach(a -> result.add(new AnswerDto(a.getId(), a.getText())));

        game.setUsedFiftyFifty(true);
        gameRepo.save(game);

        return result;
    }

    //skip question

    public void skipQuestion(Long gameId) {

        GameSession game = getGame(gameId);

        if (game.isUsedSkip()) {
            throw new RuntimeException("Skip already used");
        }

        nextQuestion(game);

        game.setUsedSkip(true);
        gameRepo.save(game);
    }

    //Timer for question

    public QuestionDto getNextQuestionWithTimer(Long gameId) {

        GameSession game = getGame(gameId);

        game.setQuestionStartTime(LocalDateTime.now());
        gameRepo.save(game);

        return toDTO(getCurrentQuestion(game));
    }

    // Save Score of the player
    public void saveScore(String nickname, int score) {

        Score s = new Score();
        s.setNickname(nickname);
        s.setPoints(score);

        scoreRepo.save(s);
    }

    // check games status

    private void checkGameStatus(GameSession game){
        if(game.getStatus() != GameStatus.IN_PROCESS){
            throw new RuntimeException(" Game already finished");
        }
    }

    // Points by level

    private int getPointByLevel(DifficultyLevel level){

        return switch(level){
            case EASY -> 100;
            case MEDIUM -> 200;
            case HARD -> 500;
        };
    }

    // Next Level

    private DifficultyLevel nextLevel(DifficultyLevel current) {

        return switch (current) {
            case EASY -> DifficultyLevel.MEDIUM;
            case MEDIUM -> DifficultyLevel.HARD;
            case HARD -> DifficultyLevel.HARD;
        };
    }

    //finish Game

    private void finishGame(GameSession game, boolean won) {

        game.setStatus(won ? GameStatus.WON : GameStatus.LOST);
        gameRepo.save(game);
    }

    //Correct Answer
    private boolean isCorrectAnswer(Question question, Long answerId) {

        return question.getAnswers()
                .stream()
                .anyMatch(a -> a.getId().equals(answerId) && a.isCorrect());
    }

    // Current Question

    private Question getCurrentQuestion(GameSession game) {

        List<Question> questions = getQuestionsByLevel(game.getCurrentLevel());

        return questions.get(game.getCurrentQuestionIndex());
    }

    // Ask the Audience lifeline
    public Map<Long, Integer> useAskAudience(Long gameId) {
        // Obtém a sessão do jogo
        GameSession game = getGame(gameId);

        // Verifica se a ajuda já foi usada
        if (game.isUsedAskAudience()) {
            throw new RuntimeException("Ask Audience already used");
        }

        // Obtém a pergunta atual
        Question question = getCurrentQuestion(game);
        List<Answer> answers = question.getAnswers();

        // Simula votos da audiência: resposta correta recebe 60-80%, o resto dividido entre as erradas
        Map<Long, Integer> votes = new HashMap<>();
        Answer correct = answers.stream().filter(Answer::isCorrect).findFirst().orElseThrow();
        int correctVotes = 60 + (int)(Math.random() * 21); // 60-80%
        votes.put(correct.getId(), correctVotes);

        int remainingVotes = 100 - correctVotes;
        List<Answer> incorrect = answers.stream().filter(a -> !a.isCorrect()).toList();
        int votesPerIncorrect = remainingVotes / incorrect.size();
        for (Answer a : incorrect) {
            votes.put(a.getId(), votesPerIncorrect);
        }

        // Marca a ajuda como usada e salva
        game.setUsedAskAudience(true);
        gameRepo.save(game);

        return votes;
    }

    // Phone a Friend lifeline
    public Long usePhoneFriend(Long gameId) {
        // Obtém a sessão do jogo
        GameSession game = getGame(gameId);

        // Verifica se a ajuda já foi usada
        if (game.isUsedPhoneFriend()) {
            throw new RuntimeException("Phone a Friend already used");
        }

        // Obtém a pergunta atual
        Question question = getCurrentQuestion(game);
        List<Answer> answers = question.getAnswers();

        // 70% de chance de sugerir a correta, 30% aleatória
        Long suggestedId;
        if (Math.random() < 0.7) {
            suggestedId = answers.stream().filter(Answer::isCorrect).findFirst().get().getId();
        } else {
            suggestedId = answers.get((int)(Math.random() * answers.size())).getId();
        }

        // Marca a ajuda como usada e salva
        game.setUsedPhoneFriend(true);
        gameRepo.save(game);

        return suggestedId;
    }
}
