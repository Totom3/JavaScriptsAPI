package io.github.totom3.scripts.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import io.github.totom3.scripts.ScriptsMain;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/**
 *
 * @author Totom3
 */
public class ScriptsScheduler {

    private static ScriptsScheduler instance;

    public static synchronized ScriptsScheduler get() {
	if (instance == null) {
	    instance = new ScriptsScheduler();
	}
	return instance;
    }

    private final HashMultimap<JSContext, BukkitTask> tasks = HashMultimap.create();

    public void register(JSContext context, BukkitTask task) {
	Preconditions.checkNotNull(context);
	tasks.put(context, task);
    }

    public void unregister(JSContext context, BukkitTask task) {
	tasks.remove(context, task);
    }

    public boolean hasTasks(JSContext context) {
	return tasks.containsKey(context);
    }

    public Set<BukkitTask> getTasks(JSContext ctx) {
	return ImmutableSet.copyOf(tasks.get(ctx));
    }

    public void unregisterAll(JSContext context) {
	for (BukkitTask task : tasks.removeAll(context)) {
	    task.cancel();
	}
    }

    public void unregisterAll() {
	tasks.values().forEach(BukkitTask::cancel);
	tasks.clear();
    }

    public static class ScriptRunnable extends BukkitRunnable {

	private BukkitTask task;
	private final ScriptObjectMirror mirror;
	private final JSContext context;

	public ScriptRunnable(ScriptObjectMirror mirror, JSContext context) {
	    this.mirror = Preconditions.checkNotNull(mirror);
	    this.context = Preconditions.checkNotNull(context);
	}

	public ScriptObjectMirror getObject() {
	    return mirror;
	}

	public JSContext getContext() {
	    return context;
	}

	public BukkitTask getTask() {
	    return task;
	}

	@Override
	public void cancel() {
	    task.cancel();
	    ScriptsScheduler.instance.unregister(context, task);
	    task = null;
	}

	@Override
	public void run() {
	    try {
		JSEngine.executeTask(this);
	    } catch (OutOfMemoryError error) {
		throw error;
	    } catch (RuntimeException | Error ex) {
		Logger log = ScriptsMain.getInstance().getLogger();
		log.log(Level.SEVERE, "Could not execute scheduled task to script " + context.getScript().getName() + ": ", ex);
		context.destroy();
		cancel();
	    }
	}

	void run0() {
	    if (mirror.isFunction()) {
		mirror.call(mirror);
	    } else {
		mirror.callMember("run");
	    }
	}

	@Override
	public boolean equals(Object o) {
	    if (!(o instanceof ScriptRunnable)) {
		return false;
	    }
	    if (o == this) {
		return true;
	    }
	    return mirror.equals(((ScriptRunnable) o).mirror);
	}

	@Override
	public int hashCode() {
	    int hash = 5;
	    hash = 59 * hash + Objects.hashCode(this.mirror);
	    hash = 59 * hash + Objects.hashCode(this.context);
	    return hash;
	}

	// ----------------------------------------------
	@Override
	public BukkitTask runTask(Plugin plugin) throws IllegalArgumentException, IllegalStateException {
	    return task = super.runTask(plugin);
	}

	@Override
	public BukkitTask runTaskTimer(Plugin plugin, long delay, long period) throws IllegalArgumentException, IllegalStateException {
	    return task = super.runTaskTimer(plugin, delay, period);
	}

	@Override
	public BukkitTask runTaskLater(Plugin plugin, long delay) throws IllegalArgumentException, IllegalStateException {
	    return task = super.runTaskLater(plugin, delay);
	}

	// ----------------------------------------------
	@Override
	public BukkitTask runTaskAsynchronously(Plugin plugin) throws UnsupportedOperationException {
	    throw new UnsupportedOperationException();
	}

	@Override
	public BukkitTask runTaskTimerAsynchronously(Plugin plugin, long delay, long period) throws UnsupportedOperationException {
	    throw new UnsupportedOperationException();
	}

	@Override
	public BukkitTask runTaskLaterAsynchronously(Plugin plugin, long delay) throws UnsupportedOperationException {
	    throw new UnsupportedOperationException();
	}
    }
}
