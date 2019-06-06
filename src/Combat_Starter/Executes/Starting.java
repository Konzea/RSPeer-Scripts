package Combat_Starter.Executes;

import Combat_Starter.Combat_Starter;
import Combat_Starter.Enums.ScriptState;
import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.api.commons.ArrayUtils;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.component.tab.*;

import java.util.Arrays;
import java.util.stream.Stream;

public class Starting {

    private Starting(){
        //Private Default Constructor
    }

    public static void execute(){
        //Starting
        //TODO Grab starting xp

        //Set target npc based on levels
        //Set attack style
        Combat_Starter.onLevelUpEvent();

        //Make sure player has just wep and shield
        if (onlyHasEquipment())
            Combat_Starter.updateScriptState(ScriptState.FIGHTING);
        else
            Combat_Starter.updateScriptState(ScriptState.BANKING);


    }

    //If the only things the player has is a sword and shield
    private static boolean onlyHasEquipment(){
        //Items from both invent and equipped
        Item[] items = Stream.concat(
                Arrays.stream(Inventory.getItems()),
                Arrays.stream(Equipment.getItems()))
                .toArray(Item[]::new);

        if (items.length != 2)
            return false;

        if (items[0].getName().contains("sword") && items[1].getName().contains("shield"))
            return true;
        if (items[1].getName().contains("sword") && items[0].getName().contains("shield"))
            return true;
        return false;
    }


}
