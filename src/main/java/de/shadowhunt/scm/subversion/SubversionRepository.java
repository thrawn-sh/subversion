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

public class SubversionRepository {

	private static final TrustManager DUMMY_MANAGER = new X509TrustManager() {

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

	static boolean contains(final int statusCode, final int... expectedStatusCodes) {
		for (final int expectedStatusCode : expectedStatusCodes) {
			if (expectedStatusCode == statusCode) {
				return true;
			}
		}
		return false;
	}

	private static HttpClient createClient(final URI host, final String username, final String password, @Nullable final String workstation) {
		final DefaultHttpClient client = new DefaultHttpClient();

		final Credentials credentials = creteCredentials(username, password, workstation);
		final AuthScope authscope = new AuthScope(host.getHost(), AuthScope.ANY_PORT);
		client.getCredentialsProvider().setCredentials(authscope, credentials);

		final Scheme scheme = createTrustingAnySslCertScheme(host.getPort());
		client.getConnectionManager().getSchemeRegistry().register(scheme);

		return new DecompressingHttpClient(client); // add gzip support
	}

	private static Scheme createTrustingAnySslCertScheme(final int port) {
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

	private static Credentials creteCredentials(final String user, final String password, @Nullable final String workstation) {
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

	private static int ensureResonse(final HttpResponse response, final boolean consume, final int... expectedStatusCodes) {
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

	private static int ensureResonse(final HttpResponse response, final int... expectedStatusCodes) {
		return ensureResonse(response, true, expectedStatusCodes);
	}

	private static InputStream getContent(final HttpResponse response) {
		try {
			return response.getEntity().getContent();
		} catch (final Exception e) {
			throw new SubversionException("could not retrieve content stream", e);
		}
	}

	private final HttpClient client;

	/**
	 * HttpContext is *NOT threadsafe, use getHttpContext() to retrieve it
	 */
	private final ThreadLocal<HttpContext> context = new ThreadLocal<HttpContext>();

	private final URI host;

	private final String module;

	SubversionRepository(final URI host, final String module, final HttpClient client) {
		this.client = client;
		this.host = host;
		this.module = module;

		triggerAuthentication();
	}

	public SubversionRepository(final URI host, final String module, final String username, final String password) {
		this(host, module, username, password, null);
	}

	public SubversionRepository(final URI host, final String module, final String username, final String password, @Nullable final String workstation) {
		this(host, module, createClient(host, username, password, workstation));
	}

	private void closeQuiet(final InputStream in) {
		try {
			in.close();
		} catch (final IOException e) {
			// ignore
		}
	}

	private void contentUpload(final String resource, final UUID uuid, final InputStream content) {
		final URI uri = URI.create(host + module + "/!svn/wrk/" + uuid + resource);

		final HttpUriRequest request = SubversionRequestFactory.createUploadRequest(uri, content);
		final HttpResponse response = execute(request);
		ensureResonse(response, HttpStatus.SC_CREATED, HttpStatus.SC_NO_CONTENT);
	}

	public void create(final String resource, final String message, final InputStream content) {
		if (content == null) {
			throw new IllegalArgumentException("content can not be null");
		}

		final UUID uuid = UUID.randomUUID();

		createTemporyStructure(uuid);
		try {
			final String infoResource = createMissingFolders(resource, uuid);
			final SubversionInfo info = info(infoResource, false);
			final long version = info.getVersion();
			prepareCheckin(uuid);
			setCommitMessage(uuid, version, message);
			contentUpload(resource, uuid, content);
			merge(uuid);
		} finally {
			deleteTemporyStructure(uuid);
		}
	}

	private String createMissingFolders(final String resource, final UUID uuid) {
		final String[] resourceParts = resource.split("/");

		String infoResource = "/";
		final StringBuilder partial = new StringBuilder();
		for (int i = 1; i < (resourceParts.length - 1); i++) {
			partial.append('/');
			partial.append(resourceParts[i]);

			final String partialResource = partial.toString();
			final URI uri = URI.create(host + module + "/!svn/wrk/" + uuid + partialResource);
			final HttpUriRequest request = SubversionRequestFactory.createMakeFolderRequest(uri);
			final HttpResponse response = execute(request);
			final int status = ensureResonse(response, /* created */HttpStatus.SC_CREATED, /* existed */HttpStatus.SC_METHOD_NOT_ALLOWED);
			if (status == HttpStatus.SC_METHOD_NOT_ALLOWED) {
				infoResource = partialResource;
			}
		}

		return infoResource;
	}

	private void createTemporyStructure(final UUID uuid) {
		final URI uri = URI.create(host + module + "/!svn/act/" + uuid);

		final HttpUriRequest request = SubversionRequestFactory.createActivityRequest(uri);
		final HttpResponse response = execute(request);
		ensureResonse(response, HttpStatus.SC_CREATED);
	}

	public void delete(final String resource, final String message) {
		final UUID uuid = UUID.randomUUID();
		final SubversionInfo info = info(resource, false);
		final long version = info.getVersion();

		createTemporyStructure(uuid);
		try {
			prepareCheckin(uuid);
			setCommitMessage(uuid, version, message);
			prepareContentUpload(resource, uuid, version);
			delete(uuid, resource);
			merge(uuid);
		} finally {
			deleteTemporyStructure(uuid);
		}
	}

	private void delete(final UUID uuid, final String resource) {
		final URI uri = URI.create(host + module + "/!svn/wrk/" + uuid + resource);

		final HttpUriRequest request = new HttpDelete(uri);
		final HttpResponse response = execute(request);
		ensureResonse(response, HttpStatus.SC_NO_CONTENT);
	}

	public void deleteProperties(final String resource, final String message, final SubversionProperty... properties) {
		final UUID uuid = UUID.randomUUID();
		final SubversionInfo info = info(resource, false);
		final long version = info.getVersion();

		createTemporyStructure(uuid);
		try {
			prepareCheckin(uuid);
			setCommitMessage(uuid, version, message);
			prepareContentUpload(resource, uuid, version);
			propertiesRemove(resource, uuid, properties);
			merge(uuid);
		} finally {
			deleteTemporyStructure(uuid);
		}
	}

	private void deleteTemporyStructure(final UUID uuid) {
		final URI uri = URI.create(host + module + "/!svn/act/" + uuid);

		final HttpUriRequest request = new HttpDelete(uri);
		final HttpResponse response = execute(request);
		ensureResonse(response, HttpStatus.SC_NO_CONTENT);
	}

	public InputStream download(final String resource) {
		final URI uri = URI.create(host + module + resource);

		final HttpUriRequest request = SubversionRequestFactory.createDownloadRequest(uri);
		final HttpResponse response = execute(request);
		ensureResonse(response, false, HttpStatus.SC_OK);

		return getContent(response);
	}

	public InputStream download(final String resource, final long version) {
		final URI uri = URI.create(host + module + "/!svn/bc/" + version + resource);

		final HttpUriRequest request = SubversionRequestFactory.createDownloadRequest(uri);
		final HttpResponse response = execute(request);
		ensureResonse(response, false, HttpStatus.SC_OK);

		return getContent(response);
	}

	private HttpResponse execute(final HttpUriRequest request) {
		try {
			return client.execute(request, getHttpContext());
		} catch (final Exception e) {
			throw new SubversionException("could not execute request (" + request + ")", e);
		}
	}

	public boolean exisits(final String resource) {
		final URI uri = URI.create(host + module + resource);

		final HttpUriRequest request = SubversionRequestFactory.createExistsRequest(uri);
		final HttpResponse response = execute(request);
		ensureResonse(response, /* found */HttpStatus.SC_OK, /* not found */HttpStatus.SC_NOT_FOUND);
		return (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK);
	}

	private HttpContext getHttpContext() {
		HttpContext httpContext = context.get();
		if (httpContext == null) {
			httpContext = new BasicHttpContext();

			// enable preemptive authentication for basic
			final AuthCache authCache = new BasicAuthCache();
			final HttpHost httpHost = new HttpHost(host.getHost(), host.getPort(), host.getScheme());
			authCache.put(httpHost, new BasicScheme());
			httpContext.setAttribute(ClientContext.AUTH_CACHE, authCache);

			context.set(httpContext);
		}
		return httpContext;
	}

	public SubversionInfo info(final String resource, final boolean withCustomProperties) {
		final URI uri = URI.create(host + module + resource);

		final HttpUriRequest request = SubversionRequestFactory.createInfoRequest(uri, 0);
		final HttpResponse response = execute(request);
		ensureResonse(response, false, HttpStatus.SC_MULTI_STATUS);

		final InputStream in = getContent(response);
		try {
			return SubversionInfo.read(in, withCustomProperties);
		} finally {
			closeQuiet(in);
		}
	}

	public List<SubversionInfo> list(final String resource, final int depth, final boolean withCustomProperties) {
		final SubversionInfo info = info(resource, false);
		final URI uri = URI.create(host + module + "/!svn/bc/" + info.getVersion() + resource);

		final HttpUriRequest request = SubversionRequestFactory.createInfoRequest(uri, depth);
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
		final URI uri = URI.create(host + module + resource);

		final HttpUriRequest request = SubversionRequestFactory.createLockRequest(uri);
		final HttpResponse response = execute(request);
		ensureResonse(response, HttpStatus.SC_OK);
	}

	public List<SubversionLog> log(final String resource) {
		final URI uri = URI.create(host + module + resource);

		final SubversionInfo info = info(resource, false);
		final HttpUriRequest request = SubversionRequestFactory.createLogRequest(uri, info.getVersion(), 0L);
		final HttpResponse response = execute(request);
		ensureResonse(response, false, HttpStatus.SC_OK);

		final InputStream in = getContent(response);
		try {
			return SubversionLog.read(in);
		} finally {
			closeQuiet(in);
		}
	}

	private void merge(final UUID uuid) {
		final URI uri = URI.create(host + module);

		final HttpUriRequest request = SubversionRequestFactory.createMergeRequest(uri, uuid);
		final HttpResponse response = execute(request);
		ensureResonse(response, HttpStatus.SC_OK);
	}

	private void prepareCheckin(final UUID uuid) {
		final URI uri = URI.create(host + module + "/!svn/vcc/default");

		final HttpUriRequest request = SubversionRequestFactory.createCheckoutRequest(uri, uuid);
		final HttpResponse response = execute(request);
		ensureResonse(response, HttpStatus.SC_CREATED);
	}

	private void prepareContentUpload(final String resource, final UUID uuid, final long version) {
		final URI uri = URI.create(host + module + "/!svn/ver/" + version + resource);

		final HttpUriRequest request = SubversionRequestFactory.createCheckoutRequest(uri, uuid);
		final HttpResponse response = execute(request);
		ensureResonse(response, HttpStatus.SC_CREATED);
	}

	private void propertiesRemove(final String resource, final UUID uuid, final SubversionProperty[] properties) {
		final SubversionProperty[] filtered = SubversionProperty.filteroutSystemProperties(properties);
		if (filtered.length == 0) {
			return;
		}

		final URI uri = URI.create(host + module + "/!svn/wrk/" + uuid + resource);

		final HttpUriRequest request = SubversionRequestFactory.createRemovePropertiesRequest(uri, filtered);
		final HttpResponse response = execute(request);
		ensureResonse(response, HttpStatus.SC_MULTI_STATUS);
	}

	private void propertiesSet(final String resource, final UUID uuid, final SubversionProperty... properties) {
		final SubversionProperty[] filtered = SubversionProperty.filteroutSystemProperties(properties);
		if (filtered.length == 0) {
			return;
		}

		final URI uri = URI.create(host + module + "/!svn/wrk/" + uuid + resource);

		final HttpUriRequest request = SubversionRequestFactory.createSetPropertiesRequest(uri, filtered);
		final HttpResponse response = execute(request);
		ensureResonse(response, HttpStatus.SC_MULTI_STATUS);
	}

	private void setCommitMessage(final UUID uuid, final long version, final String message) {
		final URI uri = URI.create(host + module + "/!svn/wbl/" + uuid + "/" + version);

		final String trimmedMessage = StringUtils.trimToEmpty(message);
		final HttpUriRequest request = SubversionRequestFactory.createCommitMessageRequest(uri, trimmedMessage);
		final HttpResponse response = execute(request);
		ensureResonse(response, HttpStatus.SC_MULTI_STATUS);
	}

	public void setProperties(final String resource, final String message, final SubversionProperty... properties) {
		uploadWithProperties0(resource, message, null, properties);
	}

	private final void triggerAuthentication() {
		final HttpUriRequest request = SubversionRequestFactory.createAuthRequest(URI.create(host + module));
		final HttpResponse response = execute(request);
		EntityUtils.consumeQuietly(response.getEntity());
	}

	public void unlock(final String resource, final String token) {
		final URI uri = URI.create(host + module + resource);

		final HttpUriRequest request = SubversionRequestFactory.createUnlockRequest(uri, "<" + token + ">");
		final HttpResponse response = execute(request);
		ensureResonse(response, HttpStatus.SC_NO_CONTENT);
	}

	public void upload(final String resource, final String message, final InputStream content) {
		if (content == null) {
			throw new IllegalArgumentException("content can not be null");
		}
		uploadWithProperties0(resource, message, content, (SubversionProperty[]) null);
	}

	public void uploadWithProperties(final String resource, final String message, final InputStream content, final SubversionProperty... properties) {
		if ((content == null) && (properties == null)) {
			throw new IllegalArgumentException("content and properties can not both be null");
		}
		uploadWithProperties0(resource, message, content, properties);
	}

	void uploadWithProperties0(final String resource, final String message, @Nullable final InputStream content, @Nullable final SubversionProperty... properties) {
		final UUID uuid = UUID.randomUUID();
		final SubversionInfo info = info(resource, false);
		final long version = info.getVersion();

		createTemporyStructure(uuid);
		try {
			prepareCheckin(uuid);
			setCommitMessage(uuid, version, message);
			prepareContentUpload(resource, uuid, version);
			propertiesSet(resource, uuid, properties);

			if (content != null) {
				contentUpload(resource, uuid, content);
			}
			merge(uuid);
		} finally {
			deleteTemporyStructure(uuid);
		}
	}
}
