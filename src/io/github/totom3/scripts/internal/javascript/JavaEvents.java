package io.github.totom3.scripts.internal.javascript;

import io.github.totom3.scripts.internal.ScriptsEventManager;
import io.github.totom3.scripts.internal.JSContext;
import io.github.totom3.scripts.internal.JSEngine;
import jdk.internal.dynalink.beans.StaticClass;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.bukkit.event.Event;

/**
 *
 * @author Totom3
 */
public class JavaEvents {

    private static final JavaEvents instance = new JavaEvents();

    private static final ScriptsEventManager manager = ScriptsEventManager.get();

    public static JavaEvents get() {
	return instance;
    }

    private static Class<? extends Event> toClass(StaticClass c) {
	if (c == null) {
	    throw new NullPointerException("event class cannot be null");
	}

	Class<?> clazz = c.getRepresentedClass();
	if (!Event.class.isAssignableFrom(clazz)) {
	    throw new IllegalArgumentException("class " + clazz.getName() + " is not an event class");
	}

	return (Class<? extends Event>) clazz;
    }

    public static void register(StaticClass clazz) {
	manager.register(toClass(clazz), JSEngine.getListener());
    }

    public static void register(StaticClass clazz, ScriptObjectMirror mirror) {
	checkNotNull(mirror);

	JSContext ctx = JSEngine.getContext();
	manager.register(toClass(clazz), ctx, mirror);
    }

    public static void registerAll() {
	manager.registerAll(JSEngine.getListener());
    }

    public static void registerAll(ScriptObjectMirror mirror) {
	checkNotNull(mirror);

	JSContext ctx = JSEngine.getContext();
	manager.registerAll(ctx, mirror);
    }

    public static void unregister(StaticClass clazz) {
	manager.unregister(toClass(clazz), JSEngine.getListener());
    }

    public static void unregister(StaticClass clazz, ScriptObjectMirror mirror) {
	checkNotNull(mirror);

	JSContext ctx = JSEngine.getContext();
	manager.unregister(toClass(clazz), ctx, mirror);
    }

    public static void unregisterAll() {
	manager.unregisterAll(JSEngine.getListener());
    }

    public static void unregisterAll(ScriptObjectMirror mirror) {
	checkNotNull(mirror);

	JSContext ctx = JSEngine.getContext();
	manager.unregisterAll(ctx, mirror);
    }

    private static void checkNotNull(ScriptObjectMirror mirror) {
	if (mirror == null) {
	    throw new NullPointerException("listener cannot be null");
	}
    }

    private JavaEvents() {
    }
}
