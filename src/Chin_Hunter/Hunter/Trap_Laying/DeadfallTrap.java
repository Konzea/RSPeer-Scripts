package Chin_Hunter.Hunter.Trap_Laying;

import Chin_Hunter.Hunter.Hunting;
import Chin_Hunter.Hunter.Trap_Admin.LaidTrap;
import Chin_Hunter.Hunter.Trap_Admin.TrapType;
import Chin_Hunter.Main;
import org.jetbrains.annotations.Nullable;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.Dialog;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.api.scene.SceneObjects;
import org.rspeer.ui.Log;

import java.util.Arrays;
import java.util.function.BooleanSupplier;

public class DeadfallTrap {

    private static final int SETTING_DEADFALL_ANIMATION = 5212;
    private static final TrapType DEADFALL = TrapType.DEADFALL;

    //region Trap Checking
    public static boolean trapNeedsAttention(LaidTrap laidTrap){
        Position trapTile = laidTrap.getLocation();
        SceneObject deadfallTrap = getTrapOnTile(trapTile);

        if (deadfallTrap == null) {
            Log.info("Can't find deadfall trap...");
            Main.walkTo(trapTile);
            return false;
        }

        if (deadfallTrap.getActions().length == 0)
            return false;

        if (deadfallTrap.containsAction(DEADFALL.getAction())) {
            laidTrap.flagForRemoval();
            return false;
        }

        return !deadfallTrap.containsAction("Dismantle");
    }
    //endregion

    //region Trap Fixing
    public static void attemptToFixTrap(LaidTrap laidTrap){
        Position trapTile = laidTrap.getLocation();
        SceneObject deadfallTrap = getTrapOnTile(trapTile);

        if (deadfallTrap == null) {
            Log.severe("Can't find deadfall trap...");
            Main.walkTo(trapTile);
            return;
        }

        if (deadfallTrap.getActions().length == 0){
            Log.severe("Fucking Deadfall has no actions?! WTF");
            Time.sleep(50, 200);
            return;
        }

        if (deadfallTrap.containsAction("Check")) {
            int inventCount = Inventory.getCount();
            if (deadfallTrap.interact("Check")) {
                Time.sleepUntil(()->Players.getLocal().isMoving(), 1000);
                if (Time.sleepUntil(()->!Players.getLocal().isMoving() || Inventory.getCount() != inventCount, 8000))
                    Time.sleepUntil(() -> Inventory.getCount() != inventCount, 4000);
            }
            if (canLayTrap(trapTile))
                laidTrap.flagForRemoval();
        }
    }
    //endregion

    //region Tile Checking
    /**
     * Checks if a tile has no objects on it allowing you to place a trap.
     */
    public static boolean canLayTrap(Position tile){
        SceneObject Trap = getTrapOnTile(tile);
        if (Trap == null)
            return false;

        if (Trap.getActions().length == 0){
            long startTime = System.currentTimeMillis();
            Log.severe("No deadfall actions found");
            Time.sleepUntil(()->{
                SceneObject tempTrap = getTrapOnTile(tile);
                if (tempTrap == null)
                    return false;
                return tempTrap.getActions().length > 0;
            }, 2000);
            Log.info("-> Took " + (System.currentTimeMillis() - startTime) + "ms to appear");
        }
        return Trap.containsAction(DEADFALL.getAction());
    }

    public static boolean trapIsOnTile(Position tile){
        return !canLayTrap(tile);
    }
    //endregion

    //region Trap Laying
    /**
     * Lays a given trap type on a given tile.
     * @return Returns true if trap laid successfully false if not.
     */
    public static boolean Lay(Position tile){
        SceneObject deadfallTrap = getTrapOnTile(tile);

        if (deadfallTrap == null) {
            Log.severe("Can't find deadfall trap..., walking to where it should be.");
            Main.walkTo(tile);
            return false;
        }

        if (deadfallTrap.getActions().length == 0){
            Log.severe("Deadfall has no actions, give it a sec and we'll check again.");
            Time.sleep(50, 200);
            return false;
        }

        if (!deadfallTrap.containsAction(DEADFALL.getAction())){
            Log.severe("Trap already set where we were going to use.");
            return false;
        }

        int initialInventCount = Inventory.getCount();
        if (deadfallTrap.interact(DEADFALL.getAction())) {
            final BooleanSupplier playerIsSettingDeadfall = () -> Players.getLocal().getAnimation() == SETTING_DEADFALL_ANIMATION || Dialog.isOpen();
            Time.sleepUntil(playerIsSettingDeadfall, 5000);
            if (Players.getLocal().isMoving())
                Time.sleepUntil(playerIsSettingDeadfall, 10000);
            if (Players.getLocal().getAnimation() != SETTING_DEADFALL_ANIMATION)
                return false;

            Time.sleepUntil(() -> Players.getLocal().getAnimation() == -1, 4000);

            //We had the setting deadfall anim and we can no longer set the trap and used a log. Probably our trap...
            return !canLayTrap(tile) && Inventory.getCount() != initialInventCount;
        }
        return false;
    }
    //endregion

    @Nullable
    private static SceneObject getTrapOnTile(Position trapTile){
        SceneObject[] deadfallTrap = SceneObjects.getLoaded(x-> x.getPosition().equals(trapTile)
                && (x.getName().equalsIgnoreCase("Boulder")
                || x.getName().equalsIgnoreCase("Deadfall")));

        if (deadfallTrap.length == 0)
            return null;

        return deadfallTrap[0];
    }
}
