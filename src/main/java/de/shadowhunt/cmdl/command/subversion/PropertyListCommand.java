package de.shadowhunt.cmdl.command.subversion;

import org.apache.commons.cli.CommandLine;

import de.shadowhunt.scm.subversion.SubversionInfo;
import de.shadowhunt.scm.subversion.SubversionProperty;

public class PropertyListCommand extends AbstractInfoCommand {

	public PropertyListCommand() {
		super("proplist");
	}

	@Override
	protected void execute0(final CommandLine cmdl) throws Exception {
		final SubversionInfo info = getInfo(cmdl);

		final SubversionProperty[] customProperties = info.getCustomProperties();
		if (customProperties.length > 0) {
			System.out.println("Properties on '" + info.getRelativePath() + "':");
			for (final SubversionProperty property : customProperties) {
				System.out.println("  " + property.getName());
			}
		}
	}
}
