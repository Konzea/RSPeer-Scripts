package Master_Thiever.Executes;

import Master_Thiever.Enums.ScriptState;
import Master_Thiever.Enums.Target;
import Master_Thiever.Main;
import org.rspeer.runetek.api.component.tab.Combat;
import org.rspeer.runetek.api.component.tab.Inventory;
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

        //Set target npc based on levels
        Main.onLevelUpEvent();

        //Make sure player has just wep and shield
        //TODO Check Equipped items too
        if (Main.getCurrentTarget() == Target.MEN && Inventory.getItems(x->!x.getName().contains("Coin") || x.getStackSize() > 1000).length > 0)
            Main.updateScriptState(ScriptState.BANKING);
        else
            Main.updateScriptState(ScriptState.THIEVING);


    }

//If looting men and has anything other than coin
}
