package Chin_Hunter.Executes.Hunting;

import Chin_Hunter.Helpers.Trapping;
import Chin_Hunter.Main;
import Chin_Hunter.States.ScriptState;
import org.rspeer.ui.Log;

import java.util.HashMap;
import java.util.Map;

public class Chinchompas {

    private static final Map<String, Integer> MINIMUM_REQUIRED_ITEMS = new HashMap<>();
    private static final Map<String, Integer> REQUIRED_ITEMS = new HashMap<>();

    private Chinchompas(){
        //Private default constructor
    }

    public static void onStart(){

    }

    public static void execute(){
        if (!haveMinimumRequiredItems()){
            Log.severe("Don't have minimum required items. Banking");
            Main.updateScriptState(ScriptState.BANKING);
            return;
        }

        //HuntButterflies
    }

    public static void populateHashMaps(){
        if (MINIMUM_REQUIRED_ITEMS.isEmpty()) {
            MINIMUM_REQUIRED_ITEMS.put("Box trap", Trapping.getMaxTrapCount());
        }
        if (REQUIRED_ITEMS.isEmpty()){
            REQUIRED_ITEMS.put("Box trap", 24);
            REQUIRED_ITEMS.put("Varrock teleport", 1);
            REQUIRED_ITEMS.put("Feldip hills teleport", 1);
        }
    }

    public static Map<String, Integer> getRequiredItems(){
        return REQUIRED_ITEMS;
    }

    public static boolean haveMinimumRequiredItems(){
        if (MINIMUM_REQUIRED_ITEMS.isEmpty()) {
            Log.severe("Hashmap not populated.");
            Main.updateScriptState(null);
            return false;
        }
        return Main.hasItems(MINIMUM_REQUIRED_ITEMS);
    }

    public static boolean haveRequiredItems(){
        if (REQUIRED_ITEMS.isEmpty()) {
            Log.severe("Hashmap not populated.");
            Main.updateScriptState(null);
            return false;
        }
        return Main.hasItems(REQUIRED_ITEMS);
    }

}
