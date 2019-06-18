package Chin_Hunter;

import Chin_Hunter.States.ScriptState;
import Chin_Hunter.Executes.EaglesPeakQuest;
import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.api.component.tab.*;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.event.listeners.ChatMessageListener;
import org.rspeer.runetek.event.types.ChatMessageEvent;
import org.rspeer.runetek.event.types.ChatMessageType;
import org.rspeer.script.Script;
import org.rspeer.script.ScriptCategory;
import org.rspeer.script.ScriptMeta;
import org.rspeer.ui.Log;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

@ScriptMeta(desc = "Hunts your mums numerous chins", developer = "Shteve", name = "Chin Hunter", category = ScriptCategory.HUNTER, version = 0.1)
public class Main extends Script implements ChatMessageListener {

    private static ScriptState currentState = ScriptState.STARTING;
    private static ScriptState previousState;

    private static final Area PISCATORIS_AREA = Area.polygonal(
            new Position( 2249, 3646, 0),
            new Position( 2305, 3660, 0),
            new Position( 2377, 3661, 0),
            new Position( 2419, 3578, 0),
            new Position( 2365, 3516, 0),
            new Position( 2260, 3506, 0));

    private static final Area FELDIP_HILLS_AREA = Area.polygonal(
                    new Position(2495, 2997, 0),
                    new Position(2646, 2997, 0),
                    new Position(2662, 2955, 0),
                    new Position(2613, 2868, 0),
                    new Position(2464, 2880, 0));


    @Override
    public void onStart() {
        Log.fine("Running Chin Hunter by Shteve");
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
    public void notify(ChatMessageEvent chatMessageEvent) {
        String Message = chatMessageEvent.getMessage();
        if (chatMessageEvent.getType() == ChatMessageType.SERVER) {
            if (Message.contains("Congratulations, ")) {
                onLevelUpEvent();
            } else if (Message.equals("Oh dear, you are dead!")) {
                //On Death Event
            }
        }
    }


    //region Public Methods

    public static boolean isAtFeldipHills(){
        return FELDIP_HILLS_AREA.contains(Players.getLocal());
    }

    public static boolean isAtPiscatoris(){
        return PISCATORIS_AREA.contains(Players.getLocal());
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

    public static int getMaxTrapCount(){
        int hunterLevel = Skills.getLevel(Skill.HUNTER);
        if (hunterLevel < 20)
            return 1;
        if (hunterLevel < 40)
            return 2;
        if (hunterLevel < 60)
            return 3;
        if (hunterLevel < 80)
            return 4;
        return 5;
    }

    public static boolean hasItems(Map<String, Integer> map){
        Iterator iterator = map.entrySet().iterator();
        Item[] items;
        while (iterator.hasNext()){
            Map.Entry mapItem = (Map.Entry)iterator.next();
            Item[] invent = Inventory.getItems(x->x.getName().toLowerCase().contains(mapItem.getKey().toString().toLowerCase())
            && !x.isNoted());
            Item[] equipped = Equipment.getItems(x->x.getName().toLowerCase().contains(mapItem.getKey().toString().toLowerCase()));

            //Equipped items and items in invent.
            items = Stream.concat(Arrays.stream(invent), Arrays.stream(equipped)).toArray(Item[]::new);
            if (items.length > 0){
                //Item found
                if (items[0].isStackable()){
                    if (items[0].getStackSize() < (Integer) mapItem.getValue())
                        return false;
                }else{
                    if (items.length < (Integer) mapItem.getValue())
                        return false;
                }
            }else
                return false;
            iterator.remove();
        }
        return true;
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
            updateScriptState(bestState);
    }


    public static ScriptState getBestHuntingState(){
        int hunterLevel = Skills.getLevel(Skill.HUNTER);

        if (hunterLevel < 9)
            return ScriptState.MUSEUM_QUIZ;
        if (hunterLevel == 9){
            if (currentState == ScriptState.MUSEUM_QUIZ)
                return currentState;
            return ScriptState.BANKING;
        }
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
