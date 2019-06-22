package Chin_Hunter.Executes.Hunting;

import Chin_Hunter.Main;
import org.rspeer.ui.Log;

import java.util.HashMap;
import java.util.Map;

public class FalconKebbits {

    private static final Map<String, Integer> MINIMUM_REQUIRED_ITEMS = new HashMap<>();
    private static final Map<String, Integer> REQUIRED_ITEMS = new HashMap<>();

    private FalconKebbits(){
        //Private default constructor
    }

    public static void execute(){

    }

    public static void populateHashMaps(){
        if (MINIMUM_REQUIRED_ITEMS.isEmpty()) {
            MINIMUM_REQUIRED_ITEMS.put("Coins", 500);
        }
        if (REQUIRED_ITEMS.isEmpty()) {
            REQUIRED_ITEMS.put("Coins", 7500);
            REQUIRED_ITEMS.put("Piscatoris teleport", 1);
        }
    }

    public static Map<String, Integer> getMinimumRequiredItems(){
        return MINIMUM_REQUIRED_ITEMS;
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
