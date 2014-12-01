package de.shadowhunt.subversion.internal.httpv1.v1_6;

import de.shadowhunt.subversion.internal.AbstractRepositoryEncoding;

public class RepositoryEncoding extends AbstractRepositoryEncoding {

    private static final Helper HELPER = new Helper();

    public RepositoryEncoding() {
        super(HELPER.getRepositoryA(), HELPER.getTestId(), HELPER.getRoot());
    }
}
