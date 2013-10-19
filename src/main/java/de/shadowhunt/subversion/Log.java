package de.shadowhunt.subversion;

import java.util.Date;

public interface Log {
	Date getDate();

	String getMessage();

	Revision getRevision();

	String getUser();
}
