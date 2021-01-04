package no.fint.components.componententities;

import lombok.Data;

@Data
public class Entity {

    private boolean abstrakt;
    private Identifikator id;
    private String navn;
    private String stereotype;
}
