package de.netzkronehd.eventhandler.example;

import de.netzkronehd.eventhandler.event.EventHandler;
import de.netzkronehd.eventhandler.event.EventPriority;
import de.netzkronehd.eventhandler.event.Listener;
import de.netzkronehd.eventhandler.example.events.DoneStuffEvent;
import de.netzkronehd.eventhandler.example.events.MakeStuffEvent;

public class MakeStuffListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onMakeStuff(MakeStuffEvent e) {
        if(e.getResult() > 100) {
            e.setCancelled(true);
        } else if(e.getResult() < 3 && e.getResult() > 1) {
            e.setResult(2);
        }
    }

    @EventHandler
    public void onDoneStuff(DoneStuffEvent e) {
        System.out.println("Done stuff: "+e.getResult());
    }

}
