/**
 * Copyright (C) 2013-2017 shadowhunt (dev@shadowhunt.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.shadowhunt.subversion;

import java.util.Map;

/**
 * {@link Transaction} allows the application to define units of work, it can be
 * created via {@link Repository#createTransaction()}.
 */
public interface Transaction extends View {

    /**
     * Defines the status of each {@link Resource} that is part of the current
     * active {@link Transaction}.
     */
    enum Status {
        ADDED("A", 2), DELETED("D", 3), EXISTS("E", -1), MODIFIED("M", 1), NOT_TRACKED("?", Integer.MIN_VALUE);

        private final String abbreviation;

        /**
         * Priority of this {@link Status} compared to the other {@link Status}.
         */
        public final int order;

        Status(final String abbreviation, final int order) {
            this.abbreviation = abbreviation;
            this.order = order;
        }

        @Override
        public String toString() {
            return abbreviation;
        }
    }

    /**
     * Returns a {@link Map} of {@link Resource}s (that are part of this
     * {@link Transaction} and their {@link Status}.
     *
     * @return the {@link Map} of {@link Resource}s and their {@link Status}
     */
    Map<Resource, Status> getChangeSet();

    /**
     * Returns the identifier of the {@link Transaction} (unique for each
     * {@link Repository}.
     *
     * @return the identifier of the {@link Transaction}
     */
    String getId();

    /**
     * After {@link Repository#commit(Transaction, String, boolean)} or
     * {@link Repository#rollback(Transaction)} the {@link Transaction} is
     * invalidated.
     *
     * For internal usage only. Use
     * {@link Repository#commit(Transaction, String, boolean)} or
     * {@link Repository#rollback(Transaction)} instead
     */
    void invalidate();

    /**
     * Determines whether the {@link Transaction} can still be used. It cannot
     * be used after {@link Repository#commit(Transaction, String, boolean)} or
     * {@link Repository#rollback(Transaction)} where called.
     *
     * @return {@code true} if the {@link Transaction} can still be used
     *         otherwise {@code false}
     */
    boolean isActive();

    /**
     * Whether there are any {@link Resource}s that are affected by the
     * {@link Transaction}.
     *
     * @return {@code true} if there are no {@link Resource}s affected by the
     *         {@link Transaction}, otherwise {@code
     * false}
     */
    boolean isChangeSetEmpty();

    /**
     * Tell the {@link Transaction} the specified {@link Resource} will be
     * affected during {@link Repository#commit(Transaction, String, boolean)}.
     *
     * For internal usage only. Use the methods from {@link Repository} instead
     *
     * @param resource
     *            {@link Resource} to register to the {@link Transaction}
     * @param status
     *            {@link Status} of the registered {@link Resource}
     *
     * @return {@code true} if the registration modified the change set,
     *         otherwise {@code false}
     *
     * @throws NullPointerException
     *             if any parameter is {@code null}
     */
    boolean register(Resource resource, Status status);
}
