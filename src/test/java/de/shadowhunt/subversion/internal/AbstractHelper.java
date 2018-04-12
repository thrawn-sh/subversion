/**
 * Copyright © 2013-2018 shadowhunt (dev@shadowhunt.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.shadowhunt.subversion.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import de.shadowhunt.http.client.SubversionRequestRetryHandler;
import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.RepositoryFactory;
import de.shadowhunt.subversion.Resource;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

public abstract class AbstractHelper {

    public static final Resource BASE_PATH = Resource.create("/trunk");

    public static final Resource DEEP_PATH = Resource.create("/trunk/00000000-0000-0000-0000-000000000000/exists/");

    public static final String PASSWORD = "svnpass";

    private static final UUID testId = UUID.randomUUID();

    public static final String USERNAME_A = "svnuser";

    public static final String USERNAME_B = "svnuser2";

    private static URI concat(final URI base, final Resource resource) {
        final URIBuilder builder = new URIBuilder(base);
        final String path = base.getPath() + resource.getValue();
        builder.setPath(path);
        final String uri = builder.toString();
        return URI.create(uri);
    }

    private final File base;

    private final URI dumpUri;

    private final URI md5Uri;

    private Repository repositoryA;

    private Repository repositoryB;

    private final URI repositoryBaseUri;

    private Repository repositoryDeep;

    private final URI repositoryDeepBaseUri;

    private final URI repositoryDeepUri;

    private Repository repositoryPath;

    private final URI repositoryPathBaseUri;

    private final URI repositoryPathUri;

    private Repository repositoryReadOnly;

    private final URI repositoryReadOnlyBaseUri;

    private final URI repositoryReadOnlyUri;

    private final URI repositoryUri;

    private final File root;

    protected AbstractHelper(final File base, final String protocol, final String host) {
        this.base = base;
        root = new File(base, "dump");
        dumpUri = URI.create(protocol + "://" + host + "/dump.zip");
        md5Uri = URI.create(protocol + "://" + host + "/dump.zip.md5");

        repositoryBaseUri = URI.create(protocol + "://" + host + "/svn-basic/test");
        repositoryUri = concat(repositoryBaseUri, BASE_PATH);

        repositoryReadOnlyBaseUri = URI.create(protocol + "://" + host + "/svn-non/test");
        repositoryReadOnlyUri = concat(repositoryReadOnlyBaseUri, BASE_PATH);

        repositoryDeepBaseUri = URI.create(protocol + "://" + host + "/svn-non/test");
        repositoryDeepUri = concat(repositoryDeepBaseUri, DEEP_PATH);

        repositoryPathBaseUri = URI.create(protocol + "://" + host + "/svn-path/test");
        repositoryPathUri = concat(repositoryPathBaseUri, BASE_PATH);
    }

    private String calculateMd5(final File zip) throws IOException {
        try (final InputStream is = new FileInputStream(zip)) {
            return DigestUtils.md5Hex(is);
        }
    }

    private String copyUrlToString(final URL source) throws IOException {
        try (final InputStream is = source.openStream()) {
            return IOUtils.toString(is, StandardCharsets.UTF_8);
        }
    }

    private void extractArchive(final File zip, final File prefix) throws Exception {
        try (final ZipFile zipFile = new ZipFile(zip)) {
            final Enumeration<? extends ZipEntry> enu = zipFile.entries();
            while (enu.hasMoreElements()) {
                final ZipEntry zipEntry = enu.nextElement();

                final String name = zipEntry.getName();

                final File file = new File(prefix, name);
                if (name.charAt(name.length() - 1) == Resource.SEPARATOR_CHAR) {
                    if (!file.isDirectory() && !file.mkdirs()) {
                        throw new IOException("can not create directory structure: " + file);
                    }
                    continue;
                }

                final File parent = file.getParentFile();
                if (parent != null) {
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("can not create directory structure: " + parent);
                    }
                }

                try (final InputStream is = zipFile.getInputStream(zipEntry)) {
                    try (final OutputStream os = new FileOutputStream(file)) {
                        IOUtils.copy(is, os);
                    }
                }
            }
        }
    }

    public HttpClient getHttpClient(final String username, final HttpRequestInterceptor... interceptors) {
        final HttpClientBuilder builder = HttpClientBuilder.create();

        if (StringUtils.isNotBlank(username)) {
            final CredentialsProvider cp = new BasicCredentialsProvider();
            final Credentials credentials = new UsernamePasswordCredentials(username, PASSWORD);
            cp.setCredentials(AuthScope.ANY, credentials);
            builder.setDefaultCredentialsProvider(cp);
        }

        for (final HttpRequestInterceptor interceptor : interceptors) {
            builder.addInterceptorFirst(interceptor);
        }

        builder.setRetryHandler(new SubversionRequestRetryHandler());
        return builder.build();
    }

    public HttpContext getHttpContext() {
        return new BasicHttpContext();
    }

    public Repository getRepositoryA(final HttpRequestInterceptor... interceptors) {
        if (repositoryA == null) {
            final HttpContext context = getHttpContext();
            final HttpClient client = getHttpClient(USERNAME_A, interceptors);

            final RepositoryFactory factory = RepositoryFactory.getInstance();
            repositoryA = factory.createRepository(repositoryUri, client, context, true);
        }
        return repositoryA;
    }

    public Repository getRepositoryB(final HttpRequestInterceptor... interceptors) {
        if (repositoryB == null) {
            final HttpContext context = getHttpContext();
            final HttpClient client = getHttpClient(USERNAME_B, interceptors);

            final RepositoryFactory factory = RepositoryFactory.getInstance();
            repositoryB = factory.createRepository(repositoryUri, client, context, true);
        }
        return repositoryB;
    }

    public URI getRepositoryBaseUri() {
        return repositoryBaseUri;
    }

    public URI getRepositoryDeepBaseUri() {
        return repositoryDeepBaseUri;
    }

    public Repository getRepositoryDeepPath(final HttpRequestInterceptor... interceptors) {
        if (repositoryDeep == null) {
            final HttpContext context = getHttpContext();
            final HttpClient client = getHttpClient(null, interceptors);

            final RepositoryFactory factory = RepositoryFactory.getInstance();
            repositoryDeep = factory.createRepository(repositoryDeepUri, client, context, false);
        }
        return repositoryDeep;
    }

    public URI getRepositoryDeepUri() {
        return repositoryDeepUri;
    }

    public Repository getRepositoryPath(final HttpRequestInterceptor... interceptors) {
        if (repositoryPath == null) {
            final HttpContext context = getHttpContext();
            final HttpClient client = getHttpClient(USERNAME_A, interceptors);

            final RepositoryFactory factory = RepositoryFactory.getInstance();
            repositoryPath = factory.createRepository(repositoryPathUri, client, context, true);
        }
        return repositoryPath;
    }

    public URI getRepositoryPathBaseUri() {
        return repositoryPathBaseUri;
    }

    public Repository getRepositoryReadOnly(final HttpRequestInterceptor... interceptors) {
        if (repositoryReadOnly == null) {
            final HttpContext context = getHttpContext();
            final HttpClient client = getHttpClient(null, interceptors);

            final RepositoryFactory factory = RepositoryFactory.getInstance();
            repositoryReadOnly = factory.createRepository(repositoryReadOnlyUri, client, context, false);
        }
        return repositoryReadOnly;
    }

    public URI getRepositoryReadOnlyBaseUri() {
        return repositoryReadOnlyBaseUri;
    }

    public URI getRepositoryReadOnlyUri() {
        return repositoryReadOnlyUri;
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

    public void pullCurrentDumpData() throws Exception {
        final File zip = new File(base, "dump.zip");
        if (zip.exists()) {
            final String localMD5 = calculateMd5(zip);
            final String remoteMD5 = copyUrlToString(md5Uri.toURL());
            if (localMD5.equals(remoteMD5)) {
                return;
            }
        }
        FileUtils.deleteQuietly(base);

        base.mkdirs();
        FileUtils.copyURLToFile(dumpUri.toURL(), zip);
        extractArchive(zip, base);
    }
}
