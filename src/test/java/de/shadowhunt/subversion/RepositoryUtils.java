package de.shadowhunt.subversion;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

public final class RepositoryUtils {

	public static String retrieveContent(final InputStream download) throws IOException {
		try {
			return StringUtils.trimToEmpty(IOUtils.toString(download, "UTF-8"));
		} finally {
			IOUtils.closeQuietly(download);
		}
	}

	private RepositoryUtils() {
		// prevent instantiation
	}
}
