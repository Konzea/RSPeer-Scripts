package Chin_Hunter.Helpers;

import Chin_Hunter.Main;
import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.adapter.scene.Pickable;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.component.tab.Skill;
import org.rspeer.runetek.api.component.tab.Skills;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Pickables;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.api.scene.SceneObjects;
import org.rspeer.ui.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.BooleanSupplier;

public class Trapping {
    public enum TrapType {

        BOX_TRAP("Box trap", "Lay"),
        BIRD_SNARE("Bird snare", "Lay");

        private final String Name;

        public String getName() {
            return Name;
        }

        private final String Action;

        public String getAction() {
            return Action;
        }

        TrapType(String name, String action) {
            this.Name = name;
            this.Action = action;
        }
    }

    private static Position[] potentialTrapTiles = null;
    private static List<Position> trapLocations = new ArrayList<>();


    //region Teleport

    /**
     * Teleports a player to piscatoris, must have teleport
     */
    public static void teleportToPiscatoris() {
        Item piscTele = Inventory.getFirst("Piscatoris teleport");
        if (piscTele != null && piscTele.interact("Teleport")) {
            Time.sleepUntil(Main::isAtPiscatoris, 8000);
        }
    }

    //endregion

    //region Removing Traps

    public static boolean pickUpAllTrapsExcept(TrapType trapType, int trapsToRemain) {
        if (trapLocations.size() <= trapsToRemain)
            return true;

        List<Position> tilesToRemove = new ArrayList<>();
        for (int i = 0; i < trapLocations.size(); i++) {
            if (pickUpSingleTrap(trapType, trapLocations.get(i)))
                tilesToRemove.add(trapLocations.get(i));
            break;
            //Break so it will loop back round and attempt again on the same trap.
        }
        removeTrapTiles(tilesToRemove);

        return trapLocations.size() <= trapsToRemain;
    }

    private static boolean pickUpSingleTrap(TrapType trapType, Position trapPos) {
        SceneObject[] trap = SceneObjects.getLoaded(x -> x.getPosition().equals(trapPos) && x.getName().equalsIgnoreCase(trapType.Name));
        int inventStartCount = Inventory.getCount();

        BooleanSupplier inventCountChanged = () -> Inventory.getCount() != inventStartCount;
        if (trap.length == 0) {
            Pickable[] droppedTrap = Pickables.getLoaded(x -> x.getPosition().equals(trapPos) && x.getName().equalsIgnoreCase(trapType.Name));
            if (droppedTrap.length == 0)
                return true;

            //Try and pick a trap up if we can.
            if (droppedTrap[0].interact("Take"))
                Time.sleepUntil(inventCountChanged, 2899);
            return inventStartCount != Inventory.getCount();
        }

        if (trap[0].getActions().length == 0)
            return false;

        if (trap[0].containsAction("Dismantle")) {
            if (trap[0].interact("Dismantle"))
                Time.sleepUntil(inventCountChanged, 2899);

        }

        if (trap[0].containsAction("Check")){
            if (trap[0].interact("Check"))
                Time.sleepUntil(inventCountChanged, 2899);
        }

        return inventStartCount != Inventory.getCount();

    }

    //endregion

    //region Trap Checking

    /**
     * Checks all trap locations to see if any need to be fixed
     *
     * @return A single position where a trap needs to be fixed, or null if no traps to be fixed
     */
    public static Position getTrapToFix(TrapType trapType) {
        List<Position> tilesToRemove = new ArrayList<>();
        for (Position trapPos : trapLocations) {
            SceneObject[] trap = SceneObjects.getLoaded(x -> x.getPosition().equals(trapPos) && x.getName().equalsIgnoreCase(trapType.Name));
            if (trap.length == 0) {
                Pickable[] droppedTrap = Pickables.getLoaded(x -> x.getPosition().equals(trapPos) && x.getName().equalsIgnoreCase(trapType.Name));
                if (droppedTrap.length == 0) {
                    tilesToRemove.add(trapPos);
                    continue;
                }
                removeTrapTiles(tilesToRemove);
                return trapPos;
            }

            if (trap[0].getActions().length == 0)
                continue;

            if (!trap[0].containsAction("Investigate")) {
                removeTrapTiles(tilesToRemove);
                return trapPos;
            }
        }

        //This has to be done after and not in the loop to avoid funky exceptions
        removeTrapTiles(tilesToRemove);
        return null;
    }

    /**
     * Handles timed out traps, failed traps and successful traps
     *
     * @param trapPos Position of the trap to fix
     */
    public static void checkTrap(TrapType trapType, Position trapPos) {
        SceneObject[] trap = SceneObjects.getLoaded(x -> x.getPosition().equals(trapPos) && x.getName().equalsIgnoreCase(trapType.Name));
        if (trap.length == 0) {
            Pickable[] droppedTrap = Pickables.getLoaded(x -> x.getPosition().equals(trapPos) && x.getName().equalsIgnoreCase(trapType.Name));
            if (droppedTrap.length == 1 && droppedTrap[0].interact(trapType.getAction()))
                Time.sleepUntil(() -> !isTileEmpty(trapPos), 4000);
        } else {
            if (trap[0].getActions().length == 0)
                return;

            if (trap[0].containsAction("Dismantle") && trap[0].interact("Dismantle"))
                Time.sleepUntil(() -> isTileEmpty(trapPos), 4000);
            else if (trap[0].containsAction("Check") && trap[0].interact("Check"))
                Time.sleepUntil(() -> isTileEmpty(trapPos), 4000);

        }

        if (isTileEmpty(trapPos))
            trapLocations.remove(trapPos);
    }

    //endregion

    //region Laying Trap

    /**
     * Attempts to lay a specified trap on a specified tile.
     * If successful will update the list of trapLocations
     *
     * @return Returns true if trap was laid successfully, false if not
     */
    public static boolean layTrap(TrapType trapType, Position centreTile) {
        Item[] inventTraps = Inventory.getItems(x -> x.getName().equalsIgnoreCase(trapType.Name));
        //No traps in invent
        if (inventTraps.length == 0)
            return false;

        Position trapTile = getBestTrapTile(centreTile);
        Pickable[] droppedTraps = Pickables.getLoaded(x -> x.getPosition().equals(trapTile) && x.getName().equalsIgnoreCase(trapType.Name));
        SceneObject[] objectOnTile = SceneObjects.getLoaded(x -> x.getPosition().equals(trapTile) && !x.getName().equals("null"));

        //Object already on tile
        if (objectOnTile.length > 0)
            return false;

        //Walk to tile
        if (!Players.getLocal().getPosition().equals(trapTile)) {
            Main.walkTo(trapTile);
            return false;
        }
        //Handle setting up a dropped trap
        if (droppedTraps.length > 0 && layTrapFromGround(trapType, trapTile)) {
            addTrapLocation(trapTile);
            return true;
        }
        //Otherwise try setting up from invent
        if (layTrapFromInvent(trapType, trapTile)) {
            addTrapLocation(trapTile);
            return true;
        }
        Log.severe("Failed to lay trap from invent and from ground?");
        return false;
    }

    private static boolean layTrapFromGround(TrapType trapType, Position trapTile) {
        Pickable[] droppedTraps = Pickables.getLoaded(x -> x.getPosition().equals(trapTile) && x.getName().equalsIgnoreCase(trapType.Name));
        int droppedTrapsCount = droppedTraps.length;

        if (droppedTraps[0].getActions().length == 0)
            return false;

        if (droppedTraps[0].interact(trapType.getAction()))
            Time.sleepUntil(() -> !isTileEmpty(trapTile), 4000);
        else
            return false;

        droppedTraps = Pickables.getLoaded(x -> x.getPosition().equals(trapTile) && x.getName().equalsIgnoreCase(trapType.Name));

        //Trap removed from the ground
        if (droppedTraps.length != droppedTrapsCount - 1)
            return false;
        //1 object on the tile hopefully our trap
        if (SceneObjects.getLoaded(x -> x.getPosition().equals(trapTile) && !x.getName().equals("null")).length != 1)
            return false;

        return true;
    }

    private static boolean layTrapFromInvent(TrapType trapType, Position trapTile) {
        Item[] inventTraps = Inventory.getItems(x -> x.getName().equalsIgnoreCase(trapType.Name));
        Pickable[] droppedTraps = Pickables.getLoaded(x -> x.getPosition().equals(trapTile) && x.getName().equalsIgnoreCase(trapType.Name));
        int droppedTrapsCount = droppedTraps.length;
        int inventTrapsCount = inventTraps.length;

        if (inventTraps[0].interact(trapType.getAction()))
            Time.sleepUntil(() -> !isTileEmpty(trapTile), 4000);
        else
            return false;

        //Update after placing a trap
        droppedTraps = Pickables.getLoaded(x -> x.getPosition().equals(trapTile) && x.getName().equalsIgnoreCase(trapType.Name));
        inventTraps = Inventory.getItems(x -> x.getName().equalsIgnoreCase(trapType.Name));

        //Number of traps on the tile changed, probably failed while placing it.
        if (droppedTraps.length != droppedTrapsCount)
            return false;
        //Didn't lose a trap from invent, probably didn't place it.
        if (inventTraps.length != inventTrapsCount - 1)
            return false;
        //1 Object on the tile, hopefully the trap we placed...
        if (SceneObjects.getLoaded(x -> x.getPosition().equals(trapTile) && !x.getName().equals("null")).length != 1)
            return false;

        return true;
    }
    //endregion

    //region Handling tiles for traps

    /**
     * @return Returns the best empty tile to place a trap on
     */
    private static Position getBestTrapTile(Position centreTile) {
        if (potentialTrapTiles == null || !potentialTrapTiles[0].equals(centreTile)) {
            Log.fine("Generating optimal trap tiles.");
            potentialTrapTiles = generateTrapTiles(centreTile);
        }

        for (Position tile : potentialTrapTiles) {
            SceneObject[] objectOnTile = SceneObjects.getAt(tile);
            if (objectOnTile.length == 0 || objectOnTile[0].getName().equals("null"))
                return tile;
        }
        //TODO Hop worlds
        Log.severe("Could not find a tile to place a trap on.");
        Time.sleep(20000);
        return null;
    }

    /**
     * Generates potential trap tiles around a centre point, does not check if any objects on the tile
     *
     * @return And ordered array of most optimal to least optimal potential trap locations
     */
    private static Position[] generateTrapTiles(Position centreTile) {
        int[] xTransforms = {0, 1, 1, -1, -1, 0, 0, 2, -2, -1, 0, 1, 0};
        int[] yTransforms = {0, 1, -1, -1, 1, 2, -2, 0, 0, 0, 1, 0, -1};
        Position[] potentialTrapTiles = new Position[xTransforms.length];
        int x = centreTile.getX();
        int y = centreTile.getY();
        int z = centreTile.getFloorLevel();

        for (int i = 0; i < xTransforms.length; i++)
            potentialTrapTiles[i] = new Position(x + xTransforms[i], y + yTransforms[i], z);
        return potentialTrapTiles;
    }

    private static boolean isTileEmpty(Position tile) {
        return SceneObjects.getLoaded(x -> x.getPosition().equals(tile) && !x.getName().equals("null")).length == 0;
    }
    //endregion

    //region Mutators

    /**
     * Resets trap locations and potential trap tiles.
     */
    public static void reset() {
        trapLocations = new ArrayList<>();
        potentialTrapTiles = null;
    }

    private static void addTrapLocation(Position newTraploc) {
        if (trapLocations.contains(newTraploc)) {
            Log.severe("Tried putting a trap where we already have a trap?");
            return;
        }
        trapLocations.add(newTraploc);
    }

    public static Queue<Position> previousTrapTiles = new LinkedList<>();
    private static void removeTrapTiles(List<Position> tilesToRemove) {
        for (Position tileToRemove : tilesToRemove) {
            trapLocations.remove(tileToRemove);
            previousTrapTiles.add(tileToRemove);
            if (previousTrapTiles.size() > 5)
                previousTrapTiles.poll();
        }
    }

    public static int getPlacedTrapsCount() {
        return trapLocations.size();
    }

    public static List<Position> getTrapLocations() {
        return trapLocations;
    }

    /**
     * Does not account for any wilderness increased trap counts
     *
     * @return Returns the maximum number of traps that can be placed at your level.
     */
    public static int getMaxTrapCount() {
        int hunterLevel = Skills.getLevel(Skill.HUNTER);
        if (hunterLevel < 20)
            return 1;
        if (hunterLevel < 40)
            return 2;
        if (hunterLevel < 60)
            return 3;
        if (hunterLevel < 80)
            return 4;
        return 5;
    }

    //endregion

}
