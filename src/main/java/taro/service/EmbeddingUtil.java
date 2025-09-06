package taro.service;

import com.theokanning.openai.embedding.EmbeddingRequest;
import com.theokanning.openai.embedding.EmbeddingResult;
import com.theokanning.openai.service.OpenAiService;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

public class EmbeddingUtil {

    private final OpenAiService service;

    public EmbeddingUtil(String apiKey) {
        this.service = new OpenAiService(apiKey);
    }

    /**
     * 문자열을 임베딩으로 변환 후 byte 배열 반환
     */
    public byte[] getEmbeddingBytes(String text) {
        EmbeddingRequest request = EmbeddingRequest.builder()
                .model("text-embedding-3-small") // 모델 선택
                .input(Arrays.asList(text))
                .build();

        EmbeddingResult response = service.createEmbeddings(request);

        List<Double> embeddingVector = response.getData().get(0).getEmbedding();

        // double → float → byte 변환
        ByteBuffer buffer = ByteBuffer.allocate(embeddingVector.size() * 4);
        for (Double d : embeddingVector) {
            buffer.putFloat(d.floatValue());
        }

        return buffer.array();
    }

    public static void main(String[] args) {
        String apiKey = "YOUR_OPENAI_API_KEY";
        String text = "안녕하세요! 테스트 문장입니다.";

        EmbeddingUtil util = new EmbeddingUtil(apiKey);
        byte[] bytes = util.getEmbeddingBytes(text);

        System.out.println("Byte 배열 크기: " + bytes.length);
    }
}