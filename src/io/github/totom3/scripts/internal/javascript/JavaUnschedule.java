package io.github.totom3.scripts.internal.javascript;

import io.github.totom3.scripts.internal.JSContext;
import io.github.totom3.scripts.internal.JSEngine;
import io.github.totom3.scripts.internal.ScriptsScheduler;
import org.bukkit.scheduler.BukkitTask;

/**
 *
 * @author Totom3
 */
public class JavaUnschedule {

    private static final JavaUnschedule instance = new JavaUnschedule();

    public static JavaUnschedule get() {
	return instance;
    }

    public static void task(BukkitTask task) {
	if (task == null) {
	    throw new NullPointerException("cannot unschedule null task");
	}

	ScriptsScheduler.get().unregister(JSEngine.getContext(), task);
	task.cancel();
    }

    public static void self() {
	BukkitTask task = JSEngine.getRunnable().getTask();
	if (task == null) {
	    throw new IllegalStateException("not scheduled");
	}

	ScriptsScheduler.get().unregister(JSEngine.getContext(), task);
	task.cancel();
    }

    public static void all() {
	JSContext context = JSEngine.getContext();
	ScriptsScheduler.get().unregisterAll(context);
    }

    private JavaUnschedule() {
    }

}
