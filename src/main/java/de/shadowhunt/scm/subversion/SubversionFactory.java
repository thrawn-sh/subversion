package de.shadowhunt.scm.subversion;

import java.net.URI;

import javax.annotation.Nullable;

public final class SubversionFactory {

	private SubversionFactory() {
		// prevent instantiation
	}

	public static final SubversionRepository getInstance(final URI root, final String user, final String password, @Nullable final String workstation, final ServerVersion version) {
		if (version != null) {
			switch (version) {
				case V1_6:
					return new SubversionRepository1_6(root, user, password, workstation);
				case V1_7:
					return new SubversionRepository1_7(root, user, password, workstation);
			}
		}
		throw new SubversionException("unsupported subversion version: " + version);
	}
}
