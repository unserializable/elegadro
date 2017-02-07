package org.elegadro.iota.parser.exception;

import org.xml.sax.SAXParseException;

/**
 * @author Taimo Peelo
 */
public class SAXParseRuntimeException extends RuntimeException {
    public SAXParseRuntimeException(String message, SAXParseException cause) {
        super(message, cause);
    }

    public SAXParseRuntimeException(SAXParseException cause) {
        super(cause);
    }

    @Override
    public synchronized SAXParseException getCause() {
        return (SAXParseException) super.getCause();
    }
}
