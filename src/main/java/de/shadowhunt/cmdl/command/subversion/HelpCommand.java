package de.shadowhunt.cmdl.command.subversion;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

public class HelpCommand extends AbstractCommand {

	public HelpCommand() {
		super("help");
	}

	@Override
	public void execute0(final CommandLine cmdl) {
		System.out.println("usage: <subcommand> [options] <resource>");
		System.out.println("Type '<subcommand>' --help for help on a specific subcommand.");
		System.out.println();

		System.out.println("Available subcommands:");
		System.out.println("   cat"); // done
		System.out.println("   checkout"); // done
		System.out.println("   commit");
		//		System.out.println("   copy"); TODO
		System.out.println("   delete");
		System.out.println("   help"); // done
		System.out.println("   info"); // done
		System.out.println("   list");
		System.out.println("   lock"); // done
		System.out.println("   log");
		//		System.out.println("   move"); TODO
		System.out.println("   propdel"); // done
		System.out.println("   propget"); // done
		System.out.println("   proplist"); // done
		System.out.println("   propset");
		System.out.println("   unlock"); // done
		System.out.println();
	}

	@Override
	protected Options getOptions() {
		return new Options();
	}
}
