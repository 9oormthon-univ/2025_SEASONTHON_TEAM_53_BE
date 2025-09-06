package taro.service;

import org.springframework.stereotype.Service;

/**
 * 임베딩 서비스 인터페이스
 * 추후 실제 임베딩 모델 연동 시 구현 예정
 */
@Service
public class EmbeddingService {
    
    /**
     * 텍스트를 임베딩 벡터로 변환
     * @param text 임베딩할 텍스트
     * @return 임베딩 벡터 (현재는 더미 데이터 반환)
     */
    public float[] embedText(String text) {
        // TODO: 실제 임베딩 모델 연동 구현
        // 현재는 더미 데이터 반환 (1536차원 - OpenAI 임베딩 기준)
        float[] dummyEmbedding = new float[1536];
        for (int i = 0; i < dummyEmbedding.length; i++) {
            dummyEmbedding[i] = (float) Math.random();
        }
        return dummyEmbedding;
    }
}
