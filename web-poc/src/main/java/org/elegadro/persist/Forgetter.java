package org.elegadro.persist;

/**
 * @author Taimo Peelo
 */
public interface Forgetter<M> {
    void forget(M meta);
}
