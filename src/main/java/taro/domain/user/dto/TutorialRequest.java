package taro.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import taro.domain.JobCategory;
import taro.domain.Personality;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TutorialRequest {
    
    @NotBlank(message = "닉네임은 필수입니다")
    @Pattern(regexp = "^[가-힣a-zA-Z0-9_-]{2,16}$", 
            message = "닉네임은 2~16자의 한글, 영문, 숫자, _, -만 사용 가능합니다")
    private String nickname;
    
    @NotNull(message = "직업 분야는 필수입니다")
    private JobCategory jobCategory;
    
    @NotNull(message = "성향은 필수입니다")
    private Personality personality;
}
