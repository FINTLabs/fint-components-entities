package no.fint.components.components_with_entities;

import lombok.Data;

@Data
public class Entity {

    private boolean abstrakt;
    private Identifikator id;
    private String navn;
    private String stereotype;
}
