package de.shadowhunt.cmdl.command.subversion;

import java.io.PrintWriter;

import org.apache.commons.cli.CommandLine;

import de.shadowhunt.scm.subversion.SubversionInfo;

public class InfoCommand extends AbstractInfoCommand {

	public InfoCommand(final PrintWriter out) {
		super("info", out);
	}

	@Override
	protected void execute0(final CommandLine cmdl) throws Exception {
		final SubversionInfo info = getInfo(cmdl);

		out.println("Path: " + info.getRelativePath());
		out.println("Repository Root: " + info.getRoot());
		out.println("Repository UUID:" + info.getRepositoryUuid());
		out.println("Revision: " + info.getRevision());
		if (info.isDirectory()) {
			out.println("Node Kind: directory");
		} else {
			out.println("Node Kind: file");
		}
		out.println("Checksum: " + info.getMd5());
		if (info.isLocked()) {
			out.println("Lock Token: " + info.getLockToken());
		}
	}
}
