package Chin_Hunter;

import Chin_Hunter.Executes.Herblore.Druidic_Ritual;
import Chin_Hunter.Executes.Hunting.FalconKebbits;
import Chin_Hunter.Executes.MuseumQuiz;
import Chin_Hunter.Helpers.Paint;
import Chin_Hunter.Helpers.Trapping;
import Chin_Hunter.States.ScriptState;
import Chin_Hunter.Executes.Questing.QuestMain;
import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.adapter.scene.Pickable;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.tab.*;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Pickables;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.api.scene.Projection;
import org.rspeer.runetek.api.scene.SceneObjects;
import org.rspeer.runetek.event.listeners.ChatMessageListener;
import org.rspeer.runetek.event.listeners.RenderListener;
import org.rspeer.runetek.event.types.ChatMessageEvent;
import org.rspeer.runetek.event.types.ChatMessageType;
import org.rspeer.runetek.event.types.RenderEvent;
import org.rspeer.script.Script;
import org.rspeer.script.ScriptCategory;
import org.rspeer.script.ScriptMeta;
import org.rspeer.ui.Log;

import java.util.Arrays;
import java.util.Map;

@ScriptMeta(desc = "Start anywhere and it will do EVERYTHING required to hunt red chins.", developer = "Shteve", name = "AIO Chin Hunter", category = ScriptCategory.HUNTER, version = 0.1)
public class Main extends Script implements ChatMessageListener, RenderListener {

    private static boolean onStartCalled = false;
    private static ScriptState currentState = ScriptState.STARTING;
    private static ScriptState previousState;

    private static final Area PISCATORIS_AREA = Area.polygonal(
            new Position(2249, 3646, 0),
            new Position(2305, 3660, 0),
            new Position(2377, 3661, 0),
            new Position(2419, 3578, 0),
            new Position(2365, 3516, 0),
            new Position(2260, 3506, 0));

    private static final Area FELDIP_HILLS_AREA = Area.polygonal(
            new Position(2495, 2997, 0),
            new Position(2646, 2997, 0),
            new Position(2662, 2955, 0),
            new Position(2613, 2868, 0),
            new Position(2464, 2880, 0));

    private static final Area VARROCK_AREA = Area.rectangular(3071, 3518, 3295, 3334);
    private static final Area LUMBRIDGE_AREA = Area.rectangular(3210, 3233, 3234, 3204);
    private static final Area CAMELOT_AREA = Area.rectangular(2688, 3517, 2780, 3465);

    private static final int MIN_WALK_WAIT = 700;
    private static final int MAX_WALK_WAIT = 2000;

    private static Paint paint = null;

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public int loop() {
        if (currentState == null) {
            Log.severe("Null script state, stopping. Expand logger for an explanation.");
            return -1;
        }

        if (!onStartCalled) {
            currentState.onStart();
            onStartCalled = true;
        }

        currentState.execute();

        Paint.canDisplayPaint = !Projection.isLowCPUMode() && currentState != null;

        return Random.nextInt(100, 350);
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
                Log.fine(Message);
                onLevelUpEvent();
            } else if (Message.equals("Oh dear, you are dead!")) {
                //On Death Event
            }else if (Message.contains("You may set up only")){
                Log.severe("We already heave the max number of traps put down.");
                Log.severe("Printing debug data and stopping.");
                Log.info("Max trap count: " + Trapping.getMaxTrapCount());
                Log.info("Traps placed: " + Trapping.getPlacedTrapsCount());
                for (Position activeTile : Trapping.previousTrapTiles){
                    String output = "Active Trap on " + activeTile.toString() + " : ";
                    SceneObject[] objectsOnTile = SceneObjects.getLoaded(x -> x.getPosition().equals(activeTile) && !x.getName().equalsIgnoreCase("null"));
                    if (objectsOnTile.length > 0) {
                        String objectNames = "Objects[";
                        for (SceneObject obj : objectsOnTile)
                            objectNames = objectNames + obj.getName() + "|";
                        output = output + objectNames + "], ";
                    }
                    Pickable[] pickablesOnTile = Pickables.getLoaded(x -> x.getPosition().equals(activeTile) && x.getName().equalsIgnoreCase("Bird snare"));
                    if (pickablesOnTile.length > 0) {
                        String pickableNames = "GroundItems[";
                        for (Pickable pickable : pickablesOnTile)
                            pickableNames = pickableNames + pickable.getName() + "|";
                        output = output + pickableNames + "], ";
                    }
                    Log.info(output);
                }
                for (Position prevTile : Trapping.previousTrapTiles){
                    String output = "Previous Trap on " + prevTile.toString() + " : ";
                    SceneObject[] objectsOnTile = SceneObjects.getLoaded(x -> x.getPosition().equals(prevTile) && !x.getName().equalsIgnoreCase("null"));
                    if (objectsOnTile.length > 0) {
                        String objectNames = "Objects[";
                        for (SceneObject obj : objectsOnTile)
                            objectNames = objectNames + obj.getName() + "|";
                        output = output + objectNames + "], ";
                    }
                    Pickable[] pickablesOnTile = Pickables.getLoaded(x -> x.getPosition().equals(prevTile) && x.getName().equalsIgnoreCase("Bird snare"));
                    if (pickablesOnTile.length > 0) {
                            String pickableNames = "GroundItems[";
                            for (Pickable pickable : pickablesOnTile)
                                pickableNames = pickableNames + pickable.getName() + "|";
                            output = output + pickableNames + "], ";
                        }
                    Log.info(output);
                }
                updateScriptState(null);
            }
        }
    }

    //region Paint

    @Override
    public void notify(RenderEvent e) {
        if (Paint.canDisplayPaint)
            paint.Render(e);
    }

    //endregion

    //region Public Methods

    public static boolean isAtFeldipHills() {
        return FELDIP_HILLS_AREA.contains(Players.getLocal());
    }

    public static boolean isAtPiscatoris() {
        return PISCATORIS_AREA.contains(Players.getLocal());
    }

    public static boolean isInVarrock() {
        return VARROCK_AREA.contains(Players.getLocal());
    }

    public static boolean isInLumbridge() {
        return LUMBRIDGE_AREA.contains(Players.getLocal());
    }

    public static boolean isInCamelot() {
        return CAMELOT_AREA.contains(Players.getLocal());
    }


    /**
     * Handles updating current script state and updating previousState.
     *
     * @param inState The state you wish to set or null to stop script.
     */
    public static void updateScriptState(ScriptState inState) {
        previousState = currentState;
        if (inState == currentState)
            Log.severe("Error: New script state same as previous.");
        else {
            currentState = inState;
            onStartCalled = false;
            if (currentState != null)
                Log.info("State Updated to: " + currentState.name());

            //Clear museum data cache if not needed.
            if (currentState != ScriptState.MUSEUM_QUIZ)
                MuseumQuiz.clearQuizData();
        }
    }

    public static ScriptState getPreviousScriptState() {
        return previousState;
    }

    public static ScriptState getCurrentState(){
        return currentState;
    }

    public static void setPaint(Paint p){
        paint = p;
    }

    public static boolean hasItems(Map<String, Integer> map){
        return hasItems(map, null);
    }

    public static boolean walkTo(Position tile){
        if (Movement.walkTo(tile)) {
            int mainSleep = Movement.isRunEnabled()?Random.low(MIN_WALK_WAIT, MAX_WALK_WAIT):Random.low(MIN_WALK_WAIT*2, MAX_WALK_WAIT*2);

            if (!Movement.isRunEnabled()) {
                if ((Players.getLocal().isHealthBarVisible() && Movement.getRunEnergy() > 5) || Movement.getRunEnergy() > Random.nextInt(40, 80))
                    Movement.toggleRun(true);
            }
            //Log.info("Total Walk Sleep: " + mainSleep);

            //Initial sleep is used to allow for player to start moving
            int initialSleep = Players.getLocal().isMoving()?0:MIN_WALK_WAIT;
            Time.sleep(initialSleep);
            mainSleep -= initialSleep;

            if (Movement.getDestination() != null && Movement.getDestination().equals(tile))
                Time.sleepUntil(()->Players.getLocal().getPosition().equals(tile), Random.nextInt(8000,12000));
            else
                Time.sleepUntil(()->(!Players.getLocal().isMoving() && Players.getLocal().getAnimation() == -1)|| Players.getLocal().getPosition().equals(tile), mainSleep);
            return true;
        }
        return false;
    }

    public static boolean hasItems(Map<String, Integer> map, Trapping.TrapType trapType) {
        for (Map.Entry<String, Integer> mapItem : map.entrySet()) {
            String itemName = mapItem.getKey();
            int reqAmount = mapItem.getValue();

            Item[] invent = Inventory.getItems(x -> x.getName().equalsIgnoreCase(itemName)
                    && !x.isNoted());

            if (invent.length > 0){
                int layedTrapAdjustment = trapType != null && trapType.getName().equalsIgnoreCase(itemName) ? Trapping.getPlacedTrapsCount(): 0;
                int itemCount = getCount(invent) + layedTrapAdjustment;
                if (itemCount >= reqAmount)
                    continue;
            }

            Item[] equipped = Equipment.getItems(x -> x.getName().equalsIgnoreCase(itemName));
            if (equipped.length > 0){
                if (getCount(equipped) >= reqAmount)
                    continue;
            }
            return false;
        }
        return true;
    }

    public static int getCount(Item... items) {
        int count = 0;
        if (items == null || items.length == 0)
            return count;
        for (Item item : items) {
            if (item.isStackable())
                count = count + item.getStackSize();
            else
                count = count + 1;
        }
        return count;
    }

    public static boolean inventContains(String... items) {
        return Inventory.newQuery().names(items).results().size() > 0;
    }

    public static void handleJunkItems(String... items) {
        Inventory.accept(x -> Arrays.asList(items).contains(x.getName()), x -> {
            /*
            if (x.containsAction("Bury") && x.interact("Bury")) {
                Time.sleep(500);
                Time.sleepUntil(() -> Players.getLocal().getAnimation() != 827, 2000);
            }
            */
            if (x.interact("Drop"))
                Time.sleep(196, 513);
        });
    }


    /**
     * Checks for the best attack style and best target and updates if necessary.
     */
    public static void onLevelUpEvent() {
        if (!Movement.isRunEnabled())
            Movement.toggleRun(true);

        ScriptState bestState = getBestHuntingState();
        //If we're level 9 hunter we've just finished the quiz
        //Leave it on the current state so it can switch to banking manually.
        if (Skills.getLevel(Skill.HUNTER) == 9 && currentState == ScriptState.MUSEUM_QUIZ)
            return;

        if (bestState.name().equalsIgnoreCase("FALCON_KEBBITS"))
            FalconKebbits.updateTargetKebbits();

        if (!currentState.name().equals(bestState.name())) {
            updateScriptState(bestState);
        }
    }


    public static ScriptState getBestHuntingState() {
        int hunterLevel = Skills.getLevel(Skill.HUNTER);
        if (hunterLevel < 9)
            return ScriptState.MUSEUM_QUIZ;
        if (hunterLevel < 15)
            return ScriptState.LONGTAILS;
        if (hunterLevel < 37)
            return ScriptState.BUTTERFLIES;
        if (hunterLevel < 43)
            return ScriptState.DEADFALL_KEBBITS;
        if (hunterLevel < 63)
            return ScriptState.FALCON_KEBBITS;

        if (!QuestMain.questComplete())
            return ScriptState.EAGLES_PEAK_QUEST;

        if (!Druidic_Ritual.questComplete())
            return ScriptState.DRUIDIC_RITUAL_QUEST;

        return ScriptState.CHINCHOMPAS;

    }

    //endregion
}
