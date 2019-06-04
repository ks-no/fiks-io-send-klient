package no.ks.fiks.io.klient;

import lombok.Builder;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import java.io.IOException;

public class RequestFactoryImpl implements RequestFactory {
    static final String BASE_PATH = "/fiks-io/api/v1/";
    private final HttpClient client;
    private final String scheme;
    private final String hostName;
    private final Integer portNumber;

    @Builder
    public RequestFactoryImpl(String scheme, String hostName, Integer portNumber) {
        this.scheme = scheme;
        this.hostName = hostName;
        this.portNumber = portNumber;

        this.client = new HttpClient(new SslContextFactory.Client());
        try {
            client.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Request createSendToFiksIORequest(ContentProvider contentProvider) {
        return client.newRequest(hostName, portNumber)
                                .scheme(scheme)
                                .method(HttpMethod.POST)
                                .path(BASE_PATH + "send")
                                .content(contentProvider);
    }

    @Override
    public void close() {
        try {
            client.stop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
