package io.github.totom3.scripts.internal;

/**
 *
 * @author Totom3
 */
public class ExitScriptException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public final static int CURRENT = 0;
    public final static int ALL = 1;

    private final int value;

    public ExitScriptException(int value) {
	if (value != CURRENT && value != ALL) {
	    throw new IllegalArgumentException("invalid exit type " + value);
	}

	this.value = value;
    }

    public int getValue() {
	return value;
    }
}
