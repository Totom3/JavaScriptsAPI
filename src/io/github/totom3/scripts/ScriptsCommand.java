package io.github.totom3.scripts;

import io.github.totom3.commons.command.BaseCommandExecutor;
import io.github.totom3.commons.command.CommandSenders;
import io.github.totom3.commons.command.Subcommand;
import io.github.totom3.commons.command.Subcommand.CommandFlags;
import io.github.totom3.scripts.internal.ExitScriptException;
import io.github.totom3.scripts.internal.JSContext;
import java.io.FileNotFoundException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

/**
 *
 * @author Totom3
 */
public class ScriptsCommand extends BaseCommandExecutor {

    private static final String SUCCESS = ChatColor.DARK_GREEN + "[JavaScripts] " + ChatColor.GREEN;
    private static final String INFO = ChatColor.GOLD + "[JavaScripts] " + ChatColor.YELLOW;
    private static final String ERROR = ChatColor.DARK_RED + "[JavaScripts] " + ChatColor.RED;

    private final ScriptsMain main;
    private final ScriptsCache cache;

    ScriptsCommand(ScriptsMain main) {
	super("scripts2");
	this.main = main;
	this.cache = main.getCache();
    }

    @Subcommand(name = "refresh",
		description = "Refreshes a script",
		usage = "<script>",
		flags = CommandFlags.OP_ONLY)
    private void refresh(CommandSender sender, String[] args) {
	if (args.length != 1) {
	    sender.sendMessage(ERROR + "Usage: /scripts refresh <script>");
	    return;
	}

	String name = args[0];
	cache.unload(name);

	if (loadScript(name, sender) != null) {
	    sender.sendMessage(SUCCESS + "Successfully refreshed script");
	}
    }

    @Subcommand(name = "load",
		description = "Loads a script",
		usage = "<script name>",
		flags = CommandFlags.OP_ONLY)
    private void load(CommandSender sender, String[] args) {
	if (args.length != 1) {
	    sender.sendMessage(ERROR + "Usage: /scripts load <script name>");
	    return;
	}

	String name = args[0];
	if (cache.isLoaded(name)) {
	    sender.sendMessage(ERROR + "Script is already loaded");
	    return;
	}

	Script script = loadScript(name, sender);
	if (script != null) {
	    sender.sendMessage(SUCCESS + "Successfully loaded script " + name);
	}
    }

    @Subcommand(name = "unload",
		description = "Unloads a script",
		usage = "<script name>",
		flags = CommandFlags.OP_ONLY)
    private void unload(CommandSender sender, String[] args) {
	if (args.length != 1) {
	    sender.sendMessage(ERROR + "Usage: /scripts load <script name>");
	    return;
	}

	String name = args[0];
	if (cache.unload(name) != null) {
	    sender.sendMessage(SUCCESS + "Successfully unloaded script " + name);
	} else {
	    sender.sendMessage(ERROR + "Script " + name + " wasn't loaded");
	}
    }

    @Subcommand(name = "list",
		description = "Lists all loaded scripts",
		flags = CommandFlags.OP_ONLY)
    private void list(CommandSender sender, String[] args) {
	String scriptList = StringUtils.join(cache.all().keySet(), ", ");
	sender.sendMessage(INFO + "Currently loaded scripts: " + ChatColor.YELLOW + scriptList);
    }

    @Subcommand(name = "exec",
		description = "Executes a script",
		flags = CommandFlags.OP_ONLY,
		usage = "<script> (world) [arg1 [arg2 [arg3...]]]")
    private void execute(CommandSender sender, String[] args) {
	World world = (CommandSenders.hasWorld(sender) ? CommandSenders.getWorld(sender) : null);

	if (args.length == 0) {
	    sender.sendMessage(ERROR + "Usage: /scripts <script> (world) [arg1 [arg2 [arg3...]]]");
	    return;
	}

	if (args.length < 2 && world == null) {
	    sender.sendMessage(ERROR + "Usage: /scripts <script> <world> [arg1 [arg2 [arg3...]]]");
	    return;
	}

	Script script = loadScript(args[0], sender);
	if (script == null) {
	    return;
	}

	if (args.length < 2) {
	    exec(script, world, sender);
	    return;
	}

	world = Bukkit.getWorld(args[1]);
	if (world == null) {
	    sender.sendMessage(ERROR + "World '" + args[1] + "' doesn't exist.");
	    return;
	}

	if (args.length < 3) {
	    exec(script, world, sender);
	    return;
	}

	String[] scriptArgs = ArrayUtils.subarray(args, 2, args.length);
	exec(script, world, sender, scriptArgs);
    }

    private void exec(Script script, World world, CommandSender sender) {
	exec(script, world, sender, new String[0]);
    }

    private void exec(Script script, World world, CommandSender sender, String[] args) {
	JSContext context = new JSContext(script, world);
	try {
	    script.eval(context, args);
	} catch (ScriptException ex) {
	    if (ex.getCause() instanceof ExitScriptException) {
		ExitScriptException exitException = (ExitScriptException) ex.getCause();
		if (exitException.getValue() == ExitScriptException.ALL) {
		    context.destroy();
		}
		return;
	    }

	    String msg = "Error while executing script " + script.getName();

	    Logger log = main.getLogger();
	    log.log(Level.SEVERE, msg, ex);

	    if (!(sender instanceof ConsoleCommandSender)) {
		sender.sendMessage(ERROR + msg + ": " + ex.getCause().toString());
	    }

	}
    }

    private Script loadScript(String name, CommandSender sender) {
	try {
	    return cache.getOrLoad(name);
	} catch (ExecutionException ex) {
	    if (ex.getCause() instanceof FileNotFoundException) {
		sender.sendMessage(ERROR + "Missing file for script '" + name + "'");
		return null;
	    }

	    String msg = "Could not load script '" + name + "'";
	    sender.sendMessage(ERROR + msg);
	    main.getLogger().log(Level.SEVERE, msg, ex);
	    return null;
	}
    }
}
