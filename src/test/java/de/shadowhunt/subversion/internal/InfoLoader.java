package de.shadowhunt.subversion.internal;

import java.io.File;
import java.io.FileInputStream;

import javax.xml.parsers.SAXParser;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;

public class InfoLoader extends BaseLoader {

	private static final String SUFFIX = ".info";

	static class InfoHandler extends BasicHandler {

		private InfoImpl current = null;

		@Override
		public void endElement(final String uri, final String localName, final String qName) throws SAXException {
			final String name = getNameFromQName(qName);

			if ("token".equals(name)) {
				final String text = getText();
				current.setLockToken(text.substring(16));
				return;
			}

			if ("uuid".equals(name)) {
				current.setRepositoryUuid(getText());
				return;
			}
		}

		InfoImpl getInfo() {
			return current;
		}

		@Override
		public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) {
			clearText();

			final String name = getNameFromQName(qName);
			if ("commit".equals(name)) {
				final String revision = attributes.getValue("revision");
				current.setRevision(Revision.create(Integer.parseInt(revision)));
			}
		}
	}

	public static Info load(final Resource resource, final Revision revision) throws Exception {
		final File infoFile = new File(ROOT, resolve(revision) + resource.getValue() + SUFFIX);

		final SAXParser saxParser = BasicHandler.FACTORY.newSAXParser();
		final InfoHandler handler = new InfoHandler();

		saxParser.parse(infoFile, handler);
		final InfoImpl info = handler.getInfo();

		info.setResource(resource);
		final File f = new File(ROOT, resolve(revision) + resource.getValue());
		info.setDirectory(f.isDirectory());
		if (info.isFile()) {
			final FileInputStream fis = new FileInputStream(new File("foo"));
			try {
				info.setMd5(DigestUtils.md5Hex(fis));
			} finally {
				IOUtils.closeQuietly(fis);
			}
		}

		info.setCustomProperties(ResourcePropertyLoader.load(resource, revision));
		return info;
	}
}
