package de.netzkronehd.eventhandler;

import com.google.common.eventbus.Subscribe;
import com.google.common.base.Preconditions;
import de.netzkronehd.eventhandler.event.Event;
import de.netzkronehd.eventhandler.event.EventBus;
import de.netzkronehd.eventhandler.event.Listener;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EventHandler {

    private static EventBus eventBus;

    public EventHandler(Logger logger) {
        if(eventBus == null) {
            eventBus = new EventBus(logger);
        }
    }

    public void registerListener(Listener listener) {
        for (Method method : listener.getClass().getDeclaredMethods()) {
            Preconditions.checkArgument(!method.isAnnotationPresent(Subscribe.class),
                    "Listener %s has registered using deprecated subscribe annotation! Please update to @EventHandler.", listener);
        }
        eventBus.register(listener);

    }

    public void unregisterListener(Listener listener) {
        eventBus.unregister(listener);
    }

    public <T extends Event> T callEvent(T event) {
        Preconditions.checkNotNull(event, "event");
        final long start = System.currentTimeMillis();
        eventBus.post(event);
        event.postCall();
        final long elapsed = System.currentTimeMillis() - start;
        if (elapsed > 250) {
            eventBus.getLogger().log(Level.WARNING, "Event {0} took {1}ms to process!", new Object[]
                    {
                            event, elapsed
                    });
        }
        return event;
    }

    public void setLogger(Logger logger) {
        eventBus.setLogger(logger);
    }

}
