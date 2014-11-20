package de.shadowhunt.subversion.internal;

import java.util.UUID;

import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.View;

class ViewImpl implements View {

    private final Revision headRevision;

    private final UUID repositoryId;

    public ViewImpl(final UUID repositoryId, final Revision headRevision) {
        this.repositoryId = repositoryId;
        this.headRevision = headRevision;
    }

    @Override
    public Revision getHeadRevision() {
        return headRevision;
    }

    @Override
    public UUID getRepositoryId() {
        return repositoryId;
    }
}
