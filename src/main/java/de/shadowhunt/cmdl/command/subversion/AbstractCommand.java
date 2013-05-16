package de.shadowhunt.cmdl.command.subversion;

import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Arrays;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import de.shadowhunt.cmdl.command.Command;
import de.shadowhunt.scm.subversion.ServerVersion;
import de.shadowhunt.scm.subversion.SubversionFactory;
import de.shadowhunt.scm.subversion.SubversionRepository;

abstract class AbstractCommand implements Command {

	private static final String HELP_OPTION = "h";

	private static final String PASSWORD_OPTION = "p";

	private static final String REPOSITORY_OPTION = "r";

	private static final String SERVER_OPTION = "s";

	private static final String TRUST_SERVER_OPTION = "T";

	private static final String USERNAME_OPTION = "u";

	private static final String WORKSPACE_OPTION = "w";

	private final String name;

	protected final PrintWriter out;

	protected AbstractCommand(final String name, final PrintWriter out) {
		this.name = name;
		this.out = out;
	}

	protected final SubversionRepository createRepository(final CommandLine cmdl) {
		final URI root = getRepositoryRoot(cmdl);
		final String user = getUser(cmdl);
		final String password = getPassword(cmdl);
		final String workstation = getWorkstation(cmdl);
		final ServerVersion version = getServerVersion(cmdl);
		final boolean trustServer = getTrustServer(cmdl);

		final SubversionRepository repository = SubversionFactory.getInstance(root, trustServer, version);
		repository.setCredentials(user, password, workstation);
		return repository;

	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final AbstractCommand other = (AbstractCommand) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}

	@Override
	public final void execute(final String... arguments) throws Exception {
		final CommandLineParser parser = new BasicParser();
		final Options options = getOptions();
		final CommandLine cmdl = parser.parse(options, arguments);
		if (cmdl.hasOption(HELP_OPTION)) {
			final HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(getName(), options);
			return;
		}
		execute0(cmdl);
	}

	protected abstract void execute0(final CommandLine cmdl) throws Exception;

	@Override
	public String getName() {
		return name;
	}

	protected Options getOptions() {
		final Options options = new Options();
		options.addOption(new Option(HELP_OPTION, "help", false, "print this help message"));

		options.addOption(new Option(USERNAME_OPTION, "username", true, "specify the username (for NTLM: DOMAIN\\USER)"));
		options.addOption(new Option(PASSWORD_OPTION, "password", true, "specify the password"));

		final Option repository = new Option(REPOSITORY_OPTION, "repository", true, "specify the url to a repository root");
		repository.setRequired(true);
		options.addOption(repository);

		options.addOption(new Option(WORKSPACE_OPTION, "workstation", true, "specify the name of the host"));

		options.addOption(new Option(SERVER_OPTION, "server", true, "specify the version of the server "
				+ Arrays.toString(ServerVersion.values())));

		options.addOption(new Option(TRUST_SERVER_OPTION, "trust-server-cert", false, "accept SSL server certificates from unknown certificate authorities without prompting"));

		return options;
	}

	protected final String getPassword(final CommandLine cmdl) {
		return cmdl.getOptionValue(PASSWORD_OPTION);
	}

	protected final URI getRepositoryRoot(final CommandLine cmdl) {
		final String value = cmdl.getOptionValue(REPOSITORY_OPTION);
		return URI.create(value);
	}

	protected final ServerVersion getServerVersion(final CommandLine cmdl) {
		final String value = cmdl.getOptionValue(SERVER_OPTION, "1.6");
		if ("1.6".equals(value)) {
			return ServerVersion.V1_6;
		}
		if ("1.7".equals(value)) {
			return ServerVersion.V1_7;
		}
		throw new IllegalArgumentException(value + " is not a valid server version: "
				+ Arrays.toString(ServerVersion.values()));
	}

	protected final String getTargetResource(final CommandLine cmdl) {
		final String[] args = cmdl.getArgs();
		if (args.length > 0) {
			return args[args.length - 1];
		}
		return null;
	}

	private boolean getTrustServer(final CommandLine cmdl) {
		return cmdl.hasOption(TRUST_SERVER_OPTION);
	}

	protected final String getUser(final CommandLine cmdl) {
		return cmdl.getOptionValue(USERNAME_OPTION);
	}

	protected final String getWorkstation(final CommandLine cmdl) {
		final String value = cmdl.getOptionValue(WORKSPACE_OPTION, "");
		if ("".equals(value)) {
			try {
				return InetAddress.getLocalHost().getHostName();
			} catch (final UnknownHostException e) {
				return "";
			}
		}
		return value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public final String toString() {
		return name;
	}
}
