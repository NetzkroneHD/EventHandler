package de.netzkronehd.eventhandler.event;

import com.google.common.collect.ImmutableSet;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EventBus {

    private final Map<Class<?>, Map<Byte, Map<Object, Method[]>>> byListenerAndPriority = new HashMap<>();
    private final Map<Class<?>, EventHandlerMethod[]> byEventBaked = new ConcurrentHashMap<>();
    private final Lock lock = new ReentrantLock();
    private Logger logger;

    public EventBus(Logger logger) {
        this.logger = logger;
    }

    public void post(Object event) {
        final EventHandlerMethod[] handlers = byEventBaked.get(event.getClass());

        if (handlers != null) {
            for (EventHandlerMethod method : handlers) {
                try {
                    method.invoke(event);
                } catch (IllegalAccessException ex) {
                    throw new Error("Method became inaccessible: " + event, ex);
                } catch (IllegalArgumentException ex) {
                    throw new Error("Method rejected target/argument: " + event, ex);
                } catch (InvocationTargetException ex) {
                    logger.log(Level.WARNING, MessageFormat.format("Error dispatching event {0} to listener {1}", event, method.getListener()), ex.getCause());
                }
            }
        }
    }

    private Map<Class<?>, Map<Byte, Set<Method>>> findHandlers(Object listener) {
        final Map<Class<?>, Map<Byte, Set<Method>>> handler = new HashMap<>();
        final Set<Method> methods = ImmutableSet.<Method>builder().add(listener.getClass().getMethods()).add(listener.getClass().getDeclaredMethods()).build();
        for (final Method m : methods) {
            final EventHandler annotation = m.getAnnotation(EventHandler.class);
            if (annotation != null) {
                final Class<?>[] params = m.getParameterTypes();
                if (params.length != 1) {
                    logger.log(Level.INFO, "Method {0} in class {1} annotated with {2} does not have single argument", new Object[]
                            {
                                    m, listener.getClass(), annotation
                            });
                    continue;
                }
                final Map<Byte, Set<Method>> prioritiesMap = handler.computeIfAbsent(params[0], k -> new HashMap<>());
                final Set<Method> priority = prioritiesMap.computeIfAbsent(annotation.priority(), k -> new HashSet<>());
                priority.add(m);
            }
        }
        return handler;
    }

    public void register(Object listener) {
        final Map<Class<?>, Map<Byte, Set<Method>>> handler = findHandlers(listener);
        lock.lock();
        try {
            for (Map.Entry<Class<?>, Map<Byte, Set<Method>>> e : handler.entrySet()) {
                final Map<Byte, Map<Object, Method[]>> prioritiesMap = byListenerAndPriority.computeIfAbsent(e.getKey(), k -> new HashMap<>());
                for (Map.Entry<Byte, Set<Method>> entry : e.getValue().entrySet()) {
                    final Map<Object, Method[]> currentPriorityMap = prioritiesMap.computeIfAbsent(entry.getKey(), k -> new HashMap<>());
                    currentPriorityMap.put(listener, entry.getValue().toArray(new Method[0]));
                }
                bakeHandlers(e.getKey());
            }
        } finally {
            lock.unlock();
        }
    }

    public void unregister(Object listener) {
        final Map<Class<?>, Map<Byte, Set<Method>>> handler = findHandlers(listener);
        lock.lock();
        try {
            for (Map.Entry<Class<?>, Map<Byte, Set<Method>>> e : handler.entrySet()) {
                final Map<Byte, Map<Object, Method[]>> prioritiesMap = byListenerAndPriority.get(e.getKey());
                if (prioritiesMap != null) {
                    for (Byte priority : e.getValue().keySet()) {
                        final Map<Object, Method[]> currentPriority = prioritiesMap.get(priority);
                        if (currentPriority != null) {
                            currentPriority.remove(listener);
                            if (currentPriority.isEmpty()) {
                                prioritiesMap.remove(priority);
                            }
                        }
                    }
                    if (prioritiesMap.isEmpty()) {
                        byListenerAndPriority.remove(e.getKey());
                    }
                }
                bakeHandlers(e.getKey());
            }
        } finally {
            lock.unlock();
        }
    }

    private void bakeHandlers(Class<?> eventClass) {
        final Map<Byte, Map<Object, Method[]>> handlersByPriority = byListenerAndPriority.get(eventClass);
        if (handlersByPriority != null) {
            final List<EventHandlerMethod> handlersList = new ArrayList<>(handlersByPriority.size() * 2);


            byte value = Byte.MIN_VALUE;
            do {
                final Map<Object, Method[]> handlersByListener = handlersByPriority.get(value);
                if (handlersByListener != null) {
                    for (Map.Entry<Object, Method[]> listenerHandlers : handlersByListener.entrySet()) {
                        for (Method method : listenerHandlers.getValue()) {
                            final EventHandlerMethod ehm = new EventHandlerMethod(listenerHandlers.getKey(), method);
                            handlersList.add(ehm);
                        }
                    }
                }
            } while (value++ < Byte.MAX_VALUE);
            byEventBaked.put(eventClass, handlersList.toArray(new EventHandlerMethod[0]));
        } else {
            byEventBaked.remove(eventClass);
        }
    }


    public void setLogger(Logger logger) {
        this.logger = logger;
    }
    public Logger getLogger() {
        return logger;
    }
}
