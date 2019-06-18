package Chin_Hunter.Executes;

import Chin_Hunter.Main;
import Chin_Hunter.States.ScriptState;
import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.component.tab.Magic;
import org.rspeer.runetek.api.component.tab.Spell;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.api.scene.Players;

import java.util.Map;

public class Banking {

    private static final Area VARROCK_AREA = Area.rectangular(3071, 3518, 3295, 3334);
    private static final Area LUMBRIDGE_AREA = Area.rectangular(3210, 3233, 3234, 3204);

    private static Map<String, Integer> itemsRequired;

    private Banking(){
        //Private Default Constructor
    }


    public static void execute(){
        ScriptState bestState = Main.getBestHuntingState();

        switch (bestState){
            case MUSEUM_QUIZ:
                break;
            case LONGTAILS:
                break;
            case BUTTERFLIES:
                break;
            case TRAPFALL_KEBBITS:
                break;
            case FALCON_KEBBITS:
                break;
            case EAGLES_PEAK_QUEST:
                break;
            case CHINCHOMPAS:
                break;
        }

        if (Main.isAtFeldipHills() || Main.isAtPiscatoris()){
            teleportToBank();
            return;
        }



    }

    private static void teleportToBank(){
        Item varrockTele = Inventory.getFirst("Varrock teleport");
        if (varrockTele != null){
            if (varrockTele.interact("Break"))
                Time.sleepUntil(()->VARROCK_AREA.contains(Players.getLocal()), 10000);
            return;
        }
        if (Magic.interact(Spell.Modern.HOME_TELEPORT, "Cast"))
            Time.sleepUntil(()->LUMBRIDGE_AREA.contains(Players.getLocal()), 15000);
    }
}
