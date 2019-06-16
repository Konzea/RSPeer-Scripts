package Chin_Hunter;

import Chin_Hunter.Executes.MuseumQuiz;
import Chin_Hunter.States.ScriptState;
import Chin_Hunter.Executes.EaglesPeakQuest;
import org.rspeer.runetek.api.component.tab.*;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.event.listeners.SkillListener;
import org.rspeer.runetek.event.types.SkillEvent;
import org.rspeer.script.Script;
import org.rspeer.script.ScriptCategory;
import org.rspeer.script.ScriptMeta;
import org.rspeer.ui.Log;

@ScriptMeta(desc = "Hunts your mums numerous chins", developer = "Shteve", name = "Chin Hunter", category = ScriptCategory.HUNTER, version = 0.1)
public class Main extends Script implements SkillListener {

    private static ScriptState currentState = ScriptState.STARTING;
    private static ScriptState previousState;

    private static Area piscatorisArea = Area.polygonal(
            new Position( 2249, 3646, 0),
            new Position( 2305, 3660, 0),
            new Position( 2377, 3661, 0),
            new Position( 2419, 3578, 0),
            new Position( 2365, 3516, 0),
            new Position( 2260, 3506, 0));

    private static Area feldipHillsArea = Area.polygonal(
                    new Position(2495, 2997, 0),
                    new Position(2646, 2997, 0),
                    new Position(2662, 2955, 0),
                    new Position(2613, 2868, 0),
                    new Position(2464, 2880, 0));

    @Override
    public void onStart() {
        Log.fine("Running Chin Hunter by Shteve");
        Main.updateScriptState(ScriptState.MUSEUM_QUIZ);
        super.onStart();
    }

    @Override
    public int loop() {
        if (currentState == null) {
            Log.severe("Null script state, stopping.");
            setStopping(true);
        }else {

            if (!onStartCalled) {
                currentState.onStart();
                onStartCalled = true;
            }

            currentState.execute();
            //Log.info("Current state: " + currentState.name());
        }
        return 150;
    }

    @Override
    public void onStop() {
        super.onStop();
    }


    @Override
    public void notify(SkillEvent skillEvent) {
        Log.info("Skill Event: " + skillEvent.toString());
        onLevelUpEvent();
    }


    //region Public Methods

    public static boolean isAtFeldipHills(){
        return feldipHillsArea.contains(Players.getLocal());
    }

    public static boolean isAtPiscatoris(){
        return piscatorisArea.contains(Players.getLocal());
    }

    private static boolean onStartCalled = false;
    /**
     * Handles updating current script state and updating previousState.
     * @param inState The state you wish to set or null to stop script.
     */
    public static void updateScriptState(ScriptState inState){
            previousState = currentState;
            if (inState == currentState)
                Log.severe("Error: New script state same as previous.");
            else {
                currentState = inState;
                onStartCalled = false;
            }
    }

    public static ScriptState getPreviousScriptState(){
        return previousState;
    }


    /**
     * Checks for the best attack style and best target and updates if necessary.
     */
    public static void onLevelUpEvent(){
        //TODO Potentially rename method.

        if (!Movement.isRunEnabled())
            Movement.toggleRun(true);

        ScriptState bestState = getBestHuntingState();
        if (currentState != bestState)
            currentState = bestState;
    }


    public static ScriptState getBestHuntingState(){
        int hunterLevel = Skills.getLevel(Skill.HUNTER);

        if (hunterLevel < 9)
            return ScriptState.MUSEUM_QUIZ;
        if (hunterLevel < 15)
            return ScriptState.LONGTAILS;
        if (hunterLevel < 37)
            return ScriptState.BUTTERFLIES;
        if (hunterLevel < 43)
            return ScriptState.TRAPFALL_KEBBITS;
        if (hunterLevel < 63)
            return ScriptState.FALCON_KEBBITS;

        if (EaglesPeakQuest.questComplete())
            return ScriptState.CHINCHOMPAS;
        else
            return ScriptState.EAGLES_PEAK_QUEST;

    }


    //endregion
}
