package de.netzkronehd.eventhandler.event;

public interface Cancellable {

    boolean isCancelled();

    void setCancelled(boolean cancel);
}
