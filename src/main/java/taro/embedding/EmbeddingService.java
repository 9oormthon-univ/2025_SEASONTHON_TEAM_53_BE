package taro.embedding;


import com.theokanning.openai.embedding.EmbeddingRequest;
import com.theokanning.openai.embedding.EmbeddingResult;
import com.theokanning.openai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import taro.card.Card;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Collections;
import java.util.List;

@Service
public class EmbeddingService {

    private final OpenAiService openAiService;

    public EmbeddingService(@Value("${OPENAI_API_KEY}") String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("OpenAI API Key is missing!");
        }
        this.openAiService = new OpenAiService(apiKey);
    }

    public byte[] getEmbeddingBytes(String text) {
        EmbeddingRequest request = EmbeddingRequest.builder()
                .model("text-embedding-3-small")
                .input(Collections.singletonList(text))
                .build();

        EmbeddingResult result = openAiService.createEmbeddings(request);
        List<Double> vector = result.getData().get(0).getEmbedding();

        ByteBuffer buffer = ByteBuffer.allocate(vector.size() * 4);
        for (Double v : vector) {
            buffer.putFloat(v.floatValue());
        }
        return buffer.array();


    }

    public float[] bytesToFloatArray(byte[] bytes) {
        if (bytes == null) return null;
        FloatBuffer fb = ByteBuffer.wrap(bytes).asFloatBuffer();
        float[] array = new float[fb.remaining()];
        fb.get(array);
        return array;
    }

    // 1️⃣ 여러 float[] 벡터를 평균 내기
    public float[] averageEmbedding(List<float[]> embeddings) {
        if (embeddings == null || embeddings.isEmpty()) return null;

        int length = embeddings.get(0).length;
        float[] avg = new float[length];

        for (float[] vec : embeddings) {
            for (int i = 0; i < length; i++) {
                avg[i] += vec[i];
            }
        }

        for (int i = 0; i < length; i++) {
            avg[i] /= embeddings.size();
        }

        return avg;
    }

    // 2️⃣ 가장 유사한 카드 찾기 (코사인 유사도)
    public Card findMostSimilar(float[] target, List<Card> cards) {
        if (target == null || cards == null || cards.isEmpty()) return null;

        Card bestCard = null;
        double bestScore = -1;

        for (Card card : cards) {
            float[] cardEmbedding = card.getEmbedding();
            if (cardEmbedding == null) continue;

            double score = cosineSimilarity(target, cardEmbedding);
            if (score > bestScore) {
                bestScore = score;
                bestCard = card;
            }
        }

        return bestCard;
    }

    public double cosineSimilarity(float[] vecA, float[] vecB) {
        if (vecA.length != vecB.length) {
            throw new IllegalArgumentException("벡터 길이가 달라요.");
        }

        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vecA.length; i++) {
            dot += vecA[i] * vecB[i];          // 내적
            normA += vecA[i] * vecA[i];        // A 벡터 크기 제곱
            normB += vecB[i] * vecB[i];        // B 벡터 크기 제곱
        }

        return dot / (Math.sqrt(normA) * Math.sqrt(normB) + 1e-10); // 0으로 나누는 경우 방지
    }

}
