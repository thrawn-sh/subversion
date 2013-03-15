package de.shadowhunt.scm.subversion;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeSocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.DecompressingHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

public abstract class AbstractSubversionRepository<T extends AbstractSubversionRequestFactory> implements SubversionRepository {

	protected static final TrustManager DUMMY_MANAGER = new X509TrustManager() {

		@Override
		public void checkClientTrusted(final X509Certificate[] certs, final String authType) {
			// do nothing
		}

		@Override
		public void checkServerTrusted(final X509Certificate[] certs, final String authType) {
			// do nothing
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return new X509Certificate[0];
		}

	};

	protected static final long HEAD_VERSION = -1L;

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

	protected static DefaultHttpClient createClient(final URI host) {
		final PoolingClientConnectionManager connectionManager = new PoolingClientConnectionManager();
		connectionManager.setMaxTotal(100); // TODO
		connectionManager.setDefaultMaxPerRoute(100); // TODO

		final Scheme scheme = createTrustingAnySslCertScheme(host.getPort());
		connectionManager.getSchemeRegistry().register(scheme);

		final DefaultHttpClient client = new DefaultHttpClient(connectionManager);
		client.setCredentialsProvider(new ThreadLocalCredentialsProvider());
		return client;
	}

	protected static Scheme createTrustingAnySslCertScheme(final int port) {
		try {
			final SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, new TrustManager[] { DUMMY_MANAGER }, new SecureRandom());

			final SchemeSocketFactory socketFactory = new SSLSocketFactory(sc);
			final int sslPort = (port <= 0) ? 443 : port;
			return new Scheme("https", sslPort, socketFactory);
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
			domain = null;
		}
		return new NTCredentials(username, password, workstation, domain);
	}

	static void ensureResonse(final HttpResponse response, final boolean consume, @Nullable final int... expectedStatusCodes) throws IOException {
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

	protected static String sanatizeResource(final String resource) {
		final String trimmed = StringUtils.trimToNull(resource);
		if (trimmed == null) {
			return "/";
		}

		if (trimmed.charAt(0) == '/') {
			return StringUtils.removeEnd(trimmed, "/");
		}
		return "/" + StringUtils.removeEnd(trimmed, "/");
	}

	private final DefaultHttpClient backend;

	protected final HttpClient client;

	/**
	 * HttpContext is *NOT threadsafe, use getHttpContext() to retrieve it
	 */
	private final ThreadLocal<HttpContext> context = new ThreadLocal<HttpContext>();

	protected final URI repository;

	protected final T requestFactory;

	protected AbstractSubversionRepository(final DefaultHttpClient backend, final URI repository, final T requestFactory) {
		this.client = new DecompressingHttpClient(backend);
		this.backend = backend;
		this.repository = repository;
		this.requestFactory = requestFactory;
	}

	protected AbstractSubversionRepository(final URI repository, final T requestFactory) {
		this(createClient(repository), repository, requestFactory);
	}

	protected void closeQuiet(final InputStream in) {
		try {
			in.close();
		} catch (final IOException e) {
			// ignore
		}
	}

	@Override
	public abstract void delete(final String resource, final String message);

	@Override
	public abstract void deleteProperties(final String resource, final String message, final SubversionProperty... properties);

	@Override
	public InputStream download(final String resource) {
		return download0(sanatizeResource(resource), HEAD_VERSION);
	}

	@Override
	public InputStream download(final String resource, final long version) {
		if (version <= 0L) {
			throw new IllegalArgumentException("version must be greater than 0, was:" + version);
		}
		return download0(sanatizeResource(resource), version);
	}

	protected abstract InputStream download0(final String sanatizeResource, final long version);

	protected HttpResponse execute(final HttpUriRequest request, final boolean consume, @Nullable final int... expectedStatusCodes) {
		try {
			final HttpResponse response = client.execute(request, getHttpContext());
			ensureResonse(response, consume, expectedStatusCodes);
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
		return exisits0(sanatizeResource(resource));
	}

	protected boolean exisits0(final String sanatizedResource) {
		final URI uri = URI.create(repository + sanatizedResource);

		final HttpUriRequest request = requestFactory.createExistsRequest(uri);
		final HttpResponse response = execute(request, /* found */HttpStatus.SC_OK, /* not found */HttpStatus.SC_NOT_FOUND);
		return (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK);
	}

	HttpContext getHttpContext() {
		HttpContext httpContext = context.get();
		if (httpContext == null) {
			httpContext = new BasicHttpContext();

			// enable preemptive authentication for basic
			final AuthCache authCache = new BasicAuthCache();
			final HttpHost httpHost = new HttpHost(repository.getHost(), repository.getPort(), repository.getScheme());
			authCache.put(httpHost, new BasicScheme());
			httpContext.setAttribute(ClientContext.AUTH_CACHE, authCache);

			context.set(httpContext);
		}
		return httpContext;
	}

	@Override
	public SubversionInfo info(final String resource, final boolean withCustomProperties) {
		return info0(sanatizeResource(resource), HEAD_VERSION, withCustomProperties);
	}

	@Override
	public SubversionInfo info(final String resource, final long version, final boolean withCustomProperties) {
		if (version <= 0L) {
			throw new IllegalArgumentException("version must be greater than 0, was:" + version);
		}
		return info0(sanatizeResource(resource), version, withCustomProperties);
	}

	protected abstract SubversionInfo info0(String sanatizeResource, long version, boolean withCustomProperties);

	@Override
	public SubversionLog lastLog(final String resource) {
		final String sanatizedResource = sanatizeResource(resource);
		final URI uri = URI.create(repository + sanatizedResource);

		final SubversionInfo info = info0(sanatizedResource, HEAD_VERSION, false);
		final List<SubversionLog> logs = log(uri, info.getVersion(), info.getVersion());
		if (logs.isEmpty()) {
			throw new SubversionException("no logs available");
		}
		return logs.get(0);
	}

	@Override
	public void lock(final String resource) {
		final URI uri = URI.create(repository + sanatizeResource(resource));

		final HttpUriRequest request = requestFactory.createLockRequest(uri);
		execute(request, HttpStatus.SC_OK);
	}

	@Override
	public List<SubversionLog> log(final String resource) {
		final String sanatizedResource = sanatizeResource(resource);
		final URI uri = URI.create(repository + sanatizedResource);

		final SubversionInfo info = info0(sanatizedResource, HEAD_VERSION, false);
		return log(uri, info.getVersion(), 0L);
	}

	@Override
	public List<SubversionLog> log(final URI uri, final long start, final long end) {
		final HttpUriRequest request = requestFactory.createLogRequest(uri, start, end);
		final HttpResponse response = execute(request, false, HttpStatus.SC_OK);

		final InputStream in = getContent(response);
		try {
			return SubversionLog.read(in);
		} finally {
			closeQuiet(in);
		}
	}

	@Override
	public final void setCredentials(final URI host, @Nullable final String username, @Nullable final String password, @Nullable final String workstation) {
		final Credentials credentials = creteCredentials(username, password, workstation);
		final AuthScope authscope = new AuthScope(host.getHost(), AuthScope.ANY_PORT);
		final CredentialsProvider credentialsProvider = backend.getCredentialsProvider();

		if (!credentials.equals(credentialsProvider.getCredentials(authscope))) {
			credentialsProvider.setCredentials(authscope, credentials);

			// if we use new credentials we must reset the authCache 
			final HttpContext httpContext = getHttpContext();
			final AuthCache authCache = (AuthCache) httpContext.getAttribute(ClientContext.AUTH_CACHE);
			if (authCache != null) {
				authCache.clear();
			}

			triggerAuthentication();
		}
	}

	@Override
	public void setProperties(final String resource, final String message, final SubversionProperty... properties) {
		uploadWithProperties0(sanatizeResource(resource), message, null, properties);
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
		final URI uri = URI.create(repository + sanatizeResource(resource));

		final HttpUriRequest request = requestFactory.createUnlockRequest(uri, info);
		execute(request, HttpStatus.SC_NO_CONTENT);
	}

	@Override
	public void upload(final String resource, final String message, final InputStream content) {
		if (content == null) {
			throw new IllegalArgumentException("content can not be null");
		}
		uploadWithProperties0(sanatizeResource(resource), message, content, (SubversionProperty) null);
	}

	@Override
	public void uploadWithProperties(final String resource, final String message, final InputStream content, final SubversionProperty... properties) {
		if (content == null) {
			throw new IllegalArgumentException("content can not be null");
		}
		uploadWithProperties0(sanatizeResource(resource), message, content, properties);
	}

	protected abstract void uploadWithProperties0(final String sanatizedResource, final String message, @Nullable final InputStream content, @Nullable final SubversionProperty... properties);
}
