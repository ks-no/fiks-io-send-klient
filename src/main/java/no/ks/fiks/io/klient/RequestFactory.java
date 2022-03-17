package no.ks.fiks.io.klient;

import org.eclipse.jetty.client.api.Request;

import java.io.Closeable;

public interface RequestFactory extends Closeable {
    Request createSendToFiksIORequest(Request.Content contentProvider);
}
