package taro.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회원가입 요청")
public class RegisterRequest {
    
    @NotBlank(message = "아이디를 입력해주세요")
    @Size(min = 4, max = 20, message = "아이디는 4자 이상 20자 이하로 입력해주세요")
    @Schema(description = "로그인 아이디", example = "yeeeun123")
    private String loginId;
    
    @NotBlank(message = "비밀번호를 입력해주세요")
    @Size(min = 8, message = "비밀번호는 8자 이상으로 입력해주세요")
    @Schema(description = "비밀번호", example = "abcd1234!")
    private String password;
    
    @NotBlank(message = "비밀번호 확인을 입력해주세요")
    @Schema(description = "비밀번호 확인", example = "abcd1234!")
    private String passwordConfirm;
    
    @NotBlank(message = "닉네임을 입력해주세요")
    @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하로 입력해주세요")
    @Schema(description = "닉네임", example = "예은")
    private String nickname;
}
