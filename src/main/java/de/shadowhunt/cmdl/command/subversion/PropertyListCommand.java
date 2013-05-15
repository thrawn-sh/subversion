package de.shadowhunt.cmdl.command.subversion;

import java.io.PrintWriter;

import org.apache.commons.cli.CommandLine;

import de.shadowhunt.scm.subversion.SubversionInfo;
import de.shadowhunt.scm.subversion.SubversionProperty;

public class PropertyListCommand extends AbstractInfoCommand {

	public PropertyListCommand(final PrintWriter out) {
		super("proplist", out);
	}

	@Override
	protected void execute0(final CommandLine cmdl) throws Exception {
		final SubversionInfo info = getInfo(cmdl);

		final SubversionProperty[] customProperties = info.getCustomProperties();
		if (customProperties.length > 0) {
			out.println("Properties on '" + info.getRelativePath() + "':");
			for (final SubversionProperty property : customProperties) {
				out.println("  " + property.getName());
			}
		}
	}
}
