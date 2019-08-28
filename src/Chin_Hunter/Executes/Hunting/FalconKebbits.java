package Chin_Hunter.Executes.Hunting;

import Chin_Hunter.Helpers.RequiredItem;
import Chin_Hunter.Main;
import Chin_Hunter.States.ScriptState;
import org.rspeer.runetek.adapter.component.InterfaceComponent;
import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.adapter.scene.PathingEntity;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.Dialog;
import org.rspeer.runetek.api.component.tab.EquipmentSlot;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.HintArrow;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.api.scene.SceneObjects;
import org.rspeer.ui.Log;

import java.util.Arrays;

public class FalconKebbits {

    private static final RequiredItem[] MINIMUM_REQUIRED_ITEMS = {
            new RequiredItem("Coins", 500)
    };

    private static final RequiredItem[] REQUIRED_ITEMS = {
            new RequiredItem("Coins", 2000),
            new RequiredItem("Piscatoris teleport", 1),
            new RequiredItem("Varrock teleport", 1)
    };

    private static final String[] JUNK_ITEMS = {"Spotted kebbit fur", "Bones", "Dark kebbit fur", "Logs",
            "Kebbit spike", "Bird snare", "Butterfly jar","Raw bird meat", "Orange feather", "Butterfly net",
            "Bronze axe", "Knife", "Ruby harvest"};

    private static final Position CENTRE_TILE = new Position(2376, 3587, 0);
    private static final Position NORTH_PEN_TILE = new Position(2370, 3618, 0);
    private static final Position STILE_TILE = new Position(2371,3620,0);
    private static final Position MATTHIAS_TILE = new Position(2375,3607,0);

    private static final String[] CHAT_OPTIONS = {"Could I have a go with your bird?", "Ok, that seems reasonable.", "Yes, please."};
    private static final String FALCON_NAME = "Gyr Falcon";
    private static final int FALCON_GLOVE_ID = 10024;
    private static final int EMPTY_FALCON_GLOVE_ID = 10023;

    private static String[] targetKebbitNames = {"Spotted kebbit", "Dark kebbit"};

    private static int timeWithoutBird = 0;
    private static boolean inNeedOfNewBird = false;


    private FalconKebbits() {
        //Private default constructor
    }

    public static void onStart() {

    }

    public static void execute() {
        if (!Main.isAtPiscatoris()) {
            if (!haveRequiredItems()) {
                Main.updateScriptState(ScriptState.BANKING);
                return;
            }
            Main.teleportToPiscatoris();
            return;
        }

        //Having bird means you are in area ready to hunt
        if (isGloveEquipped() && !inNeedOfNewBird) {
            huntKebbits();
            return;
        }

        if (inNeedOfNewBird && isIdleWithBird()){
            Log.fine("Grabbed a new bird.");
            inNeedOfNewBird = false;
            return;
        }

        if (!haveMinimumRequiredItems()) {
            Log.severe("Minimum required items not found");
            Main.updateScriptState(ScriptState.BANKING);
            return;
        }

        if (!isInPen()) {
            enterPen();
            return;
        }

        if (!canEquipBird() && !isWaitingForBird()){
            Log.info("Removing Gear");
            removeGear();
            return;
        }
        if (Dialog.isOpen()) {
            handleTalking();
            return;
        }

        talkToMatthias();
    }

    public static void updateTargetKebbits(){
        //57
        int hunterLevel = Main.getHunterLevel();
        if (hunterLevel >= 57)
            targetKebbitNames = new String[]{"Spotted kebbit", "Dark kebbit"};
        else
            targetKebbitNames = new String[]{"Spotted kebbit"};
    }

    private static void removeGear(){
        EquipmentSlot[] gear = {EquipmentSlot.MAINHAND,
                EquipmentSlot.OFFHAND,
                EquipmentSlot.HANDS};

        for (EquipmentSlot slot : gear){
            if (slot.getItem() == null)
                continue;
            if (slot.unequip())
                Time.sleep(Random.low(150, 500));
        }
        Time.sleepUntil(FalconKebbits::canEquipBird, 2000);
    }

    private static void enterPen(){
        SceneObject[] Stile = SceneObjects.getLoaded(x->x.getPosition().equals(STILE_TILE) && x.getName().equalsIgnoreCase("Stile"));
        //Climb-over

        if (Stile.length == 0){
            Main.walkTo(STILE_TILE);
            return;
        }
        if (Stile[0].distance(Players.getLocal()) > 10){
            Main.walkTo(STILE_TILE);
            return;
        }
        if (Stile[0].interact("Climb-over"))
            Time.sleepUntil(FalconKebbits::isInPen, 4000);
    }

    private static void huntKebbits() {

        if (Inventory.getCount() > 26) {
            Main.handleJunkItems(JUNK_ITEMS);
            return;
        }

        if (isIdleWithBird()) {
            if (Main.inventContains(JUNK_ITEMS) && Random.nextInt(0,10) == 1){
                Main.handleJunkItems(JUNK_ITEMS);
                return;
            }

            timeWithoutBird = 0;
            Npc kebbit = Npcs.getNearest(x->Arrays.asList(targetKebbitNames).contains(x.getName()));
            if (kebbit == null){
                Log.severe("Could not find a kebbit...");
                if (Players.getLocal().distance(CENTRE_TILE) <= 6) {
                    Time.sleep(2000, 6000);
                    return;
                }
                Main.walkTo(CENTRE_TILE);
                return;
            }
            if (kebbit.interact("Catch")) {
                Time.sleepUntil(()-> !isIdleWithBird(), 1000);
                Time.sleepUntil(() -> HintArrow.isPresent() || isIdleWithBird(), 4000);
            }
            return;
        }

        if (HintArrow.isPresent()) {
            PathingEntity arrowTarget = HintArrow.getTarget();
            Npc targetNpc = null;
            if (arrowTarget instanceof Npc)
                targetNpc = (Npc)arrowTarget;

            if (targetNpc == null) {
                Log.severe("Arrow found but it's not pointing at an NPC?");
                Time.sleep(5000);
                return;
            }
            if (targetNpc.interact("Retrieve"))
                Time.sleepUntil(FalconKebbits::isIdleWithBird, 4000);
            return;
        }

        //Been waiting for either a hint arrow or the bird to come back for 20 seconds...
        //Probably lost bird.
        if (timeWithoutBird > 20000){
            Log.severe("Not seen our bird for ~20 seconds. Lets grab a new one.");
            inNeedOfNewBird = true;
            return;
        }

        int sleepTime = Random.low(200, 1000);
        Time.sleep(sleepTime);
        timeWithoutBird = timeWithoutBird + sleepTime;

    }

    private static void talkToMatthias() {
        Npc matthias = Npcs.getNearest("Matthias");
        if (matthias == null){
            Log.severe("Could not find Matthias, walking to him");
            Main.walkTo(MATTHIAS_TILE);
            return;
        }
        if (matthias.interact("Talk-to"))
            Time.sleepUntil(Dialog::isOpen, 4000);
    }

    private static void handleTalking() {
        if (!Dialog.isOpen()) {
            Log.severe("Attempted to handle talking without an open dialog.");
            return;
        }

        //Click continue
        if (Dialog.canContinue()) {
            Dialog.processContinue();
            Time.sleep(50, 400);
            Time.sleepUntil(() -> !Dialog.isProcessing(), 2000);
            return;
        }

        //Click an option
        InterfaceComponent option = Dialog.getChatOption(x -> Arrays.asList(CHAT_OPTIONS).contains(x));
        if (option == null) return;
        if (option.interact("Continue")) {
            Time.sleep(50, 400);
            Time.sleepUntil(Dialog::canContinue, 2000);
        }
    }

    //region Boolean Checks

    private static boolean canEquipBird() {
        if (EquipmentSlot.MAINHAND.getItem() != null)
            return false;
        if (EquipmentSlot.HANDS.getItem() != null)
            return false;
        if (EquipmentSlot.OFFHAND.getItem() != null)
            return false;
        return true;
    }

    private static boolean isGloveEquipped() {
        Item mainhand = EquipmentSlot.MAINHAND.getItem();
        if (mainhand == null)
            return false;
        return mainhand.getName().equalsIgnoreCase("Falconer's glove");
    }

    private static boolean isInPen() {
        return CENTRE_TILE.isPositionWalkable() || NORTH_PEN_TILE.isPositionWalkable();
    }

    private static boolean isWaitingForBird(){
        if (EquipmentSlot.MAINHAND.getItem() == null)
            return false;
        int mainhandItemID = EquipmentSlot.MAINHAND.getItemId();
        return mainhandItemID == EMPTY_FALCON_GLOVE_ID;
    }

    private static boolean isIdleWithBird() {
        if (EquipmentSlot.MAINHAND.getItem() == null)
            return false;
        int mainhandItemID = EquipmentSlot.MAINHAND.getItemId();
        return mainhandItemID == FALCON_GLOVE_ID;
    }


    //endregion

    public static RequiredItem[] getMinimumRequiredItems() {
        return MINIMUM_REQUIRED_ITEMS;
    }

    public static RequiredItem[] getRequiredItems() {
        return REQUIRED_ITEMS;
    }

    public static boolean haveMinimumRequiredItems() {
        return Main.hasItems(MINIMUM_REQUIRED_ITEMS);
    }

    public static boolean haveRequiredItems() {
        return Main.hasItems(REQUIRED_ITEMS);
    }
}
