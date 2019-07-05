package Chin_Hunter;

import Chin_Hunter.Executes.MuseumQuiz;
import Chin_Hunter.States.ScriptState;
import Chin_Hunter.Executes.EaglesPeakQuest;
import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.api.commons.math.Random;
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

    private static boolean onStartCalled = false;
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

    private static final Area VARROCK_AREA = Area.rectangular(3071, 3518, 3295, 3334);
    private static final Area LUMBRIDGE_AREA = Area.rectangular(3210, 3233, 3234, 3204);
    private static final Area CAMELOT_AREA = Area.rectangular(2688, 3517, 2780, 3465);

    private static long startTime;
    public static int hunterStartXP;
    private static BufferedImage paint = null;

    @Override
    public void onStart() {
        startTime = System.currentTimeMillis();
        Log.fine("Running Chin Trapping by Shteve");
        try {
            paint = ImageIO.read(new URL("https://i.gyazo.com/855c72b587bd410ef71f2043befc9931.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (paint == null)
            Log.severe("Unable to load paint. Honestly your not missing much.");
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
        return Random.nextInt(100,350);
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
                Log.severe("Attempting to set a trap we can't put down.");
                updateScriptState(null);
            }
        }
    }

    @Override
    public void notify(RenderEvent e) {
        Graphics g = e.getSource();
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int y = 6;
        int x = 344;
        g2.setColor(Color.WHITE);
        if (paint != null)
            g2.drawImage(paint, null, x,y);

        int xpGained = Skills.getExperience(Skill.HUNTER) - hunterStartXP;
        long runTime = System.currentTimeMillis() - startTime;
        g2.drawString("Trap Count: " + Trapping.getPlacedTrapsCount(), x + 18, y + 51);
        g2.drawString("Kebbit Trap Placed: : " + DeadfallKebbits.deadfallIsOurs, x, y += 20);


        g2.drawString("Run Time: " + runTime, x,y);
        g2.drawString("XP Gained: " + xpGained, x, y);
        g2.drawString("Current State: " + currentState.name(), x, y);

        try {
            for (Position pos : Trapping.getTrapLocations())
                pos.outline(g2);
        }catch (ConcurrentModificationException ignored){ }
    }


    //region Public Methods

    public static boolean isAtFeldipHills(){
        return FELDIP_HILLS_AREA.contains(Players.getLocal());
    }
    public static boolean isAtPiscatoris(){
        return PISCATORIS_AREA.contains(Players.getLocal());
    }
    public static boolean isInVarrock(){return VARROCK_AREA.contains(Players.getLocal()); }
    public static boolean isInLumbridge(){ return LUMBRIDGE_AREA.contains(Players.getLocal());  }
    public static boolean isInCamelot(){ return CAMELOT_AREA.contains(Players.getLocal()); }


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
                Log.info("State Updated to: " + currentState.name());
            }
    }

    public static ScriptState getPreviousScriptState(){
        return previousState;
    }



    public static boolean hasItems(Map<String, Integer> map){
        Item[] items;
        for (Map.Entry<String, Integer> mapItem : map.entrySet()) {
            Item[] invent = Inventory.getItems(x->x.getName().equalsIgnoreCase(mapItem.getKey())
            && !x.isNoted());
            Item[] equipped = Equipment.getItems(x->x.getName().equalsIgnoreCase(mapItem.getKey()));

            //Equipped items and items in invent.
            items = Stream.concat(Arrays.stream(invent), Arrays.stream(equipped)).toArray(Item[]::new);
            if (getCount(items) < mapItem.getValue())
                return false;
        }
        return true;
    }

    public static int getCount(Item... items){
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


    /**
     * Checks for the best attack style and best target and updates if necessary.
     */
    public static void onLevelUpEvent(){
        //TODO Potentially rename method.

        if (!Movement.isRunEnabled())
            Movement.toggleRun(true);

        ScriptState bestState = getBestHuntingState();
        //If we're level 9 hunter we've just finished the quiz
        //Leave it on the current state so it can switch to banking manually.
        if (Skills.getLevel(Skill.HUNTER) == 9){
            if (currentState == ScriptState.MUSEUM_QUIZ)
                return;
            updateScriptState(ScriptState.BANKING);
            return;
        }

        if (currentState != bestState) {
            updateScriptState(bestState);
        }
    }


    public static ScriptState getBestHuntingState(){
        int hunterLevel = Skills.getLevel(Skill.HUNTER);
//TODO BUTTERFLIES
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

        if (EaglesPeakQuest.questComplete())
            return ScriptState.CHINCHOMPAS;
        else
            return ScriptState.EAGLES_PEAK_QUEST;

    }


    //endregion
}
