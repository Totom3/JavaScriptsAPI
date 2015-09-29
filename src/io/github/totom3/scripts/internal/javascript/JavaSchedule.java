package io.github.totom3.scripts.internal.javascript;

import io.github.totom3.scripts.ScriptsMain;
import io.github.totom3.scripts.internal.JSContext;
import io.github.totom3.scripts.internal.JSEngine;
import io.github.totom3.scripts.internal.ScriptsScheduler;
import io.github.totom3.scripts.internal.ScriptsScheduler.ScriptRunnable;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.bukkit.scheduler.BukkitTask;

/**
 *
 * @author Totom3
 */
public class JavaSchedule {

    private static final JavaSchedule instance = new JavaSchedule();

    public static JavaSchedule get() {
	return instance;
    }

    public static BukkitTask in(int delay) {
	JSContext context = JSEngine.getContext();
	ScriptRunnable run = JSEngine.getRunnable();

	BukkitTask task = run.runTaskLater(ScriptsMain.getInstance(), delay);
	ScriptsScheduler.get().register(context, task);
	return task;
    }

    public static BukkitTask repeated(int delay, int repeatDelay) {
	JSContext context = JSEngine.getContext();
	ScriptRunnable run = JSEngine.getRunnable();

	BukkitTask task = run.runTaskTimer(ScriptsMain.getInstance(), delay, repeatDelay);
	ScriptsScheduler.get().register(context, task);
	return task;
    }

    public static BukkitTask in(int delay, ScriptObjectMirror mirror) {
	JSContext context = JSEngine.getContext();
	ScriptRunnable run = new ScriptRunnable(mirror, context);

	BukkitTask task = run.runTaskLater(ScriptsMain.getInstance(), delay);
	ScriptsScheduler.get().register(context, task);
	return task;
    }

    public static BukkitTask repeated(int delay, int repeatDelay, ScriptObjectMirror mirror) {
	JSContext context = JSEngine.getContext();
	ScriptRunnable run = new ScriptRunnable(mirror, context);

	BukkitTask task = run.runTaskTimer(ScriptsMain.getInstance(), delay, repeatDelay);
	ScriptsScheduler.get().register(context, task);
	return task;
    }

    private JavaSchedule() {
    }

}
