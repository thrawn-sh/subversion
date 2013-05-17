package de.shadowhunt.cmdl.command.subversion;

import java.io.InputStream;
import java.io.PrintWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.io.IOUtils;

/**
 * Subversion cat command
 */
public class CatCommand extends AbstractContentCommand {

	/**
	 * Create a new Subversion cat command
	 * @param out {@link PrintWriter} to write the complete command output to (will not be closed)
	 */
	public CatCommand(final PrintWriter out) {
		super("cat", out);
	}

	@Override
	protected void execute0(final CommandLine cmdl) throws Exception {
		final InputStream content = getContent(cmdl);
		IOUtils.copy(content, out);
		IOUtils.closeQuietly(content);
	}
}
