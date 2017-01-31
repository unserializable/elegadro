package org.elegadro.rt.service.law.impl;

import org.elegadro.jetty.client.util.FutureResponseCompleter;
import org.elegadro.rt.service.law.RawRiigiTeatajaService;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.elegadro.actronym.Actronym;

import java.util.concurrent.CompletableFuture;

/**
 * @author Taimo Peelo
 */
@Slf4j
@Repository
public class RawRiigiTeatajaServiceImpl implements RawRiigiTeatajaService {
    @Autowired
    private HttpClient httpClient;

    @Override
    public CompletableFuture<ContentResponse> act(Actronym act) {
        CompletableFuture<ContentResponse> future = new CompletableFuture<>();
        httpClient
            .newRequest(actUrl(act))
            .method(HttpMethod.GET)
            .send(new FutureResponseCompleter(future));

        return future;
    }

    @Override
    public CompletableFuture<ContentResponse> actXml(Actronym act) {
        CompletableFuture<ContentResponse> future = new CompletableFuture<>();
        httpClient
            .newRequest(actXmlUrl(act))
            .method(HttpMethod.GET)
            .send(new FutureResponseCompleter(4*1024*1024, future));

        return future;
    }
}
