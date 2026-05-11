package com.bluefateludi.critiqueboard.review.agent;

import java.util.UUID;

public record DocumentChunkContext(
        UUID documentChunkId,
        int chunkIndex,
        String content
) {
}
