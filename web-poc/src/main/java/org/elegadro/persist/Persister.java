package org.elegadro.persist;

/**
 * @author Taimo Peelo
 */
public interface Persister<M, T> {
    void persist(M meta, T data);
}
