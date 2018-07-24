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

import java.util.Map;

/**
 * {@link Transaction} allows the application to define units of work, it can be created via {@link Repository#createTransaction()}.
 */
public interface Transaction extends View {

    /**
     * Defines the status of each {@link Resource} that is part of the current active {@link Transaction}.
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
     * Returns a {@link Map} of {@link Resource}s (that are part of this {@link Transaction} and their {@link Status}.
     *
     * @return the {@link Map} of {@link Resource}s and their {@link Status}
     */
    Map<Resource, Status> getChangeSet();

    /**
     * Returns the identifier of the {@link Transaction} (unique for each {@link Repository}.
     *
     * @return the identifier of the {@link Transaction}
     */
    String getId();

    /**
     * After {@link Repository#commit(Transaction, String, boolean)} or {@link Repository#rollback(Transaction)} the {@link Transaction} is invalidated.
     *
     * For internal usage only. Use {@link Repository#commit(Transaction, String, boolean)} or {@link Repository#rollback(Transaction)} instead
     */
    void invalidate();

    /**
     * Determines whether the {@link Transaction} can still be used. It cannot be used after {@link Repository#commit(Transaction, String, boolean)} or {@link Repository#rollback(Transaction)} where called.
     *
     * @return {@code true} if the {@link Transaction} can still be used otherwise {@code false}
     */
    boolean isActive();

    /**
     * Whether there are any {@link Resource}s that are affected by the {@link Transaction}.
     *
     * @return {@code true} if there are no {@link Resource}s affected by the {@link Transaction}, otherwise {@code
     * false}
     */
    boolean isChangeSetEmpty();

    /**
     * Tell the {@link Transaction} the specified {@link Resource} will be affected during {@link Repository#commit(Transaction, String, boolean)}.
     *
     * For internal usage only. Use the methods from {@link Repository} instead
     *
     * @param resource {@link Resource} to register to the {@link Transaction}
     * @param status {@link Status} of the registered {@link Resource}
     *
     * @return {@code true} if the registration modified the change set, otherwise {@code false}
     *
     * @throws NullPointerException if any parameter is {@code null}
     */
    boolean register(Resource resource, Status status);
}
