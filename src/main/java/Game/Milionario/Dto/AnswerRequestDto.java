package Game.Milionario.Dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AnswerRequestDto {
    @NotNull(message = "O ID da resposta é obrigatório")
    private Long answerId;
}
