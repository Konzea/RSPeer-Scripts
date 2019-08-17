package Chin_Hunter.Executes;

import Chin_Hunter.Executes.Eagles_Peak.QuestMain;
import Chin_Hunter.Executes.Herblore.Herblore_Training;
import Chin_Hunter.Helpers.ItemBuying;
import Chin_Hunter.Helpers.RequiredItem;
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
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

public class Banking {

    private static RequiredItem[] itemsRequired;
    private static RequiredItem[] itemsToBuy ;
    private static boolean initialBankCheckComplete;
    private static boolean finalBankCheckComplete;

    private static ItemBuying itemBuyer = null;

    private Banking(){
        //Private Default Constructor
    }

    public static void onStart(){
        itemBuyer = new ItemBuying();

        ScriptState bestState = Main.getBestHuntingState();
        itemsRequired = bestState.getItemsToBuy();

        itemsToBuy = null;
        initialBankCheckComplete = false;
        finalBankCheckComplete = false;
    }

    public static void execute(){
        if (Main.hasItems(itemsRequired) && finalBankCheckComplete){
            //Got all the items, close the bank and begin hunting
            if (Bank.isOpen()){
                if (Bank.close())
                    Time.sleepUntil(Bank::isClosed,2000);
            }else {
                Log.fine("Got all items, lets go!");
                Main.updateScriptState(Main.getBestHuntingState());
            }
            return;
        }

        if (itemBuyer.isFinishedBuying()){
            //If we're training herb, herb banking is handled in it's class so leave now
            if (Main.getBestHuntingState() == ScriptState.HERBLORE_TRAINING){
                Log.fine("Finished buying all herblore training supplies");
                Herblore_Training.setHasAllItems(true);
                Main.updateScriptState(ScriptState.HERBLORE_TRAINING);
                return;
            }
            //Otherwise continue to handle withdrawing correct items
        }else if (itemsToBuy != null){
            itemBuyer.BuyItems(itemsToBuy);
            return;
        }

        if (Main.isAtFeldipHills() || Main.isAtPiscatoris() || !isInGielinor()){
            teleportToBank();
            return;
        }

        if (!Bank.isOpen()){
            openNearestBank();
            return;
        }

        if (isGeBuyWindowOpen()){
            closeGeWindow();
            return;
        }

        if (!initialBankCheckComplete){
            if (Inventory.getCount() > 0){
                //Deposit all to avoid walking across RS with your cash stack...
                if (Bank.depositInventory())
                    Time.sleepUntil(()->Inventory.getCount() == 0, 2000);
                return;
            }
            //Find all item we need to buy
            itemsToBuy = ItemBuying.getAllItemsToBuy(QuestMain.questComplete()?itemsRequired:RequiredItem.concat(itemsRequired, QuestMain.getRequiredItems()));
            initialBankCheckComplete = true;
            if (itemsToBuy.length > 0) {
                Log.fine("Need to buy items");
                RequiredItem.logAll(itemsToBuy);
                return;
            }else
                finalBankCheckComplete = true;

            //Let herblore training handle banking
            if (Main.getBestHuntingState() == ScriptState.HERBLORE_TRAINING){
                Herblore_Training.setHasAllItems(true);
                Log.fine("Found all the items we need in the bank.");
                Main.updateScriptState(ScriptState.HERBLORE_TRAINING);
                return;
            }
        }
        withdrawRequiredItems();
    }

    private static void openNearestBank(){
        Position nearestBank = BankLocation.getNearest().getPosition();
        if (Players.getLocal().distance(nearestBank) < 15) {
            if (GrandExchange.isOpen()){
                closeGrandExchange();
                return;
            }
            if (Bank.open())
                Time.sleepUntil(Bank::isOpen, 5000, 10000);
            return;
        }
        if (Inventory.contains("Varrock teleport") && !Main.isInVarrock()
                && BankLocation.getNearest().getPosition().distance(Players.getLocal()) > 30){
            teleportToBank();
            return;
        }
        Main.walkTo(nearestBank);
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

    private static void withdrawRequiredItems(){
        //Deposit all items once, then withdraw the ones we want.
        if (!finalBankCheckComplete){
            if (Inventory.getCount() == 0) {
                finalBankCheckComplete = true;
            }else {
                if (Bank.depositInventory())
                    Time.sleepUntil(() -> Inventory.getCount() == 0, 2500);
            }
            return;
        }
        for (RequiredItem requiredItem : itemsRequired){
            Predicate<Item> pred = x->x.getName().equalsIgnoreCase(requiredItem.getName());
            Item[] inventItem = Inventory.getItems(pred);
            int withdrawnAmount = Main.getCount(inventItem);

            if (inventItem.length > 0){
                if (inventItem[0].isNoted()) {
                    if (Bank.depositAll(inventItem[0].getName()))
                        Time.sleepUntil(() -> Main.getCount(inventItem) != withdrawnAmount, 2000);
                    return;
                }
            }

            if (withdrawnAmount == requiredItem.getAmountRequired())
                continue;


            final BooleanSupplier correctAmountInInvent = () -> Main.getCount(Inventory.getItems(pred)) == requiredItem.getAmountRequired();
            if (withdrawnAmount == 0){
                if (Bank.withdraw(requiredItem.getName(), requiredItem.getAmountRequired()))
                    Time.sleepUntil(correctAmountInInvent, 2000);
                continue;
            }

            if (withdrawnAmount < requiredItem.getAmountRequired()){
                if (Bank.withdraw(requiredItem.getName(), requiredItem.getAmountRequired() - withdrawnAmount))
                    Time.sleepUntil(correctAmountInInvent, 2000);
                continue;
            }

            if (Bank.deposit(requiredItem.getName(), -(requiredItem.getAmountRequired() - withdrawnAmount)))
                Time.sleepUntil(correctAmountInInvent, 2000);

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
            Time.sleepUntil(teleportSuccessful,  15000);
    }

    private static void closeGrandExchange() {
        InterfaceComponent closeBtn = Interfaces.getComponent(465, 2).getComponent(11);
        if (closeBtn == null) return;
        if (closeBtn.interact("Close"))
            Time.sleepUntil(()->!GrandExchange.isOpen(), 3000);
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
