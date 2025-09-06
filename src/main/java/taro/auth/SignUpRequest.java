package taro.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import taro.user.JobCategory;
import taro.user.Personality;
import taro.user.UserRole;

@Data
@Getter
@Schema(description = "회원가입 요청 DTO")
public class SignUpRequest {

    @Schema(description = "카카오 이메일", example = "test@kakao.com")
    private String kakaoEmail;

    @Schema(description = "닉네임")
    private String nickName;

    private String role= "ROLE_USER";

    @Schema(description = "직업")
    private JobCategory jobCategory;

    @Schema(description = "성향")
    private Personality personality;

}
