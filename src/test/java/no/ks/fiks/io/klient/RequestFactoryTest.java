package no.ks.fiks.io.klient;

import org.apache.hc.core5.http.io.entity.StringEntity;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;

class RequestFactoryTest {

    @Test
    void createSendToFiksIORequest() throws URISyntaxException {
        final String scheme = "http";
        final String hostName = "localhost";
        final int portNumber = 9999;

        final var sendToFiksIORequest = RequestFactoryImpl.builder()
                                                              .scheme(scheme)
                                                              .hostName(hostName)
                                                              .portNumber(portNumber)
                                                              .build()
                                                              .createSendToFiksIORequest(new StringEntity("stuff"));
        assertThat(sendToFiksIORequest.getPath()).isEqualTo(RequestFactoryImpl.BASE_PATH + "send");
        final URI uri = sendToFiksIORequest.getUri();
        assertThat(uri.getHost()).isEqualTo(hostName);
        assertThat(uri.getScheme()).isEqualTo(scheme);
        assertThat(uri.getPort()).isEqualTo(portNumber);
    }
}