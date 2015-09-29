package io.github.totom3.scripts.internal;

import org.bukkit.event.Event;

/**
 *
 * @author Totom3
 */
public interface ScriptListener {

    JSContext getContext();

    void handle(Event event, JSContext context) throws RuntimeException;
}
