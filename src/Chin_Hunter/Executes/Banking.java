package Chin_Hunter.Executes;

import Chin_Hunter.Executes.Hunting.Chinchompas;
import Chin_Hunter.Executes.Hunting.DeadfallKebbits;
import Chin_Hunter.Executes.Hunting.FalconKebbits;
import Chin_Hunter.Main;
import Chin_Hunter.States.ScriptState;
import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.component.tab.Magic;
import org.rspeer.runetek.api.component.tab.Spell;
import org.rspeer.ui.Log;

import java.util.Map;
import java.util.function.Predicate;

public class Banking {


    private static Map<String, Integer> itemsRequired = null;
    private static boolean doneBankCheck = false;

    private Banking(){
        //Private Default Constructor
    }

    public static void onStart(){

    }

    public static void execute(){

        if (itemsRequired == null) {
            ScriptState bestState = Main.getBestHuntingState();

            switch (bestState) {
                case LONGTAILS:
                case BUTTERFLIES:
                case DEADFALL_KEBBITS:
                    itemsRequired = DeadfallKebbits.getRequiredItems();
                    FalconKebbits.getRequiredItems().forEach(itemsRequired::put);
                    break;
                case FALCON_KEBBITS:
                    itemsRequired = FalconKebbits.getRequiredItems();
                    break;
                case EAGLES_PEAK_QUEST:
                    itemsRequired = EaglesPeakQuest.getRequiredItems();
                    break;
                case CHINCHOMPAS:
                    itemsRequired = Chinchompas.getRequiredItems();
                    break;

                default:
                    Log.severe("Error: Invalid banking state");
                    break;
            }
            return;
        }

        if (Main.hasItems(itemsRequired)){
            //Got all the items, close the bank and begin hunting
            if (Bank.isOpen()){
                if (Bank.close())
                    Time.sleepUntil(Bank::isClosed,2000);
            }else {
                Log.fine("Got all items, lets hunt!");
                Main.updateScriptState(Main.getBestHuntingState());
            }
            return;
        }

        if (Main.isAtFeldipHills() || Main.isAtPiscatoris()){
            teleportToBank();
            return;
        }

        if (!Bank.isOpen()){
            if (Bank.open())
                Time.sleepUntil(Bank::isOpen, 2000);
            return;
        }

        if (!doneBankCheck){
            if (Inventory.getCount() > 0){
                if (Bank.depositInventory())
                    Time.sleepUntil(()->Inventory.getCount() == 0, 2000);
                return;
            }
            if (PurchaseItems.getAllItemsToBuy().size() > 0) {
                Main.updateScriptState(ScriptState.PURCHASE_ITEMS);
                Log.fine("Need to buy items");
                return;
            }
            doneBankCheck = true;
        }
        handleBanking();

    }

    private static void handleBanking(){
        for (Map.Entry<String, Integer> reqItem : itemsRequired.entrySet()) {
            Predicate<Item> pred = x->x.getName().toLowerCase().equals(reqItem.getKey().toLowerCase());
            Item[] inventItem = Inventory.getItems(pred);
            int withdrawnAmount = Main.getCount(inventItem);

            if (withdrawnAmount == reqItem.getValue())
                continue;

            if (withdrawnAmount == 0){
                if (Bank.withdraw(reqItem.getKey(), reqItem.getValue()))
                    Time.sleepUntil(()->Inventory.contains(reqItem.getKey()), 2000);
                continue;
            }

            if (withdrawnAmount < reqItem.getValue()){
                if (Bank.withdraw(reqItem.getKey(), reqItem.getValue() - withdrawnAmount))
                    Time.sleepUntil(()->Main.getCount(Inventory.getItems(pred)) == reqItem.getValue(), 2000);
                continue;
            }

            if (Bank.deposit(reqItem.getKey(), reqItem.getValue() - withdrawnAmount))
                Time.sleepUntil(()->Main.getCount(Inventory.getItems(pred)) == reqItem.getValue(), 2000);

        }
    }

    private static void teleportToBank(){
        Item varrockTele = Inventory.getFirst("Varrock teleport");
        if (varrockTele != null){
            if (varrockTele.interact("Break"))
                Time.sleepUntil(Main::isInVarrock, 10000);
            return;
        }
        Item camelotTele = Inventory.getFirst("Camelot teleport");
        if (camelotTele != null){
            if (camelotTele.interact("Break"))
                Time.sleepUntil(Main::isInCamelot, 10000);
            return;
        }
        if (Magic.interact(Spell.Modern.HOME_TELEPORT, "Cast"))
            Time.sleepUntil(Main::isInLumbridge, 15000);
    }


}
