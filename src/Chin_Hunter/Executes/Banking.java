package Chin_Hunter.Executes;

import Chin_Hunter.Executes.Herblore.Druidic_Ritual;
import Chin_Hunter.Executes.Hunting.Chinchompas;
import Chin_Hunter.Executes.Hunting.DeadfallKebbits;
import Chin_Hunter.Executes.Hunting.FalconKebbits;
import Chin_Hunter.Executes.Questing.QuestMain;
import Chin_Hunter.Main;
import Chin_Hunter.States.ScriptState;
import org.rspeer.runetek.adapter.component.InterfaceComponent;
import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.api.commons.BankLocation;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.component.GrandExchange;
import org.rspeer.runetek.api.component.Interfaces;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.component.tab.Magic;
import org.rspeer.runetek.api.component.tab.Spell;
import org.rspeer.runetek.api.input.Keyboard;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.ui.Log;

import java.awt.event.KeyEvent;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

public class Banking {

    private static Map<String, Integer> itemsRequired = null;
    private static boolean doneBankCheck = false;

    private Banking(){
        //Private Default Constructor
    }

    public static void onStart(){
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
                itemsRequired = QuestMain.getRequiredItems();
                break;
            case CHINCHOMPAS:
                itemsRequired = Chinchompas.getRequiredItems();
                break;
            case DRUIDIC_RITUAL_QUEST:
                itemsRequired = Druidic_Ritual.getRequiredItems();
                break;
            default:
                Log.severe("Error: Invalid banking state");
                break;
        }
    }

    public static void execute(){
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

        if (Main.isAtFeldipHills() || Main.isAtPiscatoris() || !isInGielinor()){
            teleportToBank();
            return;
        }

        if (!Bank.isOpen()){
            if (GrandExchange.isOpen()){
                if (PurchaseItems.closeGE())
                    Time.sleepUntil(()->!GrandExchange.isOpen(), 3000);
                return;
            }
            Position nearestBank = BankLocation.getNearest().getPosition();
            if (Players.getLocal().distance(nearestBank) < 15) {
                if (Bank.open())
                    Time.sleepUntil(Bank::isOpen, 5000);
                return;
            }

            Main.walkTo(nearestBank);
            return;
        }

        if (isGeBuyWindowOpen()){
            closeGeWindow();
            return;
        }

        if (!doneBankCheck){
            if (Inventory.getCount() > 0){
                if (Bank.depositInventory())
                    Time.sleepUntil(()->Inventory.getCount() == 0, 2000);
                return;
            }
            if (PurchaseItems.getAllItemsToBuy().size() > 0) {
                Log.fine("Need to buy items");
                PurchaseItems.getAllItemsToBuy().forEach((k,v)->Log.info(k + ": " + v));
                Main.updateScriptState(ScriptState.PURCHASE_ITEMS);
                return;
            }
            doneBankCheck = true;
        }
        handleBanking();

    }

    private static boolean isInGielinor(){
        Position Local = Players.getLocal().getPosition();
        int x = Local.getX();
        int y = Local.getY();
        int z = Local.getFloorLevel();
        if (z != 0)
            return false;
        if (x < 1152 || y < 2496)
            return false;
        if (x > 3903 || y > 4159)
            return false;
        return true;
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
        Position startPosition = Players.getLocal().getPosition();
        Item varrockTele = Inventory.getFirst("Varrock teleport");
        final BooleanSupplier teleportSuccessful = () -> startPosition.distance(Players.getLocal()) > 30;
        if (varrockTele != null){
            if (varrockTele.interact("Break"))
                Time.sleepUntil(teleportSuccessful, 10000);
            return;
        }
        Item camelotTele = Inventory.getFirst("Camelot teleport");
        if (camelotTele != null){
            if (camelotTele.interact("Break"))
                Time.sleepUntil(teleportSuccessful, 10000);
            return;
        }
        Item faladorTele = Inventory.getFirst("Falador teleport");
        if (faladorTele != null){
            if (faladorTele.interact("Break"))
                Time.sleepUntil(teleportSuccessful, 10000);
            return;
        }
        if (Magic.interact(Spell.Modern.HOME_TELEPORT, "Cast"))
            Time.sleepUntil(teleportSuccessful, 15000);
    }

    private static boolean isGeBuyWindowOpen(){
        InterfaceComponent Window = Interfaces.getComponent(162, 45);
        if (Window == null) return false;
        return Window.isVisible() && Window.getText().contains("What would you like to buy?");
    }

    private static void closeGeWindow(){
        if (!isGeBuyWindowOpen())
            return;
        Keyboard.pressEventKey(KeyEvent.VK_ESCAPE);
        Time.sleepUntil(()->!isGeBuyWindowOpen(), 2500);
    }

}
