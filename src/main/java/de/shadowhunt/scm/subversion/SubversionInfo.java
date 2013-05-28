package de.shadowhunt.scm.subversion;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.xml.parsers.SAXParser;

import org.xml.sax.Attributes;

import de.shadowhunt.scm.subversion.SubversionProperty.Type;

/**
 * Container that holds all status information for a single revision of a resource
 */
public class SubversionInfo {

	static class SubversionInfoHandler extends BasicHandler {

		private SubversionInfo current = null;

		private List<SubversionProperty> customProperties;

		private final boolean includeDirectories;

		private final List<SubversionInfo> infos = new ArrayList<SubversionInfo>();

		private boolean locktoken = false;

		private boolean resourceType = false;

		private final boolean withCustomProperties;

		SubversionInfoHandler(final boolean withCustomProperties, final boolean includeDirectories) {
			super();
			this.withCustomProperties = withCustomProperties;
			this.includeDirectories = includeDirectories;
		}

		@Override
		public void endElement(final String uri, final String localName, final String qName) {
			final String name = getNameFromQName(qName);

			if (current == null) {
				return;
			}

			if ("response".equals(name)) {
				if (withCustomProperties) {
					current.setCustomProperties(customProperties.toArray(new SubversionProperty[customProperties.size()]));
					customProperties = null;
				}

				infos.add(current);
				current = null;
				return;
			}

			if ("baseline-relative-path".equals(name)) {
				current.setRelativePath(getText());
				return;
			}

			if (resourceType && "collection".equals(name)) {
				if (!includeDirectories) {
					// we don't want to include directories in our result list
					current = null;
					return;
				}

				current.setDirectory(true);
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
				final int revision = Integer.parseInt(getText());
				current.setRevision(Revision.create(revision));
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

		List<SubversionInfo> getInfos() {
			return infos;
		}

		@Override
		public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) {
			clearText();

			final String name = getNameFromQName(qName);

			if ("response".equals(name)) {
				current = new SubversionInfo();
				locktoken = false;
				resourceType = false;

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

	private static final SubversionProperty[] EMPTY = new SubversionProperty[0];

	/**
	 * {@link Comparator} orders {@link SubversionInfo}s by their relative path
	 */
	public static final Comparator<SubversionInfo> PATH_COMPARATOR = new Comparator<SubversionInfo>() {

		@Override
		public int compare(final SubversionInfo si1, final SubversionInfo si2) {
			return si1.getRelativePath().compareTo(si2.getRelativePath());
		}
	};

	/**
	 * Reads status information for a single revision of a resource from the given {@link InputStream}
	 * @param in {@link InputStream} from which the status information is read (Note: will not be closed)
	 * @param withCustomProperties whether to read user defined properties
	 * @return {@link SubversionInfo} for the resource
	 */
	public static SubversionInfo read(final InputStream in, final boolean withCustomProperties) {
		final List<SubversionInfo> infos = readList(in, withCustomProperties, true);
		if (infos.isEmpty()) {
			throw new SubversionException("could not find any SubversionInfo in input");
		}
		return infos.get(0);
	}

	/**
	 * Reads a {@link List} of status information for a single revision of various resources from the given {@link InputStream}
	 * @param in {@link InputStream} from which the status information is read (Note: will not be closed)
	 * @param withCustomProperties whether to read user defined properties
	 * @param includeDirectories whether directory resources shall be included in the result
	 * @return {@link SubversionInfo} for the resources
	 */
	public static List<SubversionInfo> readList(final InputStream in, final boolean withCustomProperties, final boolean includeDirectories) {
		try {
			final SAXParser saxParser = BasicHandler.FACTORY.newSAXParser();
			final SubversionInfoHandler handler = new SubversionInfoHandler(withCustomProperties, includeDirectories);

			saxParser.parse(in, handler);
			return handler.getInfos();
		} catch (final Exception e) {
			throw new SubversionException("could not parse input", e);
		}
	}

	private SubversionProperty[] customProperties = EMPTY;

	private boolean directory;

	// NOTE: not part of xml response but determined by a response header
	private String lockOwner;

	private String lockToken;

	private String md5;

	private String relativePath;

	private String repositoryUuid;

	private Revision revision;

	private String root;

	SubversionInfo() {
		// prevent direct instantiation
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final SubversionInfo other = (SubversionInfo) obj;
		if (!Arrays.equals(customProperties, other.customProperties)) {
			return false;
		}
		if (directory != other.directory) {
			return false;
		}
		if (lockOwner == null) {
			if (other.lockOwner != null) {
				return false;
			}
		} else if (!lockOwner.equals(other.lockOwner)) {
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
		if (relativePath == null) {
			if (other.relativePath != null) {
				return false;
			}
		} else if (!relativePath.equals(other.relativePath)) {
			return false;
		}
		if (repositoryUuid == null) {
			if (other.repositoryUuid != null) {
				return false;
			}
		} else if (!repositoryUuid.equals(other.repositoryUuid)) {
			return false;
		}
		if (revision == null) {
			if (other.revision != null) {
				return false;
			}
		} else if (!revision.equals(other.revision)) {
			return false;
		}
		if (root == null) {
			if (other.root != null) {
				return false;
			}
		} else if (!root.equals(other.root)) {
			return false;
		}
		return true;
	}

	/**
	 * Returns an array of the custom {@link SubversionProperty}
	 * @return the array of the custom {@link SubversionProperty} or an empty array if there a non
	 */
	public SubversionProperty[] getCustomProperties() {
		return Arrays.copyOf(customProperties, customProperties.length);
	}

	/**
	 * Returns a name of the lock owner
	 * @return the name of the lock owner or {@code null} if the resource is not locked
	 */
	@CheckForNull
	public String getLockOwner() {
		return lockOwner;
	}

	/**
	 * Returns a lock-token
	 * @return the lock-token or {@code null} if the resource is not locked
	 */
	@CheckForNull
	public String getLockToken() {
		return lockToken;
	}

	/**
	 * Returns a MD5 checksum of the resource
	 * @return the MD5 checksum of the resource or {@code null} if the resource is a directory
	 */
	@CheckForNull
	public String getMd5() {
		return md5;
	}

	/**
	 * Returns a relative path of the resource (relative to the root of the repository)
	 * @return the relative path of the resource (relative to the root of the repository)
	 */
	public String getRelativePath() {
		return relativePath;
	}

	/**
	 * Returns a globally unique identifier of the repository
	 * @return the globally unique identifier of the repository
	 */
	public String getRepositoryUuid() {
		return repositoryUuid;
	}

	/**
	 * Returns a {@link Revision} of the resource
	 * @return the {@link Revision} of the resource
	 */
	public Revision getRevision() {
		return revision;
	}

	/**
	 * Returns a root-path of the repository (relative to the root of the subversion server)
	 * @return the root-path of the repository (relative to the root of the subversion server)
	 */
	public String getRoot() {
		return root;
	}

	/**
	 * Returns the value of the custom property with the given name
	 * @param name name of the custom property
	 * @return the value of the custom property or {@code null} if no custom property with the given name was found
	 */
	@CheckForNull
	public String getSubversionPropertyValue(final String name) {
		for (final SubversionProperty property : customProperties) {
			if (name.equals(property.getName())) {
				return property.getValue();
			}
		}
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + Arrays.hashCode(customProperties);
		result = (prime * result) + (directory ? 1231 : 1237);
		result = (prime * result) + ((lockOwner == null) ? 0 : lockOwner.hashCode());
		result = (prime * result) + ((lockToken == null) ? 0 : lockToken.hashCode());
		result = (prime * result) + ((md5 == null) ? 0 : md5.hashCode());
		result = (prime * result) + ((relativePath == null) ? 0 : relativePath.hashCode());
		result = (prime * result) + ((repositoryUuid == null) ? 0 : repositoryUuid.hashCode());
		result = (prime * result) + ((revision == null) ? 0 : revision.hashCode());
		result = (prime * result) + ((root == null) ? 0 : root.hashCode());
		return result;
	}

	/**
	 * Determines if the resource is a directory
	 * @return {@code true} if the resource is a directory otherwise {@code false}
	 */
	public boolean isDirectory() {
		return directory;
	}

	/**
	 * Determines if the resource is a file
	 * @return {@code true} if the resource is a file otherwise {@code false}
	 */
	public boolean isFile() {
		return !directory;
	}

	/**
	 * Determines if the resource is locked
	 * @return {@code true} if the resource is locked otherwise {@code false}
	 */
	public boolean isLocked() {
		return lockToken != null;
	}

	void setCustomProperties(@Nullable final SubversionProperty[] customProperties) {
		if ((customProperties == null) || (customProperties.length == 0)) {
			this.customProperties = EMPTY;
		} else {
			this.customProperties = Arrays.copyOf(customProperties, customProperties.length);
		}
	}

	void setDirectory(final boolean directory) {
		this.directory = directory;
	}

	void setFile(final boolean file) {
		directory = !file;
	}

	void setLockOwner(final String lockOwner) {
		this.lockOwner = lockOwner;
	}

	void setLockToken(final String lockToken) {
		this.lockToken = lockToken;
	}

	void setMd5(final String md5) {
		this.md5 = md5;
	}

	void setRelativePath(final String relativePath) {
		this.relativePath = relativePath;
	}

	void setRepositoryUuid(final String repositoryUuid) {
		this.repositoryUuid = repositoryUuid;
	}

	void setRevision(final Revision revision) {
		this.revision = revision;
	}

	void setRoot(final String root) {
		this.root = root;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("SubversionInfo [customProperties=");
		builder.append(Arrays.toString(customProperties));
		builder.append(", directory=");
		builder.append(directory);
		builder.append(", lockOwner=");
		builder.append(lockOwner);
		builder.append(", lockToken=");
		builder.append(lockToken);
		builder.append(", md5=");
		builder.append(md5);
		builder.append(", repositoryUuid=");
		builder.append(repositoryUuid);
		builder.append(", revision=");
		builder.append(revision);
		builder.append(", root=");
		builder.append(root);
		builder.append(", relativePath=");
		builder.append(relativePath);
		builder.append("]");
		return builder.toString();
	}
}
