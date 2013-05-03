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
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeSocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DecompressingHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import de.shadowhunt.http.auth.NtlmSchemeFactory;
import de.shadowhunt.http.client.ThreadLocalCredentialsProvider;
import de.shadowhunt.http.conn.ssl.NonValidatingX509TrustManager;
import de.shadowhunt.http.protocol.ThreadLocalHttpContext;

public abstract class AbstractSubversionRepository<T extends AbstractSubversionRequestFactory> implements SubversionRepository {

	protected static final int HEAD_VERSION = -1;

	protected static boolean contains(final int statusCode, @Nullable final int... expectedStatusCodes) {
		if (expectedStatusCodes == null) {
			return true;
		}

		for (final int expectedStatusCode : expectedStatusCodes) {
			if (expectedStatusCode == statusCode) {
				return true;
			}
		}
		return false;
	}

	protected static DefaultHttpClient createClient(final int maxConnections, final boolean addSvnHeader) {
		final PoolingClientConnectionManager connectionManager = new PoolingClientConnectionManager();
		connectionManager.setMaxTotal(maxConnections);
		connectionManager.setDefaultMaxPerRoute(maxConnections);

		final Scheme scheme = createTrustingAnySslCertScheme();
		connectionManager.getSchemeRegistry().register(scheme);

		final DefaultHttpClient defaultClient = new DefaultHttpClient(connectionManager);
		defaultClient.setCredentialsProvider(new ThreadLocalCredentialsProvider());
		if (addSvnHeader) {
			defaultClient.getParams().setParameter(ClientPNames.DEFAULT_HEADERS, createDefaultHeaders());
		}

		if (hasJcifsSupport()) {
			defaultClient.getAuthSchemes().register(AuthPolicy.NTLM, NtlmSchemeFactory.INSTANCE);
		}

		return defaultClient;
	}

	private static Collection<Header> createDefaultHeaders() {
		final Collection<Header> parameters = new ArrayList<Header>();
		parameters.add(new BasicHeader("Connection", "TE"));
		parameters.add(new BasicHeader("TE", "trailers"));
		parameters.add(new BasicHeader("DAV", "http://subversion.tigris.org/xmlns/dav/svn/depth"));
		parameters.add(new BasicHeader("DAV", "http://subversion.tigris.org/xmlns/dav/svn/mergeinfo"));
		parameters.add(new BasicHeader("DAV", "http://subversion.tigris.org/xmlns/dav/svn/log-revprops"));
		return parameters;
	}

	protected static Scheme createTrustingAnySslCertScheme() {
		try {
			final SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, new TrustManager[] { NonValidatingX509TrustManager.INSTANCE }, new SecureRandom());

			final SchemeSocketFactory socketFactory = new SSLSocketFactory(sc, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			return new Scheme("https", 443, socketFactory);
		} catch (final Exception e) {
			throw new SubversionException("could not create ssl scheme", e);
		}
	}

	@CheckForNull
	protected static Credentials creteCredentials(final String user, final String password, @Nullable final String workstation) {
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
		final String trimmed = StringUtils.trimToNull(resource);
		if (trimmed == null) {
			return "/";
		}

		if (trimmed.charAt(0) == '/') {
			return StringUtils.removeEnd(trimmed, "/");
		}
		return "/" + StringUtils.removeEnd(trimmed, "/");
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

	protected AbstractSubversionRepository(final URI repository, final T requestFactory) {
		this(createClient(100, false), repository, requestFactory);
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
	public InputStream download(final String resource, final int version) {
		if (version <= HEAD_VERSION) {
			throw new IllegalArgumentException("version must be greater than 0, was:" + version);
		}
		return download0(normalizeResource(resource), version);
	}

	protected abstract InputStream download0(final String normalizeResource, final int version);

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
	public boolean exisits(final String resource) {
		return exisits0(normalizeResource(resource));
	}

	protected boolean exisits0(final String normalizedResource) {
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
	public SubversionInfo info(final String resource, final int version, final boolean withCustomProperties) {
		if (version <= HEAD_VERSION) {
			throw new IllegalArgumentException("version must be greater than 0, was:" + version);
		}
		return info0(normalizeResource(resource), version, withCustomProperties);
	}

	protected abstract SubversionInfo info0(String normalizeResource, int version, boolean withCustomProperties);

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
		final List<SubversionLog> logs = log(uri, info.getVersion(), info.getVersion());
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
		return log(uri, info.getVersion(), 0);
	}

	@Override
	public List<SubversionLog> log(final URI uri, final int startVersion, final int endVersion) {
		final HttpUriRequest request = requestFactory.createLogRequest(uri, startVersion, endVersion);
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
			credentialsProvider.setCredentials(authscope, credentials);
			context.clear();
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
	public void unlock(final String resource, final SubversionInfo info) {
		if (info.getLockToken() == null) {
			return;
		}
		final URI uri = URI.create(repository + normalizeResource(resource));

		final HttpUriRequest request = requestFactory.createUnlockRequest(uri, info);
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
