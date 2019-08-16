package Chin_Hunter.Executes.Hunting;

import Chin_Hunter.Helpers.RequiredItem;
import Chin_Hunter.Helpers.Trapping;
import Chin_Hunter.Main;
import Chin_Hunter.States.ScriptState;
import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.adapter.scene.PathingEntity;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.tab.EquipmentSlot;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.ui.Log;

public class Butterflies {

    private static final RequiredItem[] MINIMUM_REQUIRED_ITEMS = RequiredItem.concat(new RequiredItem[]{
            new RequiredItem("Butterfly net", 1),
            new RequiredItem("Butterfly jar", 1)
    }, Longtails.getMinimumRequiredItems());

    private static final RequiredItem[] REQUIRED_ITEMS = RequiredItem.concat(new RequiredItem[]{
            new RequiredItem("Butterfly net", 1),
            new RequiredItem("Butterfly jar", 4),
            new RequiredItem("Piscatoris teleport", 1),
            new RequiredItem("Varrock teleport", 1)
    }, Longtails.getRequiredItems());


    private static final String BUTTERFLY_NAME = "Ruby harvest";
    private static final Position CENTRE_TILE = new Position(2321, 3600, 0);

    private static final Area SMALLER_LONGTAIL_AREA = Area.rectangular(2308, 3596, 2318, 3588);

    //2321,3600,0

    private Butterflies(){
        //Private default constructor
    }

    public static void onStart(){
        Longtails.onStart();
        Longtails.setCentreTile(SMALLER_LONGTAIL_AREA.getTiles().get(Random.mid(0, SMALLER_LONGTAIL_AREA.getTiles().size())));
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
            Main.walkTo(CENTRE_TILE);
            return;
        }

        if (butterfly.interact("Catch"))
            Time.sleepUntil(Butterflies::isChasingAButterfly, 4000);
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



    public static RequiredItem[] getMinimumRequiredItems() {
        return MINIMUM_REQUIRED_ITEMS;
    }

    public static RequiredItem[] getRequiredItems() {
        return REQUIRED_ITEMS;
    }

    public static boolean haveMinimumRequiredItems() {
        return Main.hasItems(MINIMUM_REQUIRED_ITEMS, Trapping.TrapType.BIRD_SNARE);
    }

    public static boolean haveRequiredItems() {
        return Main.hasItems(REQUIRED_ITEMS);
    }

}
