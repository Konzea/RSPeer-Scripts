package Master_Thiever.Executes;

import Master_Thiever.Enums.ScriptState;
import Master_Thiever.Enums.Target;
import Master_Thiever.Main;
import org.rspeer.runetek.adapter.Positionable;
import org.rspeer.runetek.adapter.scene.Player;
import org.rspeer.runetek.api.commons.BankLocation;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Players;

public class Walking {

    private Walking(){
        //Private Default Constructor
    }

    public static void execute(){
        Player local = Players.getLocal();
        Target target = Main.getCurrentTarget();

        if (Main.getPreviousScriptState() == ScriptState.THIEVING) {
            if (!target.inReach(local)) {
                Movement.walkTo(target.getLocation());
                Time.sleep(400, 1400);
            } else {
                Main.updateScriptState(ScriptState.THIEVING);
            }
        } else {
            Positionable bankTile = BankLocation.getNearest().getPosition();
            if (nearBank(local, bankTile)){
                Main.updateScriptState(ScriptState.BANKING);
            } else {
                Movement.walkTo(bankTile);
                Time.sleep(400, 1400);
            }
        }
    }

    private static boolean nearBank(Positionable local, Positionable bankTile){
        return bankTile.distance(local) < 5;
    }

}
