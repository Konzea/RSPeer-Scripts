package Chin_Hunter.Executes.Hunting;

import Chin_Hunter.Helpers.RequiredItem;
import Chin_Hunter.Helpers.Trapping;
import Chin_Hunter.Main;
import Chin_Hunter.States.ScriptState;
import org.rspeer.runetek.adapter.scene.Pickable;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Pickables;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.ui.Log;

public class Longtails {

    private static final RequiredItem[] MINIMUM_REQUIRED_ITEMS = {
            new RequiredItem("Bird snare", Trapping.getMaxTrapCount())
    };

    private static final RequiredItem[] REQUIRED_ITEMS = {
            new RequiredItem("Bird snare", 8),
            new RequiredItem("Piscatoris teleport", 1),
            new RequiredItem("Varrock teleport", 1)
    };

    private static final Position[] POSSIBLE_CENTRE_TILES = {
            new Position(2310,3587,0),
            new Position(2313,3584,0),
            new Position(2315,3587,0),
            new Position(2309,3593,0),
            new Position(2307,3596,0)};

    private static Position CENTRE_TILE = POSSIBLE_CENTRE_TILES[Random.nextInt(0, POSSIBLE_CENTRE_TILES.length)];

    private static final String[] JUNK_ITEMS = {"Raw bird meat", "Orange feather", "Bones"};

    private Longtails() {
        //Private default constructor
    }

    public static void onStart() {

    }

    public static void execute() {
        if (!Main.isAtPiscatoris()) {
            if (!haveRequiredItems()) {
                Main.updateScriptState(ScriptState.BANKING);
                return;
            }
            Trapping.teleportToPiscatoris();
            return;
        }
        if (HuntLongtails()) {
            if (Players.getLocal().getPosition().distance(CENTRE_TILE) <= 2)
                Main.walkTo(getNearbyTile(CENTRE_TILE, 3));
        }

    }

    static boolean HuntLongtails(){
        return HuntLongtails(Trapping.getMaxTrapCount());
    }

    static boolean HuntLongtails(int maxTrapCount){
        if (!haveMinimumRequiredItems()) {
            Log.severe("Minimum required items not found");
            Main.updateScriptState(ScriptState.BANKING);
            return false;
        }

        if (Trapping.getPlacedTrapsCount() < maxTrapCount) {
            Trapping.layTrap(Trapping.TrapType.BIRD_SNARE, CENTRE_TILE);
            return false;
        }

        if (Main.inventContains(JUNK_ITEMS)) {
            Main.handleJunkItems(JUNK_ITEMS);
            return false;
        }

        if (Players.getLocal().getPosition().distance(CENTRE_TILE) > 24){
            Main.walkTo(CENTRE_TILE);
            return false;
        }

        Position trapToCheck = Trapping.getTrapToFix(Trapping.TrapType.BIRD_SNARE);
        if (trapToCheck != null) {
            Trapping.checkTrap(Trapping.TrapType.BIRD_SNARE, trapToCheck);
            return false;
        }

        Pickable[] droppedTraps = Pickables.getLoaded(x->x.getName().equalsIgnoreCase(Trapping.TrapType.BIRD_SNARE.getName()));
        if (droppedTraps.length > 0 && canLootTrap()){
            Log.fine("Found a random trap on the floor, might be ours? Yoinked it regardless.");
            int inventCount = Inventory.getCount();
            if (droppedTraps[0].interact("Take"))
                Time.sleepUntil(()->Inventory.getCount() != inventCount, 4000);
            return false;
        }

        return true;
    }

    private static boolean canLootTrap(){
        int totalTrapsOwned = Inventory.getCount(Trapping.TrapType.BIRD_SNARE.getName()) + Trapping.getPlacedTrapsCount();
        RequiredItem trapItem = RequiredItem.getByName(Trapping.TrapType.BIRD_SNARE.getName(), REQUIRED_ITEMS);
        if (trapItem == null)
            return false;
        int requiredTraps = trapItem.getAmountRequired();
        return totalTrapsOwned < requiredTraps;
    }

    public static void setCentreTile(Position tile){
        CENTRE_TILE = tile;
    }

    /**
     * Gets a nearby tile to stand on while waiting for a trap to activate
     * @param distance Distance away from the centre tile to stand
     */
    private static Position getNearbyTile(Position centreTile, int distance){
        int attempts = 0;
        int[] modifierOptions = {-1, 1};
        //The way this works is it splits the distance randomly between x and y
        //Then multiplied by 1 or -1 to get a random tile a set distance away
        while (attempts < 10) {
            int distanceRemaining = distance;

            int transform = Random.nextInt(0,distanceRemaining + 1);
            int modifier = modifierOptions[Random.nextInt(0, modifierOptions.length)];
            int x = centreTile.getX() + (modifier*transform);

            transform = distanceRemaining - transform;
            modifier = modifierOptions[Random.nextInt(0, modifierOptions.length)];
            int y = centreTile.getY() + (modifier*transform);

            int z = centreTile.getFloorLevel();

            Position tempTile = new Position(x,y,z);
            if (tempTile.isPositionWalkable())
                return tempTile;
            attempts++;
        }
        Log.severe("No good tiles to walk to while waiting found.");
        return centreTile;
    }


    public static RequiredItem[] getMinimumRequiredItems() {
        return MINIMUM_REQUIRED_ITEMS;
    }

    public static RequiredItem[] getRequiredItems() {
        return REQUIRED_ITEMS;
    }

    public static boolean haveMinimumRequiredItems() {
        return Main.hasItems(MINIMUM_REQUIRED_ITEMS, Trapping.TrapType.BIRD_SNARE);
    }

    public static boolean haveRequiredItems() {
        return Main.hasItems(REQUIRED_ITEMS);
    }
}
