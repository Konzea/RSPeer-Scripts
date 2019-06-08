package Combat_Starter.Enums;

import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.api.component.tab.Skill;
import org.rspeer.runetek.api.component.tab.Skills;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.api.movement.position.Position;

public enum Target{
    //Must be in order of target level, low to high
    GOBLINS(new String[]{"Goblin"},
            Area.polygonal(0, new Position( 3237, 3254), new Position( 3268, 3254), new Position( 3268, 3210), new Position( 3247, 3223), new Position( 3239, 3237)),
            5),

    COWS(new String[]{"Cow", "Cow calf"},
            Area.polygonal(0, new Position (3240, 3298), new Position (3266, 3301 ), new Position (3266, 3255 ), new Position (3253, 3255 ), new Position (3253, 3272 ), new Position (3248, 3278 ), new Position (3245, 3278 ), new Position (3240, 3284 ), new Position (3241, 3291 )),
            15),

    RATS(new String[]{"Giant rat", "Frog"},
            new int[]{6, 5},
            Area.polygonal(0, new Position( 3190, 3197 ), new Position( 3199, 3198 ), new Position( 3223, 3198 ), new Position( 3236, 3193 ), new Position( 3231, 3174 ), new Position( 3207, 3174 )),
            20),

    FROGS(new String[]{"Giant frog", "Big frog"},
            Area.polygonal(0, new Position( 3173, 3200) , new Position( 3227, 3199 ), new Position( 3230, 3166 ), new Position( 3191, 3168 ), new Position( 3179, 3176 )),
            100);


    Target(String[] inNames, Area inZone, int inTargetLevel){
        this(inNames, null, inZone, inTargetLevel);
    }

    Target(String[] inNames, int[] inAttackableCmbLvls, Area inZone, int inTargetLevel){
        Names = inNames;
        Zone = inZone;
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
    private final Area Zone;
    public Area getZone(){ return Zone; }

     //The level at which you no longer want to be killing this target.
     //e.g. Cows have a target level of 5, after level 5 we will kill the next target
    private final int targetLevel;
    public int getTargetLevel(){ return targetLevel; }


    /**
     * Checks the inputted npc to see if it matches the target profiles name(s) and level(s).
     * @param npc The npc to check as a potential target.
     * @return Returns true if the npc matches target profile.
     */
    public final boolean matches(Npc npc){
        boolean combatLvlMatch = false;
        if (attackableCmbLvls != null){
            for (int lvl : attackableCmbLvls){
                if (npc.getCombatLevel() == lvl){
                    combatLvlMatch = true;
                    break;
                }
            }
        }else
            combatLvlMatch = true;

        if (!combatLvlMatch)
            return false;

        for (String n : Names){
            if (npc.getName().equals(n))
                return true;
        }
        return false;
    }

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
            if (lowestCMBLevel <= t.getTargetLevel()) {
                bestTarget = t;
                break;
            }
        }

        return bestTarget;
    }
}
