package Chin_Hunter.Executes;

import Chin_Hunter.Executes.Herblore.Druidic_Ritual;
import Chin_Hunter.Executes.Hunting.*;
import Chin_Hunter.Executes.Eagles_Peak.QuestMain;
import Chin_Hunter.Helpers.Paint;
import Chin_Hunter.Main;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.tab.Combat;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.ui.Log;

public class Starting {

    private Starting(){
        //Private Default Constructor
    }

    public static void execute(){
        //Starting
        Log.fine("Running AIO Chin Hunter by Shteve");
        Log.info("Waiting a few seconds for all the RS stuff to load.");
        Time.sleep(Random.nextInt(3000, 6000));
        Log.info("Ready to go.");
        Main.setPaint(new Paint());

        if (Combat.isAutoRetaliateOn())
            Combat.toggleAutoRetaliate(false);

        if (!Movement.isRunEnabled())
            Movement.toggleRun(true);

        //Sets best target
        Main.onLevelUpEvent();


    }


}
