package de.shadowhunt.scm.subversion;

import java.io.InputStream;
import java.net.URI;
import java.util.List;

import javax.annotation.Nullable;

public interface SubversionRepository {

	public void delete(String resource, String message);

	public void deleteProperties(String resource, String message, SubversionProperty... properties);

	public InputStream download(String resource);

	public InputStream download(String resource, int version);

	public URI downloadURI(String resource);

	public URI downloadURI(String resource, int version);

	public boolean exists(String resource);

	public SubversionInfo info(String resource, boolean withCustomProperties);

	public SubversionInfo info(String resource, int version, boolean withCustomProperties);

	public SubversionLog lastLog(String resource);

	public List<SubversionInfo> list(String resource, Depth depth, boolean withCustomProperties);

	public void lock(String resource);

	public List<SubversionLog> log(String resource);

	public List<SubversionLog> log(URI uri, int startVersion, int endVersion);

	public void setCredentials(@Nullable String user, @Nullable String password, @Nullable String workstation);

	public void setProperties(String resource, String message, SubversionProperty... properties);

	public void unlock(String resource, SubversionInfo info);

	public void upload(String resource, String message, InputStream content);

	public void uploadWithProperties(String resource, String message, InputStream content, SubversionProperty... properties);

}
