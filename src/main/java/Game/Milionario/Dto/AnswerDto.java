package Game.Milionario.Dto;

public class AnswerDto {
    private Long id;
    private String text;

    public AnswerDto() {}

    public AnswerDto(Long id, String text) {
        this.id = id;
        this.text = text;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
