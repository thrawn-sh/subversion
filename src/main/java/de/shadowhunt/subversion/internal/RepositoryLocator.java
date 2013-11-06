package de.shadowhunt.subversion.internal;

import java.net.URI;

import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;

import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.Resource;

public interface RepositoryLocator {

	boolean isSupported(Repository.ProtocolVersion version);

	Repository create(URI repository, Resource prefix, HttpClient client, HttpContext context);
}
