package Chin_Hunter.Executes.Hunting;

import Chin_Hunter.Helpers.Hunter;
import Chin_Hunter.Main;
import Chin_Hunter.States.ScriptState;
import org.rspeer.ui.Log;

import java.util.HashMap;
import java.util.Map;

public class DeadfallKebbits {

    private static final Map<String, Integer> MINIMUM_REQUIRED_ITEMS = new HashMap<>();
    private static final Map<String, Integer> REQUIRED_ITEMS = new HashMap<>();

    private DeadfallKebbits(){
        //Private default constructor
    }

    public static void onStart(){

    }

    public static void execute(){
        if (!Main.isAtPiscatoris()) {
            if (!haveRequiredItems()) {
                Main.updateScriptState(ScriptState.BANKING);
                return;
            }
            Hunter.teleportToPiscatoris();
            return;
        }



    }

    static void HuntDeadfallKebbits(){
        if (!haveMinimumRequiredItems()) {
            Log.severe("Minimum required items not found");
            Main.updateScriptState(ScriptState.BANKING);
            return;
        }

        //If don't have 1 logs, chop

    }

    public static void populateHashMaps(){
        if (MINIMUM_REQUIRED_ITEMS.isEmpty()) {
            MINIMUM_REQUIRED_ITEMS.put("Knife", 1);
            MINIMUM_REQUIRED_ITEMS.put("Axe", 1);
            Butterflies.getMinimumRequiredItems().forEach(MINIMUM_REQUIRED_ITEMS::put);
        }
        if (REQUIRED_ITEMS.isEmpty()){
            REQUIRED_ITEMS.put("Knife", 1);
            REQUIRED_ITEMS.put("Bronze Axe", 1);
            REQUIRED_ITEMS.put("Piscatoris teleport", 1);
            Butterflies.getRequiredItems().forEach(REQUIRED_ITEMS::put);
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
        return Main.hasItems(MINIMUM_REQUIRED_ITEMS)
                && Butterflies.haveMinimumRequiredItems();
    }

    public static boolean haveRequiredItems(){
        if (REQUIRED_ITEMS.isEmpty()) {
            Log.severe("Hashmap not populated.");
            Main.updateScriptState(null);
            return false;
        }
        return Main.hasItems(REQUIRED_ITEMS)
                && Butterflies.haveRequiredItems();
    }

}
