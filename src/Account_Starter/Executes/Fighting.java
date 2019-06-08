package Account_Starter.Executes;

import Account_Starter.Enums.ScriptState;
import Account_Starter.Enums.Target;
import Account_Starter.Main;
import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.adapter.scene.Player;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.tab.Combat;
import org.rspeer.runetek.api.component.tab.Skill;
import org.rspeer.runetek.api.component.tab.Skills;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.Players;

import java.util.function.Predicate;

public class Fighting {

    private Fighting() {
        //Private Default Constructor
    }

    public static void execute() {

        Player local = Players.getLocal();
        Target target = Main.getCurrentTarget();
        if (target.getZone().contains(local)) {
            if (local.isMoving())
                Time.sleepUntil(local::isMoving, 2000);
            else if (local.getTargetIndex() != -1)
                Time.sleepUntil(() -> local.getTargetIndex() == -1, 5000);
            else {
                fightNPC(x -> target.matches(x)
                        && ((x.getTarget() != null && x.getTarget().equals(local)) || x.getTargetIndex() == -1)
                        && x.getHealthPercent() > 0
                        && Movement.isInteractable(x.getPosition()));
            }
        } else
            Main.updateScriptState(ScriptState.WALKING);

    }

    //Fights an npc that matches the given predicate
    private static void fightNPC(Predicate<Npc> targetPredicate) {
        Npc npc = Npcs.getNearest(targetPredicate);
        if (npc != null && npc.interact("Attack"))
            Time.sleep(200, 600);

    }


    /**
     * Calculates the lowest combat skill and returns the attack style that will train it.
     * @return Returns the most optimal attack style to train.
     */
    public static Combat.AttackStyle getBestAttackStyle() {
        int attackLevel = Skills.getLevel(Skill.ATTACK);
        int strengthLevel = Skills.getLevel(Skill.STRENGTH);
        int defenceLevel = Skills.getLevel(Skill.DEFENCE);

        int lowestCMBLevel = strengthLevel;
        Combat.AttackStyle bestAttackStyle = Combat.AttackStyle.AGGRESSIVE;

        if (attackLevel < lowestCMBLevel) {
            lowestCMBLevel = attackLevel;
            bestAttackStyle = Combat.AttackStyle.ACCURATE;
        }
        if (defenceLevel < lowestCMBLevel) {
            bestAttackStyle = Combat.AttackStyle.DEFENSIVE;
        }

        return bestAttackStyle;
    }


}
