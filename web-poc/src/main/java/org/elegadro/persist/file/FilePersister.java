package org.elegadro.persist.file;

import org.elegadro.persist.Persister;

import java.nio.file.Path;

/**
 * @author Taimo Peelo
 */
public interface FilePersister extends Persister<Path, byte[]> {
}
