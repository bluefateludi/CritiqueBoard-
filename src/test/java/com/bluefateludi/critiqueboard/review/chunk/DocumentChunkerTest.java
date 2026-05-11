package com.bluefateludi.critiqueboard.review.chunk;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentChunkerTest {

    @Test
    void splitsTextByBlankLinesAndPreservesOrder() {
        DocumentChunker chunker = new DocumentChunker();

        var chunks = chunker.chunk("""
                First paragraph.

                Second paragraph has
                two lines.


                Third paragraph.
                """);

        assertThat(chunks).hasSize(3);
        assertThat(chunks.get(0).index()).isEqualTo(0);
        assertThat(chunks.get(0).content()).isEqualTo("First paragraph.");
        assertThat(chunks.get(1).index()).isEqualTo(1);
        assertThat(chunks.get(1).content()).isEqualTo("Second paragraph has\ntwo lines.");
        assertThat(chunks.get(2).index()).isEqualTo(2);
        assertThat(chunks.get(2).content()).isEqualTo("Third paragraph.");
    }

    @Test
    void returnsNoChunksForBlankText() {
        DocumentChunker chunker = new DocumentChunker();

        assertThat(chunker.chunk("  \n\n  ")).isEmpty();
    }
}
