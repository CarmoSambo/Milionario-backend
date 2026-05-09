package Game.Milionario.Dto;

import java.util.List;

public class QuestionDto {

    private String question;
    private List<AnswerDto> answers;

    private int timeRemaining;

    public QuestionDto(){}

    public QuestionDto(String question, List<AnswerDto> answers,int timeRemaining){
        this.question=question;
        this.answers=answers;
        this.timeRemaining=timeRemaining;
    }

    public String getQuestion(){
        return question;
    }

    public void setQuestion(String question){
        this.question = question;
    }

    public List<AnswerDto> getAnswers() {
        return answers;
    }

    public void setAnswers(List<AnswerDto> answers) {
        this.answers = answers;
    }

    public int getTimeRemaining() {
        return timeRemaining;
    }

    public void setTimeRemaining(int timeRemaining) {
        this.timeRemaining = timeRemaining;
    }
}
