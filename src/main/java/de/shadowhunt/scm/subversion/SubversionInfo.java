package de.shadowhunt.scm.subversion;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;

import de.shadowhunt.scm.subversion.SubversionProperty.Type;

public class SubversionInfo {

	static class SubversionInfoHandler extends BasicHandler {

		private SubversionInfo current = null;

		private List<SubversionProperty> customProperties;

		private final List<SubversionInfo> infos = new ArrayList<SubversionInfo>();

		private boolean locktoken = false;

		private boolean resourceType = false;

		private final boolean withCustomProperties;

		SubversionInfoHandler(final boolean withCustomProperties) {
			this.withCustomProperties = withCustomProperties;
		}

		@Override
		public void endElement(final String uri, final String localName, final String qName) {
			final String name = getNameFromQName(qName);

			if ("response".equals(name)) {
				if (withCustomProperties) {
					current.setCustomProperties(customProperties);
					customProperties = null;
				}

				infos.add(current);
				current = null;
				return;
			}

			if (current == null) {
				return;
			}

			if (resourceType && "collection".equals(name)) {
				current.setDirecotry(true);
				resourceType = false;
				return;
			}

			if (locktoken && "href".equals(name)) {
				current.setLockToken(getText());
				locktoken = false;
				return;
			}

			if ("md5-checksum".equals(name)) {
				current.setMd5(getText());
				return;
			}

			if ("repository-uuid".equals(name)) {
				current.setRepositoryUuid(getText());
				return;
			}

			if ("version-name".equals(name)) {
				final long version = Long.parseLong(getText());
				current.setVersion(version);
				return;
			}

			if (!withCustomProperties) {
				return;
			}

			final String namespace = getNamespaceFromQName(qName);
			if ("C".equals(namespace)) {
				final SubversionProperty property = new SubversionProperty(Type.CUSTOM, name, getText());
				customProperties.add(property);
			}
		}

		public List<SubversionInfo> getInfos() {
			return infos;
		}

		@Override
		public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) {
			clearText();

			final String name = getNameFromQName(qName);

			if ("response".equals(name)) {
				current = new SubversionInfo();

				if (withCustomProperties) {
					customProperties = new ArrayList<SubversionProperty>();
				}
				return;
			}

			if ("locktoken".equals(name)) {
				locktoken = true;
				return;
			}

			if ("resourcetype".equals(name)) {
				resourceType = true;
				return;
			}
		}
	}

	private static List<SubversionProperty> EMPTY = Collections.emptyList();

	public static SubversionInfo read(final InputStream in, final boolean withCustomProperties) throws Exception {
		return readList(in, withCustomProperties).get(0);
	}

	public static List<SubversionInfo> readList(final InputStream in, final boolean withCustomProperties) throws Exception {
		final SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(false);
		factory.setValidating(false);
		final SAXParser saxParser = factory.newSAXParser();
		final SubversionInfoHandler handler = new SubversionInfoHandler(withCustomProperties);

		saxParser.parse(in, handler);
		return handler.getInfos();
	}

	private List<SubversionProperty> customProperties = new ArrayList<SubversionProperty>();

	private boolean direcotry;

	private String lockToken;

	private String md5;

	private String repositoryUuid;

	private long version;

	SubversionInfo() {
		// prevent direct instantiation
	}

	public List<SubversionProperty> getCustomProperties() {
		return (customProperties == EMPTY) ? EMPTY : new ArrayList<SubversionProperty>(customProperties);
	}

	public String getLockToken() {
		return lockToken;
	}

	public String getMd5() {
		return md5;
	}

	public String getRepositoryUuid() {
		return repositoryUuid;
	}

	public long getVersion() {
		return version;
	}

	public boolean isDirecotry() {
		return direcotry;
	}

	public boolean isFile() {
		return !direcotry;
	}

	public boolean isLocked() {
		return lockToken != null;
	}

	public void setCustomProperties(final List<SubversionProperty> customProperties) {
		final boolean nullOrEmpty = (customProperties == null) || customProperties.isEmpty();
		this.customProperties = nullOrEmpty ? EMPTY : new ArrayList<SubversionProperty>(customProperties);
	}

	public void setDirecotry(final boolean direcotry) {
		this.direcotry = direcotry;
	}

	public void setFile(final boolean file) {
		direcotry = !file;
	}

	public void setLockToken(final String lockToken) {
		this.lockToken = lockToken;
	}

	public void setMd5(final String md5) {
		this.md5 = md5;
	}

	public void setRepositoryUuid(final String repositoryUuid) {
		this.repositoryUuid = repositoryUuid;
	}

	public void setVersion(final long version) {
		this.version = version;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("SubversionInfo [customProperties=");
		builder.append(customProperties);
		builder.append(", direcotry=");
		builder.append(direcotry);
		builder.append(", lockToken=");
		builder.append(lockToken);
		builder.append(", md5=");
		builder.append(md5);
		builder.append(", repositoryUuid=");
		builder.append(repositoryUuid);
		builder.append(", version=");
		builder.append(version);
		builder.append("]");
		return builder.toString();
	}
}
