package Chin_Hunter.Executes.Herblore;

import Chin_Hunter.Helpers.RequiredItem;
import Chin_Hunter.Main;
import Chin_Hunter.States.ScriptState;
import org.rspeer.runetek.adapter.Interactable;
import org.rspeer.runetek.adapter.component.InterfaceComponent;
import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.component.Dialog;
import org.rspeer.runetek.api.component.Interfaces;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.component.tab.Skill;
import org.rspeer.runetek.api.component.tab.Skills;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.ui.Log;

import java.util.function.BooleanSupplier;

public class Herblore_Training {

    private static final String HERB = "Guam leaf";
    private static final String UNFINISHED_POTION = "Guam potion (unf)";
    private static final String SECONDARY = "Eye of newt";
    private static final String WATER = "Vial of water";

    private static final RequiredItem[] REQUIRED_ITEMS = {
            new RequiredItem(HERB, getPotionsRequired()),
            new RequiredItem(WATER, getPotionsRequired()),
            new RequiredItem(SECONDARY, getPotionsRequired())
    };
    private static boolean haveAllItems = false;

    public static void setHasAllItems(boolean bool){
        haveAllItems = bool;
    }

    public static void onStart() {

    }

    public static void execute() {
        if (!haveAllItems) {
            Main.updateScriptState(ScriptState.BANKING);
            return;
        }
        if (Players.getLocal().getAnimation() != -1){
            Time.sleepUntil(()->!haveMinimumIngredients() || Players.getLocal().getAnimation() == -1 || Dialog.isOpen(), 3000);
            if (!haveMinimumIngredients())
                return;
            Time.sleepUntil(()->!haveMinimumIngredients() || Players.getLocal().getAnimation() != -1 || Dialog.isOpen(), Random.nextInt(2000, 4000));
            return;
        }

        if (Dialog.isOpen()){
            if (Dialog.processContinue())
                Time.sleepUntil(()->!Dialog.isOpen(), Random.nextInt(800, 1000));
            return;
        }

        if (!haveMinimumIngredients() || inventContainsNotedItems()){
            withdrawSupplies();
            return;
        }
        makePotions();

        //if chat open
            //press continue
        //If is making a potion
            //Sleep until done making or chat opens
        //If bank is closed and got items to make a potion
            //Make
        //withdraw supplies
    }

    private static boolean isCreationDialogOpen(){
        return Interfaces.getComponent(270, 14) != null;
    }

    private static void clickMakeAll(){
        if (!isCreationDialogOpen()){
            return;
        }
        if (!isMakeAllSelected()){
            selectMakeAll();
            return;
        }
        InterfaceComponent Make = Interfaces.getComponent(270, 14);
        if (Make == null){
            Log.info("Could not find make button");
            return;
        }
        if (Make.interact("Make"))
            Time.sleepUntil(()->Players.getLocal().getAnimation() != -1, 2000);
    }

    private static boolean isMakeAllSelected(){
        InterfaceComponent makeAllBtn = Interfaces.getComponent(270, 12);
        if (makeAllBtn == null)
            return false;
        return !makeAllBtn.containsAction("All");
    }

    private static void selectMakeAll(){
        InterfaceComponent makeAllBtn = Interfaces.getComponent(270, 12);
        if (makeAllBtn == null)
            return;
        if (makeAllBtn.interact("All"))
            Time.sleepUntil(Herblore_Training::isMakeAllSelected, 2000);
    }

    private static void makePotions(){
        if (Bank.isOpen()) {
            if (Bank.close())
                Time.sleepUntil(Bank::isClosed, 2500);
            return;
        }
        if (isCreationDialogOpen()){
            clickMakeAll();
            return;
        }
        if (Inventory.getCount(WATER) > 0 && Inventory.getCount(HERB) > 0) {
            if (Inventory.use(x->x.getName().equalsIgnoreCase(HERB), Inventory.getFirst(WATER)))
                Time.sleepUntil(Herblore_Training::isCreationDialogOpen, 2500);
        }
        if (Inventory.getCount(UNFINISHED_POTION) > 0 && Inventory.getCount(SECONDARY) > 0) {
            if (Inventory.use(x->x.getName().equalsIgnoreCase(SECONDARY), Inventory.getFirst(UNFINISHED_POTION)))
                Time.sleepUntil(Herblore_Training::isCreationDialogOpen, 2500);
        }
    }

    private static boolean haveAllIngredients(){
        if (Inventory.getCount(WATER) == 14 && Inventory.getCount(HERB) == 14)
            return true;
        if (Inventory.getCount(UNFINISHED_POTION) == 14 && Inventory.getCount(SECONDARY) == 14)
            return true;
        return false;
    }

    private static boolean haveMinimumIngredients(){
        if (Inventory.getCount(WATER) > 0 && Inventory.getCount(HERB) > 0)
            return true;
        if (Inventory.getCount(UNFINISHED_POTION) > 0 && Inventory.getCount(SECONDARY) > 0)
            return true;
        return false;
    }

    private static void withdrawSupplies(){
        if (haveAllIngredients() && Bank.isOpen()){
            if (Bank.close())
                Time.sleepUntil(Bank::isClosed, 2500);
            return;
        }
        if (!Bank.isOpen()){
            if (Bank.open())
                Time.sleepUntil(Bank::isOpen, 4000);
            return;
        }
        if (needToDepositInvent()){
            if (Bank.depositInventory())
                Time.sleepUntil(()->Inventory.getCount() == 0, 2500);
            return;
        }
        if (Bank.getCount(UNFINISHED_POTION) + Inventory.getCount(UNFINISHED_POTION) < getPotionsRequired() ){
            if (Inventory.contains(UNFINISHED_POTION)){
                if (Bank.depositInventory())
                    Time.sleepUntil(()->Inventory.getCount() == 0, 2500);
                return;
            }
            if (Bank.contains(WATER) && Inventory.getCount(WATER) != 14){
                withdraw14Items(WATER);
                return;
            }
            if (Bank.contains(HERB) && Inventory.getCount(HERB) != 14){
                withdraw14Items(HERB);
                return;
            }
            if (Inventory.contains(WATER) && Inventory.contains(HERB)){
                if (Bank.close())
                    Time.sleepUntil(Bank::isClosed, 2500);
            }
            return;
        }
        if (Inventory.contains("Attack potion (3)")){
            if (Bank.depositInventory())
                Time.sleepUntil(()->Inventory.getCount() == 0, 2500);
            return;
        }
        if (Bank.contains(UNFINISHED_POTION) && Inventory.getCount(UNFINISHED_POTION) != 14){
            withdraw14Items(UNFINISHED_POTION);
            return;
        }
        if (Bank.contains(SECONDARY) && Inventory.getCount(SECONDARY) != 14){
            withdraw14Items(SECONDARY);
            return;
        }
        if (Inventory.contains(UNFINISHED_POTION) && Inventory.contains(SECONDARY)){
            if (Bank.close())
                Time.sleepUntil(Bank::isClosed, 2500);
        }
    }

    private static boolean needToDepositInvent(){
        if (Inventory.getCount() == 0)
            return false;
        if (Inventory.contains("Coins"))
            return true;
        if (inventContainsNotedItems())
            return true;
        String[] validItemNames = {WATER, SECONDARY, UNFINISHED_POTION, HERB};
        if (Inventory.getCount(validItemNames) != Inventory.getCount())
            return true;
        return false;
    }

    private static boolean inventContainsNotedItems(){
        return Inventory.getCount(Item::isNoted) > 0;
    }

    private static void withdraw14Items(String itemName){
        int amountInInvent = Inventory.getCount(itemName);
        BooleanSupplier correctAmountInInvent = ()->Inventory.getCount(itemName) == 14;
        if (amountInInvent < 14){
            if (Bank.withdraw(itemName, 14 - amountInInvent))
                Time.sleepUntil(correctAmountInInvent, 2000);
            return;
        }
        if (amountInInvent > 14){
            if (Bank.deposit(itemName, -(14 - amountInInvent)))
                Time.sleepUntil(correctAmountInInvent, 2000);
        }
    }

    private static int getPotionsRequired(){
        double xpPerPotion = 25;
        int startXp = Skills.getExperience(Skill.HERBLORE);
        int goalXp = 4000;

        int xpToGoal = goalXp - startXp;
        return (int)(xpToGoal/xpPerPotion);
    }

    public static RequiredItem[] getRequiredItems() {
        /*
        if (Bank.isOpen()){
            int herbCount = Bank.getCount(HERB);
            int waterCount = Bank.getCount(WATER);
            int newtCount = Bank.getCount(SECONDARY);
            int unfPotionCount = Bank.getCount(UNFINISHED_POTION);

            RequiredItem herbItem = RequiredItem.getByName(HERB, REQUIRED_ITEMS);
            RequiredItem waterItem = RequiredItem.getByName(WATER, REQUIRED_ITEMS);
            RequiredItem secondaryCount = RequiredItem.getByName(SECONDARY, REQUIRED_ITEMS);


        }
        */
        return REQUIRED_ITEMS;
    }


    public static boolean haveRequiredItems() {
        return Main.hasItems(REQUIRED_ITEMS);
    }
}
