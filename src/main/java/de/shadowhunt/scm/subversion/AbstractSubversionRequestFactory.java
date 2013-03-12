package de.shadowhunt.scm.subversion;

import java.io.InputStream;
import java.net.URI;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;

public abstract class AbstractSubversionRequestFactory {

	public static final class DavTemplateRequest extends HttpEntityEnclosingRequestBase {

		private static final int DEFAULT_DEPTH = 0;

		private int depth;

		private final String method;

		public DavTemplateRequest(final String method) {
			this(method, DEFAULT_DEPTH);
		}

		public DavTemplateRequest(final String method, final int depth) {
			super();
			this.method = method;
			setDepth(depth);
		}

		public int getDepth() {
			return depth;
		}

		@Override
		public String getMethod() {
			return method;
		}

		public void setDepth(final int depth) {
			this.depth = depth;
			removeHeaders("Depth");
			addHeader("Depth", Integer.toString(depth));
		}

		@Override
		public String toString() {
			return method + " " + getURI() + " " + getProtocolVersion();
		}
	}

	protected static final ContentType CONTENT_TYPE_XML = ContentType.create("text/xml", "UTF-8");

	protected static final String XML_PREAMBLE = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";

	public abstract HttpUriRequest createActivityRequest(final URI uri);

	public abstract HttpUriRequest createAuthRequest(final URI uri);

	public abstract HttpUriRequest createCheckoutRequest(final URI uri, final String path);

	public abstract HttpUriRequest createCommitMessageRequest(final URI uri, final String message);

	public abstract HttpUriRequest createDownloadRequest(final URI uri);

	public abstract HttpUriRequest createExistsRequest(final URI uri);

	public abstract HttpUriRequest createInfoRequest(final URI uri, final int depth);

	public abstract HttpUriRequest createLockRequest(final URI uri);

	public abstract HttpUriRequest createLogRequest(final URI uri, final long start, final long end);

	public abstract HttpUriRequest createMakeFolderRequest(final URI uri);

	public abstract HttpUriRequest createMergeRequest(final URI uri, final String path);

	public abstract HttpUriRequest createRemovePropertiesRequest(final URI uri, final SubversionProperty... properties);

	public abstract HttpUriRequest createSetPropertiesRequest(final URI uri, final SubversionProperty... properties);

	public abstract HttpUriRequest createUnlockRequest(final URI uri, final String token);

	public abstract HttpUriRequest createUploadRequest(final URI uri, final InputStream content);
}
