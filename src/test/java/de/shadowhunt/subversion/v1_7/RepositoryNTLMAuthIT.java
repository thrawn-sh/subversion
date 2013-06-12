package de.shadowhunt.subversion.v1_7;

import java.net.URI;

import de.shadowhunt.subversion.AbstractRepositoryLockedWriteIT;
import de.shadowhunt.subversion.ServerVersion;

public class RepositoryNTLMAuthIT extends AbstractRepositoryLockedWriteIT {

	public RepositoryNTLMAuthIT() {
		super(URI.create("http://subversion-17.vm.shadowhunt.de/svn-ntlm/test"), ServerVersion.V1_7, "SVNDOMAIN\\svnuser", "svnpass", "svn-client.shadowhunt.de");
	}

	@Override
	protected String getUsername() {
		final String username = super.getUsername();
		final int indexOf = username.indexOf('\\');
		return username.substring(indexOf + 1);
	}
}
