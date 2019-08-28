package Chin_Hunter.Hunter.Trap_Admin;

import Chin_Hunter.Hunter.Hunting;
import Chin_Hunter.Hunter.Trap_Laying.DeadfallTrap;
import Chin_Hunter.Hunter.Trap_Laying.StandardTrap;
import org.jetbrains.annotations.Nullable;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.ui.Log;

import java.util.List;

public class LaidTrap {

    private static long lastErorrMessageTime = System.currentTimeMillis();

    private Position Location;
    private TrapType Type;
    private long timePlaced;

    private boolean isFlaggedForRemoval = false;

    private LaidTrap(){
        //private default constructor
    }

    public LaidTrap(Position tilePlaced, TrapType trapType){
        timePlaced = System.currentTimeMillis();
        Location = tilePlaced;
        Type = trapType;
    }

    public long getActiveTimeMs(){
        return System.currentTimeMillis() - timePlaced;
    }

    public Position getLocation(){
        return Location;
    }

    public TrapType getType(){
        return Type;
    }

    public boolean trapNeedsAttention(){
        switch (Type) {
            case BOX_TRAP:
            case BIRD_SNARE:
                return StandardTrap.trapNeedsAttention(this);
            case DEADFALL:
                return DeadfallTrap.trapNeedsAttention(this);
        }
        Log.severe("Could not find trap type when checking if trap requires attention.");
        return false;
    }

    public void fixTrap(){
        switch (Type) {
            case BOX_TRAP:
            case BIRD_SNARE:
                StandardTrap.attemptToFixTrap(this);
                break;
            case DEADFALL:
                DeadfallTrap.attemptToFixTrap(this);
                break;
        }
        Hunting.removeFlaggedTraps();
    }

    public boolean isFlaggedForRemoval(){
        return isFlaggedForRemoval;
    }

    public void flagForRemoval(){
        isFlaggedForRemoval = true;
    }

    public boolean trapNearlyTimedOut(){
        return getActiveTimeMs() > (getType().getTimeoutMs() - 5000);
    }

    @Nullable
    public static LaidTrap getByLocation(Position tile){
        for (LaidTrap activeTrap : Hunting.getActiveTraps()){
            if (activeTrap.getLocation().equals(tile))
                return activeTrap;
        }
        return null;
    }
}
