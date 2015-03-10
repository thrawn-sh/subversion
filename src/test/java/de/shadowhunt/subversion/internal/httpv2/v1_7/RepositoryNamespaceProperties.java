package de.shadowhunt.subversion.internal.httpv2.v1_7;

import de.shadowhunt.subversion.internal.AbstractRepositoryNamespaceProperties;

public class RepositoryNamespaceProperties extends AbstractRepositoryNamespaceProperties {

    private static final Helper HELPER = new Helper();

    public RepositoryNamespaceProperties() {
        super(HELPER.getRepositoryA(), HELPER.getRoot(), HELPER.getTestId());
    }
}
