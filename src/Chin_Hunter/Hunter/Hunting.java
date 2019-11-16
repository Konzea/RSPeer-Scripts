package Chin_Hunter.Hunter;

import Chin_Hunter.Hunter.Trap_Admin.LaidTrap;
import Chin_Hunter.Hunter.Trap_Admin.TrapType;
import Chin_Hunter.Hunter.Trap_Laying.DeadfallTrap;
import Chin_Hunter.Hunter.Trap_Laying.StandardTrap;
import Chin_Hunter.Main;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.SceneObjects;
import org.rspeer.ui.Log;

import java.util.ArrayList;
import java.util.List;

public class Hunting {

    private static List<LaidTrap> activeTraps = new ArrayList<>();
    private static List<LaidTrap> previousTraps= new ArrayList<>();

    private static Position[] potentialTrapTiles = null;

    private static LaidTrap currentFocusedTrap = null;
    private static int maxTrapCount = calculateMaxTrapCount();

    /**
     * Returns the last or current trap we worked with.
     */
    public static LaidTrap getCurrentFocusedTrap(){
        return currentFocusedTrap;
    }

    public static void removeFlaggedTraps(){
        List<LaidTrap> trapsToRemove = new ArrayList<>();
        for (LaidTrap activeTrap : activeTraps){
            if (activeTrap.isFlaggedForRemoval())
                trapsToRemove.add(activeTrap);
        }
        if (trapsToRemove.size() == 0)
            return;
        for (LaidTrap trap : trapsToRemove)
            removeActiveTrap(trap);
    }

    //region Getters and Setters
    public static int getMaxTrapCount(){
        return maxTrapCount;
    }

    public static List<LaidTrap> getActiveTraps(){
        return activeTraps;
    }

    public static List<LaidTrap> getPreviousTraps(){
        return previousTraps;
    }

    public static int getActiveTrapCount(){
        return activeTraps.size();
    }


    private static void removeActiveTrap(LaidTrap trap){
        removeActiveTrap(trap, true);
    }

    public static void removeActiveTrap(LaidTrap trap, boolean addToPreviousTraps){
        activeTraps.remove(trap);
        if (addToPreviousTraps) {
            previousTraps.add(trap);
            if (previousTraps.size() > maxTrapCount)
                previousTraps.remove(0);
        }
    }

    public static void removePreviousTrap(LaidTrap trap){
        previousTraps.remove(trap);
    }

    public static void addActiveTrap(LaidTrap trap){
        LaidTrap issueTrap = LaidTrap.getByLocation(trap.getLocation());
        if (issueTrap != null) {
            Log.severe("Active trap found on tile we're adding a new trap too, been there for " + (issueTrap.getActiveTimeMs() / 1000) + "s");
            Log.info("The old trap has been removed.");
            issueTrap.flagForRemoval();
        }
        activeTraps.add(trap);
    }

    //endregion

    //region Active Trap Checking

    public static LaidTrap getTrapToFix(TrapType trapType){
        LaidTrap output = null;
        for (LaidTrap activeTrap : activeTraps){
            if (!activeTrap.getType().equals(trapType))
                continue;
            if (activeTrap.trapNeedsAttention()) {
                currentFocusedTrap = activeTrap;
                output = activeTrap;
                break;
            }
        }
        removeFlaggedTraps();
        return output;
    }

    //endregion

    //region Trap Laying

    public static boolean Lay_Trap(TrapType trapType, Position tile){
        return Lay_Trap(trapType, tile, false);
    }

    public static boolean Lay_Trap(TrapType trapType, Position tile, boolean centreTile){
        if (centreTile)
            tile = getBestTrapTile(tile);

        boolean trapSuccessfullyPlaced = false;
        switch (trapType) {
            case BOX_TRAP:
            case BIRD_SNARE:
                trapSuccessfullyPlaced = StandardTrap.Lay(trapType, tile);
                break;

            case DEADFALL:
                trapSuccessfullyPlaced = DeadfallTrap.Lay(tile);
                break;
        }
        if (!trapSuccessfullyPlaced)
            return false;

        addActiveTrap(new LaidTrap(tile, trapType));
        return true;
    }

    //endregion

    //region Ability to lay a trap checks

    /**
     * Checks if you can place a trap of a specified type on a given tile.
     */
    public static boolean canPlaceTrapOnTile(TrapType trapType, Position tile){
        switch (trapType) {
            case BOX_TRAP:
            case BIRD_SNARE:
                return StandardTrap.canLayTrap(tile);
            case DEADFALL:
                return DeadfallTrap.canLayTrap(tile);
        }
        Log.severe("Looking to place an unknown trap type: " + trapType.getName());
        return false;
    }

    public static boolean trapIsOnTile(Position tile){
        if (StandardTrap.trapIsOnTile(tile))
            return true;
        return DeadfallTrap.trapIsOnTile(tile);
    }

    //endregion

    //region Max Trap Count

    /**
     * Updates the max number of traps you can place.
     */
    public static void updateMaxTrapCount(){
        maxTrapCount = calculateMaxTrapCount();
    }

    /**
     * Calculates and returns the maximum number of traps that can be placed
     * at your hunter level.
     */
    private static int calculateMaxTrapCount(){
        return (Main.getHunterLevel()/20) + 1;
    }

    public static boolean canPlaceTrap(){
        return getActiveTrapCount() < getMaxTrapCount();
    }

    //endregion

    //region Generating best tile for traps

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

    //endregion



}
