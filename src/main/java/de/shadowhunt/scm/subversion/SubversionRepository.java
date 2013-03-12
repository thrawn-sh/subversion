package de.shadowhunt.scm.subversion;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

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
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeSocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.DecompressingHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

public abstract class SubversionRepository<T extends AbstractSubversionRequestFactory> {

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

	protected static final String PREFIX_ACT = "/!svn/act/";

	protected static final String PREFIX_BC = "/!svn/bc/";

	protected static final String PREFIX_WRK = "/!svn/wrk/";

	protected static boolean contains(final int statusCode, final int... expectedStatusCodes) {
		for (final int expectedStatusCode : expectedStatusCodes) {
			if (expectedStatusCode == statusCode) {
				return true;
			}
		}
		return false;
	}

	protected static HttpClient createClient(final URI host, final String username, final String password, @Nullable final String workstation) {
		final DefaultHttpClient client = new DefaultHttpClient();

		final Credentials credentials = creteCredentials(username, password, workstation);
		final AuthScope authscope = new AuthScope(host.getHost(), AuthScope.ANY_PORT);
		client.getCredentialsProvider().setCredentials(authscope, credentials);

		final Scheme scheme = createTrustingAnySslCertScheme(host.getPort());
		client.getConnectionManager().getSchemeRegistry().register(scheme);

		return new DecompressingHttpClient(client); // add gzip support
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

	protected static Credentials creteCredentials(final String user, final String password, @Nullable final String workstation) {
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

	protected static int ensureResonse(final HttpResponse response, final boolean consume, final int... expectedStatusCodes) {
		final int statusCode = response.getStatusLine().getStatusCode();
		if (consume) {
			EntityUtils.consumeQuietly(response.getEntity());
		}

		if (!contains(statusCode, expectedStatusCodes)) {
			EntityUtils.consumeQuietly(response.getEntity()); // in case of unexpected status code we consume everything
			throw new SubversionException("status code is: " + statusCode + ", expected was: "
					+ Arrays.toString(expectedStatusCodes));
		}

		return statusCode;
	}

	protected static int ensureResonse(final HttpResponse response, final int... expectedStatusCodes) {
		return ensureResonse(response, true, expectedStatusCodes);
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
			return trimmed;
		}
		return "/" + trimmed;
	}

	protected final HttpClient client;

	/**
	 * HttpContext is *NOT threadsafe, use getHttpContext() to retrieve it
	 */
	private final ThreadLocal<HttpContext> context = new ThreadLocal<HttpContext>();

	protected final URI repository;

	protected final T requestFactory;

	protected SubversionRepository(final HttpClient client, final URI repository, final T requestFactory) {
		this.client = client;
		this.repository = repository;
		this.requestFactory = requestFactory;
	}

	protected void closeQuiet(final InputStream in) {
		try {
			in.close();
		} catch (final IOException e) {
			// ignore
		}
	}

	protected void contentUpload(final String sanatizedResource, final UUID uuid, final InputStream content) {
		final URI uri = URI.create(repository + PREFIX_WRK + uuid + sanatizedResource);

		final HttpUriRequest request = requestFactory.createUploadRequest(uri, content);
		final HttpResponse response = execute(request);
		ensureResonse(response, HttpStatus.SC_CREATED, HttpStatus.SC_NO_CONTENT);
	}

	public void create(final String resource, final String message, final InputStream content) {
		if (content == null) {
			throw new IllegalArgumentException("content can not be null");
		}
		createWithProperties0(sanatizeResource(resource), message, content, (SubversionProperty[]) null);
	}

	protected String createMissingFolders(final String sanatizedResource, final UUID uuid) {
		final String[] resourceParts = sanatizedResource.split("/");

		String infoResource = "/";
		final StringBuilder partial = new StringBuilder();
		for (int i = 1; i < (resourceParts.length - 1); i++) {
			partial.append('/');
			partial.append(resourceParts[i]);

			final String partialResource = partial.toString();
			final URI uri = URI.create(repository + PREFIX_WRK + uuid + partialResource);
			final HttpUriRequest request = requestFactory.createMakeFolderRequest(uri);
			final HttpResponse response = execute(request);
			final int status = ensureResonse(response, /* created */HttpStatus.SC_CREATED, /* existed */HttpStatus.SC_METHOD_NOT_ALLOWED);
			if (status == HttpStatus.SC_METHOD_NOT_ALLOWED) {
				infoResource = partialResource;
			}
		}

		return infoResource;
	}

	protected void createTemporyStructure(final UUID uuid) {
		final URI uri = URI.create(repository + PREFIX_ACT + uuid);

		final HttpUriRequest request = requestFactory.createActivityRequest(uri);
		final HttpResponse response = execute(request);
		ensureResonse(response, HttpStatus.SC_CREATED);
	}

	public void createWithProperties(final String resource, final String message, final InputStream content, final SubversionProperty... properties) {
		if ((content == null) && (properties == null)) {
			throw new IllegalArgumentException("content and properties can not both be null");
		}
		createWithProperties0(sanatizeResource(resource), message, content, properties);
	}

	protected abstract void createWithProperties0(final String sanatizedResource, final String message, final InputStream content, final SubversionProperty... properties);

	public abstract void delete(final String resource, final String message);

	protected void delete(final String sanatizedResource, final UUID uuid) {
		final URI uri = URI.create(repository + PREFIX_WRK + uuid + sanatizedResource);
		delete(uri);
	}

	protected void delete(final URI uri) {
		final HttpUriRequest request = new HttpDelete(uri);
		final HttpResponse response = execute(request);
		ensureResonse(response, HttpStatus.SC_NO_CONTENT);
	}

	public abstract void deleteProperties(final String resource, final String message, final SubversionProperty... properties);

	protected void deleteTemporyStructure(final UUID uuid) {
		final URI uri = URI.create(repository + PREFIX_ACT + uuid);
		delete(uri);
	}

	public InputStream download(final String resource) {
		final URI uri = URI.create(repository + sanatizeResource(resource));

		final HttpUriRequest request = requestFactory.createDownloadRequest(uri);
		final HttpResponse response = execute(request);
		ensureResonse(response, false, HttpStatus.SC_OK);

		return getContent(response);
	}

	public InputStream download(final String resource, final long version) {
		final URI uri = URI.create(repository + PREFIX_BC + version + sanatizeResource(resource));

		final HttpUriRequest request = requestFactory.createDownloadRequest(uri);
		final HttpResponse response = execute(request);
		ensureResonse(response, false, HttpStatus.SC_OK);

		return getContent(response);
	}

	protected HttpResponse execute(final HttpUriRequest request) {
		try {
			return client.execute(request, getHttpContext());
		} catch (final Exception e) {
			throw new SubversionException("could not execute request (" + request + ")", e);
		}
	}

	public boolean exisits(final String resource) {
		final URI uri = URI.create(repository + sanatizeResource(resource));

		final HttpUriRequest request = requestFactory.createExistsRequest(uri);
		final HttpResponse response = execute(request);
		ensureResonse(response, /* found */HttpStatus.SC_OK, /* not found */HttpStatus.SC_NOT_FOUND);
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

	public SubversionInfo info(final String resource, final boolean withCustomProperties) {
		return info0(sanatizeResource(resource), withCustomProperties);
	}

	protected SubversionInfo info0(final String sanatizedResource, final boolean withCustomProperties) {
		final URI uri = URI.create(repository + sanatizedResource);

		final HttpUriRequest request = requestFactory.createInfoRequest(uri, 0);
		final HttpResponse response = execute(request);
		ensureResonse(response, false, HttpStatus.SC_MULTI_STATUS);

		final InputStream in = getContent(response);
		try {
			return SubversionInfo.read(in, withCustomProperties);
		} finally {
			closeQuiet(in);
		}
	}

	public SubversionLog lastLog(final String resource) {
		final String sanatizedResource = sanatizeResource(resource);
		final URI uri = URI.create(repository + sanatizedResource);

		final SubversionInfo info = info0(sanatizedResource, false);
		final List<SubversionLog> logs = log(uri, info.getVersion(), info.getVersion());
		if (logs.isEmpty()) {
			throw new SubversionException("no logs available");
		}
		return logs.get(0);
	}

	public List<SubversionInfo> list(final String resource, final int depth, final boolean withCustomProperties) {
		final String sanatizedResource = sanatizeResource(resource);
		final SubversionInfo info = info0(sanatizedResource, false);
		final URI uri = URI.create(repository + PREFIX_BC + info.getVersion() + sanatizedResource);

		final HttpUriRequest request = requestFactory.createInfoRequest(uri, depth);
		final HttpResponse response = execute(request);
		ensureResonse(response, false, HttpStatus.SC_MULTI_STATUS);

		final InputStream in = getContent(response);
		try {
			return SubversionInfo.readList(in, withCustomProperties);
		} finally {
			closeQuiet(in);
		}
	}

	public void lock(final String resource) {
		final URI uri = URI.create(repository + sanatizeResource(resource));

		final HttpUriRequest request = requestFactory.createLockRequest(uri);
		final HttpResponse response = execute(request);
		ensureResonse(response, HttpStatus.SC_OK);
	}

	public List<SubversionLog> log(final String resource) {
		final String sanatizedResource = sanatizeResource(resource);
		final URI uri = URI.create(repository + sanatizedResource);

		final SubversionInfo info = info0(sanatizedResource, false);
		return log(uri, info.getVersion(), 0L);
	}

	public List<SubversionLog> log(final URI uri, final long start, final long end) {
		final HttpUriRequest request = requestFactory.createLogRequest(uri, start, end);
		final HttpResponse response = execute(request);
		ensureResonse(response, false, HttpStatus.SC_OK);

		final InputStream in = getContent(response);
		try {
			return SubversionLog.read(in);
		} finally {
			closeQuiet(in);
		}
	}

	protected void merge(final String path) {
		final HttpUriRequest request = requestFactory.createMergeRequest(repository, path);
		final HttpResponse response = execute(request);
		ensureResonse(response, HttpStatus.SC_OK);
	}

	public void setProperties(final String resource, final String message, final SubversionProperty... properties) {
		uploadWithProperties0(sanatizeResource(resource), message, null, properties);
	}

	protected final void triggerAuthentication() {
		final HttpUriRequest request = requestFactory.createAuthRequest(repository);
		final HttpResponse response = execute(request);
		EntityUtils.consumeQuietly(response.getEntity());
	}

	public void unlock(final String resource, final String token) {
		final URI uri = URI.create(repository + sanatizeResource(resource));

		final HttpUriRequest request = requestFactory.createUnlockRequest(uri, "<" + token + ">");
		final HttpResponse response = execute(request);
		ensureResonse(response, HttpStatus.SC_NO_CONTENT);
	}

	public void upload(final String resource, final String message, final InputStream content) {
		if (content == null) {
			throw new IllegalArgumentException("content can not be null");
		}
		uploadWithProperties0(sanatizeResource(resource), message, content, (SubversionProperty[]) null);
	}

	public void uploadWithProperties(final String resource, final String message, final InputStream content, final SubversionProperty... properties) {
		if ((content == null) && (properties == null)) {
			throw new IllegalArgumentException("content and properties can not both be null");
		}
		uploadWithProperties0(sanatizeResource(resource), message, content, properties);
	}

	protected abstract void uploadWithProperties0(final String sanatizedResource, final String message, @Nullable final InputStream content, @Nullable final SubversionProperty... properties);

}
