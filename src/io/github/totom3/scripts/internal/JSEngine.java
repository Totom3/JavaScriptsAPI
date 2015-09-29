package io.github.totom3.scripts.internal;

import io.github.totom3.scripts.internal.ScriptsScheduler.ScriptRunnable;
import java.io.Reader;
import javax.script.CompiledScript;
import javax.script.ScriptException;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.bukkit.event.Event;

/**
 *
 * @author Totom3
 */
public class JSEngine {

    private static final NashornScriptEngine engine;
    private static JSContext context;
    private static ScriptRunnable runnable;
    private static ScriptListener listener;

    static {
	engine = (NashornScriptEngine) new NashornScriptEngineFactory().getScriptEngine(new String[]{"-doe", "-strict"});
    }

    public static NashornScriptEngine getEngine() {
	return engine;
    }

    public static CompiledScript compile(Reader reader) throws ScriptException {
	return engine.compile(reader);
    }

    public static CompiledScript compile(String str) throws ScriptException {
	return engine.compile(str);
    }

    public static boolean isExecuting() {
	return context != null;
    }

    public static boolean isExecutingRunnable() {
	return runnable != null;
    }

    public static boolean isExecutingListener() {
	return listener != null;
    }

    public static JSContext getContext() {
	if (context == null) {
	    throw new IllegalStateException("not executing");
	}
	return context;
    }

    public static ScriptRunnable getRunnable() {
	if (runnable == null) {
	    throw new IllegalStateException("not executing runnable");
	}
	return runnable;
    }

    public static ScriptListener getListener() {
	if (listener == null) {
	    throw new IllegalStateException("not executing listener");
	}
	return listener;
    }

    public static void execute(JSContext ctx, Runnable run) {
	if (ctx == null) {
	    throw new IllegalArgumentException("context cannot be null");
	}

	if (context != null) {
	    throw new IllegalStateException("already executing script");
	}

	try {
	    context = ctx;
	    run.run();
	    context = null;
	    postRun(ctx);
	} catch (RuntimeException ex) {
	    context = null;
	    if (ex instanceof ExitScriptException) {
		if (((ExitScriptException) ex).getValue() == ExitScriptException.ALL) {
		    ctx.destroy();
		}
		return;
	    } else if (ex.getCause() instanceof ExitScriptException) {
		if (((ExitScriptException) ex.getCause()).getValue() == ExitScriptException.ALL) {
		    ctx.destroy();
		}
		return;
	    }
	    throw ex;
	}
    }

    public static void executeScript(JSContext ctx) throws ScriptException {
	CompiledScript script = ctx.getScript().getScript();
	if (context != null) {
	    throw new IllegalStateException("already executing script");
	}

	try {
	    context = ctx;
	    script.eval(ctx);
	    context = null;
	    postRun(ctx);
	} catch (ExitScriptException | ScriptException ex) {
	    context = null;
	    if (ex instanceof ExitScriptException) {
		if (((ExitScriptException) ex).getValue() == ExitScriptException.ALL) {
		    ctx.destroy();
		}
		return;
	    } else if (ex.getCause() instanceof ExitScriptException) {
		if (((ExitScriptException) ex.getCause()).getValue() == ExitScriptException.ALL) {
		    ctx.destroy();
		}
		return;
	    }
	    throw ex;
	}
    }

    public static void executeTask(ScriptRunnable run) {
	JSContext ctx = run.getContext();
	if (context != null) {
	    throw new IllegalStateException("already executing script");
	}

	try {
	    context = ctx;
	    runnable = run;
	    run.run0();
	    context = null;
	    runnable = null;
	    postRun(ctx);
	} catch (RuntimeException ex) {
	    context = null;
	    runnable = null;
	    if (ex instanceof ExitScriptException) {
		if (((ExitScriptException) ex).getValue() == ExitScriptException.ALL) {
		    ctx.destroy();
		}
		return;
	    } else if (ex.getCause() instanceof ExitScriptException) {
		if (((ExitScriptException) ex.getCause()).getValue() == ExitScriptException.ALL) {
		    ctx.destroy();
		}
		return;
	    }
	    throw ex;
	}
    }

    public static void executeListener(Event event, ScriptListener list) {
	JSContext ctx = list.getContext();
	if (context != null) {
	    throw new IllegalStateException("already executing script");
	}

	try {
	    context = ctx;
	    listener = list;
	    list.handle(event, ctx);
	    context = null;
	    listener = null;
	    postRun(ctx);
	} catch (RuntimeException ex) {
	    context = null;
	    listener = null;
	    if (ex instanceof ExitScriptException) {
		if (((ExitScriptException) ex).getValue() == ExitScriptException.ALL) {
		    ctx.destroy();
		}
		return;
	    } else if (ex.getCause() instanceof ExitScriptException) {
		if (((ExitScriptException) ex.getCause()).getValue() == ExitScriptException.ALL) {
		    ctx.destroy();
		}
		return;
	    }
	    throw ex;
	}
    }

    private static void postRun(JSContext ctx) {
	if (!ScriptsEventManager.get().hasListeners(ctx) && !ScriptsScheduler.get().hasTasks(ctx)) {
	    ctx.destroy();
	}
    }

    private JSEngine() {
	throw new AssertionError();
    }
}
