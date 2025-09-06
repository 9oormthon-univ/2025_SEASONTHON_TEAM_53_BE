package taro.card;

import jakarta.persistence.*;
import lombok.*;

import java.nio.ByteBuffer;

@Entity
@Table(name = "cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100, unique = true)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String strength;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardType type;

    @Lob
    private byte[] embedding;  // float[]를 byte[]로 변환하여 저장

    // 저장할 때 float[] → byte[]
    public void settingEmbedding(float[] embedding) {
        if (embedding == null) {
            this.embedding = null;
            return;
        }
        ByteBuffer buffer = ByteBuffer.allocate(embedding.length * 4);
        for (float f : embedding) {
            buffer.putFloat(f);
        }
        this.embedding = buffer.array();
    }

    // 불러올 때 byte[] → float[]
    public float[] getEmbedding() {
        if (embedding == null) return null;
        ByteBuffer buffer = ByteBuffer.wrap(embedding);
        float[] array = new float[embedding.length / 4];
        for (int i = 0; i < array.length; i++) {
            array[i] = buffer.getFloat();
        }
        return array;
    }


    @Column(nullable = false)
    private String question;
}