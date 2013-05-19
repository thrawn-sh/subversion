package de.shadowhunt.scm.subversion;

/**
 * {code Depth} defines the recursion level for the listing call {@link SubversionRepository #list(String, Depth, boolean)}
 */
public enum Depth {

	/**
	 * only list the resources itself, no sub-resources
	 */
	EMPTY("0"),

	/**
	 * only list all direct file sub-resources
	 */
	FILES("1"),

	/**
	 * only list all direct sub-resources (files and directories)
	 */
	IMMEDIATES("1"),

	/**
	 * recursively list all sub-resources (files and directories)
	 */
	INFINITY("-1");

	/**
	 * recursion level
	 */
	public final String value;

	private Depth(final String value) {
		this.value = value;
	}
}