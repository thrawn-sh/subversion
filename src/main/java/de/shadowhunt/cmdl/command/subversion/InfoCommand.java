package de.shadowhunt.cmdl.command.subversion;

import org.apache.commons.cli.CommandLine;

import de.shadowhunt.scm.subversion.SubversionInfo;

public class InfoCommand extends AbstractInfoCommand {

	public InfoCommand() {
		super("info");
	}

	@Override
	protected void execute0(final CommandLine cmdl) throws Exception {
		final SubversionInfo info = getInfo(cmdl);

		System.out.println("Path: " + info.getRelativePath());
		System.out.println("Repository Root: " + info.getRoot());
		System.out.println("Repository UUID:" + info.getRepositoryUuid());
		System.out.println("Revision: " + info.getRevision());
		if (info.isDirectory()) {
			System.out.println("Node Kind: directory");
		} else {
			System.out.println("Node Kind: file");
		}
		System.out.println("Checksum: " + info.getMd5());
		if (info.isLocked()) {
			System.out.println("Lock Token: " + info.getLockToken());
		}
	}
}
