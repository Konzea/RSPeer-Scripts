package Master_Thiever.Executes;

import Master_Thiever.Enums.ScriptState;
import Master_Thiever.Main;
import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.ui.Log;

public class Banking {

    private static int foodAmount = 16;
    private static int necklaceAmount = 2;

    private Banking(){
        //Private Default Constructor
    }

    public static void execute(){
        int inventFoodCount = Inventory.getCount(Main.getFoodName());
        int inventNeckCount = Inventory.getCount(Main.getNecklaceName());

        if (Bank.isOpen()){
            //Adjust food and necklaces to withdraw so we leave the bank with exactly the right amount
            //Also so we can eat to full before going back to thieving
            int adjustedFoodAmount = foodAmount + Main.getFoodToFullHP();
            int adjustedNecklaceAmount = (Main.necklaceEquipped()) ? necklaceAmount:necklaceAmount + 1;

            Item[] notedItems = Inventory.getItems(Item::isNoted);
            if (Inventory.getItems(x->x.getName().contains("seed")).length > 0 || notedItems.length > 0){
                //If we have seeds in invent or noted items, deposit all
                //TODO Potentially change this as it could cause issues. Works so far tho
                if (Bank.depositInventory())
                    Time.sleepUntil(()->Inventory.getItems().length == 0, 2000);
            }else {
                if (inventFoodCount == adjustedFoodAmount){
                    if (inventNeckCount == adjustedNecklaceAmount){
                        //Got everything we need, exit bank.
                        if (Bank.close())
                            Time.sleepUntil(Bank::isClosed, 2000);
                    }else{
                        //Withdrawing necklaces
                        if (Bank.getCount(Main.getNecklaceName()) >= adjustedNecklaceAmount) {
                            if (Bank.withdraw(Main.getNecklaceName(), adjustedNecklaceAmount))
                                Time.sleepUntil(()->Inventory.getCount(Main.getNecklaceName()) == adjustedNecklaceAmount, 2000);
                        } else {
                            Log.severe("No necklaces found. Stopping.");
                            Main.updateScriptState(null);
                        }
                    }
                }else {
                    //Withdrawing food
                    if (Bank.getCount(Main.getFoodName()) >= adjustedFoodAmount) {
                        if (Bank.withdraw(Main.getFoodName(), adjustedFoodAmount))
                            Time.sleepUntil(()->Inventory.getCount(Main.getFoodName()) == adjustedFoodAmount, 2000);
                    } else {
                        Log.severe("No food found. Stopping.");
                        Main.updateScriptState(null);
                    }
                }
            }
        }else {
            if (inventFoodCount > foodAmount) {
                //Eat to full at bank
                Main.eatFood(inventFoodCount - foodAmount);
            } else if (inventFoodCount == foodAmount){
                //Food sorted, checking necklaces now
                if (Main.necklaceEquipped()){
                    if (inventNeckCount == necklaceAmount) {
                        //Ammy equipped, got the right amount in invent and right amount of food.
                        Thieving.performDropping(Main.getCurrentTarget());
                        Main.updateScriptState(ScriptState.THIEVING);
                    }else{
                        if (Bank.open())
                            Time.sleepUntil(Bank::isOpen, 5000);
                    }
                }else{
                    //No necklace equipped, if we have one equip it, otherwise open the bank
                    if (inventNeckCount > 0)
                        Main.equipNecklace();
                    else {
                        if (Bank.open())
                            Time.sleepUntil(Bank::isOpen, 5000);
                    }

                }
            }else{
                if (Bank.open())
                    Time.sleepUntil(Bank::isOpen, 5000);
            }

        }
    }
}
