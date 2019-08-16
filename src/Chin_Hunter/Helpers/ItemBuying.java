package Chin_Hunter.Helpers;

import Chin_Hunter.Main;
import org.rspeer.runetek.adapter.component.InterfaceComponent;
import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.api.commons.BankLocation;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.component.GrandExchange;
import org.rspeer.runetek.api.component.GrandExchangeSetup;
import org.rspeer.runetek.api.component.Interfaces;
import org.rspeer.runetek.api.component.tab.Equipment;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.component.tab.Magic;
import org.rspeer.runetek.api.component.tab.Spell;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.providers.RSGrandExchangeOffer;
import org.rspeer.ui.Log;

import java.util.ArrayList;
import java.util.function.Predicate;

public class ItemBuying {

    private static final Area GE_AREA = Area.rectangular(3150, 3502, 3179, 3472);


    private boolean hasWithdrawnAllGP = false;
    private boolean hasCheckedBankForTele = false;

    private boolean finishedBuying = false;

    /**
     * GP that we need in order to buy everything.
     * If greater than 0 we will look for more GP
     */
    private int coinsNeeded = 0;

    /**
     * GP that we need to keep for later use.
     * If we go below this and can't find any more the script will stop.
     */
    private int coinsToKeep = -1;


    public void BuyItems(RequiredItem[] requiredItems){
        if (coinsToKeep == -1) {
            RequiredItem requiredCoins = RequiredItem.getByName("Coins", requiredItems);
            coinsToKeep = requiredCoins == null ? 1 : requiredCoins.getAmountRequired();
        }

        if (!isAtGrandExchange()){
            travelToGrandExchange();
            return;
        }

        if (Inventory.getCount(true,"Coins") < coinsToKeep){
            searchForCoins();
            return;
        }

        if (Bank.isOpen()){
            if (!hasWithdrawnAllGP){
                withdrawCoinsFromBank();
                return;
            }
            if (Bank.close())
                Time.sleepUntil(()->!Bank.isOpen(), 2500);
            return;
        }

        if (!GrandExchange.isOpen()){
            if (GrandExchange.open())
                Time.sleepUntil(GrandExchange::isOpen, 4000);
            return;
        }

        RequiredItem itemToBuy = getItemToBuy(requiredItems);
        //No more items to buy if null
        if (itemToBuy == null) {
            if (areOffersToBeCollected()){
                collectAllOffers();
                return;
            }
            checkAndCancelTimedOutOffers(requiredItems);
            if (!isWaitingForItems(requiredItems)){
                finishedBuying = true;
                return;
            }
            Log.info("Waiting for items to buy");
            Time.sleepUntil(()->GrandExchange.getOffers(x->x.getProgress()== RSGrandExchangeOffer.Progress.FINISHED).length > 0, Random.nextInt(5000, 10000));
            return;
        }

        if (!GrandExchangeSetup.isOpen()){
            checkAndCancelTimedOutOffers(requiredItems);
            //If there is a problem return and try again. Otherwise continue.
            if (!canCreateNewOffer())
                return;
        }
        buySingleItem(itemToBuy);
    }

    public boolean isFinishedBuying(){
        return finishedBuying;
    }

    public static RequiredItem[] getAllItemsToBuy(RequiredItem[] allRequiredItems) {
        ArrayList<RequiredItem> itemsToBuy = new ArrayList<>();

        for (RequiredItem requiredItem : allRequiredItems) {

            Predicate<Item> itemPredicate = x -> x.getName().equalsIgnoreCase(requiredItem.getName());
            int requiredAmount = requiredItem.getAmountRequired();

            //Check invent, bank and equipped
            int amountToBuy = requiredAmount;

            Item[] invent = Inventory.getItems(itemPredicate);
            if (invent.length > 0)
                amountToBuy = requiredAmount - Main.getCount(invent);

            Item[] equipped = Equipment.getItems(itemPredicate);
            if (equipped.length > 0)
                amountToBuy = requiredAmount - 1;

            if (Bank.isOpen()) {
                Item[] bank = Bank.getItems(itemPredicate);
                if (bank.length > 0)
                    amountToBuy = requiredAmount - bank[0].getStackSize();
            }

            requiredItem.setAmountToBuy(amountToBuy);
            if (amountToBuy > 0)
                itemsToBuy.add(requiredItem);
        }
        return itemsToBuy.toArray(new RequiredItem[itemsToBuy.size()]);
    }

    private boolean isWaitingForItems(RequiredItem[] requiredItems){
        RSGrandExchangeOffer[] activeOffers = GrandExchange.getOffers();
        for (RSGrandExchangeOffer offer: activeOffers){
            for (RequiredItem item : requiredItems) {
                if (offer.getItemName().equalsIgnoreCase(item.getName()))
                    return true;
            }
        }
        return false;
    }

    private RequiredItem getItemToBuy(RequiredItem[] allItems){
        for (RequiredItem item : allItems){
            if (needToBuyItem(item)) {
                return item;
            }
        }
        return null;
    }

    /**
     * Check invent and GE offers to see if we already have the item
     * @param item The item we want to check if we still need to buy
     * @return Returns true if we still need to buy this item
     */
    private boolean needToBuyItem(RequiredItem item){
        if (item.getName().equalsIgnoreCase("Coins"))
            return false;
        if (Inventory.getCount(true, item.getName()) >= item.getAmountToBuy())
            return false;
        RSGrandExchangeOffer[] offers = GrandExchange.getOffers(x-> x.getItemName().equalsIgnoreCase(item.getName()));
        if (offers.length == 0) {
            return true;
        }
        if (item.getSecondsSinceOfferPlaced() == -1)
            item.setOfferPlacedTime();
        return false;
    }

    //region Creating New Offers

    private void buySingleItem(RequiredItem item){
        if (!GrandExchangeSetup.isOpen()) {
            if (GrandExchange.createOffer(RSGrandExchangeOffer.Type.BUY))
                Time.sleepUntil(GrandExchangeSetup::isOpen, 2000);
            return;
        }
        //Select the item we want to buy
        Item currentSelectedItem = GrandExchangeSetup.getItem();
        if (currentSelectedItem == null || !currentSelectedItem.getName().equalsIgnoreCase(item.getName())){
            if (GrandExchangeSetup.setItem(item.getName()))
                Time.sleepUntil(() -> GrandExchangeSetup.getItem() != null, 2000);
            return;
        }
        //Set Quantity
        if (GrandExchangeSetup.getQuantity() != item.getAmountToBuy()) {
            if (GrandExchangeSetup.setQuantity(item.getAmountToBuy()))
                Time.sleepUntil(() -> GrandExchangeSetup.getQuantity() == item.getAmountToBuy(), 2500);
            return;
        }
        //Set price
        if (item.getPrice() == -1) {
            GrandExchangeSetup.increasePrice(RequiredItem.getDefaultPriceMultiplier());
            Time.sleep(500, 1000);
        }else{
            if (GrandExchangeSetup.getPricePerItem() != item.getPrice()) {
                if (GrandExchangeSetup.setPrice(item.getPrice()))
                    Time.sleepUntil(() -> GrandExchangeSetup.getPricePerItem() == item.getPrice(), 3000);
                return;
            }
        }

        //Check if we can afford the item
        int totalCost = GrandExchangeSetup.getPricePerItem() * GrandExchangeSetup.getQuantity();
        int coinsInInvent = Inventory.getCount(true, "Coins");
        if (totalCost >  coinsInInvent){
            Log.info("Don't have enough GP to buy " + item.getAmountToBuy() + " " + item.getName());
            coinsNeeded = totalCost - coinsInInvent;
            searchForCoins();
            return;
        }

        //Confirm the offer
        if (GrandExchangeSetup.confirm()) {
            Time.sleepUntil(() -> !GrandExchangeSetup.isOpen(), 3000);
            if (!GrandExchangeSetup.isOpen())
                item.setOfferPlacedTime();
        }
    }

    private boolean canCreateNewOffer(){
        //Check offers, collect if full that sort of thing
        RSGrandExchangeOffer[] allActiveOffers = GrandExchange.getOffers(x->!x.isEmpty());
        return allActiveOffers.length != 8;
    }

    //endregion

    //region Handling and Processing Active Orders

    private void checkAndCancelTimedOutOffers(RequiredItem[] itemArray){
        RSGrandExchangeOffer[] inProgressOffers = GrandExchange.getOffers(x->x.getProgress()== RSGrandExchangeOffer.Progress.IN_PROGRESS);
        for (RSGrandExchangeOffer offer : inProgressOffers){
            RequiredItem item = RequiredItem.getByName(offer.getItemName(), itemArray);
            if (item == null)
                continue;
            if (item.getSecondsSinceOfferPlaced() == -1) {
                item.setOfferPlacedTime();
                continue;
            }
            if (item.getSecondsSinceOfferPlaced() >= RequiredItem.getDefaultOfferTimeout()) {
                int newPrice = offer.getItemPrice() * 2;
                Log.info(item.getName() + " has been in GE for longer than " + RequiredItem.getDefaultOfferTimeout()
                        + " seconds, increasing price from " + offer.getItemPrice() + " to " + newPrice);
                item.increasePriceTo(newPrice);
                if (GrandExchangeSetup.isOpen())
                    pressGEBackButton();
                if (cancelOffer(offer)) {
                    item.restOfferPlacedTime();
                }
            }
        }
    }

    private boolean cancelOffer(RSGrandExchangeOffer offer){
        Log.info("Canceling offer: " + offer.getItemName());
        if (GrandExchangeSetup.isOpen()){
            pressGEBackButton();
            return false;
        }
        if (offer.abort())
            Time.sleepUntil(() -> GrandExchange.getOffers(x->x.getProgress()== RSGrandExchangeOffer.Progress.FINISHED
                    && x.getItemName().equalsIgnoreCase(offer.getItemName())).length > 0, 3000);

        return GrandExchange.getOffers(x->x.getProgress()== RSGrandExchangeOffer.Progress.FINISHED
                && x.getItemName().equalsIgnoreCase(offer.getItemName())).length > 0;
    }

    private void collectAllOffers(){
        if (!GrandExchange.isOpen()){
            if (Bank.isOpen()) {
                if (Bank.close())
                    Time.sleepUntil(Bank::isClosed, 2000);
                return;
            }
            if (GrandExchange.open()){
                Time.sleepUntil(GrandExchange::isOpen, 2500);
                return;
            }
        }
        if (GrandExchangeSetup.isOpen()){
            pressGEBackButton();
            return;
        }
        //This cheeky sleep is here so offers that are complete but not fully processed are collected.
        Time.sleep(800, 1500);
        int Coins = Inventory.getCount("Coins");
        if (GrandExchange.collectAll())
            Time.sleepUntil(()->Coins != Inventory.getCount("Coins"), Random.nextInt(1500, 3000));
    }

    private boolean areOffersToBeCollected(){
        RSGrandExchangeOffer[] completeOffers = GrandExchange.getOffers(x->x.getProgress() == RSGrandExchangeOffer.Progress.FINISHED);
        return completeOffers.length > 0;
    }

    //endregion

    //region Banking and GP

    /**
     * Checks bank first for coins and then the GE
     */
    private void searchForCoins(){
        if (!hasWithdrawnAllGP){
            withdrawCoinsFromBank();
            return;
        }

        if (!GrandExchange.isOpen()){
            if (Bank.isOpen()) {
                if (Bank.close())
                    Time.sleepUntil(Bank::isClosed, 2000);
                return;
            }
            if (GrandExchange.open()){
                Time.sleepUntil(GrandExchange::isOpen, 2500);
                return;
            }
        }
        if (areOffersToBeCollected()){
            collectAllOffers();
            return;
        }
        if (Inventory.getCount(true, "Coins") >= coinsNeeded){
            Log.info("Found enough coins, lets try to buy the items again.");
            coinsNeeded = 0;
            return;
        }
        Log.severe("Checked bank and GE for GP but looks like we don't have enough.");
        Main.updateScriptState(null);
    }

    private void withdrawCoinsFromBank(){
        if (GrandExchange.isOpen()){
            closeGrandExchange();
            return;
        }
        if (!Bank.isOpen()){
            if (Bank.open())
                Time.sleepUntil(Bank::isOpen, 2500);
            return;
        }
        if (Bank.getCount("Coins") == 0){
            hasWithdrawnAllGP = true;
            return;
        }
        if (Bank.withdrawAll("Coins"))
            Time.sleepUntil(()->Bank.getCount("Coins") == 0, 2000);
    }

    //endregion

    //region Travelling and Areas

    private void travelToGrandExchange() {
        //If we have a varrock tele we will use it.
        //If we're really fucking far away tele to lumby then walk
        //Otherwise just walk it all
        Position geTile = GE_AREA.getCenter();

        if (!hasCheckedBankForTele && !Main.isInVarrock() && BankLocation.getNearest().getPosition().distance(Players.getLocal()) < 15){
            if (Inventory.contains("Varrock teleport")) {
                hasCheckedBankForTele = true;
                return;
            }
            if (!Bank.isOpen()){
                if (Bank.open())
                    Time.sleepUntil(Bank::isOpen, 3500);
                return;
            }
            if (Bank.contains("Varrock teleport")){
                if (Bank.withdraw("Varrock teleport", 1))
                    Time.sleepUntil(()->Inventory.contains("Varrock teleport"), 2500);
                return;
            }
            hasCheckedBankForTele = true;
            return;
        }

        if (Bank.isOpen()){
            if (Bank.close())
                Time.sleepUntil(Bank::isClosed, 2000);
            return;
        }

        if (Main.isInVarrock()){
            Main.walkTo(geTile);
            return;
        }

        if (Inventory.contains("Varrock teleport")) {
            Item varrockTele = Inventory.getFirst("Varrock teleport");
            if (varrockTele == null) {
                Log.severe("We have a varrock tele in invent but cant use it?");
                return;
            }
            if (varrockTele.interact("Break"))
                Time.sleepUntil(Main::isInVarrock, 10000);
            return;
        }

        if (Players.getLocal().distance(geTile) > 400 && Magic.getBook().equals(Magic.Book.MODERN)){
            if (Magic.interact(Spell.Modern.HOME_TELEPORT, "Cast"))
                Time.sleepUntil(()->Players.getLocal().distance(geTile) < 400, 20000);
            return;
        }

        Main.walkTo(geTile);
    }

    private boolean isAtGrandExchange() {
        return GE_AREA.contains(Players.getLocal());
    }

    //endregion

    //region Misc Grand Exchange Interface Processing

    private void closeGrandExchange() {
        InterfaceComponent closeBtn = Interfaces.getComponent(465, 2).getComponent(11);
        if (closeBtn == null) return;
        if (closeBtn.interact("Close"))
            Time.sleepUntil(()->!GrandExchange.isOpen(), 3000);
    }
    private void pressGEBackButton(){
        InterfaceComponent Back = Interfaces.getComponent(465, 4);
        if (Back == null || !Back.isVisible()){
            Log.info("Could not find GE back button");
            return;
        }
        if (Back.interact("Back"))
            Time.sleepUntil(()->!GrandExchangeSetup.isOpen(), 2500);
    }

    //endregion


}
