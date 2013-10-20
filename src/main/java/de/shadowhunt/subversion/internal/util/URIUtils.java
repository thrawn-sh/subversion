package de.shadowhunt.subversion.internal.util;

import de.shadowhunt.subversion.Resource;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.http.client.utils.URIBuilder;

public final class URIUtils {

	public static URI createURI(final URI repository, final Resource... resources) {
		try {
			return createURI0(repository, resources);
		} catch (final URISyntaxException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private static URI createURI0(final URI repository, final Resource... resources) throws URISyntaxException {
		final URIBuilder builder = new URIBuilder();
		builder.setScheme(repository.getScheme());
		builder.setHost(repository.getHost());
		builder.setPort(repository.getPort());
		final StringBuilder completePath = new StringBuilder(repository.getPath());
		for (final Resource resource : resources) {
			completePath.append(resource.getValue());
		}
		builder.setPath(completePath.toString());
		return builder.build();
	}

	private URIUtils() {
		// prevent instantiation
	}
}
