package Chin_Hunter.Executes.Hunting;

import Chin_Hunter.Helpers.Trapping;
import Chin_Hunter.Main;
import Chin_Hunter.States.ScriptState;
import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.adapter.scene.PathingEntity;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.tab.EquipmentSlot;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.ui.Log;

import java.util.HashMap;
import java.util.Map;

public class Butterflies {

    private static final Map<String, Integer> MINIMUM_REQUIRED_ITEMS = new HashMap<>();
    private static final Map<String, Integer> REQUIRED_ITEMS = new HashMap<>();

    private static final String BUTTERFLY_NAME = "Ruby harvest";
    private static final Position CENTRE_TILE = new Position(2321, 3600, 0);

    private static final Position LONGTAIL_CENTRE_TILE = new Position(2312, 3595,0);

    //2321,3600,0

    private Butterflies(){
        //Private default constructor
    }

    public static void onStart(){
        Longtails.onStart();
        Longtails.setCentreTile(LONGTAIL_CENTRE_TILE);
    }

    public static void execute(){
        if (!Main.isAtPiscatoris()) {
            if (!haveRequiredItems()) {
                Main.updateScriptState(ScriptState.BANKING);
                return;
            }
            Trapping.teleportToPiscatoris();
            return;
        }

        if (isChasingAButterfly()){
            HuntButterflies();
            return;
        }

        if (Longtails.HuntLongtails())
            HuntButterflies();

    }

    static void HuntButterflies(){
        if (!haveMinimumRequiredItems()) {
            Log.severe("Minimum required items not found");
            Main.updateScriptState(ScriptState.BANKING);
            return;
        }

        if (!Movement.isRunEnabled() && Movement.getRunEnergy() > 50)
            Movement.toggleRun(true);

        if (!butterflyNetEquipped()) {
            equipButterflyNet();
            return;
        }

        if (Inventory.contains("Ruby harvest")) {
            releaseButterflies();
            return;
        }

        if (!isChasingAButterfly()){
            catchButterfly();
            return;
        }
        Time.sleepUntil(()->!isChasingAButterfly(), 4000);

    }

    private static void catchButterfly(){
        Npc butterfly = Npcs.getNearest(BUTTERFLY_NAME);
        if (butterfly == null) {
            Log.severe("Could not find a butterfly, walking to butterflies.");
            Movement.walkTo(CENTRE_TILE);
            Time.sleep(233, 633);
            return;
        }

        if (butterfly.interact("Catch"))
            Time.sleepUntil(Butterflies::isChasingAButterfly, 2000);
    }

    private static void releaseButterflies(){
        Inventory.accept(x -> x.getName().contains(BUTTERFLY_NAME), x -> {
            if (x.interact("Release"))
                Time.sleep(156, 513);
        });
    }

    static boolean isChasingAButterfly(){
        PathingEntity target = Players.getLocal().getTarget();
        return target != null && target.getName().equalsIgnoreCase(BUTTERFLY_NAME);
    }

    private static void equipButterflyNet(){
        Item butterflyNet = Inventory.getFirst("Butterfly net");
        if (butterflyNet != null && butterflyNet.interact("Wield"))
            Time.sleepUntil(Butterflies::butterflyNetEquipped, 2000);
    }

    private static boolean butterflyNetEquipped(){
        Item mainhandItem = EquipmentSlot.MAINHAND.getItem();
        return mainhandItem != null && mainhandItem.getName().equalsIgnoreCase("Butterfly net");
    }

    public static void populateHashMaps(){
        if (MINIMUM_REQUIRED_ITEMS.isEmpty()) {
            MINIMUM_REQUIRED_ITEMS.put("Butterfly net", 1);
            MINIMUM_REQUIRED_ITEMS.put("Butterfly jar", 1);
            Longtails.getMinimumRequiredItems().forEach(MINIMUM_REQUIRED_ITEMS::put);

        }
        if (REQUIRED_ITEMS.isEmpty()){
            REQUIRED_ITEMS.put("Butterfly net", 1);
            REQUIRED_ITEMS.put("Butterfly jar", 4);
            REQUIRED_ITEMS.put("Piscatoris teleport", 1);
            REQUIRED_ITEMS.put("Varrock teleport", 1);
            Longtails.getRequiredItems().forEach(REQUIRED_ITEMS::put);
        }
    }

    public static Map<String, Integer> getMinimumRequiredItems(){
        return MINIMUM_REQUIRED_ITEMS;
    }

    public static Map<String, Integer> getRequiredItems(){
        return REQUIRED_ITEMS;
    }

    public static boolean haveMinimumRequiredItems(){
        if (MINIMUM_REQUIRED_ITEMS.isEmpty()) {
            Log.severe("Hashmap not populated.");
            Main.updateScriptState(null);
            return false;
        }
        return Main.hasItems(MINIMUM_REQUIRED_ITEMS, Trapping.TrapType.BIRD_SNARE);
    }

    public static boolean haveRequiredItems(){
        if (REQUIRED_ITEMS.isEmpty()) {
            Log.severe("Hashmap not populated.");
            Main.updateScriptState(null);
            return false;
        }
        return Main.hasItems(REQUIRED_ITEMS);
    }

}
