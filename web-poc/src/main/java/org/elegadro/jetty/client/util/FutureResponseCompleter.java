package org.elegadro.jetty.client.util;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.client.HttpContentResponse;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;

import java.util.concurrent.CompletableFuture;

/**
 * Response listener that completes the future when request/response
 * is finished.
 *
 * @author Taimo Peelo
 */
@Slf4j
public class FutureResponseCompleter extends BufferingResponseListener {
    private final CompletableFuture<ContentResponse> ret;

    /**
     * Constructs an instance with default limit on retrieved content size.
     * @see BufferingResponseListener#BufferingResponseListener() BufferingResponseListener()
     * */
    public FutureResponseCompleter(CompletableFuture<ContentResponse> ret) {
        this.ret = ret;
    }

    /**
     * Constructs an instance with given maximum limit on retrieved content size.
     * @param maxLength the maximum length of the retrieved content, in bytes.
     */
    public FutureResponseCompleter(int maxLength, CompletableFuture<ContentResponse> ret) {
        super(maxLength);
        this.ret = ret;
    }

    // can be decorated
    protected void completeSuccessfully(CompletableFuture<ContentResponse> future, ContentResponse contentResponse) {
        future.complete(contentResponse);
    }

    // can be decorated
    protected void completeExceptionally(CompletableFuture<ContentResponse> future, Throwable t) {
        future.completeExceptionally(t);
    }

    protected void logResultContentResponse(Result result, ContentResponse contentResponse) {
        if (log.isDebugEnabled()) {
            log.debug(
                "Returning " + result.getResponse().getStatus() +
                ", content length " + contentResponse.getContent().length +
                " from: " + result.getRequest().getURI().toString()
            );
        }
    }

    protected ContentResponse resultToContentResponse(Result result) {
        ContentResponse contentResponse = new HttpContentResponse(
            result.getResponse(),
            getContent(),
            getMediaType(),
            getEncoding()
        );

        logResultContentResponse(result, contentResponse);

        return contentResponse;
    }

    @Override
    public void onComplete(Result result) {
        if (failureComplete(result))
            return;

        completeSuccessfully(ret, resultToContentResponse(result));
    }

    // handle failure completion
    protected boolean failureComplete(Result result) {
        boolean failed = result.isFailed();
        if (failed) {
            if (log.isDebugEnabled()) {
                log.debug(
                    "Failed completing request to " + result.getRequest().getURI().toString() +
                    "with status " + result.getResponse().getStatus()
                );
            }
            Throwable reqFail = result.getRequestFailure();
            Throwable resFail = result.getResponseFailure();

            if (reqFail != null) {
                completeExceptionally(ret, reqFail);
            } else if (resFail != null) {
                completeExceptionally(ret, resFail);
            } else {
                completeExceptionally(ret, new RuntimeException("Request failed, status code " + result.getResponse().getStatus()));
            }
        }

        return failed;
    }
}
