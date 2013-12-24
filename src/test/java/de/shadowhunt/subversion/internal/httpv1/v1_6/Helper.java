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
package de.shadowhunt.subversion.internal.httpv1.v1_6;

import java.io.File;
import java.net.URI;
import java.util.UUID;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.RepositoryFactory;

final class Helper {

    private static final File BASE = new File("src/test/resources/dump/v1_6");

    private static final URI DUMP_URI = URI.create("http://subversion.vm.shadowhunt.de/1.6.0/dump.zip");

    private static final String PASSWORD = "svnpass";

    private static final URI REPOSITORY_URI = URI.create("http://subversion.vm.shadowhunt.de/1.6.0/svn-basic/test");

    private static Repository repositoryA, repositoryB;

    private static final File ROOT = new File(BASE, "dump");

    private static final UUID TEST_ID = UUID.randomUUID();

    private static final String USERNAME_A = "svnuser";

    private static final String USERNAME_B = "svnuser2";

    public static File getBase() {
        return BASE;
    }

    public static URI getDumpUri() {
        return DUMP_URI;
    }

    static Repository getRepositoryA() {
        if (repositoryA == null) {
            final DefaultHttpClient client = new DefaultHttpClient();
            final HttpContext context = new BasicHttpContext();

            final CredentialsProvider cp = new BasicCredentialsProvider();
            final Credentials credentials = new UsernamePasswordCredentials(USERNAME_A, PASSWORD);
            cp.setCredentials(AuthScope.ANY, credentials);
            client.setCredentialsProvider(cp);

            repositoryA = RepositoryFactory.getInstance().createRepository(REPOSITORY_URI, client, context);

        }
        return repositoryA;
    }

    static Repository getRepositoryB() {
        if (repositoryB == null) {
            final DefaultHttpClient client = new DefaultHttpClient();
            final HttpContext context = new BasicHttpContext();

            final CredentialsProvider cp = new BasicCredentialsProvider();
            final Credentials credentials = new UsernamePasswordCredentials(USERNAME_B, PASSWORD);
            cp.setCredentials(AuthScope.ANY, credentials);
            client.setCredentialsProvider(cp);

            repositoryB = RepositoryFactory.getInstance().createRepository(REPOSITORY_URI, client, context);

        }
        return repositoryB;
    }

    static File getRoot() {
        return ROOT;
    }

    static UUID getTestId() {
        return TEST_ID;
    }
}
