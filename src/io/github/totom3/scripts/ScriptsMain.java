package io.github.totom3.scripts;

import io.github.totom3.scripts.internal.ScriptImports;
import io.github.totom3.scripts.internal.ScriptsEventManager;
import io.github.totom3.scripts.internal.ScriptsScheduler;
import java.io.IOException;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Totom3
 */
public class ScriptsMain extends JavaPlugin {

    private static ScriptsMain instance;

    public static ScriptsMain getInstance() {
	return instance;
    }

    private final ScriptsCache cache;
    private final ScriptsCommand command;

    public ScriptsMain() throws IOException {
	ScriptImports.initPackages();
	ScriptImports.initClasses();
	instance = this;

	cache = new ScriptsCache();
	command = new ScriptsCommand(this);
    }

    public ScriptsCache getCache() {
	return cache;
    }

    public ScriptsCommand getCommand() {
	return command;
    }

    @Override
    public void onEnable() {
	getCommand("scripts").setExecutor(command);
    }

    @Override
    public void onDisable() {
	cache.clear();
	ScriptImports.clear();
	ScriptsScheduler.get().unregisterAll();
	ScriptsEventManager.get().unregisterAll();
    }
}
