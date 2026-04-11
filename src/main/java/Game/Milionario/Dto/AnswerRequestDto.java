package Game.Milionario.Dto;

public class AnswerRequestDto {

    private Long answerId;

    public AnswerRequestDto(){}

    public Long getAnswerId(){
        return answerId;
    }

    public void setAnswerId(Long answerId){
        this.answerId = answerId;
    }
}
