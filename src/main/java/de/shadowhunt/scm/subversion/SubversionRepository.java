package de.shadowhunt.scm.subversion;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.annotation.CheckForNull;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.annotation.Immutable;
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

@Immutable
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

	private static boolean contains(final int statusCode, final int... expectedStatusCodes) {
		for (final int expectedStatusCode : expectedStatusCodes) {
			if (expectedStatusCode == statusCode) {
				return true;
			}
		}
		return false;
	}

	private static HttpClient createClient(final URI host, final String username, final String password) throws Exception {
		final DefaultHttpClient client = new DefaultHttpClient();

		final Credentials credentials = creteCredentials(username, password);
		final AuthScope authscope = new AuthScope(host.getHost(), AuthScope.ANY_PORT);
		client.getCredentialsProvider().setCredentials(authscope, credentials);

		final Scheme scheme = createTrustingAnySslCertScheme(host.getPort());
		client.getConnectionManager().getSchemeRegistry().register(scheme);

		return new DecompressingHttpClient(client); // add gzip support
	}

	private static Scheme createTrustingAnySslCertScheme(final int port) throws Exception {
		final SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, new TrustManager[] { DUMMY_MANAGER }, new SecureRandom());

		final SchemeSocketFactory socketFactory = new SSLSocketFactory(sc);
		final int sslPort = (port <= 0) ? 443 : port;
		return new Scheme("https", sslPort, socketFactory);
	}

	private static Credentials creteCredentials(final String user, final String password) throws Exception {
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
		final String workstation = InetAddress.getLocalHost().getHostName();
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

	private final HttpClient client;

	/**
	 * HttpContext is *NOT threadsafe, use getHttpContext() to retrieve it
	 */
	private final ThreadLocal<HttpContext> context = new ThreadLocal<HttpContext>();

	private final URI host;

	private final String module;

	public SubversionRepository(final URI host, final String module, final String username, final String password) throws Exception {
		client = createClient(host, username, password);
		this.host = host;
		this.module = module;

		// FIXME
		final HttpUriRequest request = SubversionRequestFactory.createAuthRequest(URI.create(host + module));
		final HttpResponse response = client.execute(request, getHttpContext());
		EntityUtils.consumeQuietly(response.getEntity());
	}

	public void commit(final String resource, final String message, @CheckForNull final InputStream content, @CheckForNull final Collection<SubversionProperty> properties) throws Exception {
		final UUID uuid = UUID.randomUUID();
		final SubversionInfo info = info(resource);
		final String version = info.getVersion();

		createTemporyStructure(uuid);
		try {
			prepareCheckin(uuid);
			setCommitMessage(uuid, version, message);
			prepareContentUpload(resource, uuid, version);
			if (properties != null) {
				propertiesSet(resource, uuid, properties);
			}
			if (content != null) {
				contentUpload(resource, uuid, content);
			}
			merge(uuid);
		} finally {
			deleteTemporyStructure(uuid);
		}
	}

	private void contentUpload(final String resource, final UUID uuid, final InputStream content) throws Exception {
		final URI uri = URI.create(host + module + "/!svn/wrk/" + uuid + resource);

		final HttpUriRequest request = SubversionRequestFactory.createUploadRequest(uri, content);
		final HttpResponse response = client.execute(request, getHttpContext());
		ensureResonse(response, HttpStatus.SC_CREATED, HttpStatus.SC_NO_CONTENT);
	}

	public void create(final String resource, final String message, final InputStream content) throws Exception {
		final UUID uuid = UUID.randomUUID();

		createTemporyStructure(uuid);
		try {
			final String infoResource = createMissingFolders(resource, uuid);
			final SubversionInfo info = info(infoResource);
			final String version = info.getVersion();
			prepareCheckin(uuid);
			setCommitMessage(uuid, version, message);
			contentUpload(resource, uuid, content);
			merge(uuid);
		} finally {
			deleteTemporyStructure(uuid);
		}
	}

	private String createMissingFolders(final String resource, final UUID uuid) throws Exception {
		final String[] resourceParts = resource.split("/");

		String infoResource = "/";
		final StringBuilder partial = new StringBuilder();
		for (int i = 1; i < (resourceParts.length - 1); i++) {
			partial.append('/');
			partial.append(resourceParts[i]);

			final String partialResource = partial.toString();
			final URI uri = URI.create(host + module + "/!svn/wrk/" + uuid + partialResource);
			final HttpUriRequest request = SubversionRequestFactory.createMakeFolderRequest(uri);
			final HttpResponse response = client.execute(request, getHttpContext());
			final int status = ensureResonse(response, /* created */HttpStatus.SC_CREATED, /* existed */HttpStatus.SC_METHOD_NOT_ALLOWED);
			if (status == HttpStatus.SC_METHOD_NOT_ALLOWED) {
				infoResource = partialResource;
			}
		}

		return infoResource;
	}

	private void createTemporyStructure(final UUID uuid) throws Exception {
		final URI uri = URI.create(host + module + "/!svn/act/" + uuid);

		final HttpUriRequest request = SubversionRequestFactory.createActivityRequest(uri);
		final HttpResponse response = client.execute(request, getHttpContext());
		ensureResonse(response, HttpStatus.SC_CREATED);
	}

	private void deleteTemporyStructure(final UUID uuid) throws Exception {
		final URI uri = URI.create(host + module + "/!svn/act/" + uuid);

		final HttpUriRequest request = new HttpDelete(uri);
		final HttpResponse response = client.execute(request, getHttpContext());
		ensureResonse(response, HttpStatus.SC_NO_CONTENT);
	}

	public InputStream download(final String resource) throws Exception {
		final URI uri = URI.create(host + module + resource);

		final HttpUriRequest request = SubversionRequestFactory.createDownloadRequest(uri);
		final HttpResponse response = client.execute(request, getHttpContext());
		ensureResonse(response, false, HttpStatus.SC_OK);

		return response.getEntity().getContent();
	}

	public InputStream download(final String resource, final String version) throws Exception {
		final URI uri = URI.create(host + module + "/!svn/bc/" + version + resource);

		final HttpUriRequest request = SubversionRequestFactory.createDownloadRequest(uri);
		final HttpResponse response = client.execute(request, getHttpContext());
		ensureResonse(response, false, HttpStatus.SC_OK);

		return response.getEntity().getContent();
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

	public SubversionInfo info(final String resource) throws Exception {
		final URI uri = URI.create(host + module + resource);

		final HttpUriRequest request = SubversionRequestFactory.createInfoRequest(uri, 0);
		final HttpResponse response = client.execute(request, getHttpContext());
		ensureResonse(response, false, HttpStatus.SC_MULTI_STATUS);

		final InputStream in = response.getEntity().getContent();
		try {
			return SubversionInfo.read(in);
		} finally {
			in.close();
		}
	}

	public List<SubversionInfo> list(final String resource, final int depth) throws Exception {
		final SubversionInfo info = info(resource);
		final URI uri = URI.create(host + module + "/!svn/bc/" + info.getVersion() + resource);

		final HttpUriRequest request = SubversionRequestFactory.createInfoRequest(uri, depth);
		final HttpResponse response = client.execute(request, getHttpContext());
		ensureResonse(response, false, HttpStatus.SC_MULTI_STATUS);

		final InputStream in = response.getEntity().getContent();
		try {
			return SubversionInfo.readList(in);
		} finally {
			in.close();
		}
	}

	public void lock(final String resource) throws Exception {
		final URI uri = URI.create(host + module + resource);

		final HttpUriRequest request = SubversionRequestFactory.createLockRequest(uri);
		final HttpResponse response = client.execute(request, getHttpContext());
		ensureResonse(response, HttpStatus.SC_OK);
	}

	public List<SubversionLog> log(final String resource) throws Exception {
		final URI uri = URI.create(host + module + resource);

		final SubversionInfo info = info(resource);
		final HttpUriRequest request = SubversionRequestFactory.createLogRequest(uri, info.getVersion(), "0");
		final HttpResponse response = client.execute(request, getHttpContext());
		ensureResonse(response, false, HttpStatus.SC_OK);

		final InputStream in = response.getEntity().getContent();
		try {
			return SubversionLog.read(in);
		} finally {
			in.close();
		}
	}

	private void merge(final UUID uuid) throws Exception {
		final URI uri = URI.create(host + module);

		final HttpUriRequest request = SubversionRequestFactory.createMergeRequest(uri, uuid);
		final HttpResponse response = client.execute(request, getHttpContext());
		ensureResonse(response, HttpStatus.SC_OK);
	}

	private void prepareCheckin(final UUID uuid) throws Exception {
		final URI uri = URI.create(host + module + "/!svn/vcc/default");

		final HttpUriRequest request = SubversionRequestFactory.createCheckoutRequest(uri, uuid);
		final HttpResponse response = client.execute(request, getHttpContext());
		ensureResonse(response, HttpStatus.SC_CREATED);
	}

	private void prepareContentUpload(final String resource, final UUID uuid, final String version) throws Exception {
		final URI uri = URI.create(host + module + "/!svn/ver/" + version + resource);

		final HttpUriRequest request = SubversionRequestFactory.createCheckoutRequest(uri, uuid);
		final HttpResponse response = client.execute(request, getHttpContext());
		ensureResonse(response, HttpStatus.SC_CREATED);
	}

	private void propertiesSet(final String resource, final UUID uuid, final Collection<SubversionProperty> properties) throws Exception {
		final URI uri = URI.create(host + module + "/!svn/wrk/" + uuid + resource);

		final HttpUriRequest request = SubversionRequestFactory.createSetPropertiesRequest(uri, properties);
		final HttpResponse response = client.execute(request, getHttpContext());
		ensureResonse(response, HttpStatus.SC_MULTI_STATUS);
	}

	private void setCommitMessage(final UUID uuid, final String version, final String message) throws Exception {
		final URI uri = URI.create(host + module + "/!svn/wbl/" + uuid + "/" + version);

		final HttpUriRequest request = SubversionRequestFactory.createCommitMessageRequest(uri, message);
		final HttpResponse response = client.execute(request, getHttpContext());
		ensureResonse(response, HttpStatus.SC_MULTI_STATUS);
	}

	public void setProperties(final String resource, final String message, final Collection<SubversionProperty> properties) throws Exception {
		commit(resource, message, null, properties);
	}

	public void unlock(final String resource, final String token) throws Exception {
		final URI uri = URI.create(host + module + resource);

		final HttpUriRequest request = SubversionRequestFactory.createUnlockRequest(uri, "<" + token + ">");
		final HttpResponse response = client.execute(request, getHttpContext());
		ensureResonse(response, HttpStatus.SC_NO_CONTENT);
	}

	public void upload(final String resource, final String message, final InputStream content) throws Exception {
		commit(resource, message, content, null);
	}
}
