package org.elegadro.persist.file.impl;

import org.elegadro.persist.file.FilePersister;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * @author Taimo Peelo
 */
@Component("filePersister")
public class FilePersisterImpl implements FilePersister {
    @Override
    public void persist(Path meta, byte[] data) {
        try {
            Files.write(meta, data, StandardOpenOption.CREATE_NEW);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
