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

/**
 * {@link SubversionException} is the superclass of those exceptions that can be thrown in the subversion module.
 */
public class SubversionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final int httpStatusCode;

    /**
     * Constructs a new SubversionException exception with the specified detail message. The cause is not initialized, and may subsequently be initialized by a call to {@link #initCause}
     *
     * @param message the detail message. The detail message is saved for later retrieval by the {@link #getMessage()} method
     */
    public SubversionException(final String message) {
        this(message, 0);
    }

    /**
     * Constructs a new SubversionException exception with the specified detail message. The cause is not initialized, and may subsequently be initialized by a call to {@link #initCause}
     *
     * @param message the detail message. The detail message is saved for later retrieval by the {@link #getMessage()} method
     * @param httpStatusCode for each error that is reported by the underlying subversion server, the HTTP status code is reported
     */
    public SubversionException(final String message, final int httpStatusCode) {
        super(message);
        this.httpStatusCode = httpStatusCode;
    }

    /**
     * Constructs a new SubversionException with the specified detail message and cause.
     * <p>
     * Note that the detail message associated with {@code cause} is <i>not</i> automatically incorporated in this runtime exception's detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method)
     * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method). (A {@code null} value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public SubversionException(final String message, final Throwable cause) {
        this(message, cause, 0);
    }

    /**
     * Constructs a new SubversionException with the specified detail message and cause.
     * <p>
     * Note that the detail message associated with {@code cause} is <i>not</i> automatically incorporated in this runtime exception's detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method).
     * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method). (A {@code null} value is permitted, and indicates that the cause is nonexistent or unknown.)
     * @param httpStatusCode for each error that is reported by the underlying subversion server, the HTTP status code is reported, for internal error this is {@code 0}
     */
    public SubversionException(final String message, final Throwable cause, final int httpStatusCode) {
        super(message, cause);
        this.httpStatusCode = httpStatusCode;
    }

    /**
     * Returns the status code of the server response.
     *
     * @return the status code of the server response or {@code 0} if the server was not responsible for the error
     */
    public final int getHttpStatusCode() {
        return httpStatusCode;
    }

    @Override
    public final String toString() {
        if (httpStatusCode == 0) {
            return super.toString();
        }
        return super.toString() + " [" + httpStatusCode + ']';
    }
}
