package de.shadowhunt.scm.subversion;

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

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthProtocolState;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
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

import de.shadowhunt.http.auth.NtlmSchemeFactory;
import de.shadowhunt.http.client.ThreadLocalCredentialsProvider;
import de.shadowhunt.http.conn.ssl.NonValidatingX509TrustManager;
import de.shadowhunt.http.protocol.ThreadLocalHttpContext;

/**
 * Base for all {@link SubversionRepository}
 * @param <T> {@link AbstractSubversionRequestFactory} that will be used to create request to the subversion server
 */
public abstract class AbstractSubversionRepository<T extends AbstractSubversionRequestFactory> implements SubversionRepository {

	protected static final int HEAD_VERSION = -1;

	protected static final int INITIAL_VERSION = 0;

	protected static final String LOCK_OWNER_HEADER = "X-SVN-Lock-Owner";

	protected static boolean contains(final int statusCode, @Nullable final int... expectedStatusCodes) {
		if ((expectedStatusCodes == null) || (expectedStatusCodes.length == 0)) {
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

	@CheckForNull
	protected static Credentials creteCredentials(@Nullable final String user, @Nullable final String password, @Nullable final String workstation) {
		if (user == null) {
			return null;
		}

		final String username;
		final String domain;
		final int index = user.indexOf('\\');
		if (index >= 0) {
			username = user.substring(index + 1);
			domain = user.substring(0, index);
		} else {
			username = user;
			domain = "";
		}
		return new NTCredentials(username, password, workstation, domain);
	}

	static void ensureResponse(final HttpResponse response, final boolean consume, @Nullable final int... expectedStatusCodes) throws IOException {
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

	protected static String normalizeResource(final String resource) {
		if (StringUtils.isEmpty(resource) || "/".equals(resource)) {
			return "/";
		}

		final StringBuilder normalizedResource = new StringBuilder();
		for (final String part : resource.split("/")) {
			if (!StringUtils.isEmpty(part)) {
				normalizedResource.append('/');
				normalizedResource.append(part);
			}
		}
		return normalizedResource.toString();
	}

	protected final AuthScope authscope;

	private final DefaultHttpClient backend;

	protected final HttpClient client;

	private final ThreadLocalHttpContext context = new ThreadLocalHttpContext();

	protected final URI repository;

	protected final T requestFactory;

	protected AbstractSubversionRepository(final DefaultHttpClient backend, final URI repository, final T requestFactory) {
		this.client = new DecompressingHttpClient(backend);
		this.backend = backend;
		this.repository = repository;
		this.requestFactory = requestFactory;
		this.authscope = new AuthScope(repository.getHost(), AuthScope.ANY_PORT);
	}

	protected AbstractSubversionRepository(final URI repository, final boolean trustServerCertificat, final T requestFactory) {
		this(createClient(100, trustServerCertificat), repository, requestFactory);
	}

	protected void closeQuiet(final InputStream in) {
		try {
			in.close();
		} catch (final IOException e) {
			// ignore
		}
	}

	protected String createMissingFolders(final String prefix, final String uuid, final String normalizedResource) {
		final String[] resourceParts = normalizedResource.split("/");

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

		return infoResource;
	}

	@Override
	public abstract void delete(final String resource, final String message);

	@Override
	public abstract void deleteProperties(final String resource, final String message, final SubversionProperty... properties);

	@Override
	public InputStream download(final String resource) {
		return download0(normalizeResource(resource), HEAD_VERSION);
	}

	@Override
	public InputStream download(final String resource, final int revision) {
		if (revision <= HEAD_VERSION) {
			throw new IllegalArgumentException("revision must be greater than 0, was:" + revision);
		}
		return download0(normalizeResource(resource), revision);
	}

	protected abstract InputStream download0(final String normalizeResource, final int revision);

	@Override
	public URI downloadURI(final String resource) {
		return downloadURI0(normalizeResource(resource), HEAD_VERSION);
	}

	@Override
	public URI downloadURI(final String resource, final int revision) {
		if (revision <= HEAD_VERSION) {
			throw new IllegalArgumentException("revision must be greater than 0, was:" + revision);
		}
		return downloadURI0(normalizeResource(resource), revision);
	}

	protected abstract URI downloadURI0(String normalizedResource, int revision);

	protected HttpResponse execute(final HttpUriRequest request, final boolean consume, @Nullable final int... expectedStatusCodes) {
		try {
			final HttpResponse response = client.execute(request, context);
			ensureResponse(response, consume, expectedStatusCodes);
			return response;
		} catch (final Exception e) {
			throw new SubversionException("could not execute request (" + request + ")", e);
		}
	}

	protected HttpResponse execute(final HttpUriRequest request, @Nullable final int... expectedStatusCodes) {
		return execute(request, true, expectedStatusCodes);
	}

	@Override
	public boolean exists(final String resource) {
		return exists0(normalizeResource(resource));
	}

	protected boolean exists0(final String normalizedResource) {
		final URI uri = URI.create(repository + normalizedResource);

		final HttpUriRequest request = requestFactory.createExistsRequest(uri);
		final HttpResponse response = execute(request, /* found */HttpStatus.SC_OK, /* not found */HttpStatus.SC_NOT_FOUND);
		return (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK);
	}

	@Override
	public SubversionInfo info(final String resource, final boolean withCustomProperties) {
		return info0(normalizeResource(resource), HEAD_VERSION, withCustomProperties);
	}

	@Override
	public SubversionInfo info(final String resource, final int revision, final boolean withCustomProperties) {
		if (revision <= HEAD_VERSION) {
			throw new IllegalArgumentException("revision must be greater than 0, was:" + revision);
		}
		return info0(normalizeResource(resource), revision, withCustomProperties);
	}

	protected SubversionInfo info0(final String normalizedResource, final int revision, final boolean withCustomProperties) {
		final URI uri = downloadURI0(normalizedResource, revision);

		final HttpUriRequest request = requestFactory.createInfoRequest(uri, Depth.EMPTY);
		final HttpResponse response = execute(request, false, HttpStatus.SC_MULTI_STATUS);

		final InputStream in = getContent(response);
		try {
			final SubversionInfo info = SubversionInfo.read(in, withCustomProperties);
			if (info.isLocked()) {
				final Header header = response.getFirstHeader(LOCK_OWNER_HEADER);
				info.setLockOwner(header.getValue());
			}
			return info;
		} finally {
			closeQuiet(in);
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
	public SubversionLog lastLog(final String resource) {
		final String normalizedResource = normalizeResource(resource);
		final URI uri = URI.create(repository + normalizedResource);

		final SubversionInfo info = info0(normalizedResource, HEAD_VERSION, false);
		final int revision = info.getRevision();
		final List<SubversionLog> logs = log0(uri, revision, revision);
		if (logs.isEmpty()) {
			throw new SubversionException("no logs available");
		}
		return logs.get(0);
	}

	protected List<SubversionInfo> list(final String uriPrefix, final String normalizedResource, final Depth depth, final boolean withCustomProperties) {
		final URI uri = URI.create(uriPrefix + normalizedResource);

		if (depth == Depth.INFINITY) {
			final List<SubversionInfo> root = list(uri, Depth.IMMEDIATES, withCustomProperties);
			final Set<SubversionInfo> result = new TreeSet<SubversionInfo>(SubversionInfo.PATH_COMPARATOR);
			listRecursive(uriPrefix, withCustomProperties, root, result);
			return new ArrayList<SubversionInfo>(result);
		}
		return list(uri, depth, withCustomProperties);
	}

	protected List<SubversionInfo> list(final URI uri, final Depth depth, final boolean withCustomProperties) {
		final HttpUriRequest request = requestFactory.createInfoRequest(uri, depth);
		final HttpResponse response = execute(request, false, HttpStatus.SC_MULTI_STATUS);

		final InputStream in = getContent(response);
		try {
			return SubversionInfo.readList(in, withCustomProperties, (Depth.FILES != depth));
		} finally {
			closeQuiet(in);
		}
	}

	protected void listRecursive(final String uriPrefix, final boolean withCustomProperties, final Collection<SubversionInfo> todo, final Set<SubversionInfo> done) {
		for (final SubversionInfo info : todo) {
			if (done.contains(info)) {
				continue;
			}

			done.add(info);
			if (info.isDirectory()) {
				final String path = info.getRelativePath();
				final URI uri = URI.create(uriPrefix + "/" + path);
				final List<SubversionInfo> children = list(uri, Depth.IMMEDIATES, withCustomProperties);
				listRecursive(uriPrefix, withCustomProperties, children, done);
			}
		}
	}

	@Override
	public void lock(final String resource) {
		final URI uri = URI.create(repository + normalizeResource(resource));

		final HttpUriRequest request = requestFactory.createLockRequest(uri);
		execute(request, HttpStatus.SC_OK);
	}

	@Override
	public List<SubversionLog> log(final String resource) {
		final String normalizedResource = normalizeResource(resource);
		final URI uri = URI.create(repository + normalizedResource);

		final SubversionInfo info = info0(normalizedResource, HEAD_VERSION, false);
		return log0(uri, info.getRevision(), INITIAL_VERSION);
	}

	@Override
	public List<SubversionLog> log(final String resource, final int startRevision, final int endRevision) {
		final String normalizedResource = normalizeResource(resource);
		final URI uri = URI.create(repository + normalizedResource);

		return log0(uri, startRevision, endRevision);
	}

	protected List<SubversionLog> log0(final URI uri, final int startRevision, final int endRevision) {
		final HttpUriRequest request = requestFactory.createLogRequest(uri, startRevision, endRevision);
		final HttpResponse response = execute(request, false, HttpStatus.SC_OK);

		final InputStream in = getContent(response);
		try {
			return SubversionLog.read(in);
		} finally {
			closeQuiet(in);
		}
	}

	@Override
	public final void setCredentials(@Nullable final String username, @Nullable final String password, @Nullable final String workstation) {
		final Credentials credentials = creteCredentials(username, password, workstation);
		final CredentialsProvider credentialsProvider = backend.getCredentialsProvider();

		final Credentials oldCredentials = credentialsProvider.getCredentials(authscope);
		if (!ObjectUtils.equals(credentials, oldCredentials)) {
			context.clear();
			credentialsProvider.setCredentials(authscope, credentials);
			triggerAuthentication();
		}
	}

	@Override
	public void setProperties(final String resource, final String message, final SubversionProperty... properties) {
		uploadWithProperties0(normalizeResource(resource), message, null, properties);
	}

	protected final void triggerAuthentication() {
		final HttpUriRequest request = requestFactory.createAuthRequest(repository);
		execute(request, HttpStatus.SC_OK);
	}

	@Override
	public void unlock(final String resource) {
		final String normalizedResource = normalizeResource(resource);
		final SubversionInfo info = info0(normalizedResource, HEAD_VERSION, false);
		final String lockToken = info.getLockToken();
		if (lockToken == null) {
			return;
		}
		final URI uri = URI.create(repository + normalizedResource);

		final HttpUriRequest request = requestFactory.createUnlockRequest(uri, lockToken);
		execute(request, HttpStatus.SC_NO_CONTENT);
	}

	@Override
	public void upload(final String resource, final String message, final InputStream content) {
		if (content == null) {
			throw new IllegalArgumentException("content can not be null");
		}
		uploadWithProperties0(normalizeResource(resource), message, content, (SubversionProperty) null);
	}

	@Override
	public void uploadWithProperties(final String resource, final String message, final InputStream content, final SubversionProperty... properties) {
		if (content == null) {
			throw new IllegalArgumentException("content can not be null");
		}
		uploadWithProperties0(normalizeResource(resource), message, content, properties);
	}

	protected abstract void uploadWithProperties0(final String normalizedResource, final String message, @Nullable final InputStream content, @Nullable final SubversionProperty... properties);
}
