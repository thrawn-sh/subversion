/**
 * Shadowhunt Subversion - Streaming subversion library without the need for a local sandbox
 * Copyright © 2013-2018 shadowhunt (dev@shadowhunt.de)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.shadowhunt.subversion;

import java.io.IOException;

/**
 * {@link TransmissionException} converts a checked {@link IOException} into a {@link RuntimeException}.
 */
public class TransmissionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new TransmissionException wrapping the given {@link IOException} cause.
     * <p>
     * Note that the detail message associated with {@code cause} is automatically incorporated in this runtime exception's detail message.
     *
     * @param cause
     *            the cause (which is saved for later retrieval by the {@link #getCause()} method).
     */
    public TransmissionException(final IOException cause) {
        super(cause.getMessage(), cause);
    }
}
