package Combat_Starter.Executes;

import Combat_Starter.Enums.Target;
import Combat_Starter.Main;
import Combat_Starter.Enums.ScriptState;
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
}
