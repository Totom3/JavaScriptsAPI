package io.github.totom3.scripts.internal;

import com.google.common.collect.ImmutableList;
import io.github.totom3.scripts.Script;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.List;
import javax.script.Bindings;
import javax.script.ScriptContext;
import org.bukkit.World;

/**
 *
 * @author Totom3
 */
public class JSContext implements ScriptContext {

    private static boolean testing = false;

    private static final ImmutableList<Integer> scopes = ImmutableList.of(ENGINE_SCOPE, GLOBAL_SCOPE);

    public static void setTesting() {
	testing = true;
    }

    private static String scopeMsg() {
	return "invalid scope value";
    }

    private World world;
    private final Script script;

    private final Writer writer;
    private final Writer errWriter;

    private final LocalScriptBindings localBindings;
    private final GlobalScriptBindings globalBindings;

    public JSContext(Script jScript, World w) {
	if (!testing && w == null) {
	    throw new NullPointerException("world cannot be null in non-testing mode");
	}

	world = w;
	script = jScript;
	writer = new PrintWriter(System.out, true);
	errWriter = new PrintWriter(System.err, true);
	
	localBindings = new LocalScriptBindings(this);
	globalBindings = GlobalScriptBindings.get();
    }

    public World getWorld() {
	return world;
    }

    public Script getScript() {
	return script;
    }

    public void destroy() {
	ScriptsEventManager.get().unregisterAll(this);
	ScriptsScheduler.get().unregisterAll(this);
	localBindings.clear();
	world = null;

	if (JSEngine.isExecuting() && this == JSEngine.getContext()) {
	    throw new ExitScriptException(ExitScriptException.CURRENT);
	}
    }

    // ---------------===[ Implementing Methods ]===---------------
    @Override
    public void setBindings(Bindings bindings, int scope) {
	throw new UnsupportedOperationException();
    }

    @Override
    public Bindings getBindings(int scope) {
	switch (scope) {
	    case ENGINE_SCOPE:
		return localBindings;
	    case GLOBAL_SCOPE:
		return globalBindings;
	    default:
		throw new IllegalArgumentException(scopeMsg());
	}
    }

    @Override
    public void setAttribute(String name, Object value, int scope) {
	switch (scope) {
	    case ENGINE_SCOPE:
		localBindings.put(name, value);
		break;
	    case GLOBAL_SCOPE:
		globalBindings.put(name, value);
		break;
	    default:
		throw new IllegalArgumentException(scopeMsg());
	}
    }

    @Override
    public Object getAttribute(String name, int scope) {
	switch (scope) {
	    case ENGINE_SCOPE:
		return localBindings.get(name);
	    case GLOBAL_SCOPE:
		return globalBindings.get(name);
	    default:
		throw new IllegalArgumentException(scopeMsg());
	}
    }

    @Override
    public Object removeAttribute(String name, int scope) {
	switch (scope) {
	    case ENGINE_SCOPE:
		return localBindings.remove(name);
	    case GLOBAL_SCOPE:
		return globalBindings.remove(name);
	    default:
		throw new IllegalArgumentException(scopeMsg());
	}
    }

    @Override
    public Object getAttribute(String name) {
	Object value = localBindings.get(name);
	if (value != null) {
	    return value;
	}

	return globalBindings.get(name);
    }

    @Override
    public int getAttributesScope(String name) {
	if (localBindings.get(name) != null) {
	    return ENGINE_SCOPE;
	}

	return (globalBindings.get(name) != null) ? GLOBAL_SCOPE : -1;
    }

    @Override
    public Writer getWriter() {
	return writer;
    }

    @Override
    public Writer getErrorWriter() {
	return errWriter;
    }

    @Override
    public void setWriter(Writer writer) {
	throw new UnsupportedOperationException();
    }

    @Override
    public void setErrorWriter(Writer writer) {
	throw new UnsupportedOperationException();
    }

    @Override
    public Reader getReader() {
	return null;
    }

    @Override
    public void setReader(Reader reader) {
	throw new UnsupportedOperationException();
    }

    @Override
    public List<Integer> getScopes() {
	return scopes;
    }

}
