package Chin_Hunter.Executes.Questing;

import Chin_Hunter.Executes.Questing.Puzzle_Rooms.BronzeFeather;
import Chin_Hunter.Executes.Questing.Puzzle_Rooms.GoldenFeather;
import Chin_Hunter.Executes.Questing.Puzzle_Rooms.SilverFeather;
import Chin_Hunter.Main;
import Chin_Hunter.States.ScriptState;
import org.rspeer.runetek.adapter.component.InterfaceComponent;
import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.Varps;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.Dialog;
import org.rspeer.runetek.api.component.tab.*;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.api.scene.SceneObjects;
import org.rspeer.ui.Log;

import java.util.function.BooleanSupplier;

class QuestTraveling {

    private static final Position EAGLES_PEAK_TILE = new Position(2328, 3496, 0);
    private static final Position CHARLIE_TILE = new Position(2607, 3264, 0);
    private static final Position VARROCK_TILE = new Position(3212, 3427, 0);
    private static final Position BASECAMP_TILE = new Position(2317, 3503, 0);

    //Close enough to peak that we shouldn't tele
    private static final Area NEAR_PEAK_AREA = Area.polygonal(
            new Position(2460, 3331, 0),
            new Position(2345, 3339, 0),
            new Position(2199, 3652, 0),
            new Position(2375, 3652, 0),
            new Position(2378, 3621, 0),
            new Position(2365, 3622, 0),
            new Position(2362, 3571, 0),
            new Position(2383, 3569, 0),
            new Position(2385, 3534, 0),
            new Position(2370, 3504, 0),
            new Position(2379, 3476, 0),
            new Position(2386, 3465, 0),
            new Position(2377, 3449, 0),
            new Position(2382, 3412, 0),
            new Position(2409, 3408, 0),
            new Position(2414, 3394, 0),
            new Position(2447, 3382, 0),
            new Position(2463, 3381, 0));

    private static final Area NEAR_VARROCK_AREA = Area.polygonal(
            new Position(3044, 3517, 0),
            new Position(3322, 3517, 0),
            new Position(3396, 3259, 0),
            new Position(3294, 3139, 0),
            new Position(2949, 3149, 0),
            new Position(2872, 3316, 0));



    enum PuzzleRoom{
        BRONZE_FEATHER(new Position(1986, 4949,3)),
        SILVER_FEATHER(new Position(1986, 4972, 3)),
        GOLDEN_FEATHER(new Position(2023, 4982, 3));

        PuzzleRoom(Position entranceTile){
            ENTRANCE_TILE = entranceTile;
        }
        private Position ENTRANCE_TILE;

        public Position getEntranceTile() {
            return ENTRANCE_TILE;
        }
    }

    static void travelToCharlie(){
        if (QuestAreas.isNearArdy()){
            Main.walkTo(CHARLIE_TILE);
            return;
        }
        if (hasNecklaceOfPassage()){
            teleportToOutpost();
            return;
        }
        //Bank for items like teleports
        Main.updateScriptState(ScriptState.BANKING);

    }

    static void travelToEaglesNest(){
        if (isInFeatherCave()){
            leaveFeatherCave();
            return;
        }
        if (QuestAreas.isPassedStoneDoor()){
            Npc Eagle = Npcs.getNearest("Eagle");
            if (Eagle == null){
                Log.severe("We're passed the stone door but can't find the eagle.");
                return;
            }
            if (Eagle.interact("Walk-past")){
                Time.sleepUntil(()->Players.getLocal().getAnimation() == -1
                        && QuestAreas.isAtEaglesNest(), 6000);
            }
            return;
        }
        if (!QuestAreas.isInCentralCave()){
            travelToMainCave();
            return;
        }

        SceneObject Door = SceneObjects.getNearest("Stone door");
        if (Door == null){
            Log.severe("Could not find Stone door?");
            return;
        }
        if (Door.distance(Players.getLocal()) > 5){
            Main.walkTo(Door.getPosition());
            return;
        }
        if (Varps.getBitValue(3107)==0){
            if (!useFeatherOnDoor("Bronze feather", Door))
                return;
            if (!useFeatherOnDoor("Silver feather", Door))
                return;
            if (!useFeatherOnDoor("Golden feather", Door))
                return;
            Log.severe("Don't have all the feathers in invent and door not open.");
            return;
        }
        if (Door.interact("Open"))
            Time.sleepUntil(QuestAreas::isPassedStoneDoor, 3500);

    }



    static void travelToMainCave(){
        if (QuestAreas.isPassedStoneDoor()){
            leavePassedDoorArea();
            return;
        }
        if (isInFeatherCave()){
            leaveFeatherCave();
            return;
        }
        if (QuestAreas.isAtPeak()){
            //Use feather on outcrop or just enter
            SceneObject[] Cave = SceneObjects.getAt(new Position(2328, 3494, 0));
            if (Cave.length == 0){
                Log.severe("Could not find cave entrance");
                return;
            }
            if (Cave[0].getName().equalsIgnoreCase("Rocky outcrop")){
                if (!Inventory.contains("Metal feather")){
                    Log.severe("Could not find Metal feather in invent?");
                    return;
                }
                if (Inventory.use(x->x.getName().equalsIgnoreCase("Metal feather"), Cave[0]))
                    Time.sleepUntil(()->Inventory.getCount("Metal feather") == 0, 2544);
                return;
            }
            if (Cave[0].getName().equalsIgnoreCase("Cave entrance")){
                if (Cave[0].interact("Enter"))
                    Time.sleepUntil(QuestAreas::isInCentralCave, 4344);
                return;
            }
            Log.severe("Cave entrance name not recognised?");
            return;
        }
        if (QuestAreas.isAtBasecamp() && Skills.getCurrentLevel(Skill.AGILITY) >= 25){
            usePeakAgilityShortcut();
            return;
        }
        if (!isNearPeak() && hasNecklaceOfPassage()){
            teleportToOutpost();
            return;
        }
        Main.walkTo(EAGLES_PEAK_TILE);
    }


    static void travelToBaseCamp(){
        if (isInFeatherCave()){
            leaveFeatherCave();
            return;
        }
        if (QuestAreas.isInCentralCave()){
            if (QuestAreas.isPassedStoneDoor()) {
                leavePassedDoorArea();
                return;
            }
            //Leave main cave
            SceneObject Exit = SceneObjects.getNearest(19891);
            if (Exit == null){
                Log.severe("Could not find cave exit.");
                return;
            }
            if (Exit.interact("Exit"))
                Time.sleepUntil(()->!QuestAreas.isInCentralCave(), Random.high(5000, 10000));
            return;
        }
        if (QuestAreas.isAtPeak() && Skills.getCurrentLevel(Skill.AGILITY) >= 25){
            usePeakAgilityShortcut();
            return;
        }
        if (!isNearPeak() && hasNecklaceOfPassage()){
            teleportToOutpost();
            return;
        }
        Main.walkTo(BASECAMP_TILE);
    }

    static void travelToFancyDressShop(){
        //Not in varrock, tele/walk to varrock
        if (!QuestAreas.isInVarrock()){
            Item varrockTele = Inventory.getFirst("Varrock teleport");
            if (varrockTele == null){
                //Try to home tele and then walk to varrock.
                if (!isNearVarrock()){
                    if (Magic.getBook() == Magic.Book.MODERN || Magic.getBook() == Magic.Book.ANCIENT) {
                        if (Magic.cast(Spell.Modern.HOME_TELEPORT)) {
                            Time.sleepUntil(QuestTraveling::isNearVarrock, 15000);
                            return;
                        }
                    }
                }
                Main.walkTo(VARROCK_TILE);
                return;
            }
            //Use varrock tele
            if (varrockTele.interact("Break"))
                Time.sleepUntil(QuestAreas::isInVarrock, 10000);
            return;
        }

        //Not in fenced area. Enter it.
        if (!QuestAreas.isInFencedVarrockArea()){
            Position gateTile = new Position(3264, 3405,0);
            SceneObject gate = SceneObjects.getNearest(x->x.getName().equalsIgnoreCase("Gate") && x.getPosition().equals(gateTile));
            if (gate == null){
                Main.walkTo(gateTile);
                return;
            }
            if (gate.interact("Open"))
                Time.sleepUntil(QuestAreas::isInFencedVarrockArea, 5000);
            return;
        }

        //Handle opening door to shop owner if needed.
        Position doorTile = new Position(3277, 3397,0);
        Npc shopOwner = Npcs.getNearest("Asyff");
        if (shopOwner == null){
            Log.severe("Could not find Asyff.");
            Main.walkTo(doorTile);
            return;
        }
        if (!shopOwner.isPositionInteractable()){
            SceneObject door = SceneObjects.getNearest(x->x.getName().equalsIgnoreCase("Door") && x.getPosition().equals(doorTile));
            if (door == null){
                Log.info("Could not find door to open?");
                return;
            }
            if (door.interact("Open"))
                Time.sleepUntil(shopOwner::isPositionInteractable, 3500);
        }
    }

    static void travelToFeatherRoom(PuzzleRoom Room){
        if (!QuestAreas.isInCentralCave()) {
            travelToMainCave();
            return;
        }

        SceneObject Tunnel = SceneObjects.getNearest(x->x.getName().equalsIgnoreCase("Tunnel")
                && x.getPosition().equals(Room.getEntranceTile()));

        if (Tunnel == null){
            Log.severe("Could not find tunnel entrance for cave: " + Room.name());
            Main.walkTo(Room.getEntranceTile());
            return;
        }
        Position startPos = Players.getLocal().getPosition();
        if (Tunnel.interact("Enter")){
            final BooleanSupplier enteredNewCave = () -> Players.getLocal().getPosition().distance(startPos) > 100;
            Time.sleepUntil(enteredNewCave, 5000);
            if (Players.getLocal().isMoving()) {
                //It can sometimes be a long ass walk so here is some janky ass sleeping.
                Time.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                if (!Players.getLocal().isMoving())
                    Time.sleepUntil(enteredNewCave, 1200);
            }
        }

    }

    private static boolean useFeatherOnDoor(String featherName, SceneObject Door){
        if (!Inventory.contains(featherName))
            return true;

        if (Inventory.use(x->x.getName().equalsIgnoreCase(featherName), Door))
            Time.sleepUntil(()->!Inventory.contains(featherName), 3000);

        return !Inventory.contains(featherName);
    }

    private static void usePeakAgilityShortcut(){
        SceneObject[] Shortcut = null;
        //Climb Down
        if (QuestAreas.isAtPeak())
            Shortcut = SceneObjects.getAt(new Position(2324, 3498, 0));

        //Climb Up
        if (QuestAreas.isAtBasecamp())
            Shortcut = SceneObjects.getAt(new Position(2322, 3501, 0));

        if (Shortcut == null){
            Log.severe("Not on the mountain or basecamp and trying to use the shortcut?");
            Time.sleep(2500);
            return;
        }
        if (Shortcut.length == 0){
            Log.severe("Could not find rocks to climb.");
            return;
        }

        if (Shortcut[0].interact("Climb")){
            Time.sleepUntil(()->Players.getLocal().getAnimation() != -1, 5000);
            if (Players.getLocal().getAnimation() != -1){
                Time.sleepUntil(()->Players.getLocal().getAnimation() == -1, 5000);
            }
        }
    }

    private static void leavePassedDoorArea(){
        if (QuestAreas.isAtEaglesNest()){
            //Leave eagles nest area, pass bird
            Npc Eagle = Npcs.getNearest("Eagle");
            if (Eagle == null){
                Log.severe("We're in the eagles nest but can't find the eagle.");
                return;
            }
            if (Eagle.interact("Walk-past")){
                Time.sleepUntil(()->Players.getLocal().getAnimation() == -1
                    && !QuestAreas.isAtEaglesNest(), 6000);
            }
            return;
        }
        if (QuestAreas.isPassedStoneDoor()){
            //"Open" "Stone door" (2003, 4948, 3)
            SceneObject Door = SceneObjects.getNearest("Stone Door");
            if (Door == null){
                Log.severe("Could not find stone door.");
                return;
            }
            if (Door.interact("Open"))
                Time.sleepUntil(()->!QuestAreas.isPassedStoneDoor(), 5256);
        }
    }

    private static boolean isInFeatherCave(){
        return BronzeFeather.isInCave() || SilverFeather.isInCave() || GoldenFeather.isInCave();
    }

    private static void leaveFeatherCave(){
        if (BronzeFeather.isInCave()){
            BronzeFeather.leaveCave();
            return;
        }
        if (SilverFeather.isInCave()){
            SilverFeather.leaveCave();
            return;
        }
        if (GoldenFeather.isInCave()) {
            GoldenFeather.leaveCave();
        }
    }

    private static boolean isNearVarrock(){
        return NEAR_VARROCK_AREA.contains(Players.getLocal());
    }

    private static boolean isNearPeak(){
        return NEAR_PEAK_AREA.contains(Players.getLocal());
    }

    private static boolean teleportToOutpost(){
        if (!hasNecklaceOfPassage()){
            Log.severe("Attempted teleporting to outpost without necklace.");
            return false;
        }

        if (Dialog.isOpen()){
            InterfaceComponent teleOption = Dialog.getChatOption(x->x.contains("The Outpost"));
            if (teleOption != null){
                if (teleOption.interact("Continue")){
                    Time.sleepUntil(QuestAreas::isAtOutpost, 8000);
                    return QuestAreas.isAtOutpost();
                }
                return false;
            }
            Log.info("Could not find 'The Outpost' option in dialog.");
        }

        Item necklace = Inventory.getFirst(x->x.getName().contains("Necklace of passage("));
        if (necklace != null){
            if (necklace.interact("Rub"))
                Time.sleepUntil(Dialog::isOpen, 2000);
            return false;
        }

        Item equippedNeck = EquipmentSlot.NECK.getItem();
        if (equippedNeck != null && equippedNeck.getName().contains("Necklace of passage(")){
            if (EquipmentSlot.NECK.interact("The Outpost")){
                Time.sleepUntil(QuestAreas::isAtOutpost, 8000);
                return QuestAreas.isAtOutpost();
            }
        }
        return false;
    }

    private static boolean hasNecklaceOfPassage(){
        Item necklace = Inventory.getFirst(x->x.getName().contains("Necklace of passage("));
        if (necklace != null)
            return true;
        necklace = EquipmentSlot.NECK.getItem();
        if (necklace == null)
            return false;
        return necklace.getName().contains("Necklace of passage(");
    }
}
