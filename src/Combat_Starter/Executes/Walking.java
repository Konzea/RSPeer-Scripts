package Combat_Starter.Executes;

import Combat_Starter.Main;
import Combat_Starter.Enums.ScriptState;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Players;

public class Walking {

    private static final Position bankTile = new Position(3208,3218,2);
    private static final Position combatTrainerTile = new Position(3219,3238,0);

    private Walking(){
        //Private Default Constructor
    }

    public static void execute(){
        Position localPosition = Players.getLocal().getPosition();

        switch (Main.getPreviousScriptState()) {
            case GETTING_GEAR:
                Movement.walkTo(combatTrainerTile);
                Time.sleep(400, 1800);
                break;
            case FIGHTING:
                //If in zone set back to fighting state
                Movement.walkTo(Main.getCurrentTarget().getZone().getCenter());
                Time.sleep(400, 1800);
                break;

                default:
                    //Bank
                    if (localPosition.getFloorLevel() == 2){
                        Main.updateScriptState(ScriptState.BANKING);
                    }else{
                        Movement.walkTo(bankTile);
                        Time.sleep(400, 1800);
                    }
        }
    }
}
