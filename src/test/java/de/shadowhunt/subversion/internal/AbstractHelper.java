/**
 * Copyright (C) 2013 shadowhunt (dev@shadowhunt.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.shadowhunt.subversion.internal;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.UUID;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import de.shadowhunt.http.client.SubversionRequestRetryHandler;
import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.RepositoryFactory;

public abstract class AbstractHelper {

    public static final String PASSWORD = "svnpass";

    public static final String USERNAME_A = "svnuser";

    public static final String USERNAME_B = "svnuser2";

    public static final Charset UTF8_CHARSET = Charset.forName("UTF-8");

    public static InputStream getInputStream(final String s) {
        return new ByteArrayInputStream(s.getBytes(UTF8_CHARSET));
    }

    private final File base;

    private final URI dumpUri;

    private Repository repositoryA, repositoryB;

    private final URI repositoryUri;

    private final File root;

    private final UUID testId;

    protected AbstractHelper(final File base, final URI dumpUri, final URI repositoryUri, final UUID testId) {
        this.base = base;
        this.root = new File(base, "dump");
        this.dumpUri = dumpUri;
        this.repositoryUri = repositoryUri;
        this.testId = testId;
    }

    public File getBase() {
        return base;
    }

    public URI getDumpUri() {
        return dumpUri;
    }

    public HttpClient getHttpClient(final String username) {
        final HttpClientBuilder builder = HttpClientBuilder.create();

        final CredentialsProvider cp = new BasicCredentialsProvider();
        final Credentials credentials = new UsernamePasswordCredentials(username, PASSWORD);
        cp.setCredentials(AuthScope.ANY, credentials);
        builder.setDefaultCredentialsProvider(cp);

        builder.setRetryHandler(new SubversionRequestRetryHandler());
        return builder.build();
    }

    public HttpContext getHttpContext() {
        return new BasicHttpContext();
    }

    public Repository getRepositoryA() {
        if (repositoryA == null) {
            final HttpContext context = getHttpContext();
            final HttpClient client = getHttpClient(USERNAME_A);

            repositoryA = RepositoryFactory.getInstance().createRepository(repositoryUri, client, context);
        }
        return repositoryA;
    }

    public Repository getRepositoryB() {
        if (repositoryB == null) {
            final HttpContext context = getHttpContext();
            final HttpClient client = getHttpClient(USERNAME_B);

            repositoryB = RepositoryFactory.getInstance().createRepository(repositoryUri, client, context);
        }
        return repositoryB;
    }

    public URI getRepositoryUri() {
        return repositoryUri;
    }

    public File getRoot() {
        return root;
    }

    public UUID getTestId() {
        return testId;
    }
}
