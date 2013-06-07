package de.shadowhunt.cmdl.command.subversion;

import java.io.PrintWriter;

import org.apache.commons.cli.CommandLine;

import de.shadowhunt.subversion.InfoEntry;
import de.shadowhunt.subversion.ResourceProperty;

/**
 * Subversion proplist command
 */
public class PropertyListCommand extends AbstractInfoCommand {

	/**
	 * Create a new Subversion proplist command
	 * @param out {@link PrintWriter} to write the complete command output to (will not be closed)
	 */
	public PropertyListCommand(final PrintWriter out) {
		super("proplist", out);
	}

	@Override
	protected void execute0(final CommandLine cmdl) throws Exception {
		final InfoEntry info = getInfo(cmdl);

		final ResourceProperty[] customProperties = info.getCustomProperties();
		if (customProperties.length > 0) {
			out.println("Properties on '" + info.getPath() + "':");
			for (final ResourceProperty property : customProperties) {
				out.println("  " + property.getName());
			}
		}
	}
}
