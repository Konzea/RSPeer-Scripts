package Chin_Hunter.Executes;

import Chin_Hunter.Executes.Hunting.Chinchompas;
import Chin_Hunter.Executes.Hunting.DeadfallKebbits;
import Chin_Hunter.Executes.Hunting.FalconKebbits;
import Chin_Hunter.Main;
import Chin_Hunter.States.ScriptState;
import jdk.nashorn.internal.runtime.Timing;
import org.rspeer.runetek.adapter.component.InterfaceComponent;
import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.api.Definitions;
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

    private PurchaseItems() {
        //Private default constructor
    }

    public static void onStart(){

    }

    public static void execute() {

        //Update items to buy
        if (allItemsToBuy == null) {
            if (Bank.isOpen()) {
                allItemsToBuy = getItemsToBuy(REQUIRED_ITEMS);
                itemsLeftToBuy = getItemsToBuy(REQUIRED_ITEMS);
                Log.fine("Need to buy the following items: ");
                allItemsToBuy.forEach((k,v)->Log.info(k + ": " + v));
            } else {
                if (GrandExchange.isOpen()) {
                    if (closeGE())
                        Time.sleepUntil(() -> !GrandExchange.isOpen(), 2000);
                }
                //No need to handle walking here as purchaseItems can only be called from Banking
                if (Bank.open())
                    Time.sleepUntil(Bank::isOpen, 2000);
                return;
            }
        }

        if (allItemsToBuy.size() == 0) {
            if (GrandExchange.isOpen()) {
                if (closeGE())
                    Time.sleepUntil(() -> !GrandExchange.isOpen(), 2000);
                return;
            }
            Log.fine("All items purchased");
            Main.updateScriptState(ScriptState.BANKING);
            return;
        }

        if (Bank.isOpen()) {
            handleBanking();
            return;
        }

        if (GrandExchange.isOpen()) {
            buyItems(itemsLeftToBuy);
            return;
        }

        if (!isAtGrandExchange()) {
            goToGrandExchange();
            return;
        }

        if (openGE()) {
            justOpenedGE = true;
            Time.sleepUntil(GrandExchange::isOpen, 2000);
        }

    }

    private static void goToGrandExchange(){
        //Tele to varrock if we have one, otherwise walk.
        Item varrockTele = Inventory.getFirst("Varrock teleport");
        if (!Main.isInVarrock() && varrockTele != null){
            if (varrockTele.interact("Break"))
                Time.sleepUntil(Main::isInVarrock, 10000);
            return;
        }
        Movement.walkTo(GE_AREA.getCenter());
        Time.sleep(300, 666);
    }

    private static boolean openGE() {
        Npc ge = Npcs.getNearest("Grand Exchange Clerk");
        if (ge == null)
            return false;
        return ge.interact("Exchange");
    }

    private static boolean isAtGrandExchange(){
        return GE_AREA.contains(Players.getLocal());
    }

    private static void handleBanking() {
        //Deposit all and withdraw extra gp to buy stuff
        int bankedGP = Bank.getCount("Coins");
        int gpNeeded = REQUIRED_ITEMS.get("Coins");

        if (bankedGP > gpNeeded) {
            if (Bank.withdraw("Coins", bankedGP - gpNeeded))
                Time.sleepUntil(() -> Bank.getCount("Coins") == gpNeeded, 2000);
        } else if (bankedGP < gpNeeded) {
            if (Main.getCount(Inventory.getItems(x -> x.getName().equals("Coins"))) < (gpNeeded - bankedGP)) {
                Log.severe("We do not have enough gp");
                Main.updateScriptState(null);
            } else if (Bank.deposit("Coins", gpNeeded - bankedGP))
                Time.sleepUntil(() -> Bank.getCount("Coins") == gpNeeded, 2000);
            return;
        }
        if (Inventory.containsAnyExcept("Coins")) {
            if (Bank.depositAllExcept("Coins"))
                Time.sleepUntil(() -> Inventory.getCount() == 0 || Inventory.getCount() == 1, 2000);
            return;
        }
        if (!Main.isInVarrock() && Bank.contains("Varrock teleport") && !Inventory.contains("Varrock teleport")){
            if (Bank.withdraw("Varrock teleport", 1))
                Time.sleepUntil(()->Inventory.contains("Varrock teleport"), 2000);
            return;
        }
        if (Bank.close())
            Time.sleepUntil(Bank::isClosed, 2000);
    }

    private static void buyItems(Map<String, Integer> items) {
        RSGrandExchangeOffer[] completeOffers = GrandExchange.getOffers(x -> x.getProgress() == RSGrandExchangeOffer.Progress.FINISHED);
        if ((GrandExchange.getOffers(RSGrandExchangeOffer::isEmpty).length == 0 || items.size() == 0 || justOpenedGE) && (!GrandExchangeSetup.isOpen() && completeOffers.length > 0)) {
            int inventCount = Inventory.getCount();
            //This cheeky sleep is here so offers that are complete but not fully processed are collected.
            Time.sleep(800, 1500);
            if (GrandExchange.collectAll()) {
                Time.sleepUntil(() -> Inventory.getCount() != inventCount, 2000);
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

    private static boolean buySingleItem(String name, int amount) {
        if (name.equals("Coins"))
            return true;
        if (GrandExchangeSetup.getItem() == null) {
            if (GrandExchangeSetup.setItem(name))
                Time.sleepUntil(() -> GrandExchangeSetup.getItem() != null, 2000);
            return false;
        } else GrandExchangeSetup.getItem();

        if (GrandExchangeSetup.getQuantity() != amount) {
            if (GrandExchangeSetup.setQuantity(amount))
                Time.sleepUntil(() -> GrandExchangeSetup.getQuantity() == amount, 2000);
            return false;
        }
        if (name.equals("Yellow dye"))
            GrandExchangeSetup.setPrice(2000);
        else {
            if (GrandExchangeSetup.getPricePerItem() < 200)
                GrandExchangeSetup.setPrice(250);
            else
                GrandExchangeSetup.increasePrice(5);
        }
        Time.sleep(500, 1000);

        if (GrandExchangeSetup.confirm())
            Time.sleepUntil(() -> !GrandExchangeSetup.isOpen(), 3000);
        return !GrandExchangeSetup.isOpen();
    }

    static void populateHashMap() {
        REQUIRED_ITEMS.clear();

        int hunterLevel = Skills.getLevel(Skill.HUNTER);
        if (!EaglesPeakQuest.questComplete())
            EaglesPeakQuest.getRequiredItems().forEach(REQUIRED_ITEMS::put);
        if (hunterLevel < 43)
            DeadfallKebbits.getRequiredItems().forEach(REQUIRED_ITEMS::put);
        if (hunterLevel < 63)
            FalconKebbits.getRequiredItems().forEach(REQUIRED_ITEMS::put);

        Chinchompas.getRequiredItems().forEach(REQUIRED_ITEMS::put);

        REQUIRED_ITEMS.put("Varrock teleport", 5);

    }

    public static Map<String, Integer> getAllItemsToBuy() {
        return getItemsToBuy(REQUIRED_ITEMS);
    }

    private static Map<String, Integer> getItemsToBuy(Map<String, Integer> items) {
        Map<String, Integer> itemsToBuy = new HashMap<>();

        for (Map.Entry<String, Integer> reqItem : items.entrySet()) {

            Predicate<Item> itemPredicate = x -> x.getName().toLowerCase().equals(reqItem.getKey().toLowerCase());
            int requiredAmount = reqItem.getValue();

            //Check invent, bank and equipped
            Item[] invent = Inventory.getItems(itemPredicate);
            Item[] equipped = Equipment.getItems(itemPredicate);
            Item[] bank = Bank.getItems(itemPredicate);

            int amountToBuy = requiredAmount;

            if (invent.length > 0)
                amountToBuy = requiredAmount - Main.getCount(invent);
            if (equipped.length > 0)
                amountToBuy = requiredAmount - 1;
            if (bank.length > 0)
                amountToBuy = requiredAmount - bank[0].getStackSize();


            if (amountToBuy > 0)
                itemsToBuy.put(reqItem.getKey(), amountToBuy);
        }
        return itemsToBuy;
    }

    private static boolean closeGE() {
        InterfaceComponent closeBtn = Interfaces.getComponent(465, 2).getComponent(11);
        if (closeBtn == null) return false;
        return closeBtn.interact("Close");
    }

}