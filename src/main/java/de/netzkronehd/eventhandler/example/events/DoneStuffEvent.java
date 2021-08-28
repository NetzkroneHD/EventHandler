package de.netzkronehd.eventhandler.example.events;

import de.netzkronehd.eventhandler.event.Event;

public class DoneStuffEvent extends Event {

    private double result;

    public DoneStuffEvent(double result) {
        this.result = result;
    }

    public double getResult() {
        return result;
    }
}
