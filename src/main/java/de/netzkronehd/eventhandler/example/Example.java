package de.netzkronehd.eventhandler.example;

import de.netzkronehd.eventhandler.EventHandler;
import de.netzkronehd.eventhandler.example.events.DoneStuffEvent;
import de.netzkronehd.eventhandler.example.events.MakeStuffEvent;

import java.util.Random;

public class Example {


    private EventHandler eventHandler;

    public Example() {
        eventHandler = new EventHandler();
        eventHandler.registerListener(new MakeStuffListener());
    }

    public void makeStuff() {
        final Random r = new Random();
        double resutl = r.nextDouble()/r.nextDouble();

        MakeStuffEvent event = new MakeStuffEvent(resutl);
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
