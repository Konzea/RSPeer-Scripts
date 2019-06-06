package Combat_Starter.Executes;

import Combat_Starter.Main;
import Combat_Starter.Enums.ScriptState;
import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.adapter.scene.Player;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.component.tab.Equipment;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.ui.Log;

import java.util.function.Predicate;

public class Banking {

    private Banking(){
        //Private Default Constructor
    }

    private final static Predicate<Item> weapon = x->x.getName().contains("sword");
    private final static Predicate<Item> shield = x->x.getName().contains("shield");


    public static void execute(){
        if (Players.getLocal().getPosition().getFloorLevel() != 2) {
            Log.info("Walking to bank");
            Main.updateScriptState(ScriptState.WALKING);
        }else{
            if (Bank.isOpen()){
                if (Main.onlyHasEquipment()){
                    //We have just the equipment we need, close the bank
                    if (Bank.close())
                        Time.sleepUntil(Bank::isClosed, 2000);
                }else{
                    if (hasSingleRequiredItem()) {
                        //Has 1 item, withdraw the other
                        if (Inventory.contains(weapon))
                            Bank.withdraw(shield, 1);
                        else if (Inventory.contains(shield))
                            Bank.withdraw(weapon,1);
                        Time.sleepUntil(()->Inventory.getItems().length == 2, 2000);
                    }else if (Inventory.isEmpty() && Equipment.getItems().length == 0){
                        if (bankContainsReqItems()){
                            //Withdraw Req items
                            Bank.withdraw(weapon, 1);
                            Bank.withdraw(shield, 1);
                            Time.sleepUntil(()->Inventory.getItems().length == 2, 2000);
                        }else{
                            //Not got any fucking gear.... Have to go get some myself! ffs.
                            Log.info("You seriously have no gear...? Going and getting some.");
                            Main.updateScriptState(ScriptState.GETTING_GEAR);
                        }
                    }else {
                        Log.info("Invent full of crap, depositing all");
                        Bank.depositInventory();
                        Bank.depositEquipment();
                        Time.sleepUntil(()->Equipment.getItems().length == 0 && Inventory.isEmpty(), 2000);
                    }
                }
            }else{
                if (Main.onlyHasEquipment()) {
                    //Got all the shit and bank closed, start fighting
                    Log.info("Gear found and ready to fight");
                    Main.updateScriptState(ScriptState.FIGHTING);
                    //Fighting will check where you are and then handle walking
                }
                else if (Bank.open())
                    Time.sleepUntil(Bank::isOpen, 2000);
            }
        }
    }

    //If the bank contains the items we need to run
    private static boolean bankContainsReqItems(){
        return Bank.contains(weapon) && Bank.contains(shield);
    }

    //Has one item in invent and nothing equipped. Just need to withdraw the other item now.
    private static boolean hasSingleRequiredItem(){
        return Inventory.getItems().length == 1
                && Equipment.getItems().length == 0
                && (Inventory.contains(weapon) || Inventory.contains(shield));
    }
}
