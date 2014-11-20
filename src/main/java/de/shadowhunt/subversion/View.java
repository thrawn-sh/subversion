package de.shadowhunt.subversion;

import java.util.UUID;

public interface View {

    /**
     * Returns the {@link UUID} of the {@link Repository} this {@link View} belongs to
     *
     * @return the {@link UUID} of the {@link Repository}
     */
    UUID getRepositoryId();

    Revision getHeadRevision();
}
