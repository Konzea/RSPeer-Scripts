package Chin_Hunter.Executes.Questing;

import Chin_Hunter.Executes.Questing.Puzzle_Rooms.BronzeFeather;
import Chin_Hunter.Executes.Questing.Puzzle_Rooms.GoldenFeather;
import Chin_Hunter.Executes.Questing.Puzzle_Rooms.SilverFeather;
import Chin_Hunter.Main;
import Chin_Hunter.States.ScriptState;
import org.rspeer.runetek.adapter.component.InterfaceComponent;
import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.adapter.scene.Player;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.Game;
import org.rspeer.runetek.api.Varps;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Distance;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.Dialog;
import org.rspeer.runetek.api.component.Interfaces;
import org.rspeer.runetek.api.component.tab.EquipmentSlot;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.api.scene.SceneObjects;
import org.rspeer.ui.Log;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class QuestMain {

    private static final Map<String, Integer> REQUIRED_ITEMS = new HashMap<>();
    private static final Map<String, Integer> BIRD_CLOTHES_ITEMS = new HashMap<>();

    private static boolean allFeathersCollected = false;

    /*

     */

    private QuestMain(){
        //Private default constructor

        //If has items for required part of quest

        //Tele to varrock/Bank
        //Get items
        //Tele to cammy
        //Walk to ardy
        //Talk-to Charlie (2606, 3265, 0)
            //Option 1: Ah, you sound like someone who needs a quest doing!
            //Option 1: Sure. Any idea where I should start looking?
        //Varp 934: 0 -> 5: Quest Started
        //Teleport to outpost using necklace
            //Outpost Tile (2432, 3349, 0)
        //Walk to camp (2315, 3506, 0)
        //"Inspect" "Books" (2319, 3506, 0)
            //Wait until "Bird book" is in invent
        //"Read" "Bird book"
            //Wait until invent contains "Metal feather"
        //Varp 934: 5 -> 10: Got metal feather
        //After Opening book, interface 27 open
            //27,161 click to close.
        //"Climb" "Rocks" (2322, 3501, 0)
            //Wait until on mountain - Define this area?
        //Use "Metal feather" on "Rocky outcrop" (2328, 3494, 0)
        //Varp 934: 10 -> 15: Door to cave opened
        //"Enter" "Cave entrance" (2328, 3494, 0)
            //Wait until in cave (1994, 4983, 3)
        //Walk to 2010, 4974, 3
        //"Take" "Giant feathers" (2003, 4969, 3)
        //Varp 935: 0->8 : Cutscene started
        //Varp 935: 8->16: Cutcene finished
        //Select option 1: The Ardougne zoo keeper sent me to find you.
        //Select option 1: Well if you gave me a ferret I could take it back for you.
        //Select option 1: Could I help at all?
        //Varp 935: 16 -> 24: Finished talking to the guy
        //Make sure to take 10 "Eagle feather"


        //--> Need, feathers, 50gp, swamp tar, yellow dye <--
        //Tele to varrock
        //"Open" "Gate" (3264,3405, 0)
        //Potentially "Open" "Door" (3277, 3397,0)
        //"Talk-to" "Fancy dress shop owner"
        //Option 2: Well, specifically I'm after a couple of bird costumes.
        //935: 24->32: Shop guy has asked for items
        //"Talk-to" "Fancy dress shop owner"
        //Options 2: I've got the feathers and materials you requested
        //Option 1: Okay, here are the materials. Eagle me up.
        //Varp 935: 32->40: Got eagle costume


        //Teleport to outpost using necklace
            //Outpost Tile (2432, 3349, 0)
        //Walk to camp (2315, 3506, 0)
        //"Climb" "Rocks" (2322, 3501, 0)
            //Wait until on mountain - Define this area?
        //"Enter" "Cave entrance" (2328, 3494, 0)
            //Wait until in cave (1994, 4983, 3)
        //Walk to (1986, 4949,3)

        //"Enter" "Tunnel" (1986, 4949,3)
            //Wait until near tile (11846, 9284, 2)
        //Varp 935: 40->296: Entered cave, not grabbed feather yet
        //"Take-from" "Pedestal" (8005, 9482, 2)
            //Sleep until varp change, 10000
        //Varp 934: 15->79: Feather trap triggered
        //"Operate" "Winch" 1: (8002,9487,2) Varpbit 3101 0->1
        //"Operate" "Winch" 2: (8010,9487,2) Varpbit 3102 0->1
        //"Operate" "Winch" 3: (8010,9478,2) Varpbit 3103 0->1
        //"Operate" "Winch" 4: (8002,9478,2) Varpbit 3104 0->1
        //Varpbit 3105 -> All leavers done
        //"Take-from" "Stone pedestal" (8006, 9483, 2)
        //Gets bronze feather
        //Exit room -> "Enter" "Tunnel" (8006,9475, 2)
            //Wait until back in main area

        //"Enter" "Tunnel" (1986, 4972, 3)
            //Wait until near tile (7259, 10820,2)
        //Varp 935: 296->424: Entered cave: not grabbed feather yet
        //"Inspect" "Stone pedestal" (7259,10825,2)
            //Sleep until varp change, 10000
        //Varpbit 3099: 0->1: Trap triggered
        //"Inspect" "Rocks" (7273, 10827, 2)
        //Varpbit 3099: 1->2: First rock checked
        //"Inspect" "Rocks" (7279, 10831, 2)
        //Varpbit 3099: 2->3: Second rock checked
        //"Inspect" "Opening" (7283,10838,2)
            //Wait until varp change the sleep 5 secs
        //Varpbit 3099: 3->4: Kebbit released
        //"Threaten" npc "Kebbit"
        //Option 1: "Taunt the kebbit."
        //Varpbit 3099: 4->5: Feather dropped
        //"Take" "Silver feather" from ground
        //Exit room -> "Enter" "Tunnel" (12443,5815,2)
            //Wait until back in main area

        //"Pull" "Reset Level" (2022, 4981, 3) to reset puzzle
        //"Enter" "Tunnel" (2023, 4982, 3)
            //Wait until near tile (9573, 8916,2)
        //Varp 935: 424->488: Enterd tunnel for the first time
        //"Take-from" 6 "Odd bird seed" from "Birdseed holder" (9574, 8914, 2)
        //"Pull-down" "Eagle lever" (9559, 8919, 2)
        //Varpbit 3089: 0->4: 1 Level Pulled
        //Varpbit 3092: 0->1: ^
        //"Use" "Odd bird seed" on "Bird feeder" (9582, 8898,2)
            //Sleep 3-5 seconds
        //Varpbit 3089: 4->260: 2 Bird seed place
        //Varpbit 3098: 0->1: ^
        //"Use" "Odd bird seed" on "Bird feeder" (9578,8902,2)
            //Sleep 3-5 seconds
        //Varpbit 3089: 260->388: 3 Bird seed place
        //Varpbit 3097: 0->1: ^
        //"Pull-down" "Eagle lever" (9594,8899,2)
        //Varpbit 3089: 388->396: 4 Level pulled
        //Varpbit 3093: 0->1: ^
        //"Push-up" "Eagle lever" (9559, 8919, 2)
        //Varpbit 3089: 396->392: 5 Level pulled
        //Varpbit 3092: 1->0: ^
        //"Use" "Odd bird seed" on "Bird feeder" (9563, 8906, 2)
            //Sleep 7-8 seconds
        //Varpbit 3089: 392->424: 6 Bird seed place
        //Varpbit 3095: 0->1: ^
        //"Pull-down" "Eagle lever" (9551, 8910, 2)
        //Varpbit 3089: 424->425: 7 Level Pulled
        //Varpbit 3090: 0->1: ^
        //"Use" "Odd bird seed" on "Bird feeder" (9561, 8923, 2)
            //Sleep 5-7 seconds
        //Varpbit 3089: 425->441: 8 Bird seed place
        //Varpbit 3094: 0->1: ^
        //"Pull-down" "Eagle lever" (9541, 8923, 2)
        //Varpbit 3089: 441->443: 9 Level Pulled
        //Varpbit 3091: 0->1: ^
        //"Use" "Odd bird seed" on "Bird feeder" (9551, 8905, 2)
            //Sleep 5-7 seconds
        //Varpbit 3089: 443->411: 10 Bird seed place
        //Varpbit 3095: 0->1: ^
        //"Use" "Odd bird seed" on "Bird feeder" (9563, 8906, 2)
            //Sleep 4-5 seconds
        //Varpbit 3089: 411->475: 11 Bird seed place
        //Varpbit 3096: 0->1: ^
        //"Take-from" "Stone pedestal" (9544, 8915, 2)
            //Wait until invent contains golden feather
        //leave tunnel: "Enter" "Tunnel" (9573, 8917, 2)

        //Walk to (2002, 4948, 3)
        //"Wear" "Eagle cape"
        //"Wear" "Fake beak"
        //"Use" "Bronze feather" on "Stone door" (2003, 4948, 3)
        //"Use" "Silver feather" on "Stone door" (2003, 4948, 3)
        //"Use" "Gold feather" on "Stone door" (2003, 4948, 3)
        //Varpbit 3107: 0->1: Door able to be opened
        //"Open" "Stone door" (2003, 4948, 3)
        //Can reach (2005, 4949,2) -> In eagle bit
        //"Walk-past" npc "Eagle"
            //Sleep 4 secons
        //can reach (2007, 4959, 3) -> can talk to nick
        //"Talk-to" npc "Nickolaus"
        //Varpbit 2780: 20->25: Given gear and ready to go
        //"Walk-past" npc "Eagle"
            //Sleep 4 secons
        //"Open" "Stone door" (2003, 4948, 3)

        //"Exit" "Cave entrance" (1993, 4983, 3)
        //"Climb" "Rocks" (2324, 3498, 0)
            //Anim 740 down, 4435 up + 819 & 8120
        //"Talk-to" "Nickolaus"
        //Chat Option 1
        //Chat Option 1
        //Enters a cutscene with chat. Just keep skipping chat
        //Varpbit 2780: 30->35: Got ferret

        //Tele to cammy
        //Walk to ardy
        //Talk-to Charlie (2606, 3265, 0)
        //Varpbit 2780: 35->40: Quest complete

    }

    public static void onStart(){

    }

    public static void execute(){
        if (isValidDialogOpen()){
            if (Dialog.isProcessing()) {
                Time.sleepUntil(() -> !Dialog.isProcessing(), 2000);
                return;
            }
            if (Dialog.canContinue()){
                if (Dialog.processContinue())
                    Time.sleepUntil(Dialog::isProcessing, Random.nextInt(133, 500));
                return;
            }
        }else if (Game.isInCutscene()) {
            //Else is here so we can handle dialog within cut scenes
            Time.sleepUntil(() -> !Game.isInCutscene(), 5000);
            return;
        }

        switch (Varps.get(934)){
            case 0:{
                if (!QuestAreas.isInArdyZoo()){
                    QuestTraveling.travelToCharlie();
                    return;
                }
                if (isValidDialogOpen()) {
                    selectDialogOption(1);
                    return;
                }
                initiateCharlieConversation();
                return;
            }

            case 5:{
                if (!QuestAreas.isAtBasecamp()) {
                    QuestTraveling.travelToBaseCamp();
                    return;
                }
                Item Book = Inventory.getFirst("Bird book");
                if (Book != null){
                    if (Book.interact("Read"))
                        Time.sleepUntil(()->Inventory.contains("Metal feather"), 3000);
                    return;
                }

                SceneObject Books = SceneObjects.getNearest(x->x.getName().equalsIgnoreCase("Books")
                        && x.getPosition().equals(new Position(2319,3506,0)));
                if (Books == null){
                    Log.severe("At basecamp but could not find books on the floor?");
                    return;
                }
                int inventCount = Inventory.getCount();
                if (Books.interact("Inspect")){
                    Time.sleepUntil(()-> Inventory.getCount() != inventCount, 4000);
                    return;
                }
                return;
            }

            case 10:{
                InterfaceComponent CloseBookInterface = Interfaces.getComponent(27, 162);
                if (CloseBookInterface != null){
                    if (CloseBookInterface.click()){
                        Time.sleepUntil(()->Interfaces.getComponent(27, 162) == null, 2500);
                    }
                    return;
                }
                QuestTraveling.travelToMainCave();
                return;
            }

            case 15:{
                switch (Varps.get(935)){
                    case 0:{
                        if (!QuestAreas.isInCentralCave()){
                            QuestTraveling.travelToMainCave();
                            return;
                        }
                        if (!pickUpFeathers(10))
                            return;

                        shoutToNickolaus();
                        return;
                    }
                    case 8:{
                        if (!QuestAreas.isInCentralCave()){
                            QuestTraveling.travelToMainCave();
                            return;
                        }
                        if (!Dialog.isOpen())
                            shoutToNickolaus();
                        return;
                    }
                    case 16:{
                        if (!QuestAreas.isInCentralCave()){
                            QuestTraveling.travelToMainCave();
                            return;
                        }
                        if (isValidDialogOpen()) {
                            selectDialogOption(1);
                            return;
                        }
                        shoutToNickolaus();
                        return;
                    }
                    case 24:{
                        if (!pickUpFeathers(10))
                            return;
                        if (!QuestAreas.isInVarrock()){
                            QuestTraveling.travelToFancyDressShop();
                            return;
                        }

                        if (!Main.hasItems(BIRD_CLOTHES_ITEMS)){
                            Log.severe("Don't have required quest items, checking bank for them.");
                            if (Inventory.getCount("Eagle feather") < 10){
                                Log.severe("Don't have eagle feathers. Should have picked up 10 earlier.");
                                Log.info("Really fucking hope we have some in the bank otherwise your fucked...");
                            }
                            Main.updateScriptState(ScriptState.BANKING);
                            return;
                        }

                        if (!QuestAreas.isInFencedVarrockArea()) {
                            QuestTraveling.travelToFancyDressShop();
                            return;
                        }

                        if (!isValidDialogOpen()) {
                            initiateShopOwnerConversation();
                            return;
                        }
                        selectDialogOption(2);
                        return;
                    }
                    case 32:{
                        if (!isValidDialogOpen()) {
                            initiateShopOwnerConversation();
                            return;
                        }
                        String[] validResponses = {"I've got the feathers and materials you requested",
                            "Okay, here are the materials. Eagle me up."};

                        selectDialogOption(validResponses);
                        return;
                    }
                    case 40:{
                        QuestTraveling.travelToFeatherRoom(QuestTraveling.PuzzleRoom.BRONZE_FEATHER);
                        return;
                    }
                    case 296:{
                        if (!BronzeFeather.isInCave()){
                            QuestTraveling.travelToFeatherRoom(QuestTraveling.PuzzleRoom.BRONZE_FEATHER);
                            return;
                        }
                        BronzeFeather.attemptToSolve();
                        return;
                    }
                }
            }
            default:{
                switch (Varps.get(935)) {
                    case 296:{
                        if (!BronzeFeather.isInCave()){
                            if (BronzeFeather.isComplete()){
                                QuestTraveling.travelToFeatherRoom(QuestTraveling.PuzzleRoom.SILVER_FEATHER);
                                return;
                            }
                            QuestTraveling.travelToFeatherRoom(QuestTraveling.PuzzleRoom.BRONZE_FEATHER);
                            return;
                        }
                        if (BronzeFeather.isComplete()){
                            BronzeFeather.leaveCave();
                            return;
                        }
                        if (BronzeFeather.attemptToSolve())
                            Log.fine("Bronze feather puzzle solved.");
                        return;
                    }
                    case 424:{
                        //If in silver feather cave
                        if (!SilverFeather.isInCave()){
                            if (SilverFeather.isComplete()){
                                QuestTraveling.travelToFeatherRoom(QuestTraveling.PuzzleRoom.GOLDEN_FEATHER);
                                return;
                            }
                            QuestTraveling.travelToFeatherRoom(QuestTraveling.PuzzleRoom.SILVER_FEATHER);
                            return;
                        }
                        if (SilverFeather.isComplete()){
                            SilverFeather.leaveCave();
                            return;
                        }
                        if (SilverFeather.attemptToSolve())
                            Log.fine("Silver feather puzzle solved.");
                        return;
                    }
                    case 488:{
                        switch (Varps.getBitValue(2780)){
                            case 15:{
                                //If in gold kebbit room
                                if (!GoldenFeather.isComplete() && !allFeathersCollected) {
                                    if (!GoldenFeather.isInCave()) {
                                        QuestTraveling.travelToFeatherRoom(QuestTraveling.PuzzleRoom.GOLDEN_FEATHER);
                                        return;
                                    }
                                    if (GoldenFeather.attemptToSolve())
                                        Log.fine("Golden feather puzzle solved. Legit amazed we did that one...");
                                    return;
                                }
                                allFeathersCollected = true;
                                if (GoldenFeather.isInCave()){
                                    GoldenFeather.leaveCave();
                                    return;
                                }

                                if (!eagleGearEquipped()) {
                                    equipEagleGear();
                                    return;
                                }

                                if (!QuestAreas.isAtEaglesNest())
                                    QuestTraveling.travelToEaglesNest();
                                return;
                            }
                            case 20:{
                                if (!QuestAreas.isAtEaglesNest())
                                    QuestTraveling.travelToEaglesNest();

                                Npc Nickolaus = Npcs.getNearest("Nickolaus");

                                if (Nickolaus == null){
                                    Log.severe("Could not find nick when at the nest?");
                                    return;
                                }
                                if (Nickolaus.interact("Talk-to"))
                                    Time.sleepUntil(Dialog::isOpen, 2500);
                                return;
                            }
                            case 25:{
                                if (!QuestAreas.isAtBasecamp()) {
                                    QuestTraveling.travelToBaseCamp();
                                    return;
                                }
                                return;
                            }
                            case 30:{
                                if (!QuestAreas.isAtBasecamp()) {
                                    QuestTraveling.travelToBaseCamp();
                                    return;
                                }
                                if (!Dialog.isOpen()){
                                    Npc Nickolaus = Npcs.getNearest("Nickolaus");
                                    if (Nickolaus == null){
                                        Log.severe("Could not find nick when at basecamp?");
                                        return;
                                    }
                                    if (Nickolaus.interact("Talk-to"))
                                        Time.sleepUntil(Dialog::isOpen, 2500);
                                }

                                selectDialogOption(1);
                                return;
                            }
                            case 35:{
                                if (!QuestAreas.isInArdyZoo()){
                                    QuestTraveling.travelToCharlie();
                                    return;
                                }
                                if (isValidDialogOpen()) {
                                    selectDialogOption(1);
                                    return;
                                }
                                initiateCharlieConversation();
                                return;
                            }
                            case 40:{
                                InterfaceComponent Close = Interfaces.getComponent(277,15);
                                if (Close != null){
                                    if (Close.click())
                                        Time.sleepUntil(()->Interfaces.getComponent(277,15) == null, 2000);
                                    return;
                                }
                                Log.fine("Eagles Peak Completed! Onto Chins.");
                                Main.updateScriptState(ScriptState.CHINCHOMPAS);
                            }
                            default:{
                                Log.severe("Unknown 2780 Varpbit Value: " + Varps.getBitValue(2780));
                                Time.sleep(10000);
                            }
                        }

                    }
                }

            }
        }
    }

    private static void equipEagleGear() {
        Item Cape = Inventory.getFirst("Eagle cape");
        if (Cape != null && Cape.interact("Wear"))
            Time.sleepUntil(() -> Inventory.getCount("Eagle cape") < 2, 2000);

        Item Beak = Inventory.getFirst("Fake beak");
        if (Beak != null && Beak.interact("Wear"))
            Time.sleepUntil(() -> Inventory.getCount("Fake beak") < 2, 2000);
    }

    private static boolean eagleGearEquipped() {
        Item Cape = EquipmentSlot.CAPE.getItem();
        if (Cape == null || !Cape.getName().equalsIgnoreCase("Eagle cape"))
            return false;

        Item Head = EquipmentSlot.HEAD.getItem();
        if (Head == null || !Head.getName().equalsIgnoreCase("Fake beak"))
            return false;
        return true;
    }


    private static final Area ROMEO_ZONE = Area.rectangular(3198, 3443, 3228, 3418);
    private static boolean isValidDialogOpen(){
        if (!Dialog.isOpen())
            return false;
        return !ROMEO_ZONE.contains(Players.getLocal());
    }

    private static boolean shoutToNickolaus(){
        Position tileToStand = new Position(2005, 4967,3);
        Player Local = Players.getLocal();
        if (Players.getLocal().distance(tileToStand) > 100){
            QuestTraveling.travelToMainCave();
            return false;
        }
        if (!Local.getPosition().equals(tileToStand)){
            if (Movement.walkTo(tileToStand)){
                Time.sleep(500, 1500);
                if (Local.distance(tileToStand) < 5)
                    Time.sleepUntil(()->!Local.isMoving(), 2000);
            }
            return false;
        }
        Npc Nickolaus = Npcs.getNearest("Nickolaus");
        if (Nickolaus == null){
            Log.severe("Could not find my boy Nick.");
            return false;
        }
        if (Nickolaus.interact("Shout-to")) {
            Time.sleepUntil(Dialog::isOpen, 5000);
            return true;
        }
        return false;
    }

    private static boolean initiateShopOwnerConversation(){
        if (isValidDialogOpen())
            return true;

        Npc shopOwner = Npcs.getNearest(x->x.getName().equalsIgnoreCase("Asyff")
                && x.isPositionInteractable());
        if (shopOwner == null){
            QuestTraveling.travelToFancyDressShop();
            return false;
        }
        if (shopOwner.interact("Talk-to"))
            Time.sleepUntil(Dialog::isOpen, 4000);
        return isValidDialogOpen();
    }

    private static boolean initiateCharlieConversation(){
        if (isValidDialogOpen())
            return true;

        Npc Charlie = Npcs.getNearest("Charlie");
        if (Charlie == null){
            QuestTraveling.travelToCharlie();
            return false;
        }
        if (Charlie.interact("Talk-to"))
            Time.sleepUntil(Dialog::isOpen, 4000);
        return isValidDialogOpen();
    }

    private static boolean pickUpFeathers(int amountToCollect){
        int feathersInInvent = Inventory.getCount(true,"Eagle feather");
        if (feathersInInvent >= amountToCollect)
            return true;

        if (!QuestAreas.isInCentralCave()){
            Log.severe("Trying to pick up feathers when not in the main cave?");
            return false;
        }

        Position feathersTile = new Position(2003, 4969, 3);
        SceneObject featherPile = SceneObjects.getNearest(x->x.getName().equalsIgnoreCase("Giant feathers")
                && x.getPosition().equals(feathersTile));
        if (featherPile == null){
            Movement.walkTo(feathersTile);
            Time.sleep(Random.high(400, 800));
            return false;
        }
        if (Players.getLocal().distance(feathersTile.getPosition()) > 2 && Players.getLocal().isMoving()){
            Time.sleepUntil(()->Players.getLocal().isMoving(), 5360);
            return false;
        }
        //Little antiban thing here, almost spam clicks the feathers instead of waiting for each one in invent.
        if (featherPile.interact("Take"))
            Time.sleep(Random.low(200, 900));

        return false;
    }

    public static void selectDialogOption(String... validResponses){
        if (validResponses.length == 0){
            Log.severe("No valid responses provided.");
            return;
        }
        if (Dialog.isProcessing())
            return;
        if (Dialog.process(validResponses))
            Time.sleepUntil(Dialog::isProcessing, Random.nextInt(135, 500));
    }

    public static void selectDialogOption(int optionNumber){
        if (optionNumber == 0){
            Log.severe("Cannot process option 0, please start at 1.");
            return;
        }
        if (Dialog.isProcessing())
            return;
        if (Dialog.process(optionNumber - 1))
            Time.sleepUntil(Dialog::isProcessing, Random.nextInt(135, 500));
    }

    public static Map<String, Integer> getRequiredItems(){
        return REQUIRED_ITEMS;
    }

    public static boolean questComplete(){
        return Varps.getBitValue(2780) == 40;
    }

    public static void populateHashMap(){
        if (REQUIRED_ITEMS.isEmpty()) {
            REQUIRED_ITEMS.put("Yellow dye", 1);
            REQUIRED_ITEMS.put("Swamp tar", 1);
            REQUIRED_ITEMS.put("Coins", 50);
            REQUIRED_ITEMS.put("Necklace of passage(5)", 1);
            REQUIRED_ITEMS.put("Varrock teleport", 1);
            REQUIRED_ITEMS.put("Camelot teleport", 2);
        }
        if (BIRD_CLOTHES_ITEMS.isEmpty()){
            BIRD_CLOTHES_ITEMS.put("Yellow dye", 1);
            BIRD_CLOTHES_ITEMS.put("Swamp tar", 1);
            BIRD_CLOTHES_ITEMS.put("Coins", 50);
            BIRD_CLOTHES_ITEMS.put("Eagle feather", 10);
        }
    }

    public static boolean hasAllRequiredItems(){
        if (REQUIRED_ITEMS.isEmpty()) {
            Log.severe("Hashmap not populated.");
            Main.updateScriptState(null);
            return false;
        }

        return Main.hasItems(REQUIRED_ITEMS);
    }
}
