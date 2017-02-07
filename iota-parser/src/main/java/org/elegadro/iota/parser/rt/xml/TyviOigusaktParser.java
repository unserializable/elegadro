package org.elegadro.iota.parser.rt.xml;

import _2010._02.tyviseadus_1_10.Oigusakt;
import org.elegadro.iota.parser.exception.SAXParseRuntimeException;
import org.elegadro.iota.parser.Parser;
import org.elegadro.iota.parser.rt.util.SchemaUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamSource;
import org.springframework.util.StreamUtils;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

/**
 * @author Taimo Peelo
 */
@Slf4j
public class TyviOigusaktParser extends Parser<InputStreamSource, _2010._02.tyviseadus_1_10.Oigusakt> {
    private static final String CP_XSD_PATH = "tyviseadus_1_10.02.2010.xsd";

    public TyviOigusaktParser(InputStreamSource parseSource) {
        super(parseSource);
    }

    @Override @SneakyThrows(IOException.class)
    public Optional<_2010._02.tyviseadus_1_10.Oigusakt> parse() {
        InputStream reis = reusableIS(parseSource.getInputStream());
        reis.mark(0); // readAheadLimit for BAIS has no meaning!

        /*
           There are notes in JAXBContext doc, that suggest the validation done
           here is too much in JAXB 1.0 style.

           "Validation has been changed significantly since JAXB 1.0. The
           Validator class has been deprecated and made optional. This means that
           you are advised not to use this class and, in fact, it may not even
           be available depending on your JAXB provider. JAXB 1.0 client
           applications that rely on Validator will still work properly when
           deployed with the JAXB 1.0 runtime system. In JAXB 2.0, the
           Unmarshaller has included convenince methods that expose the JAXP 1.3
           javax.xml.validation framework. Please refer to the
           Unmarshaller.setSchema(javax.xml.validation.Schema) API for more
           information."

           TODO: rework
         */

        try {
            StreamSource xmlStreamSource = new StreamSource(reis);
            tyviSeadusValidator().validate(xmlStreamSource);
            //objects.set()
        } catch (SAXParseRuntimeException spex) {
            log.warn("Validation failed for XML ", spex.getCause());
            return Optional.empty();
        } catch (SAXException | IOException e) {
            log.warn("Something rotten in the state of parsing XML ", e);
            return Optional.empty();
        }

        // allow reuse of ByteArrayInputStream
        reis.reset();

        Oigusakt oigusakt = SchemaUtil.unmarshalTyviOigusakt(new StreamSource(reis));

        return Optional.of(oigusakt);
    }

    @SneakyThrows(IOException.class)
    private InputStream reusableIS(InputStream original) {
        if (ByteArrayInputStream.class.equals(original.getClass())) {
            if (log.isDebugEnabled()) {
                log.debug(
                    "Reusable input stream creator returning original IS (ByteArrayInputStream)."
                );
            }
            return original;
        }

        byte[] bytes = StreamUtils.copyToByteArray(original);
        return new ByteArrayInputStream(bytes);
    }

    private Validator tyviSeadusValidator() {
        return SchemaUtil.newValidatorFor(SchemaUtil.TYVI_OIGUSAKT_SCHEMA);
    }
}
