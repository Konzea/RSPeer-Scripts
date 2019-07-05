package Chin_Hunter.Executes.Hunting;

import Chin_Hunter.Helpers.Hunter;
import Chin_Hunter.Main;
import Chin_Hunter.States.ScriptState;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.ui.Log;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Longtails {

    private static final Map<String, Integer> MINIMUM_REQUIRED_ITEMS = new HashMap<>();
    private static final Map<String, Integer> REQUIRED_ITEMS = new HashMap<>();

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
        Hunter.reset();
    }

    public static void execute() {
        if (!Main.isAtPiscatoris()) {
            if (!haveRequiredItems()) {
                Main.updateScriptState(ScriptState.BANKING);
                return;
            }
            Hunter.teleportToPiscatoris();
            return;
        }

        if (HuntLongtails()) {
            if (Players.getLocal().getPosition().distance(CENTRE_TILE) <= 2) {
                if (Movement.walkTo(getNearbyTile(CENTRE_TILE, 3))) {
                    Time.sleep(300, 666);
                    Time.sleepUntil(() -> !Players.getLocal().isMoving(), 3000);
                }
                else return;
            }

            Time.sleep(2000);
        }

    }

    static boolean HuntLongtails(){
        return HuntLongtails(Hunter.getMaxTrapCount());
    }

    static boolean HuntLongtails(int maxTrapCount){
        if (!haveMinimumRequiredItems()) {
            Log.severe("Minimum required items not found");
            Main.updateScriptState(ScriptState.BANKING);
            return false;
        }

        if (Hunter.trapLocations.size() < maxTrapCount) {
            Hunter.layTrap(Hunter.TrapType.BIRD_SNARE, CENTRE_TILE);
            return false;
        }

        if (inventContainsJunk()) {
            handleJunkItems();
            return false;
        }

        if (Players.getLocal().getPosition().distance(CENTRE_TILE) > 24){
            Movement.walkTo(CENTRE_TILE);
            Time.sleep(200, 744);
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
        return (Inventory.getCount(Trapping.TrapType.BIRD_SNARE.getName()) + Trapping.getPlacedTrapsCount()) < REQUIRED_ITEMS.get(Trapping.TrapType.BIRD_SNARE.getName());
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

    /**
     * Inventory contains any JUNK_ITEMS
     */
    private static boolean inventContainsJunk(){
        return Inventory.newQuery().names(JUNK_ITEMS).results().size() > 0;
    }

    /**
     * Buries bones and drops all other JUNK_ITEMS
     */
    private static void handleJunkItems(){
        Inventory.accept(x -> Arrays.asList(JUNK_ITEMS).contains(x.getName()),x -> {
            if (x.containsAction("Bury") && x.interact("Bury")) {
                //TODO FIX THIS
                Time.sleep(500);
                Time.sleepUntil(()-> Players.getLocal().getAnimation() != 827, 2000);
            }
            if (x.interact("Drop"))
                Time.sleep(196, 513);
        });
    }


    public static void populateHashMaps() {
        if (MINIMUM_REQUIRED_ITEMS.isEmpty()) {
            MINIMUM_REQUIRED_ITEMS.put("Bird snare", Hunter.getMaxTrapCount());
        }
        if (REQUIRED_ITEMS.isEmpty()) {
            REQUIRED_ITEMS.put("Bird snare", 8);
            REQUIRED_ITEMS.put("Piscatoris teleport", 1);
        }
    }

    public static Map<String, Integer> getMinimumRequiredItems() {
        return MINIMUM_REQUIRED_ITEMS;
    }

    public static Map<String, Integer> getRequiredItems() {
        return REQUIRED_ITEMS;
    }

    public static boolean haveMinimumRequiredItems() {
        if (MINIMUM_REQUIRED_ITEMS.isEmpty()) {
            Log.severe("Hashmap not populated.");
            Main.updateScriptState(null);
            return false;
        }
        return Main.hasItems(MINIMUM_REQUIRED_ITEMS);
    }

    public static boolean haveRequiredItems() {
        if (REQUIRED_ITEMS.isEmpty()) {
            Log.severe("Hashmap not populated.");
            Main.updateScriptState(null);
            return false;
        }
        return Main.hasItems(REQUIRED_ITEMS);
    }
}
