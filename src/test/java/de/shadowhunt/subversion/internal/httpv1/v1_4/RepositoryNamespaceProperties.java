package de.shadowhunt.subversion.internal.httpv1.v1_4;

import de.shadowhunt.subversion.internal.AbstractRepositoryNamespaceProperties;

public class RepositoryNamespaceProperties extends AbstractRepositoryNamespaceProperties {

    private static final Helper HELPER = new Helper();

    public RepositoryNamespaceProperties() {
        super(HELPER.getRepositoryA(), HELPER.getRoot(), HELPER.getTestId());
    }
}
