package org.elegadro.parser.rt.xml.sax;

import org.elegadro.exception.SAXParseRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

/**
 * @author Taimo Peelo
 */
@Slf4j
public class LawfulErrorHandler implements ErrorHandler {
    @Override
    public void warning(SAXParseException exception) {
        log.warn("XML SAX parse warning encountered", exception);
    }

    @Override
    public void error(SAXParseException exception) {
        if (!isValjaandjaUlenoukoguException(exception))
            throw new SAXParseRuntimeException(exception);
    }

    @Override
    public void fatalError(SAXParseException exception){
        throw new SAXParseRuntimeException(exception);
    }

    /* There are two old laws: ES (Elamuseadus) and PõRS (Põllumajandusreformi seadus)
       that were given out by "Ülemnõukogu", which is not part of XML Schema enum,
       resulting in an error like:

       value 'Ülemnõukogu' is not facet-valid with respect to enumeration
       '[Riigikogu, Rahvahääletusel vastu võetud]'. It must be a value from the enumeration.

       Take special care of that situation to not throw validation error.
    */
    private boolean isValjaandjaUlenoukoguException(SAXParseException sex) {
        String emsg = sex.getMessage();
        if (emsg != null && emsg.startsWith("cvc-enumeration-valid") && emsg.contains("'Ülemnõukogu'"))
            return true;

        if (emsg != null && emsg.startsWith("cvc-type.3.1.3") && emsg.contains("'Ülemnõukogu'"))
            return true;

        return false;
    }
}
