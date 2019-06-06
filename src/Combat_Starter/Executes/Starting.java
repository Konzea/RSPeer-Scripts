package Combat_Starter.Executes;

import Combat_Starter.Main;
import Combat_Starter.Enums.ScriptState;
import org.rspeer.runetek.api.movement.Movement;

public class Starting {

    private Starting(){
        //Private Default Constructor
    }

    public static void execute(){
        //Starting
        //TODO Grab starting xp

        if (!Movement.isRunEnabled())
            Movement.toggleRun(true);

        //Set target npc based on levels
        //Set attack style
        Main.onLevelUpEvent();

        //Make sure player has just wep and shield
        if (Main.onlyHasEquipment())
            Main.updateScriptState(ScriptState.FIGHTING);
        else
            Main.updateScriptState(ScriptState.BANKING);


    }


}
