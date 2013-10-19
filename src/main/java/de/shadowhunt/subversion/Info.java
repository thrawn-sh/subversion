package de.shadowhunt.subversion;

import java.util.Comparator;
import javax.annotation.CheckForNull;

public interface Info {
	/**
	 * {@link java.util.Comparator} orders {@link de.shadowhunt.subversion.Info}s by their relative {@link Resource}
	 */
	Comparator<Info> RESOURCE_COMPARATOR = new Comparator<Info>() {

		@Override
		public int compare(final Info i1, final Info i2) {
			return i1.getResource().compareTo(i2.getResource());
		}
	};

	ResourceProperty[] getCustomProperties();

	@CheckForNull
	String getLockOwner();

	@CheckForNull
	String getLockToken();

	@CheckForNull
	String getMd5();

	String getRepositoryUuid();

	Resource getResource();

	@CheckForNull
	String getResourcePropertyValue(String name);

	Revision getRevision();

	boolean isDirectory();

	boolean isFile();

	boolean isLocked();
}
