package de.shadowhunt.cmdl.command.subversion;

import java.io.PrintWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

public class HelpCommand extends AbstractCommand {

	public HelpCommand(final PrintWriter out) {
		super("help", out);
	}

	@Override
	public void execute0(final CommandLine cmdl) {
		out.println("usage: <subcommand> [options] <resource>");
		out.println("Type '<subcommand>' --help for help on a specific subcommand.");
		out.println();

		out.println("Available subcommands:");
		out.println("   cat"); // done
		out.println("   checkout"); // done
		out.println("   commit");
		//		out.println("   copy"); TODO
		out.println("   delete");
		out.println("   help"); // done
		out.println("   info"); // done
		out.println("   list");
		out.println("   lock"); // done
		out.println("   log");
		//		out.println("   move"); TODO
		out.println("   propdel"); // done
		out.println("   propget"); // done
		out.println("   proplist"); // done
		out.println("   propset");
		out.println("   unlock"); // done
		out.println();
	}

	@Override
	protected Options getOptions() {
		return new Options();
	}
}
