package io.github.totom3.scripts.internal;

import static com.google.common.base.Preconditions.checkNotNull;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import io.github.totom3.scripts.ScriptsMain;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import jdk.nashorn.api.scripting.JSObject;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import jdk.nashorn.api.scripting.ScriptUtils;
import jdk.nashorn.internal.runtime.ScriptFunction;
import jdk.nashorn.internal.runtime.ScriptObject;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;

/**
 *
 * @author Totom3
 */
public class ScriptsEventManager {

    private static ScriptsEventManager instance;

    public synchronized static ScriptsEventManager get() {
	if (instance == null) {
	    instance = new ScriptsEventManager();
	}
	return instance;
    }

    private final Listener dummyListener = new Listener() {
    };
    private final InternalExecutor executor = new InternalExecutor();

    // event --> listeners
    private final Map<Class<? extends Event>, Multimap<JSContext, ScriptListener>> contextsByEvents = new HashMap<>();
    private final Multimap<JSContext, Class<? extends Event>> eventsByContexts = HashMultimap.create();

    // ------------------------===[ Utility Methods ]===------------------------
    private Multimap<JSContext, ScriptListener> getReadOnly(Class<? extends Event> clazz) {
	return contextsByEvents.getOrDefault(clazz, ImmutableMultimap.of());
    }

    private Multimap<JSContext, ScriptListener> getReadAndWrite(Class<? extends Event> clazz) {
	Multimap<JSContext, ScriptListener> map = contextsByEvents.get(clazz);
	if (map != null) {
	    return map;
	}

	map = HashMultimap.create();
	contextsByEvents.put(clazz, map);
	return map;
    }

    // ------------------------===[ Raw Methods ]===------------------------
    public void register(Class<? extends Event> clazz, JSContext context, ScriptObjectMirror mirror) {
	register(clazz, makeListener(context, mirror));
    }

    public void registerAll(JSContext context, ScriptObjectMirror mirror) {
	registerAll(makeListener(context, mirror));
    }

    public void unregister(Class<? extends Event> clazz, JSContext context, ScriptObjectMirror mirror) {
	unregister(clazz, makeListener(context, mirror));
    }

    public void unregisterAll(JSContext context, ScriptObjectMirror mirror) {
	unregisterAll(new ObjectListener(context, mirror));
    }

    // ------------------------===[ Direct Methods ]===------------------------
    public void register(Class<? extends Event> clazz, ScriptListener listener) {
	JSContext ctx = listener.getContext();

	if ((listener instanceof ObjectListener) && !((ObjectListener) listener).listensTo(clazz)) {
	    throw new IllegalArgumentException("missing handler method for event " + clazz.getSimpleName());
	}

	Multimap<JSContext, ScriptListener> events = getReadAndWrite(clazz);
	if (events.isEmpty()) {
	    registerInternalListener(clazz);
	}

	eventsByContexts.put(ctx, clazz);
	events.put(ctx, listener);
    }

    public void unregister(Class<? extends Event> clazz, ScriptListener listener) {
	JSContext ctx = listener.getContext();

	eventsByContexts.remove(ctx, clazz);
	Multimap<JSContext, ScriptListener> events = getReadOnly(clazz);
	if (events == null) {
	    return;
	}

	boolean removed = events.remove(ctx, listener);
	if (removed && events.isEmpty()) {
	    contextsByEvents.remove(clazz);
	}
    }

    public void registerAll(ScriptListener listener) {
	if (!(listener instanceof ObjectListener)) {
	    throw new IllegalArgumentException("cannot registerAll() function listener");
	}

	JSContext ctx = listener.getContext();

	Set<Class<? extends Event>> listenedEvents = ((ObjectListener) listener).getListenedEvents();
	if (listenedEvents.isEmpty()) {
	    throw new IllegalArgumentException("listener doesn't have any event handlers");
	}

	for (Class<? extends Event> clazz : listenedEvents) {
	    Multimap<JSContext, ScriptListener> events = getReadAndWrite(clazz);
	    if (events.isEmpty()) {
		registerInternalListener(clazz);
	    }

	    eventsByContexts.put(ctx, clazz);
	    events.put(ctx, listener);
	}
    }

    public void unregisterAll(ScriptListener listener) {
	if (!(listener instanceof ObjectListener)) {
	    throw new IllegalArgumentException("cannot registerAll() function listener");
	}

	JSContext ctx = listener.getContext();

	for (Class<? extends Event> clazz : ((ObjectListener) listener).getListenedEvents()) {
	    eventsByContexts.remove(ctx, clazz);
	    Multimap<JSContext, ScriptListener> events = getReadOnly(clazz);
	    if (events == null) {
		continue;
	    }

	    boolean removed = events.remove(ctx, listener);
	    if (removed && events.isEmpty()) {
		contextsByEvents.remove(clazz);
	    }
	}
    }

    public boolean hasListeners(JSContext context) {
	Collection<Class<? extends Event>> events = eventsByContexts.get(context);
	if (events == null) {
	    return false;
	}

	if (events.isEmpty()) {
	    eventsByContexts.removeAll(context); // cleanup
	    return false;
	}
	return true;
    }

    public void unregisterAll(JSContext context) {
	Collection<Class<? extends Event>> events = eventsByContexts.removeAll(context);
	for (Class<? extends Event> clazz : events) {
	    Multimap<JSContext, ScriptListener> map = contextsByEvents.get(clazz);
	    map.removeAll(context);
	    if (map.isEmpty()) { // cleanup
		contextsByEvents.remove(clazz);
	    }
	}
    }

    public void unregisterAll() {
	contextsByEvents.clear();
	eventsByContexts.clear();
	HandlerList.unregisterAll(dummyListener);
    }

    // ------------------------===[ Private Methods ]===------------------------
    private void registerInternalListener(Class<? extends Event> clazz) {
	Bukkit.getPluginManager().registerEvent(clazz, dummyListener, EventPriority.HIGH, executor, ScriptsMain.getInstance(), false);
    }

    private void unregisterInternalListener(Event event) {
	event.getHandlers().unregister(dummyListener);
    }

    private ScriptListener makeListener(JSContext ctx, ScriptObjectMirror mirror) {
	if (mirror.isFunction()) {
	    return new FunctionListener(ctx, mirror);
	}

	return new ObjectListener(ctx, mirror);
    }

    static class FunctionListener implements ScriptListener {

	final JSContext context;
	final ScriptObjectMirror mirror;

	FunctionListener(JSContext context, ScriptObjectMirror mirror) {
	    if (!mirror.isFunction()) {
		throw new IllegalArgumentException("miror is not function");
	    }

	    this.mirror = mirror;
	    this.context = checkNotNull(context);
	}

	@Override
	public JSContext getContext() {
	    return context;
	}

	@Override
	public void handle(Event event, JSContext context) throws RuntimeException {
	    mirror.call(mirror, event);
	}

    }

    static class ObjectListener implements ScriptListener {

	private static String getName(Class eventClass) {
	    return "on" + eventClass.getSimpleName();
	}

	final ScriptObjectMirror object;
	final JSContext context;

	ObjectListener(JSContext context, ScriptObjectMirror object) {
	    this.object = checkNotNull(object);
	    this.context = checkNotNull(context);
	}

	@Override
	public JSContext getContext() {
	    return context;
	}

	public Set<Class<? extends Event>> getListenedEvents() {
	    Set set = new HashSet<>();
	    for (Entry<String, Object> entry : object.entrySet()) {
		String key = entry.getKey();
		if (!key.startsWith("on") || key.length() <= 2) {
		    continue;
		}

		if (!isFunction(entry.getValue())) {
		    continue;
		}

		key = key.substring(2);
		Class<?> eventClass = ScriptImports.loadClass(key);
		if (eventClass == null) {
		    continue;
		}

		if (!Event.class.isAssignableFrom(eventClass)) {
		    continue;
		}

		set.add(eventClass);
	    }
	    return set;
	}

	private boolean isFunction(Object obj) {
	    if (obj == null) {
		return false;
	    }

	    if (obj instanceof ScriptFunction) {
		return true;
	    }

	    ScriptObjectMirror mirror = null;
	    if (obj instanceof ScriptObjectMirror) {
		mirror = (ScriptObjectMirror) obj;
	    } else if (obj instanceof ScriptObject) {
		mirror = (ScriptObjectMirror) ScriptUtils.wrap(obj);
	    }

	    return mirror != null && mirror.isFunction();
	}

	@Override
	public void handle(Event event, JSContext context) throws RuntimeException {
	    String methodName = getName(event.getClass());
	    object.callMember(methodName, event);
	}

	public boolean listensTo(Class<? extends Event> clazz) {
	    Object member = object.getMember(getName(clazz));
	    if (member == null) {
		return false;
	    }

	    if (!(member instanceof JSObject)) {
		return false;
	    }

	    return ((JSObject) member).isFunction();
	}

	@Override
	public boolean equals(Object o) {
	    if (!(o instanceof ObjectListener)) {
		return false;
	    }
	    if (o == this) {
		return true;
	    }
	    return object.equals(((ObjectListener) o).object);
	}

	@Override
	public int hashCode() {
	    int hash = 7;
	    hash = 71 * hash + Objects.hashCode(this.object);
	    return hash;
	}

    }

    class InternalExecutor implements EventExecutor {

	InternalExecutor() {
	}

	@Override
	public void execute(Listener l, Event event) {
	    if (l != dummyListener) {
		throw new AssertionError("expected dummy listener, got instead " + l);
	    }

	    Class<? extends Event> clazz = event.getClass();
	    Multimap<JSContext, ScriptListener> map = getReadOnly(clazz);
	    if (map == null) {
		unregisterInternalListener(event);
		return;
	    }

	    if (map.isEmpty()) {
		contextsByEvents.remove(clazz);
		unregisterInternalListener(event);
		return;
	    }

	    // create copy: listeners might change state of the actual map when handling event.
	    //		    This might in turn cause ConcurrentModificationExceptions when more
	    //		    than one listeners are registered for the same event.
	    map = HashMultimap.create(map);
	    for (Entry<JSContext, ScriptListener> entry : map.entries()) {
		JSContext context = entry.getKey();
		ScriptListener listener = entry.getValue();

		try {
		    JSEngine.executeListener(event, listener);
		} catch (OutOfMemoryError error) {
		    throw error;
		} catch (RuntimeException | Error ex) {
		    Logger log = ScriptsMain.getInstance().getLogger();
		    log.log(Level.SEVERE, "Could not pass event " + event.getEventName() + " to script " + context.getScript().getName(), ex);
		    context.destroy();
		}
	    }

	}
    }
}
