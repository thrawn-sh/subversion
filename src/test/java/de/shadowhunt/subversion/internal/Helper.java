package de.shadowhunt.subversion.internal;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

public final class Helper {

	private static final Charset UTF8_CHARSET = Charset.forName("UTF-8");

	public static InputStream getInputStream(final String s) {
		return new ByteArrayInputStream(s.getBytes(UTF8_CHARSET));
	}

	private Helper() {
		// prevent instantiation
	}
}
