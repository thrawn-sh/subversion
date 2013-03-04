package de.shadowhunt.scm.subversion;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;

public class SubversionInfo {

	static class SubversionInfoHandler extends BasicHandler {

		private SubversionInfo current = null;

		private final List<SubversionInfo> infos = new ArrayList<SubversionInfo>();

		private boolean locktoken = false;

		private boolean resourceType = false;

		@Override
		public void endElement(final String uri, final String localName, final String qName) {
			final String name = getNameFromQName(qName);

			if ("response".equals(name)) {
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

			if ("PKIT_STATE".equals(name)) {
				current.setState(getText());
				return;
			}

			if ("version-name".equals(name)) {
				current.setVersion(getText());
				return;
			}
		}

		@Override
		public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) {
			clearText();

			final String name = getNameFromQName(qName);

			if ("response".equals(name)) {
				current = new SubversionInfo();
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

		public List<SubversionInfo> getInfos() {
			return infos;
		}
	}

	public static SubversionInfo read(final InputStream in) throws Exception {
		return readList(in).get(0);
	}

	public static List<SubversionInfo> readList(final InputStream in) throws Exception {
		final SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(false);
		factory.setValidating(false);
		final SAXParser saxParser = factory.newSAXParser();
		final SubversionInfoHandler handler = new SubversionInfoHandler();

		saxParser.parse(in, handler);
		return handler.getInfos();
	}

	private boolean direcotry;

	private String lockToken;

	private String md5;

	private String repositoryUuid;

	private String state;

	private String version;

	SubversionInfo() {
		// prevent direct instantiation
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

	public String getState() {
		return state;
	}

	public String getVersion() {
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

	public void setState(final String state) {
		this.state = state;
	}

	public void setVersion(final String version) {
		this.version = version;
	}

	@Override
	public String toString() {
		return "SubversionInfo [direcotry=" + direcotry + ", lockToken=" + lockToken + ", md5=" + md5
				+ ", repositoryUuid=" + repositoryUuid + ", state=" + state + ", version=" + version + "]";
	}
}
