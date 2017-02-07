package org.elegadro.red.pill;

import org.elegadro.iota.legal.impl.Seadus;
import org.elegadro.iota.parser.Parser;
import org.elegadro.iota.parser.rt.xml.SeadusParser;
import org.elegadro.iota.parser.rt.xml.TyviOigusaktParser;
import org.elegadro.iota.rt.actronym.Actronym;
import org.springframework.core.io.FileSystemResource;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.util.Optional;

/**
 * @author Taimo Peelo
 */
public class Trinity {
    public static String actXmlUrl(Actronym act) {
        return "https://www.riigiteataja.ee/akt/"  + act.getActId() + ".xml";
    }

    public static boolean bringRemoteLaw(Actronym actronym, Path lawPath) {
        String actXmlWebUrl = actXmlUrl(actronym);

        try {
            URL url = new URL(actXmlWebUrl);
            URLConnection conn = url.openConnection();
            InputStream inputStream = conn.getInputStream();

            FileOutputStream outputStream = new FileOutputStream(lawPath.toFile());

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();

            if (bytesRead > 0) {
                byte nge = (byte) '>';
                byte b = buffer[bytesRead - 1];
                if (b != nge)
                    throw new IOException("Bad read, xml not ending with '>'");
            }

            System.out.println(url +" ("+ actronym.getActronym() +") downloaded");
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
            return false;
        }

        return true;
    }

    public static Seadus bringLocalLaw(Path lawPath) {
        Optional<Seadus> maybeSeadus = Parser.combinedResult(
            new TyviOigusaktParser(new FileSystemResource(lawPath.toFile())),
            SeadusParser.class
        );

        return maybeSeadus.isPresent() ? maybeSeadus.get() : null;
    }
}
