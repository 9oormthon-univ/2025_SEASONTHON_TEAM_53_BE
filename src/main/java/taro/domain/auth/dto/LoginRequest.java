package taro.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "로그인 요청")
public class LoginRequest {
    
    @NotBlank(message = "아이디를 입력해주세요")
    @Schema(description = "로그인 아이디", example = "yeeeun123")
    private String loginId;
    
    @NotBlank(message = "비밀번호를 입력해주세요")
    @Schema(description = "비밀번호", example = "abcd1234!")
    private String password;
}
