package io.github.totom3.commons.command;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.bukkit.ChatColor;

/**
 * Declares a method a sub-command. It holds information about the name, the
 * description, the usage, the required permissions and the {@link CommandFlags}
 * that are involved in it.
 *
 * @author Totom3
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface Subcommand {

    /**
     * A variety of flags/options taken in count before executing a
     * {@code Subcommand}.
     */
    public static interface CommandFlags {

	/**
	 * Indicates that only OPs can use this {@code Subcommand}. This
	 * includes the Console.
	 */
	public final static byte OP_ONLY = 0x1;
	/**
	 * Indicated that only online Players can use this {@code Subcommand}
	 */
	public final static byte ONLINE_ONLY = 0x2;
	
	public final static byte HIDDEN = 0x4;
    }

    /**
     * The name of this {@code Subcommand}. It is compared to the first argument
     * when a user enters a command. For example : /cmd doSomething. The
     * {@code Subcommand} with the name {@code doSomething} will be executed.
     *
     * @return the name of this {@code Subcommand}.
     */
    public String name();

    /**
     * <b>Optional</b>. A short description of this {@code Subcommand}. Used
     * when printing the help message.
     *
     * @return the description of this {@code Subcommand}.
     */
    public String description() default "";

    /**
     * <b>Optional</b>. The syntax for this {@code Subcommand}. Used only when
     * printing the help message, and is not compared the the command a user
     * entered before executing the sub-command. This must not include the name
     * of this {@code Subcommand} nor the name of the command; it will be
     * concatenated as follows :
     * <pre>    /commandName subCmdName + <b>description</b></pre>
     *
     * @return
     */
    public String usage() default "";
    
    public String[] helpMessages() default {};
    
    ChatColor helpMessagePrefix() default ChatColor.YELLOW;

    /**
     * The flags involved in the execution of this {@code Subcommand}.
     *
     * @return
     * @see CommandFlags
     */
    public byte flags() default 0b0;
}
