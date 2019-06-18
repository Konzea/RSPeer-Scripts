package Chin_Hunter.Executes;

import Chin_Hunter.Main;
import org.rspeer.ui.Log;

import java.util.HashMap;
import java.util.Map;

public class EaglesPeakQuest {

    private static final Map<String, Integer> requiredItems = new HashMap<>();

    private EaglesPeakQuest(){
        //Private default constructor
    }

    public static void execute(){

    }

    public static void populateHashMap(){
        if (requiredItems.isEmpty()) {
            requiredItems.put("Yellow dye", 1);
            requiredItems.put("Swamp tar", 1);
            requiredItems.put("Coins", 50);
            requiredItems.put("Necklace of passage", 1);
            requiredItems.put("Camelot teleport", 5);
        }
    }

    public static boolean hasAllRequiredItems(){
        if (requiredItems.isEmpty()) {
            Log.severe("Hashmap not populated.");
            Main.updateScriptState(null);
            return false;
        }
        return Main.hasItems(requiredItems);
    }


    public static boolean questComplete(){
        return false;
    }
}
