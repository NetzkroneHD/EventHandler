package de.netzkronehd.eventhandler.event;

public interface Cancellable {

    public boolean isCancelled();

    public void setCancelled(boolean cancel);
}
