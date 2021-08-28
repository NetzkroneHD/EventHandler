package de.netzkronehd.eventhandler.example.events;

import de.netzkronehd.eventhandler.event.Cancellable;
import de.netzkronehd.eventhandler.event.Event;

public class MakeStuffEvent extends Event implements Cancellable {

    private boolean cancel;
    private double result;

    public MakeStuffEvent(double result) {
        this.result = result;
    }

    public double getResult() {
        return result;
    }

    public void setResult(double result) {
        this.result = result;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }
}
