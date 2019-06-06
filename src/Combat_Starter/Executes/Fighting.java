package Combat_Starter.Executes;

import Combat_Starter.Enums.Target;
import org.rspeer.runetek.api.component.tab.Combat;
import org.rspeer.runetek.api.component.tab.Skill;
import org.rspeer.runetek.api.component.tab.Skills;

public class Fighting {

    private Fighting(){
        //Private Default Constructor
    }

    public static void execute(){
//Fighting
//if gear equipped
//if in fight zone
//attack
//else go to fight zone
//else equip gear
    }

    /**
     * Returns the lowest 
     * */
    public static Combat.AttackStyle getBestAttackStyle(){
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
