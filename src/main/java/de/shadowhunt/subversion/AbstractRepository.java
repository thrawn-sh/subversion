/*
 * #%L
 * Shadowhunt Subversion
 * %%
 * Copyright (C) 2013 shadowhunt
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package de.shadowhunt.subversion;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.AuthPolicy;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeSocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DecompressingHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import de.shadowhunt.http.auth.CredentialsUtils;
import de.shadowhunt.http.auth.NtlmSchemeFactory;
import de.shadowhunt.http.client.ThreadLocalCredentialsProvider;
import de.shadowhunt.http.client.WebDavHttpRequestRetryHandler;
import de.shadowhunt.http.conn.ssl.NonValidatingX509TrustManager;
import de.shadowhunt.http.protocol.ThreadLocalHttpContext;
import de.shadowhunt.util.URIUtils;

/**
 * Base for all {@link Repository}
 */
public abstract class AbstractRepository implements Repository {

	@Deprecated
	protected static boolean contains(final int statusCode, final int... expectedStatusCodes) {
		for (final int expectedStatusCode : expectedStatusCodes) {
			if (expectedStatusCode == statusCode) {
				return true;
			}
		}
		return false;
	}

	@Deprecated
	protected static DefaultHttpClient createClient(final int maxConnections, final boolean trustServerCertificate) {
		final PoolingClientConnectionManager connectionManager = new PoolingClientConnectionManager();
		connectionManager.setMaxTotal(maxConnections);
		connectionManager.setDefaultMaxPerRoute(maxConnections);

		if (trustServerCertificate) {
			final Scheme scheme = createTrustingAnySslCertScheme();
			connectionManager.getSchemeRegistry().register(scheme);
		}

		final DefaultHttpClient defaultClient = new DefaultHttpClient(connectionManager);
		defaultClient.setCredentialsProvider(new ThreadLocalCredentialsProvider());
		defaultClient.setHttpRequestRetryHandler(new WebDavHttpRequestRetryHandler());

		final HttpParams params = defaultClient.getParams();
		params.setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, true);

		if (hasJcifsSupport()) {
			defaultClient.getAuthSchemes().register(AuthPolicy.NTLM, NtlmSchemeFactory.INSTANCE);
		}

		return defaultClient;
	}

	@Deprecated
	protected static Scheme createTrustingAnySslCertScheme() {
		try {
			final SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, new TrustManager[] { NonValidatingX509TrustManager.INSTANCE }, new SecureRandom());

			final X509HostnameVerifier verifier = SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
			final SchemeSocketFactory socketFactory = new SSLSocketFactory(sc, verifier);
			return new Scheme("https", 443, socketFactory);
		} catch (final Exception e) {
			throw new SubversionException("could not create ssl scheme", e);
		}
	}

	@Deprecated
	static void ensureResponse(final HttpResponse response, final boolean consume, final int... expectedStatusCodes) throws IOException {
		final int statusCode = response.getStatusLine().getStatusCode();
		if (consume) {
			EntityUtils.consume(response.getEntity());
		}

		if (!contains(statusCode, expectedStatusCodes)) {
			EntityUtils.consume(response.getEntity()); // in case of unexpected status code we consume everything
			throw new SubversionException("status code is: " + statusCode + ", expected was: "
					+ Arrays.toString(expectedStatusCodes));
		}
	}

	@Deprecated
	private static boolean hasJcifsSupport() {
		try {
			Class.forName("jcifs.ntlmssp.NtlmFlags");
			return true;
		} catch (final ClassNotFoundException e) {
			return false;
		}
	}

	protected final AuthScope authscope;

	private final DefaultHttpClient backend;

	public final HttpClient client;

	protected final RepositoryConfig config = new RepositoryConfigHttpV1();

	public final ThreadLocalHttpContext context = new ThreadLocalHttpContext();

	protected final URI repository;

	protected AbstractRepository(final DefaultHttpClient backend, final URI repository) {
		client = new DecompressingHttpClient(backend);
		this.backend = backend;
		this.repository = repository;
		authscope = new AuthScope(repository.getHost(), AuthScope.ANY_PORT);
	}

	protected AbstractRepository(final URI repository, final boolean trustServerCertificate) {
		this(createClient(100, trustServerCertificate), repository);
	}

	protected void contentUpload(final Resource resource, final InfoEntry info, final String uuid, @Nullable final InputStream content) {
		if (content == null) {
			return;
		}

		final Resource r = config.getWorkingResource(uuid).append(resource);
		final UploadOperation uo = new UploadOperation(repository, r, info.getLockToken(), content);
		uo.execute(client, context);
	}

	protected void copy0(final Resource srcResource, final Revision srcRevision, final Resource targetResource, final String uuid) {
		final Resource s = config.getVersionedResource(srcRevision).append(srcResource);
		final Resource t = config.getWorkingResource(uuid).append(targetResource);

		final CopyOperation co = new CopyOperation(repository, s, t);
		co.execute(client, context);
	}

	protected Resource createFolder(final Resource resource, final boolean parent) {
		Resource result = null;
		if (parent) {
			if (Resource.ROOT.equals(resource)) {
				return null;
			}

			result = createFolder(resource.getParent(), parent);
		}

		final CreateFolderOperation cfo = new CreateFolderOperation(repository, resource);
		final boolean created = cfo.execute(client, context);
		if (!created) {
			result = resource;
		}
		return result;
	}

	protected void delete0(final Resource resource, final String uuid) {
		final DeleteOperation o = new DeleteOperation(repository, config.getWorkingResource(uuid).append(resource));
		o.execute(client, context);
	}

	@Override
	public InputStream download(final Resource resource, final Revision revision) {
		final DownloadOperation o = new DownloadOperation(repository, resolve(resource, revision, true));
		return o.execute(client, context);
	}

	@Override
	public URI downloadURI(final Resource resource, final Revision revision) {
		return URIUtils.createURI(repository, resolve(resource, revision, true));
	}

	@Deprecated
	protected HttpResponse execute(final HttpUriRequest request, final boolean consume, final int... expectedStatusCodes) {
		try {
			final HttpResponse response = client.execute(request, context);
			ensureResponse(response, consume, expectedStatusCodes);
			return response;
		} catch (final Exception e) {
			throw new SubversionException("could not execute request (" + request + ")", e);
		} finally {
			// as the path objects can not differ between files and directories
			// each request for an directory (without ending '/') will result
			// in a redirect (with ending '/'), if another call to a redirected
			// URI occurs a CircularRedirectException is thrown, as we can't
			// determine the real target we can't prevent this from happening.
			// Allowing circular redirects globally could lead to live locks on
			// the other hand. Therefore we clear the redirection cache after
			// each completed request cycle
			context.removeAttribute(DefaultRedirectStrategy.REDIRECT_LOCATIONS);
		}
	}

	@Deprecated
	protected HttpResponse execute(final HttpUriRequest request, final int... expectedStatusCodes) {
		return execute(request, true, expectedStatusCodes);
	}

	@Override
	public boolean exists(final Resource resource, final Revision revision) {
		final ExistsOperation o = new ExistsOperation(repository, resolve(resource, revision, false));
		return o.execute(client, context);
	}

	protected Revision getConcreteRevision(final Resource resource, final Revision revision) {
		if (Revision.HEAD.equals(revision)) {
			final InfoEntry info = info(resource, revision, false);
			return info.getRevision();
		}
		return revision;
	}

	@Override
	public InfoEntry info(final Resource resource, final Revision revision, final boolean withCustomProperties) {
		final InfoOperation io = new InfoOperation(repository, resolve(resource, revision, true), Depth.EMPTY, withCustomProperties);
		return io.execute(client, context);
	}

	@Override
	public LogEntry lastLog(final Resource resource) {
		final Revision revision = getConcreteRevision(resource, Revision.HEAD);
		final List<LogEntry> logs = log(resource, revision, revision, 1);
		return logs.get(0);
	}

	protected List<InfoEntry> list(final Resource prefix, final Resource resource, final Depth depth, final boolean withCustomProperties) {
		final Resource r = prefix.append(resource);

		final ListOperation lo = new ListOperation(repository, r, depth, withCustomProperties);
		return lo.execute(client, context);
	}

	protected void listRecursive(final Resource prefix, final boolean withCustomProperties, final Collection<InfoEntry> todo, final Set<InfoEntry> done) {
		for (final InfoEntry info : todo) {
			if (done.contains(info)) {
				continue;
			}

			done.add(info);
			if (info.isDirectory()) {
				final Resource resource = prefix.append(info.getResource());
				final List<InfoEntry> children = list(prefix, resource, Depth.IMMEDIATES, withCustomProperties);
				listRecursive(prefix, withCustomProperties, children, done);
			}
		}
	}

	@Override
	public void lock(final Resource resource, final boolean steal) {
		final LockOperation lo = new LockOperation(repository, resource, steal);
		lo.execute(client, context);
	}

	@Override
	public List<LogEntry> log(final Resource resource, final Revision startRevision, final Revision endRevision, final int limit) {
		final Revision concreteStartRevision = getConcreteRevision(resource, startRevision);
		final Revision concreteEndRevision = getConcreteRevision(resource, endRevision);
		final LogOperation lo = new LogOperation(repository, resource, concreteStartRevision, concreteEndRevision, limit);
		return lo.execute(client, context);
	}

	protected void merge(final InfoEntry info, final String uuid) {
		final Resource resource = config.getTransactionResource(uuid);
		final MergeOperation mo = new MergeOperation(repository, resource, info.getLockToken());
		mo.execute(client, context);
	}

	protected void propertiesRemove(final Resource resource, final InfoEntry info, final String uuid, final ResourceProperty... properties) {
		final ResourceProperty[] filtered = ResourceProperty.filteroutSystemProperties(properties);
		if (filtered.length == 0) {
			return;
		}

		final Resource r = config.getWorkingResource(uuid).append(resource);
		final PropertiesDeleteOperation uo = new PropertiesDeleteOperation(repository, r, info.getLockToken(), filtered);
		uo.execute(client, context);
	}

	protected void propertiesSet(final Resource resource, final InfoEntry info, final String uuid, final ResourceProperty... properties) {
		final ResourceProperty[] filtered = ResourceProperty.filteroutSystemProperties(properties);
		if (filtered.length == 0) {
			return;
		}

		final Resource r = config.getWorkingResource(uuid).append(resource);
		final PropertiesSetOperation uo = new PropertiesSetOperation(repository, r, info.getLockToken(), filtered);
		uo.execute(client, context);
	}

	protected Resource resolve(final Resource resource, final Revision revision, final boolean resolve) {
		if (Revision.HEAD.equals(revision)) {
			return resource;
		}

		final Resource expectedResource = config.getVersionedResource(revision).append(resource);
		if (!resolve) {
			return expectedResource;
		}

		{ // check whether the expectedUri exists
			final ExistsOperation eo = new ExistsOperation(repository, expectedResource);
			if (eo.execute(client, context)) {
				return expectedResource;
			}
		}

		final InfoEntry headInfo = info(resource, Revision.HEAD, false);
		final ResolveOperationV1 ro = new ResolveOperationV1(repository, resource, revision, headInfo.getRevision());
		return ro.execute(client, context);
	}

	@Override
	@Deprecated
	public final void setCredentials(@Nullable final String username, @Nullable final String password, @Nullable final String workstation) {
		final Credentials credentials = CredentialsUtils.creteCredentials(username, password, workstation);
		final CredentialsProvider credentialsProvider = backend.getCredentialsProvider();

		final Credentials oldCredentials = credentialsProvider.getCredentials(authscope);
		if (!ObjectUtils.equals(credentials, oldCredentials)) {
			context.clear();
			credentialsProvider.setCredentials(authscope, credentials);

			triggerAuthentication();
		}
	}

	@Override
	public void setProperties(final Resource resource, final String message, final ResourceProperty... properties) {
		upload0(resource, message, null, properties);
	}

	@Deprecated
	protected final void triggerAuthentication() {
		final HttpOptions request = new HttpOptions(repository);
		request.addHeader("Keep-Alive", "");
		execute(request, HttpStatus.SC_OK);
	}

	@Override
	public void unlock(final Resource resource, final boolean force) {
		final InfoEntry info = info(resource, Revision.HEAD, false);
		final String lockToken = info.getLockToken();
		if (lockToken == null) {
			return;
		}
		final UnlockOperation uo = new UnlockOperation(repository, resource, lockToken, force);
		uo.execute(client, context);
	}

	@Override
	public void upload(final Resource resource, final String message, final InputStream content, final ResourceProperty... properties) {
		if (content == null) {
			throw new IllegalArgumentException("content can not be null");
		}
		upload0(resource, message, content, properties);
	}

	protected abstract void upload0(final Resource resource, final String message, @Nullable final InputStream content, final ResourceProperty... properties);
}
