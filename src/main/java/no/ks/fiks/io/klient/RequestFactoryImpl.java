package no.ks.fiks.io.klient;

import lombok.Builder;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpEntity;

import java.net.URI;

public class RequestFactoryImpl implements RequestFactory {
    static final String BASE_PATH = "/fiks-io/api/v1/";
    private final String scheme;
    private final String hostName;
    private final Integer portNumber;

    @Builder
    public RequestFactoryImpl(String scheme, String hostName, Integer portNumber) {
        this.scheme = scheme;
        this.hostName = hostName;
        this.portNumber = portNumber;
    }

    @Override
    public ClassicHttpRequest createSendToFiksIORequest(final HttpEntity contentProvider) {
        return createPostRequest(contentProvider);
    }

    private HttpPost createPostRequest(final HttpEntity contentProvider){
        final HttpPost httpPost = new HttpPost(URI.create(scheme + "://" + hostName + ":" + portNumber + BASE_PATH + "send"));
        httpPost.setEntity(contentProvider);
        return httpPost;
    }

}
