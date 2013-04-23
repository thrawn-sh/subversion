package de.shadowhunt.scm.subversion;

public enum Depth {

	EMPTY("0"),
	FILES("1"),
	IMMEDIATES("1"),
	INFINITY("-1");

	public final String value;

	private Depth(final String value) {
		this.value = value;
	}
}
