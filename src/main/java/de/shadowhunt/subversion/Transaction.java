package de.shadowhunt.subversion;

public final class Transaction {

	private final String id;

	public Transaction(final String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}
}
