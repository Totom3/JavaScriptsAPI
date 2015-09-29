package io.github.totom3.commons.command;

import io.github.totom3.commons.command.Subcommand;
import java.lang.reflect.Method;

/**
 *
 * @author Totom3
 */
public class InvalidSubCommandException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance of
     * <code>InvalidSubCommandException</code> without detail message.
     */
    public InvalidSubCommandException() {
    }

    /**
     * Constructs an instance of
     * <code>InvalidSubCommandException</code> with the specified detail
     * message.
     *
     * @param msg the detail message.
     */
    public InvalidSubCommandException(String msg) {
	super(msg);
    }
    
    public InvalidSubCommandException(Cause c, Method m) {
	this(c.getMessage(m));
    }

    public static enum Cause {

	ARGUMENT_COUNT {
	    
	    @Override
	    public String getMessage(Method m) {
		Class<?>[] types = m.getParameterTypes();
		StringBuilder b = new StringBuilder(types.length * 15);

		for (Class<?> c : types) {
		    b.append(c.getName());
		    b.append(", ");
		}
		
		return "Expected method arguments {CommandSender, String[]}, but instead got "+b.substring(0, b.length()-2)+" for method "+m.getName();
	    }
	    
	},
	
	RETURN_TYPE {
	    @Override
	    public String getMessage(Method m) {
		return "Expected return type void for method "+m.getName()+"; got instead "+m.getReturnType().getName();
	    }
	},
	
	DUPLICATE {
	    @Override
	    public String getMessage(Method m) {
		return "Method "+m.getName()+"declared a duplicate subcommand "+m.getAnnotation(Subcommand.class).name();
	    }
	    
	};
	
	public abstract String getMessage(Method m);
	
    }
}
