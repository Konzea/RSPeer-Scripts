package Chin_Hunter.Executes;

import Chin_Hunter.Executes.Hunting.*;
import Chin_Hunter.Main;
import org.rspeer.runetek.api.component.tab.Combat;
import org.rspeer.runetek.api.movement.Movement;

public class Starting {

    private Starting(){
        //Private Default Constructor
    }

    public static void execute(){
        //Starting
        //TODO Grab starting xp

        if (Combat.isAutoRetaliateOn())
            Combat.toggleAutoRetaliate(false);

        if (!Movement.isRunEnabled())
            Movement.toggleRun(true);

        populateItemHashmaps();

        //Sets best target
        Main.onLevelUpEvent();


    }

    public static void populateItemHashmaps(){
        Longtails.populateHashMaps();
        Butterflies.populateHashMaps();
        DeadfallKebbits.populateHashMaps();
        FalconKebbits.populateHashMaps();
        Chinchompas.populateHashMaps();
        EaglesPeakQuest.populateHashMap();
        PurchaseItems.populateHashMap();
    }


}
