package Chin_Hunter;

import Chin_Hunter.Executes.Herblore.Druidic_Ritual;
import Chin_Hunter.Executes.Hunting.FalconKebbits;
import Chin_Hunter.Executes.MuseumQuiz;
import Chin_Hunter.Helpers.Paint;
import Chin_Hunter.Helpers.RequiredItem;
import Chin_Hunter.Hunter.Hunting;
import Chin_Hunter.Hunter.Trap_Admin.TrapError;
import Chin_Hunter.Hunter.Trap_Admin.TrapType;
import Chin_Hunter.States.ScriptState;
import Chin_Hunter.Executes.Eagles_Peak.QuestMain;
import org.jetbrains.annotations.Nullable;
import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.tab.*;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.event.listeners.ChatMessageListener;
import org.rspeer.runetek.event.listeners.MouseInputListener;
import org.rspeer.runetek.event.listeners.RenderListener;
import org.rspeer.runetek.event.types.ChatMessageEvent;
import org.rspeer.runetek.event.types.ChatMessageType;
import org.rspeer.runetek.event.types.RenderEvent;
import org.rspeer.script.Script;
import org.rspeer.script.ScriptCategory;
import org.rspeer.script.ScriptMeta;
import org.rspeer.ui.Log;

import java.awt.event.MouseEvent;
import java.util.Arrays;

@ScriptMeta(desc = "Start anywhere and it will do EVERYTHING required to hunt red chins.", developer = "Shteve", name = "AIO Chin Hunter", category = ScriptCategory.HUNTER, version = 0.1)
public class Main extends Script implements ChatMessageListener, RenderListener, MouseInputListener {

    private static boolean onStartCalled = false;
    private static ScriptState currentState = ScriptState.STARTING;
    private static ScriptState previousState;

    private static boolean buryBones = true;
    private static boolean trainHerblore = true;

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

    private static final int MIN_WALK_WAIT = 700;
    private static final int MAX_WALK_WAIT = 2000;

    private static final int TURN_RUN_ON_LIMIT = Random.high(50, 100);

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

        if (!Movement.isRunEnabled() && Movement.getRunEnergy() > TURN_RUN_ON_LIMIT) {
            Movement.toggleRun(true);
            Time.sleepUntil(Movement::isRunEnabled, 1500);
        }

        if (TrapError.isActive())
            TrapError.solveActiveError();
        else
            currentState.execute();

        return Random.nextInt(100, 350);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public static boolean canBuryBones(){
        return buryBones;
    }

    public static boolean canTrainHerblore(){
        return trainHerblore;
    }

    @Override
    public void notify(ChatMessageEvent chatMessageEvent) {
        String Message = chatMessageEvent.getMessage();
        if (chatMessageEvent.getType() == ChatMessageType.SERVER) {
            if (Message.contains("Congratulations, ")) {
                Log.fine(Message);
                onLevelUpEvent();
                return;
            }
            if (Message.equals("Oh dear, you are dead!")) {
                //On Death Event
                return;
            }
            if (Message.contains("You may set up only")){
                TrapError.setActiveError(new TrapError(TrapError.ErrorType.TRAP_LIMIT_REACHED, null));
                return;
            }
            if (Message.equals("This isn't your trap.")){
                TrapError.setActiveError(new TrapError(TrapError.ErrorType.TRAP_NOT_OURS, Hunting.getCurrentFocusedTrap().getLocation()));
                //return;
            }
        }
    }

    //region Paint

    @Override
    public void notify(RenderEvent e) {
        if (paint != null)
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

    /**
     * Teleports a player to piscatoris, must have teleport
     */
    public static void teleportToPiscatoris() {
        Item piscTele = Inventory.getFirst("Piscatoris teleport");
        if (piscTele != null && piscTele.interact("Teleport")) {
            Time.sleepUntil(Main::isAtPiscatoris, 8000);
        }
    }

    /**
     * Handles updating current script state and updating previousState.
     *
     * @param inState The state you wish to set or null to stop script.
     */
    public static void updateScriptState(@Nullable ScriptState inState) {
        previousState = currentState;
        if (inState == currentState) {
            Log.severe("Error: New script state same as previous.");
            return;
        }
        currentState = inState;
        onStartCalled = false;
        if (currentState != null)
            Log.info("State Updated to: " + currentState.name());

        //Clear museum data cache if not needed.
        if (currentState != ScriptState.MUSEUM_QUIZ)
            MuseumQuiz.clearQuizData();

    }

    public static int getHunterLevel(){
        //return 40;
        return Skills.getLevel(Skill.HUNTER);
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
                Time.sleepUntil(()->(!Players.getLocal().isMoving() && Players.getLocal().getAnimation() == -1)
                        || Players.getLocal().getPosition().equals(tile)
                        || (Players.getLocal().isHealthBarVisible() && !Movement.isRunEnabled() && Movement.getRunEnergy() > 5), mainSleep);
            return true;
        }
        return false;
    }

    public static boolean hasItems(RequiredItem[] requiredItems){
        return hasItems(requiredItems, null);
    }

    public static boolean hasItems(RequiredItem[] requiredItems, TrapType trapType) {
        for (RequiredItem requiredItem : requiredItems) {
            String itemName = requiredItem.getName();
            int reqAmount = requiredItem.getAmountRequired();

            Item[] invent = Inventory.getItems(x -> x.getName().equalsIgnoreCase(itemName)
                    && !x.isNoted());

            if (invent.length > 0){
                int layedTrapAdjustment = trapType != null && trapType.getName().equalsIgnoreCase(itemName) ? Hunting.getActiveTrapCount(): 0;
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
        Item[] itemsToDrop = Inventory.getItems(x->Arrays.asList(items).contains(x.getName()));
        for (Item item : itemsToDrop){
            Time.sleep(Random.low(196, 1200));
            if (canBuryBones() && item.containsAction("Bury") && item.interact("Bury")) {
                Time.sleep(500);
                Time.sleepUntil(() -> Players.getLocal().getAnimation() != 827, 2000);
                continue;
            }
            item.interact("Drop");
        }
    }


    public static void onLevelUpEvent() {
        Hunting.updateMaxTrapCount();

        ScriptState bestState = getBestHuntingState();
        //If we're level 9 hunter we've just finished the quiz
        //Leave it on the current state so it can switch to banking manually.
        if (getHunterLevel() == 9 && currentState == ScriptState.MUSEUM_QUIZ)
            return;

        if (bestState.name().equalsIgnoreCase("FALCON_KEBBITS"))
            FalconKebbits.updateTargetKebbits();

        if (!currentState.name().equals(bestState.name()))
            updateScriptState(bestState);
    }


    public static ScriptState getBestHuntingState() {
        int hunterLevel = getHunterLevel();
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

        if (canTrainHerblore()) {
            if (!Druidic_Ritual.questComplete())
                return ScriptState.DRUIDIC_RITUAL_QUEST;

            if (Skills.getLevel(Skill.HERBLORE) < 19)
                return ScriptState.HERBLORE_TRAINING;
        }

        return ScriptState.CHINCHOMPAS;

    }

    private static boolean toggleButtonClicked(MouseEvent mouseEvent, int lowX, int lowY, int highX, int highY){
        if (mouseEvent.getX() < lowX || mouseEvent.getX() > highX)
            return false;
        if (mouseEvent.getY() < lowY || mouseEvent.getY() > highY)
            return false;
        return true;
    }

    @Override
    public void notify(MouseEvent mouseEvent) {
        if (mouseEvent.getID() != MouseEvent.MOUSE_CLICKED)
            return;
        //Log.info("Mouse Clicked: " + mouseEvent.getX() + ", " + mouseEvent.getY());

        //Toggle Paint
        if (toggleButtonClicked(mouseEvent, 460, 349, 508, 369)){
            Paint.canDisplayPaint = !Paint.canDisplayPaint;
            return;
        }

        //Paint not being displayed, don't allow clicks to change settings.
        if (!Paint.canDisplayPaint)
            return;

        //Toggle Prayer
        if (toggleButtonClicked(mouseEvent, 10, 346, 39, 375)){
            buryBones = !buryBones;
            Log.fine(buryBones?"We will now bury bones.":"We will no longer bury bones.");
            return;
        }

        //Toggle Herb
        if (toggleButtonClicked(mouseEvent, 44, 345, 73, 375)) {
            trainHerblore = !trainHerblore;
            Log.fine(trainHerblore?"We will now train herblore to 19.":"We will no longer train herblore to 19.");
        }
    }

    //endregion
}
