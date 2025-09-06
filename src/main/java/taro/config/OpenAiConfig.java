package taro.config;

import com.theokanning.openai.service.OpenAiService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAiConfig {

    @Bean
    public OpenAiService openAiService() {
        String apiKey = System.getenv("OPENAI_API_KEY"); // 환경변수에서 키 가져오기
        if (apiKey == null) {
            throw new RuntimeException("OPENAI_API_KEY 환경변수가 설정되지 않았습니다.");
        }
        return new OpenAiService(apiKey);
    }
}