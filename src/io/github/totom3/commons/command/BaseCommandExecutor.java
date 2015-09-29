package io.github.totom3.commons.command;

import io.github.totom3.commons.command.InvalidSubCommandException.Cause;
import io.github.totom3.commons.command.Subcommand.CommandFlags;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import static org.bukkit.ChatColor.GOLD;
import static org.bukkit.ChatColor.RED;
import static org.bukkit.ChatColor.YELLOW;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * A class that facilitate the process of handling subcommands for the
 * {@code Bukkit API}.
 * <p>
 * The {@code onCommand()} method is overridden and final. To use this system,
 * one must only extend this class, declare and annotate the sub-command methods
 * using the {@link Subcommand} annotation type. </p>
 * <p>
 * Whenever a user enters the associated command, it will check the first
 * argument. If it is equal to "help", then the help message will be printed to
 * that user. If not, then it will try to match it to a Subcommand, and will
 * execute it if it succeeds. If it does not, it will check if a default
 * Subcommand (a Subcommand with an empty or "default" name) was registered, and
 * execute it if it was. If not, it will return {@code false}, and the usage
 * command will be printed to the user, as specified in the appropriate
 * {@code plugin.yml} file. </p>
 * <p>
 * A sub-command method is a method that takes 2 parameters, in order : a
 * {@link CommandSender} and a {@link String}[] (the arguments). It must also be
 * annotated with the {@link Subcommand} annotation. It does not need to be
 * {@code public} as it will be set accessible via
 * {@link Method#setAccessible(boolean)}. The name of the method does not
 * matter. Here is an example of a valid sub-command method : </p>
 * <pre>
 * {@literal
 *
 * @Subcommand(name="op", description="Op'es someone", usage="{@literal <name>}",
 * flags=CommandFlags.OP_ONLY) private void opSubCommand(CommandSender sender,
 * String[] args) { // Do stuff... }}
 * </pre>
 *
 * @author Totom3
 * @see Subcommand
 */
public abstract class BaseCommandExecutor implements CommandExecutor {

    private final HashMap<String, SubCommandExecutor> subcmds;
    private final String commandName;
    private SubCommandExecutor defSubcmd;

    protected BaseCommandExecutor(String name) {
	this.commandName = name;
	this.subcmds = new HashMap<>(8);

	findSubcommands();
    }

    @Override
    public final boolean onCommand(CommandSender sender, Command cmnd, String label, String[] rawArgs) {
	// DEFAULT SUB COMMAND
	String subCmdName;
	if (rawArgs.length == 0 || (subCmdName = rawArgs[0].trim().toLowerCase()).isEmpty()) {
	    if (defSubcmd != null) {
		String[] args = (rawArgs.length == 0) ? new String[0] : Arrays.copyOfRange(rawArgs, 1, rawArgs.length);
		defSubcmd.execute(sender, args);
		return true;
	    }
	    return false;
	}

	// HELP MESSAGES
	if (subCmdName.equals("help")) {
	    SubCommandExecutor exec;
	    if (rawArgs.length == 1 || (exec = subcmds.get(rawArgs[1])) == null) {
		showHelp(sender, rawArgs);
	    } else {
		if (!sender.isOp() && (exec.isOpOnly() || exec.isHidden())) {
		    showHelp(sender, rawArgs);
		    return true;
		}
		String[] helpMessages = exec.getHelpMessages();
		if (helpMessages == null || helpMessages.length == 0) {
		    exec.showBasicHelp(sender);
		} else {
		    sender.sendMessage(GOLD + "Help for " + RED + "/" + commandName + " " + exec.getName() + YELLOW + ": ");
		    for (String msg : helpMessages) {
			sender.sendMessage("   "+msg);
		    }
		}
	    }
	    return true;
	}

	// EXECUTE SUB COMMAND
	SubCommandExecutor subExec = subcmds.get(subCmdName);
	if (subExec == null) {
	    if (defSubcmd != null) {
		defSubcmd.execute(sender, Arrays.copyOfRange(rawArgs, 1, rawArgs.length));
		return true;
	    }
	    return false;
	}

	subExec.execute(sender, Arrays.copyOfRange(rawArgs, 1, rawArgs.length));

	return true;
    }

    private void findSubcommands() throws InvalidSubCommandException {
	Class<?>[] expectedArgs = new Class<?>[]{CommandSender.class, String[].class};

	for (Method m : this.getClass().getDeclaredMethods()) {
	    Subcommand annotation = m.getAnnotation(Subcommand.class);

	    if (annotation == null) {
		continue;
	    }

	    if (!Arrays.equals(m.getParameterTypes(), expectedArgs)) {
		throw new InvalidSubCommandException(Cause.ARGUMENT_COUNT, m);
	    }

	    if (!m.getReturnType().equals(Void.TYPE)) {
		throw new InvalidSubCommandException(Cause.RETURN_TYPE, m);
	    }

	    m.setAccessible(true);

	    String name = annotation.name();

	    if (name.isEmpty() || name.equalsIgnoreCase("default")) {
		defSubcmd = new SubCommandExecutor(annotation, m);
	    } else {
		if (subcmds.containsKey(annotation.name())) {
		    throw new InvalidSubCommandException(Cause.DUPLICATE, m);
		}
		subcmds.put(annotation.name(), new SubCommandExecutor(annotation, m));
	    }
	}
    }

    public void showHelp(CommandSender sender, String[] args) {
	sender.sendMessage(ChatColor.GOLD + "Help for " + ChatColor.RED + "/" + commandName + ChatColor.GOLD + ": ");

	if (sender.isOp()) {
	    for (SubCommandExecutor sub : subcmds.values()) {
		sub.showBasicHelp(sender);
	    }
	} else {
	    for (SubCommandExecutor sub : subcmds.values()) {
		if (!sub.isOpOnly() && !sub.isHidden() && sub.isOnlineOnly()) {
		    sub.showBasicHelp(sender);
		}
	    }

	}
    }

    private class SubCommandExecutor {

	private final Method method;
	private final String name;
	private final String description;
	private final String[] helpMessages;
	private final byte flags;
	private final String usage;

	private SubCommandExecutor(Subcommand annotation, Method m) {
	    this.method = m;
	    this.helpMessages = annotation.helpMessages();
	    this.description = annotation.description();
	    this.flags = annotation.flags();
	    this.usage = annotation.usage();

	    String str = annotation.name();
	    this.name = str.isEmpty() || str.equalsIgnoreCase("default") ? "" : str;

	    for (int i = 0; i < helpMessages.length; i++) {
		helpMessages[i] = annotation.helpMessagePrefix() + helpMessages[i];
	    }
	}

	private void execute(CommandSender sender, String[] args) {
	    try {
		if (isOnlineOnly() && !(sender instanceof Player)) {
		    sender.sendMessage(ChatColor.RED + "You must be logged in to use this command!");
		    return;
		}

		if (isOpOnly() && !sender.isOp()) {
		    sender.sendMessage(ChatColor.RED + " You do not have the permission to use this command!");
		    return;
		}

		method.invoke(BaseCommandExecutor.this, sender, args);

	    } catch (IllegalAccessException | InvocationTargetException ex) {
		
		Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Could not execute sub-command " + name + ": ", ex);
	    }
	}

	private void showBasicHelp(CommandSender sender) {

	    sender.sendMessage(ChatColor.GOLD + "  /"
		    + BaseCommandExecutor.this.commandName + " " + ChatColor.RED + name + ChatColor.GOLD + " " + usage + " : "
		    + ChatColor.YELLOW + description);

	}

	public Method getMethod() {
	    return method;
	}

	public String getName() {
	    return name;
	}

	public String getDescription() {
	    return description;
	}

	public byte getFlags() {
	    return flags;
	}

	public String[] getHelpMessages() {
	    return Arrays.copyOf(helpMessages, helpMessages.length);
	}

	public boolean isOpOnly() {
	    return (getFlags() & CommandFlags.OP_ONLY) != 0;
	}

	public boolean isOnlineOnly() {
	    return (getFlags() & CommandFlags.ONLINE_ONLY) != 0;
	}

	public boolean isHidden() {
	    return (getFlags() & CommandFlags.HIDDEN) != 0;
	}
	
	public String getUsage() {
	    return usage;
	}
    }
}
