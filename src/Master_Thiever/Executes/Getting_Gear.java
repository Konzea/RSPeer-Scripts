package Master_Thiever.Executes;

import Master_Thiever.Main;
import org.rspeer.ui.Log;

public class Getting_Gear {

    private Getting_Gear(){
        //Private Default Constructor
    }

    public static void execute(){
//Getting Gear
//if invent contains gear
//fight
//else get gear
        Log.info("Who starts a combat script without combat gear...");
        Log.info("Currently I haven't implemented getting gear from the trainer, sorry.");
        Main.updateScriptState(null);
    }
}
