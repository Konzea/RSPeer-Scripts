package Master_Thiever.Executes;

import Master_Thiever.Enums.ScriptState;
import Master_Thiever.Main;
import jdk.nashorn.internal.runtime.Timing;
import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.adapter.scene.Player;
import org.rspeer.runetek.api.Worlds;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.component.Trade;
import org.rspeer.runetek.api.component.WorldHopper;
import org.rspeer.runetek.api.component.tab.Inventory;
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
                    if (!Trade.isOpen() && Inventory.getCount() == 0) {
                        Log.fine("Muling Successful");
                        finishMuling();
                    } else
                        handleTrade();
                } else {
                    Log.info("Handling bank");
                    handleBank();
                }
            } else {
                //Hops to mule world, closes bank if it has to
                if (!Bank.isOpen()) {
                    WorldHopper.hopTo(muleWorld);
                    Time.sleepUntil(() -> Worlds.getCurrent() == muleWorld, 4000);
                } else {
                    if (Bank.close())
                        Time.sleepUntil(Bank::isClosed, 2000);
                }
            }
        }
    }

    private static void finishMuling(){
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
            Main.updateScriptState(Main.getPreviousScriptState());
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
                        if (Bank.close()){
                            Time.sleepUntil(Bank::isClosed, 2000);
                            if (Bank.isClosed())
                                gotAllItems = true;
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
            if (Bank.open())
                Time.sleepUntil(Bank::isOpen, 3000);
        }
    }

    private static void handleTrade(){
        if (Trade.isOpen()){
            if (Trade.isOpen(true)){
                Trade.accept();
                Time.sleepUntil(()->!Trade.isOpen(), 12056);
                if (Trade.isOpen())
                    muleFailiure("Mule did not accept second trade screen");
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
                        muleFailiure(" Mule did not accept first trade screen");
                }
            }
        }else{
            //Trade not open
            Player mule = Players.getNearest(muleName);
            if (mule != null){
                if (mule.interact("Trade with")) {
                    Time.sleepUntil(Trade::isOpen, 15000);
                    if (!Trade.isOpen())
                        muleFailiure("Mule did not accept trade offer.");
                }
            }else{
                muleFailiure("Mule could not be found");
                Time.sleep(5222,10531);
            }
        }
    }

    private static void muleFailiure(String error){
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
