package com.igriss.ListIn.publication.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PublicationEntityLifecycleTest {

    @Test
    void init_shouldSetPriceToZero_whenPriceIsNull() {
        Publication publication = Publication.builder()
                .price(null)
                .build();

        publication.init();

        assertNotNull(publication.getPrice());
        assertEquals(0.0F, publication.getPrice());
    }

    @Test
    void init_shouldNotOverridePrice_whenPriceIsAlreadySet() {
        Publication publication = Publication.builder()
                .price(50000.0F)
                .build();

        publication.init();

        assertEquals(50000.0F, publication.getPrice());
    }

    @Test
    void init_shouldWorkOnPreUpdateSimulation() {
        Publication publication = new Publication();
        publication.setPrice(null);

        publication.init();
        assertEquals(0.0F, publication.getPrice());

        publication.setPrice(null);
        publication.init();
        assertEquals(0.0F, publication.getPrice());
    }
}
