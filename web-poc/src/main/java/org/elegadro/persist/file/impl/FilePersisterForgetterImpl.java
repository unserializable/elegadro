package org.elegadro.persist.file.impl;

import org.elegadro.persist.file.FileForgetter;
import org.elegadro.persist.file.FilePersister;
import org.elegadro.persist.file.FilePersisterForgetter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

/**
 * @author Taimo Peelo
 */
@Component("filePersisterForgetter")
public class FilePersisterForgetterImpl implements FilePersisterForgetter {
    @Autowired
    private FilePersister filePersister;

    @Autowired
    private FileForgetter fileForgetter;

    // TODO: thread-safety

    @Override
    public void persist(Path path, byte[] data) {
        filePersister.persist(path, data);
    }

    @Override
    public void forget(Path path) {
        fileForgetter.forget(path);
    }
}
