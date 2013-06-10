package de.shadowhunt.subversion;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthProtocolState;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.AuthPolicy;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeSocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DecompressingHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.util.EntityUtils;

import de.shadowhunt.http.auth.CredentialsUtils;
import de.shadowhunt.http.auth.NtlmSchemeFactory;
import de.shadowhunt.http.client.ThreadLocalCredentialsProvider;
import de.shadowhunt.http.conn.ssl.NonValidatingX509TrustManager;
import de.shadowhunt.http.protocol.ThreadLocalHttpContext;

/**
 * Base for all {@link Repository}
 * @param <T> {@link AbstractRequestFactory} that will be used to create request to the subversion server
 */
public abstract class AbstractRepository<T extends AbstractRequestFactory> implements Repository {

	protected static final String LOCK_OWNER_HEADER = "X-SVN-Lock-Owner";

	protected static boolean contains(final int statusCode, final int... expectedStatusCodes) {
		if (expectedStatusCodes.length == 0) {
			return true;
		}

		for (final int expectedStatusCode : expectedStatusCodes) {
			if (expectedStatusCode == statusCode) {
				return true;
			}
		}
		return false;
	}

	protected static DefaultHttpClient createClient(final int maxConnections, final boolean trustServerCertificat) {
		final PoolingClientConnectionManager connectionManager = new PoolingClientConnectionManager();
		connectionManager.setMaxTotal(maxConnections);
		connectionManager.setDefaultMaxPerRoute(maxConnections);

		if (trustServerCertificat) {
			final Scheme scheme = createTrustingAnySslCertScheme();
			connectionManager.getSchemeRegistry().register(scheme);
		}

		final DefaultHttpClient defaultClient = new DefaultHttpClient(connectionManager);
		defaultClient.setCredentialsProvider(new ThreadLocalCredentialsProvider());

		if (hasJcifsSupport()) {
			defaultClient.getAuthSchemes().register(AuthPolicy.NTLM, NtlmSchemeFactory.INSTANCE);
		}

		return defaultClient;
	}

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

	protected static InputStream getContent(final HttpResponse response) {
		try {
			return response.getEntity().getContent();
		} catch (final Exception e) {
			throw new SubversionException("could not retrieve content stream", e);
		}
	}

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

	protected final HttpClient client;

	private final ThreadLocalHttpContext context = new ThreadLocalHttpContext();

	protected final URI repository;

	protected final T requestFactory;

	protected AbstractRepository(final DefaultHttpClient backend, final URI repository, final T requestFactory) {
		this.client = new DecompressingHttpClient(backend);
		this.backend = backend;
		this.repository = repository;
		this.requestFactory = requestFactory;
		this.authscope = new AuthScope(repository.getHost(), AuthScope.ANY_PORT);
	}

	protected AbstractRepository(final URI repository, final boolean trustServerCertificat, final T requestFactory) {
		this(createClient(100, trustServerCertificat), repository, requestFactory);
	}

	protected Path createMissingFolders(final String prefix, final String uuid, final Path resource) {
		final String[] resourceParts = resource.getValue().split("/");

		String infoResource = "/";
		final StringBuilder partial = new StringBuilder();
		for (int i = 1; i < (resourceParts.length - 1); i++) {
			partial.append('/');
			partial.append(resourceParts[i]);

			final String partialResource = partial.toString();
			final URI uri = URI.create(repository + prefix + uuid + partialResource);
			final HttpUriRequest request = requestFactory.createMakeFolderRequest(uri);
			final HttpResponse response = execute(request, /* created */HttpStatus.SC_CREATED, /* existed */
					HttpStatus.SC_METHOD_NOT_ALLOWED);
			final int status = response.getStatusLine().getStatusCode();
			if (status == HttpStatus.SC_METHOD_NOT_ALLOWED) {
				infoResource = partialResource;
			}
		}

		return Path.create(infoResource);
	}

	@Override
	public InputStream download(final Path resource, final Revision revision) {
		final URI uri = downloadURI(resource, revision);

		final HttpUriRequest request = requestFactory.createDownloadRequest(uri);
		final HttpResponse response = execute(request, false, HttpStatus.SC_OK);
		return getContent(response);
	}

	protected HttpResponse execute(final HttpUriRequest request, final boolean consume, final int... expectedStatusCodes) {
		try {
			client.execute(request);
			final HttpResponse response = client.execute(request, context);
			ensureResponse(response, consume, expectedStatusCodes);
			return response;
		} catch (final Exception e) {
			throw new SubversionException("could not execute request (" + request + ")", e);
		}
	}

	protected HttpResponse execute(final HttpUriRequest request, final int... expectedStatusCodes) {
		return execute(request, true, expectedStatusCodes);
	}

	@Override
	public boolean exists(final Path resource, final Revision revision) {
		final URI uri = downloadURI(resource, revision);

		final HttpUriRequest request = requestFactory.createExistsRequest(uri);
		final HttpResponse response = execute(request, /* found */HttpStatus.SC_OK, /* not found */HttpStatus.SC_NOT_FOUND);
		return (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK);
	}

	protected Revision getConcreateRevision(final Path resource, final Revision revision) {
		if (Revision.HEAD.equals(revision)) {
			final InfoEntry info = info(resource, revision, false);
			return info.getRevision();
		}
		return revision;
	}

	@Override
	public InfoEntry info(final Path resource, final Revision revision, final boolean withCustomProperties) {
		final URI uri = downloadURI(resource, revision);

		final HttpUriRequest request = requestFactory.createInfoRequest(uri, Depth.EMPTY);
		final HttpResponse response = execute(request, false, HttpStatus.SC_MULTI_STATUS);

		final InputStream in = getContent(response);
		try {
			final InfoEntry info = InfoEntry.read(in, withCustomProperties);
			if (info.isLocked()) {
				final Header header = response.getFirstHeader(LOCK_OWNER_HEADER);
				info.setLockOwner(header.getValue());
			}
			return info;
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	protected boolean isAuthenticated() {
		final AuthState authState = (AuthState) context.getAttribute(ClientContext.TARGET_AUTH_STATE);
		if (authState != null) {
			return authState.getState() == AuthProtocolState.SUCCESS;
		}
		return false;
	}

	@Override
	public LogEntry lastLog(final Path resource) {
		final InfoEntry info = info(resource, Revision.HEAD, false);
		final Revision revision = info.getRevision();
		final List<LogEntry> logs = log(resource, revision, revision);
		if (logs.isEmpty()) {
			throw new SubversionException("no logs available");
		}
		return logs.get(0);
	}

	protected List<InfoEntry> list(final String uriPrefix, final Path resource, final Depth depth, final boolean withCustomProperties) {
		final URI uri = URI.create(uriPrefix + resource);

		if (depth == Depth.INFINITY) {
			final List<InfoEntry> root = list(uri, Depth.IMMEDIATES, withCustomProperties);
			final Set<InfoEntry> result = new TreeSet<InfoEntry>(InfoEntry.PATH_COMPARATOR);
			listRecursive(uriPrefix, withCustomProperties, root, result);
			return new ArrayList<InfoEntry>(result);
		}
		return list(uri, depth, withCustomProperties);
	}

	protected List<InfoEntry> list(final URI uri, final Depth depth, final boolean withCustomProperties) {
		final HttpUriRequest request = requestFactory.createInfoRequest(uri, depth);
		final HttpResponse response = execute(request, false, HttpStatus.SC_MULTI_STATUS);

		final InputStream in = getContent(response);
		try {
			return InfoEntry.readList(in, withCustomProperties, (Depth.FILES != depth));
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	protected void listRecursive(final String uriPrefix, final boolean withCustomProperties, final Collection<InfoEntry> todo, final Set<InfoEntry> done) {
		for (final InfoEntry info : todo) {
			if (done.contains(info)) {
				continue;
			}

			done.add(info);
			if (info.isDirectory()) {
				final Path path = info.getPath();
				final URI uri = URI.create(uriPrefix + path);
				final List<InfoEntry> children = list(uri, Depth.IMMEDIATES, withCustomProperties);
				listRecursive(uriPrefix, withCustomProperties, children, done);
			}
		}
	}

	@Override
	public void lock(final Path resource) {
		final URI uri = URI.create(repository + resource.getValue());

		final HttpUriRequest request = requestFactory.createLockRequest(uri);
		execute(request, HttpStatus.SC_OK);
	}

	@Override
	public List<LogEntry> log(final Path resource, final Revision startRevision, final Revision endRevision) {
		final URI uri = URI.create(repository + resource.getValue());

		final Revision concreateStartRevision = getConcreateRevision(resource, startRevision);
		final Revision concreateEndRevision = getConcreateRevision(resource, endRevision);
		final HttpUriRequest request = requestFactory.createLogRequest(uri, concreateStartRevision, concreateEndRevision);
		final HttpResponse response = execute(request, false, HttpStatus.SC_OK);

		final InputStream in = getContent(response);
		try {
			return LogEntry.read(in);
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	@Override
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
	public void setProperties(final Path resource, final String message, final ResourceProperty... properties) {
		upload0(resource, message, null, properties);
	}

	protected final void triggerAuthentication() {
		final HttpUriRequest request = requestFactory.createAuthRequest(repository);
		execute(request, HttpStatus.SC_OK);
	}

	@Override
	public void unlock(final Path resource) {
		final InfoEntry info = info(resource, Revision.HEAD, false);
		final String lockToken = info.getLockToken();
		if (lockToken == null) {
			return;
		}
		final URI uri = URI.create(repository + resource.getValue());

		final HttpUriRequest request = requestFactory.createUnlockRequest(uri, lockToken);
		execute(request, HttpStatus.SC_NO_CONTENT);
	}

	@Override
	public void upload(final Path resource, final String message, final InputStream content, final ResourceProperty... properties) {
		if (content == null) {
			throw new IllegalArgumentException("content can not be null");
		}
		upload0(resource, message, content, properties);
	}

	protected abstract void upload0(final Path resource, final String message, @Nullable final InputStream content, final ResourceProperty... properties);
}
