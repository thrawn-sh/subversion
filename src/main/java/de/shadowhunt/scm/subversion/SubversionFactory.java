package de.shadowhunt.scm.subversion;

import java.net.URI;

import javax.annotation.Nullable;

import de.shadowhunt.scm.subversion.v1_6.SubversionRepository1_6;
import de.shadowhunt.scm.subversion.v1_7.SubversionRepository1_7;

public final class SubversionFactory {

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

	private SubversionFactory() {
		// prevent instantiation
	}
}
