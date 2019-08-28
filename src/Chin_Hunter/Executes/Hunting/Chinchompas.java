package Chin_Hunter.Executes.Hunting;

import Chin_Hunter.Helpers.RequiredItem;
import Chin_Hunter.Hunter.Hunting;
import Chin_Hunter.Hunter.Trap_Admin.TrapType;
import Chin_Hunter.Main;
import org.rspeer.ui.Log;

public class Chinchompas {

    private static final RequiredItem[] MINIMUM_REQUIRED_ITEMS = {
            new RequiredItem("Box trap", Hunting.getMaxTrapCount())
    };

    private static final RequiredItem[] REQUIRED_ITEMS = {
            new RequiredItem("Box trap", 24),
            new RequiredItem("Varrock teleport", 1),
            new RequiredItem("Feldip hills teleport", 1)};


    private Chinchompas(){
        //Private default constructor
    }

    public static void onStart(){

    }

    public static void execute(){
        Log.info("Well... Yikes. This whole Chinchompa based script doesn't actually support chins yet.");
        Log.info("Sorry about that :/");
        Log.info("It will be added soon.");
        Main.updateScriptState(null);
        /*
        if (!haveMinimumRequiredItems()){
            Log.severe("Don't have minimum required items. Banking");
            Main.updateScriptState(ScriptState.BANKING);
            return;
        }
        */
    }



    public static RequiredItem[] getMinimumRequiredItems() {
        return MINIMUM_REQUIRED_ITEMS;
    }

    public static RequiredItem[] getRequiredItems() {
        return REQUIRED_ITEMS;
    }

    public static boolean haveMinimumRequiredItems() {
        return Main.hasItems(MINIMUM_REQUIRED_ITEMS, TrapType.BOX_TRAP);
    }

    public static boolean haveRequiredItems() {
        return Main.hasItems(REQUIRED_ITEMS);
    }

}
