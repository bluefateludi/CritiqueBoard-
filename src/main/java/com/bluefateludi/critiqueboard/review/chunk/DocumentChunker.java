package com.bluefateludi.critiqueboard.review.chunk;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DocumentChunker {

    public List<Chunk> chunk(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        String[] paragraphs = text.strip().split("\\R\\s*\\R+");
        List<Chunk> chunks = new ArrayList<>();
        for (String paragraph : paragraphs) {
            String content = paragraph.strip();
            if (!content.isBlank()) {
                chunks.add(new Chunk(chunks.size(), content));
            }
        }
        return chunks;
    }

    public record Chunk(int index, String content) {
    }
}
