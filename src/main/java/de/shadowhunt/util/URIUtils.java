package de.shadowhunt.util;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.client.utils.URIBuilder;

public class URIUtils {

	public static URI createURI(final URI repository, final String pathSuffix) {
		try {
			return createURI0(repository, pathSuffix);
		} catch (final URISyntaxException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private static URI createURI0(final URI repository, final String pathSuffix) throws URISyntaxException {
		final URIBuilder builder = new URIBuilder();
		builder.setScheme(repository.getScheme());
		builder.setHost(repository.getHost());
		builder.setPort(repository.getPort());
		final String completePath = repository.getPath() + pathSuffix;
		builder.setPath(completePath);
		return builder.build();
	}
}
