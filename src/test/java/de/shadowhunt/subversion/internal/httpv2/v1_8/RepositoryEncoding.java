package de.shadowhunt.subversion.internal.httpv2.v1_8;

import de.shadowhunt.subversion.internal.AbstractRepositoryEncoding;

public class RepositoryEncoding extends AbstractRepositoryEncoding {

    private static final Helper HELPER = new Helper();

    public RepositoryEncoding() {
        super(HELPER.getRepositoryA(), HELPER.getTestId(), HELPER.getRoot());
    }
}
