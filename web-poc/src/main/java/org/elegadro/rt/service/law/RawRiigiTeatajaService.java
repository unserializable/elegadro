package org.elegadro.rt.service.law;

import org.elegadro.error.UncompliantVmError;
import org.eclipse.jetty.client.api.ContentResponse;
import org.elegadro.actronym.Actronym;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.CompletableFuture;

/**
 * @author Taimo Peelo
 */
public interface RawRiigiTeatajaService {
    String RT_PROTOCOL = "https";
    String RT_HOST = "www.riigiteataja.ee";

    default String mainUrl()  {
        return RT_PROTOCOL + "://" + RT_HOST;
    }

    default String actUrl(Actronym act) {
        String encActronym = null;
        try {
            encActronym = URLEncoder.encode(act.getActronym(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw UncompliantVmError.UTF_8_unsupported(e);
        }
        return mainUrl() + "/akt/" + encActronym;
    }

    default String actXmlUrl(Actronym act) {
        return mainUrl() + "/akt/" + act.getActId() + ".xml";
    }

    CompletableFuture<ContentResponse> act(Actronym act);

    CompletableFuture<ContentResponse> actXml(Actronym act);
}
