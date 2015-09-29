package io.github.totom3.scripts;

import static com.google.common.base.Preconditions.checkNotNull;
import com.google.common.base.Strings;
import io.github.totom3.scripts.internal.JSContext;
import io.github.totom3.scripts.internal.JSEngine;
import java.io.File;
import javax.script.CompiledScript;
import javax.script.ScriptException;
import org.bukkit.World;

/**
 *
 * @author Totom3
 */
public class Script {

    static Script create(String name, File sourceFile, CompiledScript script) {
	return new Script(name, sourceFile, script);
    }

    private final String name;
    private final File sourceFile;
    private final CompiledScript script;

    private Script(String name, File sourceFile, CompiledScript script) {
	if (Strings.isNullOrEmpty(name)) {
	    throw new IllegalArgumentException("script name cannot be empty");
	}

	this.name = name;
	this.sourceFile = sourceFile;
	this.script = checkNotNull(script);
    }

    public String getName() {
	return name;
    }

    public File getSourceFile() {
	return sourceFile;
    }

    public boolean hasSourceFile() {
	return sourceFile != null;
    }

    public CompiledScript getScript() {
	return script;
    }

    public void eval(JSContext context) throws ScriptException {
	JSEngine.executeScript(context);
    }

    public void eval(JSContext context, String[] args) throws ScriptException {
	context.setAttribute("initialArgs", args, JSContext.ENGINE_SCOPE);
	JSEngine.executeScript(context);
    }

    public JSContext eval(World world) throws ScriptException {
	JSContext ctx = new JSContext(this, world);
	JSEngine.executeScript(ctx);
	return ctx;
    }

    public JSContext eval(World world, String[] args) throws ScriptException {
	JSContext ctx = new JSContext(this, world);
	ctx.setAttribute("initialArgs", args, JSContext.ENGINE_SCOPE);
	JSEngine.executeScript(ctx);
	return ctx;
    }
}
