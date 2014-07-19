package de.shadowhunt.subversion.internal.httpv2.v1_8;

import de.shadowhunt.subversion.internal.AbstractHelper;
import de.shadowhunt.subversion.internal.AbstractRepositoryFactory;

public class RepositoryFactory extends AbstractRepositoryFactory {

    private static final Helper HELPER = new Helper();

    public RepositoryFactory() {
        super(HELPER.getRepositoryA(), HELPER.getHttpClient(AbstractHelper.USERNAME_A), HELPER.getHttpContext());
    }
}
