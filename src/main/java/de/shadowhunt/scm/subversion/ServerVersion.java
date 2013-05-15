package de.shadowhunt.scm.subversion;

public enum ServerVersion {

	V1_6("1.6"),
	V1_7("1.7");

	private final String name;

	private ServerVersion(final String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}
