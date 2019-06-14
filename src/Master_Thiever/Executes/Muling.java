package Master_Thiever.Executes;

import Master_Thiever.Enums.ScriptState;
import Master_Thiever.Main;
import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.adapter.scene.Player;
import org.rspeer.runetek.api.Worlds;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.component.Trade;
import org.rspeer.runetek.api.component.WorldHopper;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.input.Keyboard;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.ui.Log;

public class Muling {

    private static String muleName;
    public static void setMuleName(String name){
        muleName = name;
    }

    private static int muleWorld;
    public static void setMuleWorld(int world){
        muleWorld = world;
    }

    private static int startWorld;
    public static void setStartWorld(int world){ startWorld = world; }

    private static boolean gotAllItems = false;
    private static int failiureCount = 0;
    private static boolean finishedMuling = false;

    //TODO Add timeout

    private Muling(){
        //Private default constructor
    }

    public static void execute(){
        if (muleName == null){
            Log.severe("Mule name not set. Going back to " + Main.getPreviousScriptState().name());
            finishMuling();
        }else if (failiureCount > 10) {
            Log.severe("Muling Failed. Returning to " + Main.getPreviousScriptState().name());
            finishMuling();
        }else {
            if (Worlds.getCurrent() == muleWorld) {
                if (gotAllItems) {
                    Item[] goodInventItems = Inventory.getItems(x->arrayContainsName(x, Main.getSeedsToKeep()));
                    if (!Trade.isOpen() && goodInventItems.length == 0) {
                        if (!finishedMuling)
                            Log.fine("Muling Successful");
                        finishMuling();
                    } else
                        handleTrade();
                } else {
                    handleBank();
                }
            } else {
                if (!finishedMuling){
                    //Hops to mule world, closes bank if it has to
                    if (!Bank.isOpen()) {
                        WorldHopper.hopTo(muleWorld);
                        Time.sleepUntil(() -> Worlds.getCurrent() == muleWorld, 4000);
                    } else {
                        if (Bank.close())
                            Time.sleepUntil(Bank::isClosed, 2000);
                    }
                }else{
                    finishMuling();
                }
            }
        }
    }

    private static void finishMuling(){
        finishedMuling = true;
        if (Bank.isOpen()){
            if (Bank.close())
                Time.sleepUntil(Bank::isClosed, 2000);
        }else if (Trade.isOpen()){
            if (Trade.decline())
                Time.sleepUntil(()->!Trade.isOpen(), 2000);
        }else if (Worlds.getCurrent() == startWorld) {
            muleName = null;
            muleWorld = -1;
            startWorld = -1;
            gotAllItems = false;
            failiureCount = 0;
            finishedMuling = false;
            Main.updateScriptState(ScriptState.BANKING);
        }else{
            if (WorldHopper.hopTo(startWorld))
                Time.sleepUntil(()->Worlds.getCurrent() == startWorld, 4000);
        }
    }

    private static void handleBank(){
        Item[] goodInventItems = Inventory.getItems(x->arrayContainsName(x, Main.getSeedsToKeep()));
        if (Bank.isOpen()){
            Item[] goodBankItems = Bank.getItems(x->arrayContainsName(x,Main.getSeedsToKeep()) && x.getStackSize() != 0);
            if (goodInventItems.length == Inventory.getCount()){
                //Got only the items we need in invent
                if (goodBankItems.length == 0){
                    if (goodInventItems.length > 0){
                        //No good items in bank and got some stuff in invent
                        Item bankedFood = Bank.getFirst(Main.getFoodName());
                        Item bankedNecklaces = Bank.getFirst(Main.getNecklaceName());
                        if (Bank.close()){
                            Time.sleepUntil(Bank::isClosed, 2000);
                            if (Bank.isClosed()) {
                                if (bankedFood != null && bankedNecklaces != null) {
                                    Keyboard.sendText("/Food Left: " + bankedFood.getStackSize() + ", Necklaces Left: " + bankedNecklaces.getStackSize());
                                    Keyboard.pressEnter();
                                }
                                gotAllItems = true;
                            }
                        }
                    }else{
                        Log.severe("Muling initiated but no items to mule found.");
                        Log.info("Returning to " + Main.getPreviousScriptState().name());
                        finishMuling();
                    }
                }else{
                    for (Item i : goodBankItems){
                        Bank.withdrawAll(i.getName());
                        Time.sleep(156,566);
                    }
                }
            }else{
                if (Bank.depositInventory()){
                    Time.sleepUntil(()->Inventory.getCount() == 0, 2000);
                }
            }
        }else{
            Bank.open();
            Time.sleep(123,407);
            Time.sleepUntil(Bank::isOpen, 2553);
        }
    }

    private static void handleTrade(){
        if (Trade.isOpen()){
            if (Trade.isOpen(true)){
                Trade.accept();
                Time.sleepUntil(()->!Trade.isOpen(), 12056);
                if (Trade.isOpen())
                    muleFailure("Mule did not accept second trade screen");
            }else {
                Item[] goodInventItems = Inventory.getItems(x->arrayContainsName(x, Main.getSeedsToKeep()));
                if (goodInventItems.length > 0) {
                    for (Item i : goodInventItems){
                        if (Trade.offerAll(i.getName()))
                            Time.sleep(133, 608);
                    }
                    Time.sleepUntil(Inventory::isEmpty, 2000);
                }else{
                    Trade.accept();
                    Time.sleepUntil(()->Trade.isOpen(true), 11694);
                    if (Trade.isOpen(false))
                        muleFailure(" Mule did not accept first trade screen");
                }
            }
        }else{
            //Trade not open
            Player mule = Players.getNearest(muleName);
            if (mule != null){
                if (mule.interact("Trade with")) {
                    Time.sleepUntil(Trade::isOpen, 15000);
                    if (!Trade.isOpen())
                        muleFailure("Mule did not accept trade offer.");
                }
            }else{
                muleFailure("Mule could not be found");
                Time.sleep(5222,10531);
            }
        }
    }

    private static void muleFailure(String error){
        failiureCount++;
        Log.info("(Muling Fail #" + failiureCount + ") " + error);
    }

    //Given an Item, checks if it's name is in the given array
    private static boolean arrayContainsName(Item i, String[] array){
        for (String s : array){
            if (i.getName().contains(s))
                return true;
        }
        return false;
    }
}
