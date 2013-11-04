package de.shadowhunt.subversion.internal.httpv1.v1_6;

import de.shadowhunt.subversion.internal.AbstractPrepare;

public class Prepare extends AbstractPrepare {

	public Prepare() {
		super(Helper.getDumpUri(), Helper.getBase());
	}
}
