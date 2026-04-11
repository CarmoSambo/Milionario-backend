package Game.Milionario.Exception;

import Game.Milionario.Dto.ApiResponse;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ApiResponse<?> handlerRuntime(RuntimeException ex){
        return new ApiResponse<>(false, ex.getMessage(), null);
    }
}
