package Combat_Starter.Executes;

import Combat_Starter.Enums.ScriptState;
import Combat_Starter.Main;
import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.tab.Equipment;
import org.rspeer.runetek.api.component.tab.EquipmentSlot;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.ui.Log;

public class Equipping_Gear {

    private Equipping_Gear(){
        //Private default constructor
    }

    public static void execute(){
        if (gearEquipped())
            Main.updateScriptState(Main.getPreviousScriptState());
        else{
            if (Main.onlyHasEquipment()){
                int attempt = 0;
                while (true) {
                    if (equipGear())
                        break;
                    else if (attempt <= 3)
                        attempt++;
                    else{
                        Log.info("Attempted to equip gear 3 times but failed.");
                        Main.updateScriptState(null);
                        break;
                    }
                }
            }else{
                Main.updateScriptState(ScriptState.BANKING);
            }
        }
    }

    /**
     * Checks if mainhand and offhand is equipping anything, not specifically weapon and shield.
     * @return True if both mainhand and offhand slots equipped, false if not.
     */
    public static boolean gearEquipped(){
        return Equipment.isOccupied(EquipmentSlot.MAINHAND) && Equipment.isOccupied(EquipmentSlot.OFFHAND);
    }

    /**
     * Attempts to equip sword and shield.
     * @return Returns true if gear equipped, false if not.
     */
    public static boolean equipGear(){
            Item[] items = Inventory.getItems(x -> x.containsAction("Wield") || x.containsAction("Wear"));
            for (Item i : items) {
                i.click();
                Time.sleep(200, 550);
            }
            Time.sleepUntil(Inventory::isEmpty, 2000);
            return gearEquipped();
    }
}
