package Chin_Hunter.Executes;

import Chin_Hunter.Main;
import org.rspeer.ui.Log;

import java.util.HashMap;
import java.util.Map;

public class EaglesPeakQuest {

    private static final Map<String, Integer> REQUIRED_ITEMS = new HashMap<>();

    private EaglesPeakQuest(){
        //Private default constructor
    }

    public static void execute(){

    }

    public static Map<String, Integer> getRequiredItems(){
        return REQUIRED_ITEMS;
    }


    public static void populateHashMap(){
        if (REQUIRED_ITEMS.isEmpty()) {
            REQUIRED_ITEMS.put("Yellow dye", 1);
            REQUIRED_ITEMS.put("Swamp tar", 1);
            REQUIRED_ITEMS.put("Coins", 50);
            REQUIRED_ITEMS.put("Necklace of passage(5)", 1);
            REQUIRED_ITEMS.put("Camelot teleport", 5);
        }
    }

    public static boolean hasAllRequiredItems(){
        if (REQUIRED_ITEMS.isEmpty()) {
            Log.severe("Hashmap not populated.");
            Main.updateScriptState(null);
            return false;
        }
        return Main.hasItems(REQUIRED_ITEMS);
    }


    public static boolean questComplete(){
        return false;
    }
}
