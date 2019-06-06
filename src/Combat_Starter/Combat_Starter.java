package Combat_Starter;

import Combat_Starter.Enums.ScriptState;
import Combat_Starter.Enums.Target;
import Combat_Starter.Executes.Fighting;
import Combat_Starter.Helpers.CombatStyle;
import org.rspeer.runetek.api.Game;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.tab.*;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.event.listeners.ChatMessageListener;
import org.rspeer.runetek.event.types.ChatMessageEvent;
import org.rspeer.runetek.event.types.ChatMessageType;
import org.rspeer.script.Script;
import org.rspeer.script.ScriptCategory;
import org.rspeer.script.ScriptMeta;
import org.rspeer.ui.Log;

import java.text.NumberFormat;

@ScriptMeta(desc = "Kills NPCs around Lumbridge", developer = "Shteve", name = "Combat Starter", category = ScriptCategory.COMBAT, version = 0.1)
public class Combat_Starter extends Script implements ChatMessageListener {

    private static ScriptState currentState = ScriptState.STARTING;
    public static void updateScriptState(ScriptState inState){
        if (inState == null){
            Log.severe("Error: updateScriptState was passed a null ref");
        }else {
            previousState = currentState;
            if (inState == currentState)
                Log.severe("Error: New script state same as previous.");
            else
                currentState = inState;
        }
    }

    private static ScriptState previousState;
    public static ScriptState getPreviousScriptState(){
        return previousState;
    }

    private static Target currentTarget;
    public static void updateTarget(Target inTarget){
            currentTarget = inTarget;
        }

    private static long START_TIME;
    private static int START_XP;


    @Override
    public void onStart() {
        Log.fine("Running Combat Starter by Shteve");
        super.onStart();
    }

    @Override
    public int loop() {

        currentState.execute();
        return 150;
    }

    @Override
    public void onStop() {
/*
        long millisecondsRan = System.currentTimeMillis() - START_TIME;

        int xpGained = Skills.getExperience(Skill.WOODCUTTING) - START_XP;
        String xpGainedString = NumberFormat.getIntegerInstance().format(xpGained);

        Log.fine("Run Time: " + formatTime(millisecondsRan) + " | XP Gained: " + xpGainedString + " | XP/HR: " +  getXPPerHour(xpGained,millisecondsRan));

 */
        super.onStop();
    }

    private boolean attemptInitialisation() {
        int i = 0;
        while (i < 3) {
            if (Game.isLoggedIn()) {
                START_TIME = System.currentTimeMillis();
                START_XP = Skills.getExperience(Skill.WOODCUTTING);

                if (!Movement.isRunEnabled())
                    Movement.toggleRun(true);

                return true;
            } else {
                Log.info("Not logged in, waiting. Attempt #" + (i + 1));
                Time.sleepUntil(Game::isLoggedIn, 10000);
            }
            i++;
        }
        Log.severe("Sorry but we couldn't log you in. Stopping.");
        return false;
    }





    private String getXPPerHour(int inXPGained, long inMillisecondsRan){
        double xpPerMillisecond = (inXPGained / (double)(inMillisecondsRan));
        return NumberFormat.getIntegerInstance().format((int)(xpPerMillisecond * 1000 * 60 * 60));
    }

    private String formatTime(long inMilliseconds){
        long second = (inMilliseconds / 1000) % 60;
        long minute = (inMilliseconds / (1000 * 60)) % 60;
        long hour = (inMilliseconds / (1000 * 60 * 60)) % 24;

        return String.format("%02d:%02d:%02d", hour, minute, second);
    }

    @Override
    public void notify(ChatMessageEvent chatMessageEvent) {
        String Message = chatMessageEvent.getMessage();
        if (chatMessageEvent.getType() == ChatMessageType.SERVER){
            if (Message.contains("Congratulations, ")){
               onLevelUpEvent();
            }
        }
    }

    /**
     * Checks for the best attack style and best target and updates if necessary.
     */
    public static void onLevelUpEvent(){
        //TODO Potentially rename method.
        //Update combat style to give even levels
        Combat.AttackStyle currentAttackStyle = Combat.getAttackStyle();
        Combat.AttackStyle bestAttackStyle = Fighting.getBestAttackStyle();
        if (bestAttackStyle != currentAttackStyle)
            CombatStyle.setAttackStyle(bestAttackStyle);

        //Update target to give best xp based on levels
        Target bestTarget = Target.getBestTarget();
        if (bestTarget != currentTarget){
            //Walk to the new training area unless just starting the script
            //If just starting the script we need to do more checks before walking
            if (currentTarget != null)
                updateScriptState(ScriptState.WALKING);

            Log.fine("Leveled up, moving to the next training location.");
            updateTarget(bestTarget);
        }
    }
}
