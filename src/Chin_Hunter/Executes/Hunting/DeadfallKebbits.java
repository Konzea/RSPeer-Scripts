package Chin_Hunter.Executes.Hunting;

import Chin_Hunter.Helpers.Trapping;
import Chin_Hunter.Main;
import Chin_Hunter.States.ScriptState;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.api.scene.SceneObjects;
import org.rspeer.ui.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class DeadfallKebbits {

    private static final Map<String, Integer> MINIMUM_REQUIRED_ITEMS = new HashMap<>();
    private static final Map<String, Integer> REQUIRED_ITEMS = new HashMap<>();

    private static final Position DEADFALL_TRAP_TILE = new Position(2319, 3594, 0);
    private static final Predicate<SceneObject> DEADFALL_TRAP_PREDICATE = x -> (x.getName().equalsIgnoreCase("Boulder") || x.getName().equalsIgnoreCase("Deadfall")) && x.getPosition().equals(DEADFALL_TRAP_TILE);
    private static final String[] JUNK_ITEMS = {"Bones", "Kebbit spike"};
    private static final int SETTING_DEADFALL_ANIMATION = 5212;

    public static boolean deadfallIsOurs = false;

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
            Trapping.teleportToPiscatoris();
            return;
        }

        if (playerIsAnimating()) {
            Time.sleepUntil(() -> !playerIsAnimating(), 5000);
            return;
        }

        if (Butterflies.isChasingAButterfly()) {
            Butterflies.HuntButterflies();
            return;
        }

        if (!HuntDeadfallKebbits())
            return;

        if (!Longtails.HuntLongtails(Trapping.getMaxTrapCount() - 1))
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
            if (Longtails.HuntLongtails(Trapping.getMaxTrapCount() - 1))
                chopLogs();
            return false;
        }

        if (Main.inventContains(JUNK_ITEMS)) {
            Main.handleJunkItems(JUNK_ITEMS);
            return false;
        }

        if (Players.getLocal().getPosition().distance(DEADFALL_TRAP_TILE) > 24) {
            Main.walkTo(DEADFALL_TRAP_TILE);
            return false;
        }

        if (deadfallNeedsAttention() && deadfallIsOurs)
            return fixDeadfallTrap();
        return true;
    }

    private static boolean deadfallNeedsAttention() {
        SceneObject[] deadfallTrap = SceneObjects.getLoaded(DEADFALL_TRAP_PREDICATE);

        if (deadfallTrap.length == 0) {
            Log.info("Can't find deadfall trap...");
            Main.walkTo(DEADFALL_TRAP_TILE);
            return false;
        }

        if (deadfallTrap[0].getActions().length == 0)
            return false;

        if (deadfallTrap[0].containsAction("Set-trap"))
            deadfallIsOurs = true;

        return !deadfallTrap[0].containsAction("Dismantle");

    }

    private static boolean fixDeadfallTrap() {
        SceneObject[] deadfallTrap = SceneObjects.getLoaded(DEADFALL_TRAP_PREDICATE);

        if (deadfallTrap.length == 0) {
            Log.severe("Can't find deadfall trap...");
            Main.walkTo(DEADFALL_TRAP_TILE);
            return false;
        }

        if (deadfallTrap[0].getActions().length == 0){
            Log.severe("Fucking Deadfall has no actions?! WTF");
            long startTime = System.currentTimeMillis();
            Time.sleepUntil(()->deadfallTrap[0].getActions().length > 0, 1500);
            Log.info("Time taken for actions to appear: " + (System.currentTimeMillis() - startTime));
            return false;
        }

        if (deadfallTrap[0].containsAction("Check")) {
            int inventCount = Inventory.getCount();
            if (deadfallTrap[0].interact("Check"))
                Time.sleepUntil(() -> Inventory.getCount() != inventCount, 4000);
            return false;
        }

        if (Trapping.getPlacedTrapsCount() > Trapping.getMaxTrapCount() - 1){
            Log.severe("Attempting to set deadfall with max trap count already reached.");
            Trapping.pickUpAllTrapsExcept(Trapping.TrapType.BIRD_SNARE, Trapping.getMaxTrapCount() - 1);
            return false;
        }

        if (deadfallTrap[0].containsAction("Set-trap") && deadfallTrap[0].interact("Set-trap")){
            Time.sleepUntil(() -> Players.getLocal().getAnimation() == SETTING_DEADFALL_ANIMATION, 5000);
            if (Players.getLocal().getAnimation() == SETTING_DEADFALL_ANIMATION) {
                deadfallIsOurs = true;
                Time.sleepUntil(() -> Players.getLocal().getAnimation() == -1, 3000);
                return true;
            }
        }

        deadfallIsOurs = false;
        return false;

    }

    private static boolean isDeadfallSet() {
        SceneObject[] deadfallTrap = SceneObjects.getLoaded(DEADFALL_TRAP_PREDICATE);
        return deadfallTrap.length > 0 && !deadfallTrap[0].containsAction("Set-trap");
    }

    private static boolean playerIsAnimating() {
        int anim = Players.getLocal().getAnimation();
        if (!deadfallIsOurs && anim == SETTING_DEADFALL_ANIMATION)
            deadfallIsOurs = true;
        return anim == 879 || anim == SETTING_DEADFALL_ANIMATION;
    }

    private static boolean chopLogs() {
        //Get nearest Willow tree and if not null chop it and wait
        SceneObject tree = SceneObjects.getNearest("Evergreen");
        if (tree != null && tree.interact("Chop down")) {
            Time.sleepUntil(() -> Players.getLocal().getAnimation() != -1, 4000);
            return Players.getLocal().getAnimation() != -1;
        }
        return false;
    }

    /**
     * Buries bones and drops all other JUNK_ITEMS
     */



    public static void populateHashMaps() {
        if (MINIMUM_REQUIRED_ITEMS.isEmpty()) {
            MINIMUM_REQUIRED_ITEMS.put("Knife", 1);
            MINIMUM_REQUIRED_ITEMS.put("Bronze Axe", 1);
            Butterflies.getMinimumRequiredItems().forEach(MINIMUM_REQUIRED_ITEMS::put);
        }
        if (REQUIRED_ITEMS.isEmpty()) {
            REQUIRED_ITEMS.put("Knife", 1);
            REQUIRED_ITEMS.put("Bronze Axe", 1);
            REQUIRED_ITEMS.put("Piscatoris teleport", 1);
            REQUIRED_ITEMS.put("Varrock teleport", 1);
            Butterflies.getRequiredItems().forEach(REQUIRED_ITEMS::put);
        }
    }

    public static Map<String, Integer> getMinimumRequiredItems() {
        return MINIMUM_REQUIRED_ITEMS;
    }

    public static Map<String, Integer> getRequiredItems() {
        return REQUIRED_ITEMS;
    }

    public static boolean haveMinimumRequiredItems() {
        if (MINIMUM_REQUIRED_ITEMS.isEmpty()) {
            Log.severe("Hashmap not populated.");
            Main.updateScriptState(null);
            return false;
        }
        return Main.hasItems(MINIMUM_REQUIRED_ITEMS, Trapping.TrapType.BIRD_SNARE);
    }

    public static boolean haveRequiredItems() {
        if (REQUIRED_ITEMS.isEmpty()) {
            Log.severe("Hashmap not populated.");
            Main.updateScriptState(null);
            return false;
        }
        return Main.hasItems(REQUIRED_ITEMS);
    }

}
