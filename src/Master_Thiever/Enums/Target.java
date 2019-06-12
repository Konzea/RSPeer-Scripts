package Master_Thiever.Enums;

import Master_Thiever.Main;
import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.adapter.scene.Player;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.component.tab.Skill;
import org.rspeer.runetek.api.component.tab.Skills;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.SceneObjects;
import org.rspeer.ui.Log;

public enum Target {

    //Must be in order of target level, low to high
    MEN(new String[]{"Man"}, "Pickpocket", new Position(3226,3235,0), true,5, true),

    TEA(new String[]{"Tea stall", "Market stall"},"Steal-from", new Position(3268,3410,0), false, 38, false, 635),

    MASTER_FARMERS(new String[]{"Master Farmer"}, "Pickpocket", new Position(3080,3251,0), true );



    Target(String[] Names, String Action, Position Location, boolean isNpc){
        this(Names,Action,Location,isNpc, 100);
    }

    Target(String[] Names, String Action, Position Location, boolean isNpc, int targetLevel){
        this(Names,Action,Location,isNpc, targetLevel, false);
    }

    Target(String[] Names, String Action, Position Location, boolean isNpc, int targetLevel, boolean dropsCoinPouches){
        this(Names,Action,Location,isNpc, targetLevel, dropsCoinPouches, -1);
    }

    Target(String[] Names, String Action, Position Location, boolean isNpc, int targetLevel, boolean dropsCoinPouches, int lootableID){
        this.Names = Names;
        this.Location = Location;
        this.targetLevel = targetLevel;
        this.isNpc = isNpc;
        this.Action = Action;
        this.dropsCoinPouches = dropsCoinPouches;
        this.lootableID = lootableID;
    }

    //Name or names of the target
    private final String[] Names;
    public String[] getNames(){ return this.Names; }

    //Returns he area in which to stay when fighting this target
    private final Position Location;
    public Position getLocation(){ return Location; }

     //The level at which you no longer want to be killing this target.
     //e.g. Cows have a target level of 5, after level 5 we will kill the next target
    private final int targetLevel;
    public int getTargetLevel(){ return targetLevel; }

    //Returns if the target is an npc, if not we assume its an object.
    private final boolean isNpc;
    public boolean isNpc(){ return this.isNpc; }

    //Returns the action to click to theive
    private final String Action;
    public String getAction(){ return Action; }

    //If the npc drops coin pouches
    private final boolean dropsCoinPouches;
    public boolean dropsCoinPouches(){ return dropsCoinPouches; }

    //Used for stalls, when to loot it
    private final int lootableID;
    public int getLootableID() { return lootableID;}


    /**
     * Checks if there is a target nearby
     * @return true if target is nearby, false if no targets found
     */
    public final boolean inReach(Player local){
        if (isNpc){
            Npc[] npcs = Npcs.getLoaded(this::NpcMatches);
            if (npcs.length > 0 && npcs[0] != null)
                return true;
        }else{
            SceneObject[] objects = SceneObjects.getLoaded(this::ObjectMatches);
            if (objects.length > 0 && objects[0] != null)
                return true;
        }
        return false;
    }

    /**
     * Checks the inputted npc to see if it matches the target profiles name(s) and actions.
     * @param npc The npc to check as a potential target.
     * @return Returns true if the npc matches target profile.
     */
    public final boolean NpcMatches(Npc npc){
        if (!npc.containsAction(Action))
            return false;

        for (String n : Names){
            if (npc.getName().equals(n))
                return true;
        }
        return false;
    }

    /**
     * Checks the inputted object to see if it matches the target profiles name(s).
     * Does not check the actions.
     * @param object The object to check as a potential target.
     * @return Returns true if the object matches target profile.
     */
    public final boolean ObjectMatches(SceneObject object){
        for (String n : Names){
            if (object.getName().equals(n))
                return true;
        }
        return false;
    }

    /**
     * Using you thieving level calculates the best target.
     * @return  Returns the best target for your level.
     */
    public static Target getBestTarget(){
        int thievingLevel = Skills.getLevel(Skill.THIEVING);

        Target[] allPossibleTargets = Target.values();
        Target bestTarget = allPossibleTargets[0];

        //Assumes targets are in order of max level
        for (Target t:allPossibleTargets){
            if (thievingLevel < t.getTargetLevel()) {
                bestTarget = t;
                break;
            }
        }

        return bestTarget;
    }

}
