package de.shadowhunt.scm.subversion;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
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
			super();
			this.withCustomProperties = withCustomProperties;
		}

		@Override
		public void endElement(final String uri, final String localName, final String qName) {
			final String name = getNameFromQName(qName);

			if ("response".equals(name)) {
				if (withCustomProperties) {
					current.setCustomProperties(customProperties.toArray(new SubversionProperty[customProperties.size()]));
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

			if ("version-controlled-configuration".equals(name)) {
				final String config = getText();
				final int index = config.indexOf('!');
				if (index >= 1) {
					current.setRoot(config.substring(0, index - 1));
				}
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

		@Override
		public String toString() {
			final StringBuilder builder = new StringBuilder();
			builder.append("SubversionInfoHandler [current=");
			builder.append(current);
			builder.append(", customProperties=");
			builder.append(customProperties);
			builder.append(", infos=");
			builder.append(infos);
			builder.append(", locktoken=");
			builder.append(locktoken);
			builder.append(", resourceType=");
			builder.append(resourceType);
			builder.append(", withCustomProperties=");
			builder.append(withCustomProperties);
			builder.append("]");
			return builder.toString();
		}
	}

	private static final SubversionProperty[] EMPTY = new SubversionProperty[0];

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + Arrays.hashCode(customProperties);
		result = (prime * result) + (direcotry ? 1231 : 1237);
		result = (prime * result) + ((lockToken == null) ? 0 : lockToken.hashCode());
		result = (prime * result) + ((md5 == null) ? 0 : md5.hashCode());
		result = (prime * result) + ((repositoryUuid == null) ? 0 : repositoryUuid.hashCode());
		result = (prime * result) + ((root == null) ? 0 : root.hashCode());
		result = (prime * result) + (int) (version ^ (version >>> 32));
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof SubversionInfo)) {
			return false;
		}
		final SubversionInfo other = (SubversionInfo) obj;
		if (!Arrays.equals(customProperties, other.customProperties)) {
			return false;
		}
		if (direcotry != other.direcotry) {
			return false;
		}
		if (lockToken == null) {
			if (other.lockToken != null) {
				return false;
			}
		} else if (!lockToken.equals(other.lockToken)) {
			return false;
		}
		if (md5 == null) {
			if (other.md5 != null) {
				return false;
			}
		} else if (!md5.equals(other.md5)) {
			return false;
		}
		if (repositoryUuid == null) {
			if (other.repositoryUuid != null) {
				return false;
			}
		} else if (!repositoryUuid.equals(other.repositoryUuid)) {
			return false;
		}
		if (root == null) {
			if (other.root != null) {
				return false;
			}
		} else if (!root.equals(other.root)) {
			return false;
		}
		if (version != other.version) {
			return false;
		}
		return true;
	}

	public static SubversionProperty[] getEmpty() {
		return EMPTY;
	}

	public static SubversionInfo read(final InputStream in, final boolean withCustomProperties) {
		final List<SubversionInfo> infos = readList(in, withCustomProperties);
		if (infos.isEmpty()) {
			throw new SubversionException("could not find any SubversionInfo in input");
		}
		return infos.get(0);
	}

	public static List<SubversionInfo> readList(final InputStream in, final boolean withCustomProperties) {
		final SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(false);
		factory.setValidating(false);

		try {
			final SAXParser saxParser = factory.newSAXParser();
			final SubversionInfoHandler handler = new SubversionInfoHandler(withCustomProperties);

			saxParser.parse(in, handler);
			return handler.getInfos();
		} catch (final Exception e) {
			throw new SubversionException("could not parse input", e);
		}
	}

	private SubversionProperty[] customProperties = EMPTY;

	private boolean direcotry;

	private String lockToken;

	private String md5;

	private String repositoryUuid;

	private String root;

	private long version;

	SubversionInfo() {
		// prevent direct instantiation
	}

	public SubversionProperty[] getCustomProperties() {
		return Arrays.copyOf(customProperties, customProperties.length);
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

	public String getRoot() {
		return root;
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

	public void setCustomProperties(final SubversionProperty[] customProperties) {
		if ((customProperties == null) || (customProperties.length == 0)) {
			this.customProperties = EMPTY;
		} else {
			this.customProperties = Arrays.copyOf(customProperties, customProperties.length);
		}
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

	public void setRoot(final String root) {
		this.root = root;
	}

	public void setVersion(final long version) {
		this.version = version;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("SubversionInfo [customProperties=");
		builder.append(Arrays.toString(customProperties));
		builder.append(", direcotry=");
		builder.append(direcotry);
		builder.append(", lockToken=");
		builder.append(lockToken);
		builder.append(", md5=");
		builder.append(md5);
		builder.append(", repositoryUuid=");
		builder.append(repositoryUuid);
		builder.append(", root=");
		builder.append(root);
		builder.append(", version=");
		builder.append(version);
		builder.append("]");
		return builder.toString();
	}
}
