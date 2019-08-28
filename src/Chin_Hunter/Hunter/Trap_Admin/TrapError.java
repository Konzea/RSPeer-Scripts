package Chin_Hunter.Hunter.Trap_Admin;

import Chin_Hunter.Hunter.Hunting;
import Chin_Hunter.Main;
import org.jetbrains.annotations.Nullable;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.ui.Log;

import java.util.List;

public class TrapError {

    public enum ErrorType {
        TRAP_LIMIT_REACHED,
        TRAP_NOT_OURS
    }

    private static TrapError activeError = null;

    private ErrorType Type;
    private Position Tile;

    private TrapError(){
        //Private default constructor
    }

    public TrapError(ErrorType errorType, @Nullable Position errorTile){
        Type = errorType;
        Tile = errorTile;
    }

    private boolean Solve(){
        Log.severe("Attempting to solve trap error: " + Type.name());
        boolean solvingSuccessful = false;
        switch (Type) {
            case TRAP_LIMIT_REACHED:
                solvingSuccessful = handleTrapLimitReached();
                break;
            case TRAP_NOT_OURS:
                solvingSuccessful = handleTrapNotOurs();
                break;
        }
        if (!solvingSuccessful)
            return false;

        Log.fine("Resolved trap error.");
        activeError = null;
        return true;
    }

    private static boolean handleTrapLimitReached(){
        List<LaidTrap> previousTraps = Hunting.getPreviousTraps();
        if (previousTraps.size() == 0){
            Log.severe("Trap limit reached and we don't have any previously placed traps...");
            Log.info("Please don't start the script with traps placed.");
            Main.updateScriptState(null);
            return false;
        }
        for (LaidTrap prevTrap : previousTraps){
            //If active trap on the previous tile, continue
            if (LaidTrap.getByLocation(prevTrap.getLocation()) != null)
                continue;

            if (Hunting.trapIsOnTile(prevTrap.getLocation())){
                Hunting.addActiveTrap(new LaidTrap(prevTrap.getLocation(), prevTrap.getType()));
                //Remove this from previous traps incase this error occurs again it won't use the same prev trap
                Hunting.removePreviousTrap(prevTrap);
                return true;
            }
        }

        //Scan previous trap tiles.
        //If not an active tile and trap is on tile, add it to active traps.
            //Also remove from previous tile to avoid coming back here

        //Also need to handle having traps placed when starting script.
        return false;
    }

    private static boolean handleTrapNotOurs(){
        LaidTrap notOurTrap = Hunting.getCurrentFocusedTrap();
        Hunting.removeActiveTrap(notOurTrap, false);
        //Get trap we're currently working on.
        //Remove from active traps
        //dont add to previous trap tiles to avoid checking that tile for our traps under trapLimitReached
        return true;
    }

    public static void setActiveError(@Nullable TrapError error){
        activeError = error;
    }

    public static boolean isActive(){
        return activeError != null;
    }

    public static void solveActiveError(){
        activeError.Solve();
    }
}
