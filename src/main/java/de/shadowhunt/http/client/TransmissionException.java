package de.shadowhunt.http.client;

import java.io.IOException;

/**
 * {@link TransmissionException} converts a checked {@link IOException} into a {@link RuntimeException}.
 */
public class TransmissionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public TransmissionException(final IOException e) {
        super(e);
    }
}
