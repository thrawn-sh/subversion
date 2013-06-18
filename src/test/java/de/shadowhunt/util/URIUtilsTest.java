package de.shadowhunt.util;

import java.net.URI;

import org.junit.Assert;
import org.junit.Test;

public class URIUtilsTest {

	private static final URI BASE = URI.create("http://www.example.net/foo");

	private static final URI ESCAPED_BASE = URI.create("http://www.example.net/subversion%20repository");

	@Test
	public void createURIBasicRepo() {
		final URI germanUmlautsURI = URIUtils.createURI(BASE, "/üöäÜÖÄß.txt");
		Assert.assertEquals("escaped german umlauts uri", "http://www.example.net/foo/%C3%BC%C3%B6%C3%A4%C3%9C%C3%96%C3%84%C3%9F.txt", germanUmlautsURI.toString());

		final URI specialCharsURI = URIUtils.createURI(BASE, "/^°²³\"§$%&{([)]}=?\\´`~+*'#,;.:-_µ@€<>| .txt");
		Assert.assertEquals("escaped special chars uri", "http://www.example.net/foo/%5E%C2%B0%C2%B2%C2%B3%22%C2%A7$%25&%7B(%5B)%5D%7D=%3F%5C%C2%B4%60~+*'%23,;.:-_%C2%B5@%E2%82%AC%3C%3E%7C%20.txt", specialCharsURI.toString());

		final URI utf8URI = URIUtils.createURI(BASE, "/ジャワ.txt"); // java
		final URI utf8URIencoded = URIUtils.createURI(BASE, "/\u30b8\u30e3\u30ef.txt"); // java
		Assert.assertEquals("escaped utf8 uri", "http://www.example.net/foo/%E3%82%B8%E3%83%A3%E3%83%AF.txt", utf8URI.toString());
		Assert.assertEquals("escaped utf8 uri", "http://www.example.net/foo/%E3%82%B8%E3%83%A3%E3%83%AF.txt", utf8URIencoded.toString());
	}

	@Test
	public void createURIEscapedRepo() {
		final URI germanUmlautsURI = URIUtils.createURI(ESCAPED_BASE, "/üöäÜÖÄß.txt");
		Assert.assertEquals("escaped german umlauts uri", "http://www.example.net/subversion%20repository/%C3%BC%C3%B6%C3%A4%C3%9C%C3%96%C3%84%C3%9F.txt", germanUmlautsURI.toString());

		final URI specialCharsURI = URIUtils.createURI(ESCAPED_BASE, "/^°²³\"§$%&{([)]}=?\\´`~+*'#,;.:-_µ@€<>| .txt");
		Assert.assertEquals("escaped special chars uri", "http://www.example.net/subversion%20repository/%5E%C2%B0%C2%B2%C2%B3%22%C2%A7$%25&%7B(%5B)%5D%7D=%3F%5C%C2%B4%60~+*'%23,;.:-_%C2%B5@%E2%82%AC%3C%3E%7C%20.txt", specialCharsURI.toString());

		final URI utf8URI = URIUtils.createURI(ESCAPED_BASE, "/ジャワ.txt"); // java
		final URI utf8URIencoded = URIUtils.createURI(ESCAPED_BASE, "/\u30b8\u30e3\u30ef.txt"); // java
		Assert.assertEquals("escaped utf8 uri", "http://www.example.net/subversion%20repository/%E3%82%B8%E3%83%A3%E3%83%AF.txt", utf8URI.toString());
		Assert.assertEquals("escaped utf8 uri", "http://www.example.net/subversion%20repository/%E3%82%B8%E3%83%A3%E3%83%AF.txt", utf8URIencoded.toString());
	}
}
