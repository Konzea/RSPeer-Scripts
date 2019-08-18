package Burthorpe_Buyer;

import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.Game;
import org.rspeer.runetek.api.Worlds;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.component.Shop;
import org.rspeer.runetek.api.component.WorldHopper;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.api.scene.SceneObjects;
import org.rspeer.runetek.providers.RSWorld;
import org.rspeer.script.Script;
import org.rspeer.script.ScriptCategory;
import org.rspeer.script.ScriptMeta;
import org.rspeer.ui.Log;

import java.util.Arrays;
import java.util.function.Predicate;

@ScriptMeta(desc = "Buys items from the Burthorpe Bar", developer = "Shteve", name = "Burthorpe Buyer", category = ScriptCategory.MONEY_MAKING, version = 0.1)
public class Main extends Script {

    //TODO Enter Tiles
    private static final Position SHOP_TILE = new Position(0,0,0);
    private static final Position BANK_TILE = new Position(0,0,0);
    private static final Position LADDER_DOWN_TILE = new Position(0,0,0);
    private static final Position LADDER_UP_TILE = new Position(0,0,0);

    //TODO get basement area
    private static final Area BASEMENT_AREA = null;

    //TODO Enter names
    private static final String LADDER_UP_NAME = "Ladder";
    private static final String LADDER_DOWN_NAME = "Ladder";

    //TODO Enter names
    private static final String SHOP_NAME = "Giles";
    private static final String BANKER_NAME = "Banker";

    //TODO Enter names
    private static final String[] ITEMS_TO_BUY = {"Item1", "Item2"};
    private static final int MOST_EXPENSIVE_ITEM_PRICE = 2;

    private static final int MIN_WALK_WAIT = 700;
    private static final int MAX_WALK_WAIT = 2000;

    private static boolean isOutOfGP = false;
    private static boolean isShopOutOfStock = false;
    private static int previousWorld = -1;

    private static boolean stopScript = false;

    private static final Predicate<RSWorld> validWorlds = x->x.isMembers()
            && !x.isPVP() && !x.isSeasonDeadman() && !x.isTournament() && !x.isSkillTotal()
            && !x.isDeadman() && x.getPopulation() < 1900 && x.getId() != previousWorld;

    @Override
    public int loop() {

        if (stopScript){
            setStopping(true);
            return 0;
        }

        mainLoop();

        return Random.nextInt(100, 350);
    }

    /**
     * Main Loop
     */
    private static void mainLoop(){
        if (Inventory.isFull() || isOutOfGP){
            if (Shop.isOpen() && Shop.close()){
                Time.sleepUntil(()->!Shop.isOpen(), 2500);
                return;
            }
            bankInventory();
        }

        if (isShopOutOfStock){
            hopWorlds();
            return;
        }

        if (!Shop.isOpen()){
            openShop();
            return;
        }
        if (!itemsAreInStock()) {
            previousWorld = Worlds.getCurrent();
            isShopOutOfStock = true;
        }
        buyItems();
    }

    /**
     * Hops to a 'validWorld', called when shop is out of stock
     */
    private static void hopWorlds(){
        if (Worlds.getCurrent() != previousWorld){
            isShopOutOfStock = false;
            return;
        }
        if (!Game.isLoggedIn()){
            //Let the login handler do its stuff
            Time.sleepUntil(Game::isLoggedIn, 10000);
        }
        if (WorldHopper.randomHop(validWorlds))
            Time.sleepUntil(()->Worlds.getCurrent() != previousWorld, Random.nextInt(6000, 10000));
    }

    /**
     * Walks to specific tile, also handles running in combat and turning run on
     * @param tile Destination tile
     * @return Returns true if tile clicks successfully
     */
    public static boolean walkTo(Position tile){
        if (Movement.walkTo(tile)) {
            int mainSleep = Movement.isRunEnabled()? Random.low(MIN_WALK_WAIT, MAX_WALK_WAIT):Random.low(MIN_WALK_WAIT*2, MAX_WALK_WAIT*2);

            //Turn run energy on if got enough spare or in combat.
            if (!Movement.isRunEnabled()) {
                if ((Players.getLocal().isHealthBarVisible() && Movement.getRunEnergy() > 5) || Movement.getRunEnergy() > Random.nextInt(40, 80))
                    Movement.toggleRun(true);
            }

            //Initial sleep is used to allow for player to start moving
            int initialSleep = Players.getLocal().isMoving()?0:MIN_WALK_WAIT;
            Time.sleep(initialSleep);
            mainSleep -= initialSleep;

            if (Movement.getDestination() != null && Movement.getDestination().equals(tile))
                Time.sleepUntil(()->Players.getLocal().getPosition().equals(tile), Random.nextInt(8000,12000));
            else
                Time.sleepUntil(()->(!Players.getLocal().isMoving() && Players.getLocal().getAnimation() == -1)
                        || Players.getLocal().getPosition().equals(tile)
                        || (Players.getLocal().isHealthBarVisible() && !Movement.isRunEnabled() && Movement.getRunEnergy() > 5), mainSleep);
            return true;
        }
        return false;
    }

    /**
     * Climbs a ladder and sleeps until in new area
     * @param name Name of the object to climb
     * @param action Action to interact with the object
     * @param tile Tile of the object to interact with
     */
    private static void climbLadder(String name, String action, Position tile){
        SceneObject Ladder = SceneObjects.getNearest(x->x.getName().equalsIgnoreCase(name) && x.getPosition().equals(tile));
        if (Ladder == null){
            Log.info("Could not find " + name + " to " + action + ". Walking to it.");
            walkTo(tile);
            return;
        }
        Position playerStartPos = Players.getLocal().getPosition();
        if (Ladder.interact(action)){
            //Wait for the player to start moving or climb
            Time.sleepUntil(()->Players.getLocal().isMoving(), 1000);
            Time.sleepUntil(()->Players.getLocal().getPosition().distance(playerStartPos) > 50 || !Players.getLocal().isMoving(), 8000);
            Time.sleepUntil(()->Players.getLocal().getAnimation() == -1, 2500);
        }
    }

    /**
     * Navigates to the pub from basement or elsewhere and interacts with shop guy
     */
    private static void openShop(){
        if (Shop.isOpen())
            return;

        if (isInBasement()){
            //Leave basement
            //TODO Make sure CLimb-up is right
            climbLadder(LADDER_UP_NAME, "Climb-up", LADDER_UP_TILE);
            return;
        }

        if (!isNearShop()){
            Movement.walkTo(SHOP_TILE);
            return;
        }

        Npc ShopGuy = Npcs.getNearest(SHOP_NAME);
        if (ShopGuy == null){
            Log.info("Could not find shop guy. Walking to him/her, who knows. Are we allowed to assume its gender?");
            walkTo(SHOP_TILE);
            return;
        }
        //TODO Make sure 'trade' is correct to open shop
        if (ShopGuy.interact("Trade"))
            Time.sleepUntil(Shop::isOpen, Random.nextInt(5000, 8000));
    }

    /**
     * @return Returns true if within 10 tiles of the SHOP_TILE
     */
    private static boolean isNearShop(){
        return Players.getLocal().distance(SHOP_TILE) < 10;
    }

    /**
     * @return Returns true if player is withing the BASEMENT_AREA
     */
    private static boolean isInBasement(){
        return BASEMENT_AREA.contains(Players.getLocal());
    }

    /**
     * @return Returns true if any of the items are in stock
     */
    private static boolean itemsAreInStock(){
        Item[] itemsToBuy = Shop.getItems(x->Arrays.asList(ITEMS_TO_BUY).contains(x) && x.getStackSize() > 0);
        return itemsToBuy.length > 0;
    }

    /**
     * If shop is open checks if you have enough coins and buys all of the specified items
     */
    private static void buyItems(){
        if (!Shop.isOpen())
            return;
        if (Inventory.getCount(true, "Coins") < MOST_EXPENSIVE_ITEM_PRICE){
            isOutOfGP = true;
            Log.severe("Have less than " + MOST_EXPENSIVE_ITEM_PRICE + ", checking bank for GP.");
            return;
        }
        for (String itemName : ITEMS_TO_BUY){
            int shopQuantity = Shop.getQuantity(itemName);
            if (shopQuantity == 0)
                continue;
            if (Shop.buyFifty(itemName))
                Time.sleepUntil(()->Shop.getQuantity(itemName) != shopQuantity, 2000);
        }
    }

    /**
     * Navigates to and opens the bank, deposits all except coins then checks if user has enough coins.
     * If coins found in bank withdraws all otherwise stops script.
     */
    private static void bankInventory(){
        if (Shop.isOpen() && Shop.close()){
            Time.sleepUntil(()->!Shop.isOpen(), 2500);
            return;
        }
        if (!isInBasement()){
            //TODO make sure Climb-down is correct
            climbLadder(LADDER_DOWN_NAME, "Climb-down", LADDER_DOWN_TILE);
            return;
        }
        if (!Bank.isOpen()){
            Npc Banker = Npcs.getNearest(BANKER_NAME);
            if (Banker == null){
                Log.info("Could not find bank, walking to it.");
                walkTo(BANK_TILE);
                return;
            }
            //TODO Make sure 'Bank' is correct option to open bank
            if (Banker.interact("Bank"))
                Time.sleepUntil(()->Bank.isOpen() || !Players.getLocal().isMoving(), Random.nextInt(8000, 15000));
            return;
        }

        //Deposit all bought stuff
        if (Inventory.containsAnyExcept("Coins")){
            if (Bank.depositAllExcept("Coins"))
                Time.sleepUntil(()->Inventory.getCount() <= 1, 2500);
            return;
        }

        //Look for GP if we need to
        if (isOutOfGP){
            if (Inventory.getCount(true, "Coins") >= MOST_EXPENSIVE_ITEM_PRICE){
                isOutOfGP = false;
                return;
            }
            if (!Bank.contains("Coins")){
                Log.severe("Out of coins.");
                stopScript = true;
            }
            if (Bank.withdrawAll("Coins"))
                Time.sleepUntil(()->!Bank.contains("Coins"), 2500);
            return;
        }
        //Done all bank stuff, close
        if (Bank.close())
            Time.sleepUntil(Bank::isClosed, 2500);
    }

}
