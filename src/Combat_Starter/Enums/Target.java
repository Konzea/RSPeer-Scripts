package Combat_Starter.Enums;

import org.rspeer.runetek.api.component.tab.Skill;
import org.rspeer.runetek.api.component.tab.Skills;
import org.rspeer.runetek.api.movement.position.Area;

public enum Target{
    //Must be in order of target level, low to high
    GOBLINS(new String[]{"Goblin"}, null, 5),
    COWS(new String[]{"Cow", "Cow calf"}, null, 10),
    RATS(new String[]{"Giant rat"}, new int[]{6}, null, 18),
    FROGS(new String[]{"Giant frog", "Big frog"}, null, 100);


    Target(String[] inNames, org.rspeer.runetek.api.movement.position.Area inArea, int inTargetLevel){
        this(inNames, null, inArea, inTargetLevel);
    }

    Target(String[] inNames, int[] inAttackableCmbLvls, org.rspeer.runetek.api.movement.position.Area inArea, int inTargetLevel){
        Names = inNames;
        Area = inArea;
        attackableCmbLvls = inAttackableCmbLvls;
        targetLevel = inTargetLevel;
    }


    //For use if an npc has multiple combat levels and you only want to target ones with a specific level
    //If left null will ignore the combat level of the npc
    private final int[] attackableCmbLvls;
    public int[] getAttackableCmbLvls(){ return this.attackableCmbLvls; }

    //Name or names of the target
    private final String[] Names;
    public String[] getNames(){ return this.Names; }

    //Returns he area in which to stay when fighting this target
    private final Area Area;
    public Area getArea(){ return Area; }

     //The level at which you no longer want to be killing this target.
     //e.g. Cows have a target level of 5, after level 5 we will kill the next target
    private final int targetLevel;
    public int getTargetLevel(){ return targetLevel; }


    /**
     * Using lowest melee combat level calculates the best target.
     * @return  Returns the best target for your level.
     */
    public static Target getBestTarget(){
        int attackLevel = Skills.getLevel(Skill.ATTACK);
        int strengthLevel = Skills.getLevel(Skill.STRENGTH);
        int defenceLevel = Skills.getLevel(Skill.DEFENCE);

        int lowestCMBLevel = strengthLevel;
        if (attackLevel < lowestCMBLevel)
            lowestCMBLevel = attackLevel;
        if (defenceLevel < lowestCMBLevel)
            lowestCMBLevel = defenceLevel;

        Target[] allPossibleTargets = Target.values();
        Target bestTarget = allPossibleTargets[0];

        //Assumes targets are in order of max level
        for (Target t:allPossibleTargets){
            if (lowestCMBLevel <= t.getTargetLevel())
                bestTarget = t;
            else
                break;
        }

        return bestTarget;
    }
}
