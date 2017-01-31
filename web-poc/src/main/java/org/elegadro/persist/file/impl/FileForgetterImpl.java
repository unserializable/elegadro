package org.elegadro.persist.file.impl;

import org.elegadro.persist.file.FileForgetter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Taimo Peelo
 */
@Component
public class FileForgetterImpl implements FileForgetter {
    @Override
    public void forget(Path path) {
        try {
            Files.delete(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
