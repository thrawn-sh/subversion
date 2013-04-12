package de.shadowhunt.cmdl.command.subversion;

import java.io.InputStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.io.IOUtils;

public class CatCommand extends AbstractContentCommand {

	public CatCommand() {
		super("cat");
	}

	@Override
	protected void execute0(final CommandLine cmdl) throws Exception {
		final InputStream content = getContent(cmdl);
		IOUtils.copy(content, System.out);
		IOUtils.closeQuietly(content);
	}
}
