package no.ks.fiks.io.klient;

import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringRequestContent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RequestFactoryTest {

    @Test
    void createSendToFiksIORequest() {
        final String scheme = "http";
        final String hostName = "localhost";
        final int portNumber = 9999;
        final Request sendToFiksIORequest = RequestFactoryImpl.builder()
                                                              .scheme(scheme)
                                                              .hostName(hostName)
                                                              .portNumber(portNumber)
                                                              .build()
                                                              .createSendToFiksIORequest(new StringRequestContent("stuff"));
        assertThat(sendToFiksIORequest.getPath()).isEqualTo(RequestFactoryImpl.BASE_PATH + "send");
        assertThat(sendToFiksIORequest.getHost()).isEqualTo(hostName);
        assertThat(sendToFiksIORequest.getScheme()).isEqualTo(scheme);
        assertThat(sendToFiksIORequest.getPort()).isEqualTo(portNumber);
    }
}