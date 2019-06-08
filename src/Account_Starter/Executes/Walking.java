package Account_Starter.Executes;

import Account_Starter.Enums.ScriptState;
import Account_Starter.Enums.Target;
import Account_Starter.Main;
import org.rspeer.runetek.adapter.Positionable;
import org.rspeer.runetek.adapter.scene.Player;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Players;

public class Walking {

    private static final Position bankTile = new Position(3208,3218,2);
    private static final Position combatTrainerTile = new Position(3219,3238,0);

    private Walking(){
        //Private Default Constructor
    }

    public static void execute(){
        Player local = Players.getLocal();

        switch (Main.getPreviousScriptState()) {
            case GETTING_GEAR:
                Movement.walkTo(combatTrainerTile);
                Time.sleep(400, 1400);
                break;
            case FIGHTING:
                Area zone = Main.getCurrentTarget().getZone();
                if (!zone.contains(local)){
                    if (Main.getCurrentTarget() == Target.COWS)
                        walkToCows(local, zone);
                    else
                        Movement.walkTo(zone.getCenter());
                    Time.sleep(400, 1400);
                }else{
                    Main.updateScriptState(ScriptState.FIGHTING);
                }
                break;

                default:
                    //Bank
                    if (local.getFloorLevel() == 2){
                        Main.updateScriptState(ScriptState.BANKING);
                    }else{
                        Movement.walkTo(bankTile);
                        Time.sleep(400, 1400);
                    }
        }
    }

    private static void walkToCows(Positionable localPos, Area endTarget){
        //So it doesn't get stuck in some fucking special hut...
        if (localPos.getX() < 3242 && localPos.getY() < 3244)
            Movement.walkTo(new Position(3254, 3251, 0));
        else
            Movement.walkTo(endTarget.getCenter());
    }
}
