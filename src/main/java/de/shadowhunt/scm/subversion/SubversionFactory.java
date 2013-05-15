package de.shadowhunt.scm.subversion;

import java.net.URI;

import javax.annotation.Nullable;

import de.shadowhunt.scm.subversion.v1_6.SubversionRepository1_6;
import de.shadowhunt.scm.subversion.v1_7.SubversionRepository1_7;

public final class SubversionFactory {

	public static final SubversionRepository getInstance(final URI root, final boolean trustServerCertificat, @Nullable final String user, final String password, @Nullable final String workstation, final ServerVersion version) {
		final SubversionRepository reposiotry;
		switch (version) {
			case V1_6:
				reposiotry = new SubversionRepository1_6(root, trustServerCertificat);
				break;
			case V1_7:
				reposiotry = new SubversionRepository1_7(root, trustServerCertificat);
				break;
			default:
				throw new SubversionException("unsupported subversion version: " + version);
		}

		if (user != null) {
			reposiotry.setCredentials(user, password, workstation);
		}
		return reposiotry;
	}

	private SubversionFactory() {
		// prevent instantiation
	}
}
