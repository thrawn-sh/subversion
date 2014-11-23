package de.shadowhunt.subversion.internal.httpv2.v1_8;

import de.shadowhunt.subversion.internal.AbstractRepositoryPerformance;

public class RepositoryPerformance extends AbstractRepositoryPerformance {

    private static final Helper HELPER = new Helper();

    private static final CountingHttpRequestInterceptor INTERCEPTOR = new CountingHttpRequestInterceptor();

    public RepositoryPerformance() {
        super(HELPER.getRepositoryA(INTERCEPTOR), INTERCEPTOR, HELPER.getTestId());
    }
}
