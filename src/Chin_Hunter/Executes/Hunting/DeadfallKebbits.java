package Chin_Hunter.Executes.Hunting;

import Chin_Hunter.Helpers.RequiredItem;
import Chin_Hunter.Hunter.Hunting;
import Chin_Hunter.Hunter.Trap_Admin.LaidTrap;
import Chin_Hunter.Hunter.Trap_Admin.TrapType;
import Chin_Hunter.Main;
import Chin_Hunter.States.ScriptState;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.api.scene.SceneObjects;
import org.rspeer.ui.Log;

public class DeadfallKebbits {

    private static final RequiredItem[] MINIMUM_REQUIRED_ITEMS = RequiredItem.concat(new RequiredItem[]{
            new RequiredItem("Knife", 1),
            new RequiredItem("Bronze Axe", 1)
    }, Butterflies.getMinimumRequiredItems());

    private static final RequiredItem[] REQUIRED_ITEMS = RequiredItem.concat(new RequiredItem[]{
            new RequiredItem("Knife", 1),
            new RequiredItem("Bronze Axe", 1),
            new RequiredItem("Piscatoris teleport", 1),
            new RequiredItem("Varrock teleport", 1)
    }, Butterflies.getRequiredItems());


    private static final Position DEADFALL_TRAP_TILE = new Position(2319, 3594, 0);
    private static final String[] JUNK_ITEMS = {"Bones", "Kebbit spike"};
    private static final int SETTING_DEADFALL_ANIMATION = 5212;

    private DeadfallKebbits() {
        //Private default constructor
    }

    public static void onStart() {
        Butterflies.onStart();
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

        if (playerIsAnimating()) {
            Time.sleepUntil(() -> !playerIsAnimating(), Random.nextInt(4000, 6000));
            return;
        }

        if (Butterflies.isChasingAButterfly()) {
            Butterflies.HuntButterflies();
            return;
        }

        if (!HuntDeadfallKebbits())
            return;

        if (!Longtails.HuntLongtails())
            return;

        //Kebbits and Longtails taken care of, got some spare time to chop some logs.
        if (Inventory.getCount("Logs") < 5) {
            chopLogs();
            return;
        }

        Butterflies.HuntButterflies();

    }

    private static boolean HuntDeadfallKebbits() {
        if (!haveMinimumRequiredItems()) {
            Log.severe("Minimum required items not found");
            Main.updateScriptState(ScriptState.BANKING);
            return false;
        }

        if (Inventory.getCount("Logs") == 0) {
            //If we have no logs, sort out longtails then go chop some.
            if (Longtails.HuntLongtails())
                chopLogs();
            return false;
        }

        if (Players.getLocal().getPosition().distance(DEADFALL_TRAP_TILE) > 24) {
            Main.walkTo(DEADFALL_TRAP_TILE);
            return false;
        }

        LaidTrap trapToCheck = Hunting.getTrapToFix(TrapType.DEADFALL);
        if (trapToCheck != null) {
            trapToCheck.fixTrap();
            return false;
        }

        if (Hunting.canPlaceTrap() && Hunting.canPlaceTrapOnTile(TrapType.DEADFALL, DEADFALL_TRAP_TILE)){
            Hunting.Lay_Trap(TrapType.DEADFALL, DEADFALL_TRAP_TILE);
            return false;
        }

        if (Main.inventContains(JUNK_ITEMS)) {
            Main.handleJunkItems(JUNK_ITEMS);
            return false;
        }

        return true;
    }

    private static boolean playerIsAnimating() {
        int anim = Players.getLocal().getAnimation();
        return anim == 879 || anim == SETTING_DEADFALL_ANIMATION;
    }

    private static boolean chopLogs() {
        //Make room for logs first
        if (Main.inventContains(JUNK_ITEMS))
            Main.handleJunkItems(JUNK_ITEMS);

        //Get nearest Willow tree and if not null chop it and wait
        SceneObject tree = SceneObjects.getNearest("Evergreen");
        if (tree != null && tree.interact("Chop down")) {
            Time.sleepUntil(() -> Players.getLocal().getAnimation() != -1, Random.nextInt(3500, 5000));
            return Players.getLocal().getAnimation() != -1;
        }
        return false;
    }


    public static RequiredItem[] getMinimumRequiredItems() {
        return MINIMUM_REQUIRED_ITEMS;
    }

    public static RequiredItem[] getRequiredItems() {
        return REQUIRED_ITEMS;
    }

    public static boolean haveMinimumRequiredItems() {
        return Main.hasItems(MINIMUM_REQUIRED_ITEMS, TrapType.BIRD_SNARE);
    }

    public static boolean haveRequiredItems() {
        return Main.hasItems(REQUIRED_ITEMS);
    }

}
