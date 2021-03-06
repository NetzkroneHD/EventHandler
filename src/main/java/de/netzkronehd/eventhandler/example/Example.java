package de.netzkronehd.eventhandler.example;

import de.netzkronehd.eventhandler.EventHandler;
import de.netzkronehd.eventhandler.example.events.DoneStuffEvent;
import de.netzkronehd.eventhandler.example.events.MakeStuffEvent;

import java.util.Random;
import java.util.logging.Logger;

public class Example {


    private final EventHandler eventHandler;

    public Example() {
        eventHandler = new EventHandler(Logger.getGlobal());
        eventHandler.registerListener(new MakeStuffListener());
    }

    public void makeStuff() {
        final Random r = new Random();
        double result = r.nextDouble()/r.nextDouble();

        MakeStuffEvent event = new MakeStuffEvent(result);
        eventHandler.callEvent(event);

        if(!event.isCancelled()) {
            System.out.println(event.getResult());
            eventHandler.callEvent(new DoneStuffEvent(event.getResult()));

        } else System.out.println("Make stuff was cancelled.");


    }

    public static void main(String[] args) {
        final Example example = new Example();
        example.makeStuff();

    }
}
