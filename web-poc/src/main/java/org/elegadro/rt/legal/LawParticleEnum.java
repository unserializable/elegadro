package org.elegadro.rt.legal;

/**
 * @author Taimo Peelo
 */
public enum LawParticleEnum {
    SEADUS("Seadus"),
    OSA("Osa"),
    PEATYKK("Peatykk"),
    JAGU("Jagu"),
    JAOTIS("Jaotis"),
    ALLJAOTIS("Alljaotis"),
    PARAGRAHV("Paragrahv"),
    LOIGE("Loige"),
    PUNKT("Punkt")
    ;

    /* Mutable fields in an enum are always effectively transient. */
    private String label;

    LawParticleEnum(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
