package org.elegadro.parser.util.rt;

import _2010._02.tyviseadus_1_10.Oigusakt;
import org.elegadro.parser.rt.xml.sax.LawfulErrorHandler;
import org.springframework.core.io.ClassPathResource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;

/**
 * @author Taimo Peelo
 */
public final class SchemaUtil {
    private static final String CP_XSD_PATH = "tyviseadus_1_10.02.2010.xsd";

    private static final SchemaFactory SF = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    private static final JAXBContext TYVI_OIGUSAKT_CONTEXT;

    public static final Schema TYVI_OIGUSAKT_SCHEMA;
    public static final Unmarshaller TYVI_OIGUSAKT_UNMARSHALLER;

    private SchemaUtil() {}

    static {
        ClassPathResource schemaISS = new ClassPathResource(CP_XSD_PATH);
        try {
            StreamSource scis = new StreamSource(schemaISS.getInputStream());
            TYVI_OIGUSAKT_SCHEMA = SF.newSchema(scis);
        } catch (IOException | SAXException e) {
            throw new RuntimeException(e);
        }

        try {
            TYVI_OIGUSAKT_CONTEXT = JAXBContext.newInstance(Oigusakt.class);
        } catch (JAXBException ex) {
            throw new RuntimeException(ex);
        }

        try {
            TYVI_OIGUSAKT_UNMARSHALLER = TYVI_OIGUSAKT_CONTEXT.createUnmarshaller();
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    public static _2010._02.tyviseadus_1_10.Oigusakt unmarshalTyviOigusakt(StreamSource xmlStreamSource) {
        try {
            return TYVI_OIGUSAKT_UNMARSHALLER.unmarshal(xmlStreamSource, Oigusakt.class).getValue();
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    /** Return new {@link Validator} instance for given {@link Schema}. */
    public static Validator newValidatorFor(Schema tyviSchema) {
        Validator tyviValidator = tyviSchema.newValidator();
        tyviValidator.setErrorHandler(new LawfulErrorHandler());
        return tyviValidator;
    }
}
