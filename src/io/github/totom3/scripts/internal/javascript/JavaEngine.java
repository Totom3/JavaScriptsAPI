package io.github.totom3.scripts.internal.javascript;

import io.github.totom3.scripts.internal.ExitScriptException;

/**
 *
 * @author Totom3
 */
public class JavaEngine {

    private static final JavaEngine instance = new JavaEngine();

    // JSEngine.exit(JSEngine.ALL)
    // these fields will be accessed by JS scripts
    public static final int ALL = ExitScriptException.ALL;
    public static final int CURRENT = ExitScriptException.CURRENT;

    public static JavaEngine get() {
	return instance;
    }

    public static void exit(int type) {
	throw new ExitScriptException(type);
    }

    private JavaEngine() {
    }
}
