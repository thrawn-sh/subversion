package de.shadowhunt.subversion;

import de.shadowhunt.http.client.SubversionRequestRetryHandler;
import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.RepositoryFactory;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.BasicHttpContext;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by ade on 25.11.14.
 */
public class Test {

    public static void main(String... args) throws URISyntaxException {
        final HttpClientBuilder builder = HttpClientBuilder.create();

        final CredentialsProvider cp = new BasicCredentialsProvider();
        final Credentials credentials = new UsernamePasswordCredentials("svnuser", "svnpass");
        cp.setCredentials(AuthScope.ANY, credentials);
        builder.setDefaultCredentialsProvider(cp);

        builder.setRetryHandler(new SubversionRequestRetryHandler());
        CloseableHttpClient client = builder.build();

        BasicHttpContext context = new BasicHttpContext();

        URIBuilder urib = new URIBuilder();
        urib.setScheme("http");
        urib.setHost("subversion.vm.shadowhunt.de");
        urib.setPath("/1.6.0/svn-basic/test/tags/manual test/üöä");

        RepositoryFactory rf = RepositoryFactory.getInstance();
        Repository r = rf.probeRepository(urib.build(), client, context);
        System.out.println(r.getBaseUri());

        Resource prefix = Resource.create("/tags/manual test/üöä");
        Resource file = prefix.append(Resource.create("/test.txt"));
        if (!r.exists(file, Revision.HEAD)) {
            Transaction transaction = r.createTransaction();
            try {
                r.add(transaction, file, true, new ByteArrayInputStream("test".getBytes()));
                r.commit(transaction, "commit");
            } catch (RuntimeException re) {
                r.rollback(transaction);
            }
        }

        r.lock(file, true);
        Info b = r.info(file, Revision.HEAD);
        if (!b.isLocked()) {
            throw new RuntimeException("before: must be locked");
        }

        Transaction transaction = r.createTransaction();
        try {
            r.add(transaction, file, true, new ByteArrayInputStream("test".getBytes()));
            r.commit(transaction, "commit2");
        } catch (RuntimeException re) {
            r.rollback(transaction);
        }

        Info a = r.info(file, Revision.HEAD);
        if (a.isLocked()) {
            throw new RuntimeException("after: must not be locked");
        }
    }
}
