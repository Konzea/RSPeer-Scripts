package Chin_Hunter.Hunter.Trap_Laying;

import Chin_Hunter.Hunter.Hunting;
import Chin_Hunter.Hunter.Trap_Admin.LaidTrap;
import Chin_Hunter.Hunter.Trap_Admin.TrapType;
import Chin_Hunter.Main;
import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.adapter.scene.Pickable;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Pickables;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.api.scene.SceneObjects;
import org.rspeer.ui.Log;

public class StandardTrap {

    //region Trap Fixing

    public static void attemptToFixTrap(LaidTrap laidTrap){
        TrapType trapType = laidTrap.getType();
        Position trapTile = laidTrap.getLocation();
        if (trapSuccessful(trapType, trapTile) || trapHasFailed(trapType, trapTile)){
            if (Players.getLocal().distance(trapTile) > 3){
                Main.walkTo(trapTile);
                return;
            }
            if (collectTrap(trapType, trapTile))
                laidTrap.flagForRemoval();
            return;
        }
        if (trapHasFallen(trapType, trapTile)){
            if (canLayTrap(trapTile)) {
                //If we can lay trap when there is a fallen trap leave it to another element of the
                // script to lay from ground.
                laidTrap.flagForRemoval();
                return;
            }
            //Can't lay a new trap and trap on the ground, pick it up
            pickUpTrap(trapType, trapTile);
            return;
        }
        Time.sleep(100, 300);
        //Log.severe("Attempting to fix trap but couldn't find what's wrong with it.");
    }

    private static boolean collectTrap(TrapType trapType, Position tile){
        SceneObject[] trap = SceneObjects.getLoaded(x -> x.getPosition().equals(tile) && x.getName().equalsIgnoreCase(trapType.getName()));
        if (trap.length == 0){
            Log.severe("Attempting to collect a trap but can't find one...");
            return false;
        }
        if (trap[0].containsAction("Dismantle")) {
            if (trap[0].interact("Dismantle"))
                Time.sleepUntil(() -> canLayTrap(tile), 4000);
            return canLayTrap(tile);
        }
        if (trap[0].containsAction("Check")) {
            if (trap[0].interact("Check"))
                Time.sleepUntil(() -> canLayTrap(tile), 4000);
            return canLayTrap(tile);
        }
        Log.severe("Trap does not have dismantle of check action when trying to collect.");
        return false;
    }

    private static void pickUpTrap(TrapType trapType, Position tile){
        Pickable[] droppedTraps = Pickables.getLoaded(x -> x.getPosition().equals(tile) && x.getName().equalsIgnoreCase(trapType.getName()));
        int initialInventCount = Inventory.getCount();
        if (droppedTraps.length == 0){
            Log.severe("Attempting to pick up a trap but could not find one");
            return;
        }
        if (droppedTraps[0].interact("Take"))
            Time.sleepUntil(()->Inventory.getCount() != initialInventCount, 5000);
    }

    //endregion

    //region Trap Status Checking

    public static boolean trapNeedsAttention(LaidTrap laidTrap){
        SceneObject Trap = getTrapOnTile(laidTrap.getType(), laidTrap.getLocation());
        if (Trap == null) {
            Pickable[] droppedTraps = Pickables.getLoaded(x -> x.getPosition().equals(laidTrap.getLocation()) && x.getName().equalsIgnoreCase(laidTrap.getType().getName()));
            //Return true if there is a trap on the floor to pick up
            if (droppedTraps.length == 1)
                return true;

            //No trap found on tile & more or less than 1 traps on ground. Remove
            laidTrap.flagForRemoval();
            return false;
        }
        //Trap.containsAction("Reset") || Trap.containsAction("Check")
        return !Trap.containsAction("Investigate");
    }

    private static boolean trapHasFailed(TrapType trapType, Position trapTile){
        SceneObject Trap = getTrapOnTile(trapType, trapTile);

        if (Trap == null || Trap.getActions().length == 0)
            return false;

        //Check(Success) or Reset(Failed)
        return !Trap.containsAction("Investigate") && !Trap.containsAction("Check");
    }

    private static boolean trapHasFallen(TrapType trapType, Position trapTile){
        //No objects found on the tile, and 1 dropped trap found.
        Pickable[] droppedTraps = Pickables.getLoaded(x -> x.getPosition().equals(trapTile) && x.getName().equalsIgnoreCase(trapType.getName()));
        return droppedTraps.length == 1;
    }

    private static boolean trapSuccessful(TrapType trapType, Position trapTile){
        SceneObject Trap = getTrapOnTile(trapType, trapTile);

        if (Trap == null || Trap.getActions().length == 0)
            return false;

        //Check(Success) or Reset(Failed)
        return Trap.containsAction("Check");
    }

    //endregion

    //region Tile Checking

    /**
     * Checks if a tile has no objects on it allowing you to place a trap.
     */
    public static boolean canLayTrap(Position tile){
        SceneObject[] objectsOnTile = SceneObjects.getLoaded(x->x.getPosition().equals(tile) && !x.getName().equals("null"));
        return objectsOnTile.length == 0;
    }

    public static boolean trapIsOnTile(Position tile){
        SceneObject[] trap = SceneObjects.getLoaded(x -> x.getPosition().equals(tile)
                && (x.getName().equalsIgnoreCase(TrapType.BIRD_SNARE.getName())
                || x.getName().equalsIgnoreCase(TrapType.BOX_TRAP.getName())));
        return trap.length > 0;
    }

    //endregion

    //region Trap Laying

    /**
     * Lays a given trap type on a given tile.
     * @return Returns true if trap laid successfully false if not.
     */
    public static boolean Lay(TrapType trapType, Position tile){

        Item[] inventTraps = Inventory.getItems(x -> x.getName().equalsIgnoreCase(trapType.getName()));
        if (inventTraps.length == 0) {
            Log.severe("Could not find any traps in invent.");
            return false;
        }

        if (!canLayTrap(tile)){
            Log.info("Trap already found on tile as we were going to lay.");
            return false;
        }

        //Walk to tile
        if (!Players.getLocal().getPosition().equals(tile)) {
            Main.walkTo(tile);
            if (!Players.getLocal().getPosition().equals(tile))
                return false;
        }

        //Handle setting up a dropped trap
        Pickable[] droppedTraps = Pickables.getLoaded(x -> x.getPosition().equals(tile) && x.getName().equalsIgnoreCase(trapType.getName()));
        if (droppedTraps.length > 0)
            return layTrapFromGround(trapType, tile);

        //Otherwise try setting up from invent
        if (layTrapFromInvent(trapType, tile))
            return true;

        Log.severe("Failed to lay trap from invent and from ground.");
        return false;
    }

    private static boolean layTrapFromGround(TrapType trapType, Position trapTile) {
        Pickable[] droppedTraps = Pickables.getLoaded(x -> x.getPosition().equals(trapTile) && x.getName().equalsIgnoreCase(trapType.getName()));
        int droppedTrapsCount = droppedTraps.length;

        if (droppedTraps[0].getActions().length == 0)
            return false;

        if (!droppedTraps[0].interact(trapType.getAction()))
            return false;

        Time.sleepUntil(() -> !canLayTrap(trapTile), 4000);

        droppedTraps = Pickables.getLoaded(x -> x.getPosition().equals(trapTile) && x.getName().equalsIgnoreCase(trapType.getName()));

        //Trap removed from the ground
        if (droppedTraps.length != droppedTrapsCount - 1)
            return false;

        //1 object on the tile hopefully our trap
        return SceneObjects.getLoaded(x -> x.getPosition().equals(trapTile) && !x.getName().equals("null")).length == 1;
    }

    private static boolean layTrapFromInvent(TrapType trapType, Position trapTile) {
        Item[] inventTraps = Inventory.getItems(x -> x.getName().equalsIgnoreCase(trapType.getName()));
        Pickable[] droppedTraps = Pickables.getLoaded(x -> x.getPosition().equals(trapTile) && x.getName().equalsIgnoreCase(trapType.getName()));
        int droppedTrapsCount = droppedTraps.length;
        int inventTrapsCount = inventTraps.length;

        if (!inventTraps[0].interact(trapType.getAction()))
            return false;

        Time.sleepUntil(() -> !canLayTrap(trapTile), 4000);

        //Update after placing a trap
        droppedTraps = Pickables.getLoaded(x -> x.getPosition().equals(trapTile) && x.getName().equalsIgnoreCase(trapType.getName()));
        inventTraps = Inventory.getItems(x -> x.getName().equalsIgnoreCase(trapType.getName()));

        //Number of traps on the tile changed, probably failed while placing it.
        if (droppedTraps.length != droppedTrapsCount)
            return false;

        //Didn't lose a trap from invent, probably didn't place it.
        if (inventTraps.length != inventTrapsCount - 1)
            return false;

        //1 Object on the tile, hopefully the trap we placed...
        return SceneObjects.getLoaded(x -> x.getPosition().equals(trapTile) && !x.getName().equals("null")).length == 1;
    }

    //endregion
    private static SceneObject getTrapOnTile(TrapType trapType, Position trapTile){
        SceneObject[] trap = SceneObjects.getLoaded(x -> x.getPosition().equals(trapTile)
                && x.getName().equalsIgnoreCase(trapType.getName()));

        if (trap.length == 0)
            return null;

        return trap[0];
    }

}
