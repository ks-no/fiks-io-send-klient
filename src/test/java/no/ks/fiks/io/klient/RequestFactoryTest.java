package no.ks.fiks.io.klient;

import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringRequestContent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
        assertEquals(RequestFactoryImpl.BASE_PATH + "send", sendToFiksIORequest.getPath());
        assertEquals(hostName, sendToFiksIORequest.getHost());
        assertEquals(scheme, sendToFiksIORequest.getScheme());
        assertEquals(portNumber, sendToFiksIORequest.getPort());
    }
}