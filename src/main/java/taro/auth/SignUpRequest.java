package taro.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import taro.user.FT;
import taro.user.IE;
import taro.user.JP;
import taro.user.NS;

@Data
@Getter
@Schema(description = "회원가입 요청 DTO")
public class SignUpRequest {

    @Schema(description = "카카오 이메일")
    private String kakaoEmail;

    @Schema(description = "닉네임")
    private String nickName;

    private IE ie;

    private NS ns;

    private FT ft;

    private JP jp;

    private String role= "ROLE_USER";
}
