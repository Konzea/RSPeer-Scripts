package Chin_Hunter.Executes;

import Chin_Hunter.Main;
import Chin_Hunter.States.ScriptState;
import org.rspeer.runetek.adapter.component.InterfaceComponent;
import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.component.GrandExchange;
import org.rspeer.runetek.api.component.GrandExchangeSetup;
import org.rspeer.runetek.api.component.Interfaces;
import org.rspeer.runetek.api.component.tab.Equipment;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.component.tab.Skill;
import org.rspeer.runetek.api.component.tab.Skills;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.providers.RSGrandExchangeOffer;
import org.rspeer.ui.Log;


import java.util.*;
import java.util.function.Predicate;

public class PurchaseItems {

    private static final Map<String, Integer> REQUIRED_ITEMS = new HashMap<>();

    private static Map<String, Integer> allItemsToBuy;
    private static Map<String, Integer> itemsLeftToBuy;

    private static boolean justOpenedGE = true;

    private static final Area GE_AREA = Area.rectangular(3150, 3502, 3179, 3472);

    private PurchaseItems(){
        //Private default constructor
    }

    public static void execute(){

        //Update items to buy
        if (allItemsToBuy == null){
            if (Bank.isOpen()) {
                allItemsToBuy = getItemsToBuy(REQUIRED_ITEMS);
                itemsLeftToBuy = getItemsToBuy(REQUIRED_ITEMS);
            }
            else{
                if (GrandExchange.isOpen()){
                    if (closeGE())
                        Time.sleepUntil(()->!GrandExchange.isOpen(), 2000);
                }
                if (Bank.open())
                    Time.sleepUntil(Bank::isOpen, 2000);
                return;
            }
        }

        if (allItemsToBuy.size() == 0) {
            Log.fine("Got all items");
            if (GrandExchange.isOpen()){
                if (closeGE())
                    Time.sleepUntil(()->!GrandExchange.isOpen(), 2000);
                return;
            }
            Main.updateScriptState(ScriptState.BANKING);
            return;
        }

        if (Bank.isOpen())
            handleBanking();

        if (GrandExchange.isOpen()) {
            buyItems(itemsLeftToBuy);
        }else{
            if (!GE_AREA.contains(Players.getLocal())) {
                Movement.walkTo(GE_AREA.getCenter());
                Time.sleep(200,400);
                return;
            }
            if (openGE()) {
                justOpenedGE = true;
                Time.sleepUntil(GrandExchange::isOpen, 2000);
            }
        }
    }

    private static boolean openGE(){
        Npc ge = Npcs.getNearest("Grand Exchange Clerk");
        if (ge == null)
            return false;
        return ge.interact("Exchange");

    }

    private static void handleBanking(){
        //Deposit all and withdraw extra gp to buy stuff
        int bankedGP = Bank.getCount("Coins");
        int gpNeeded = REQUIRED_ITEMS.get("Coins");

        if (bankedGP > gpNeeded){
            if (Bank.withdraw("Coins", bankedGP-gpNeeded))
                Time.sleepUntil(()->Bank.getCount("Coins") == gpNeeded, 2000);
        }else if (bankedGP < gpNeeded){
            if (getCount(Inventory.getItems(x->x.getName().equals("Coins"))) < (gpNeeded - bankedGP)){
                Log.severe("We do not have enough gp");
                Main.updateScriptState(null);
            }else if (Bank.deposit("Coins", gpNeeded - bankedGP))
                Time.sleepUntil(()->Bank.getCount("Coins") == gpNeeded, 2000);
            return;
        }
        if (Inventory.containsAnyExcept("Coins")) {
            if (Bank.depositAllExcept("Coins"))
                Time.sleepUntil(() -> Inventory.getCount() == 0 || Inventory.getCount() == 1, 2000);
            return;
        }
        if (Bank.close())
            Time.sleepUntil(Bank::isClosed, 2000);
    }

    private static int getCount(Item[] items){
        int count = 0;
        for (Item item: items){
            if (item.isStackable())
                count = count + item.getStackSize();
            else
                count = count + 1;
        }
        return count;
    }

    private static void buyItems(Map<String, Integer> items){
        RSGrandExchangeOffer[] completeOffers = GrandExchange.getOffers(x->x.getProgress() == RSGrandExchangeOffer.Progress.FINISHED);
        if ((GrandExchange.getOffers(RSGrandExchangeOffer::isEmpty).length == 0 || items.size() == 0 || justOpenedGE) && (!GrandExchangeSetup.isOpen() && completeOffers.length > 0)){
            int inventCount = Inventory.getCount();
            //This cheeky sleep is here so offers that are complete but not fully processed are collected.
            Time.sleep(800, 1500);
            if (GrandExchange.collectAll()) {
                Time.sleepUntil(()->Inventory.getCount() != inventCount, 2000);
                //This will update items to buy and assume we never lose items
                allItemsToBuy = getItemsToBuy(allItemsToBuy);
                itemsLeftToBuy = getItemsToBuy(allItemsToBuy);
            }
            return;
        }
        justOpenedGE = false;

        if (items.size() == 0)
            return;
        Map.Entry<String, Integer> itemToBuy = items.entrySet().iterator().next();

        //TODO Timeout, wait for offers to finsih
        if (GrandExchange.getOffers(RSGrandExchangeOffer::isEmpty).length == 0)
            return;

        if (!GrandExchangeSetup.isOpen() && GrandExchange.createOffer(RSGrandExchangeOffer.Type.BUY))
            Time.sleepUntil(GrandExchangeSetup::isOpen, 2000);

        if (buySingleItem(itemToBuy.getKey(), itemToBuy.getValue()))
            itemsLeftToBuy.remove(itemToBuy.getKey());

    }

    private static boolean buySingleItem(String name, int amount){
        if (GrandExchangeSetup.getItem() == null){
            if (GrandExchangeSetup.setItem(name))
                Time.sleepUntil(()->GrandExchangeSetup.getItem() != null, 2000);
            return false;
        }else GrandExchangeSetup.getItem();

        if (GrandExchangeSetup.getQuantity() != amount){
            if (GrandExchangeSetup.setQuantity(amount))
                Time.sleepUntil(()->GrandExchangeSetup.getQuantity() == amount, 2000);
            return false;
        }
        if (GrandExchangeSetup.getPricePerItem() < 200)
            GrandExchangeSetup.setPrice(250);
        else
            GrandExchangeSetup.increasePrice(5);
        Time.sleep(500, 1000);

        if (GrandExchangeSetup.confirm())
            Time.sleepUntil(()->!GrandExchangeSetup.isOpen(), 3000);
        return !GrandExchangeSetup.isOpen();
    }

    static void populateHashMap(){
        if (REQUIRED_ITEMS.isEmpty()) {
            int hunterLevel = Skills.getLevel(Skill.HUNTER);
            if (hunterLevel < 43){
                REQUIRED_ITEMS.put("Butterfly net", 1);
                REQUIRED_ITEMS.put("Butterfly jar", 4);
                REQUIRED_ITEMS.put("Bird snare", 8);
                REQUIRED_ITEMS.put("Bronze axe", 1);
                REQUIRED_ITEMS.put("Knife", 1);
            }
            if (hunterLevel < 63){
                REQUIRED_ITEMS.put("Coins", 7500);
            }else if (!EaglesPeakQuest.questComplete())
                REQUIRED_ITEMS.put("Coins", 50);

            if (!EaglesPeakQuest.questComplete()){
                REQUIRED_ITEMS.put("Yellow dye", 1);
                REQUIRED_ITEMS.put("Swamp tar", 5);
                REQUIRED_ITEMS.put("Necklace of passage(5)", 2);
                REQUIRED_ITEMS.put("Camelot teleport", 5);
            }
            REQUIRED_ITEMS.put("Box trap", 24);
            REQUIRED_ITEMS.put("Feldip hills teleport", 2);
            REQUIRED_ITEMS.put("Piscatoris teleport", 2);
            REQUIRED_ITEMS.put("Varrock teleport", 5);
        }
    }

    public static Map<String, Integer> getAllItemsToBuy(){
        return getItemsToBuy(REQUIRED_ITEMS);
    }

    private static Map<String, Integer> getItemsToBuy(Map<String, Integer> items){
        Map<String, Integer> itemsToBuy = new HashMap<>();

        for (Map.Entry<String, Integer> reqItem : items.entrySet()) {

            Predicate<Item> itemPredicate = x->x.getName().toLowerCase().equals(reqItem.getKey().toLowerCase());
            int requiredAmount = reqItem.getValue();

            //Check invent, bank and equipped
            Item[] invent = Inventory.getItems(itemPredicate);
            Item[] equipped = Equipment.getItems(itemPredicate);
            Item[] bank = Bank.getItems(itemPredicate);

            int amountToBuy = requiredAmount;

            if (invent.length > 0)
                amountToBuy = requiredAmount - getCount(invent);
            if (equipped.length > 0)
                amountToBuy = requiredAmount - 1;
            if (bank.length > 0)
                amountToBuy = requiredAmount - bank[0].getStackSize();


            if (amountToBuy > 0)
                itemsToBuy.put(reqItem.getKey().toString(), amountToBuy);
        }
        return itemsToBuy;
    }

    private static boolean closeGE(){
        InterfaceComponent closeBtn = Interfaces.getComponent(465,2).getComponent(11);
        if (closeBtn == null) return false;
        return closeBtn.interact("Close");
    }

}