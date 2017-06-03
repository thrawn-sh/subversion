/**
 * Copyright © 2013-2017 shadowhunt (dev@shadowhunt.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
     * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method).
     */
    public TransmissionException(final IOException cause) {
        super(cause.getMessage(), cause);
    }
}
