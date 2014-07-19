package de.shadowhunt.subversion.internal.httpv1.v1_4;

import de.shadowhunt.subversion.internal.AbstractHelper;
import de.shadowhunt.subversion.internal.AbstractRepositoryFactory;

public class RepositoryFactory extends AbstractRepositoryFactory {

    private static final Helper HELPER = new Helper();

    public RepositoryFactory() {
        super(HELPER.getRepositoryA(), HELPER.getHttpClient(AbstractHelper.USERNAME_A), HELPER.getHttpContext());
    }
}
