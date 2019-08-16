package Chin_Hunter.Executes.Herblore;

import Chin_Hunter.Helpers.RequiredItem;
import Chin_Hunter.Main;
import Chin_Hunter.States.ScriptState;
import com.sun.org.apache.regexp.internal.RE;
import org.rspeer.runetek.adapter.component.InterfaceComponent;
import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.Varps;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.Dialog;
import org.rspeer.runetek.api.component.Interfaces;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.api.scene.SceneObjects;
import org.rspeer.ui.Log;

import java.util.HashMap;
import java.util.Map;

public class Druidic_Ritual {

    private static final RequiredItem[] REQUIRED_ITEMS = {
            new RequiredItem("Falador teleport", 2),
            new RequiredItem("Raw bear meat", 1),
            new RequiredItem("Raw rat meat", 1),
            new RequiredItem("Raw beef", 1),
            new RequiredItem("Raw chicken", 1),
            new RequiredItem("Varrock teleport", 1)
    };

    public static Area QUEST_AREA = Area.polygonal(
            new Position(2862, 3572, 0),
            new Position(2898, 3588, 0),
            new Position(2942, 3579, 0),
            new Position(2940, 3519, 0),
            new Position(3075, 3518, 0),
            new Position(3076, 3453, 0),
            new Position(3105, 3389, 0),
            new Position(3061, 3315, 0),
            new Position(2888, 3297, 0),
            new Position(2875, 3391, 0),
            new Position(2866, 3394, 0));

    private static Area FALADOR_SQUARE_AREA = Area.rectangular(2954, 3391, 2976, 3373);

    private static Position KAQEMEEX_TILE = new Position(2924, 3485, 0);
    private static Position SANFEW_STAIRS_TILE = new Position(2898, 3428);

    private static Position DUNGEON_LADDER_TILE = new Position(2884, 3397, 0);
    private static Position PRIZON_DOOR_TILE = new Position(2889, 9830, 0);
    private static Area CAULDRON_AREA = Area.rectangular(2889, 9838, 2897, 9826);

    private static Position ARMOUR_TILE_1 = new Position(2887, 9832, 0);
    private static Position ARMOUR_TILE_2 = new Position(2887, 9829, 0);

    public static void onStart() {

    }

    public static void execute() {

        if (Dialog.isOpen()){
            if (Dialog.isProcessing()) {
                Time.sleepUntil(() -> !Dialog.isProcessing(), 2000);
                return;
            }
            if (Dialog.canContinue()){
                if (Dialog.processContinue())
                    Time.sleepUntil(Dialog::isProcessing, Random.nextInt(133, 500));
                return;
            }
        }
        switch (Varps.get(80)){
            case 0:{
                if (!hasAllRequiredItems() && !isInWalkingDistance() && !Inventory.contains("Falador teleport")){
                    Log.severe("Don't have the required items for the quest");
                    Main.updateScriptState(ScriptState.BANKING);
                    return;
                }
                if (!Dialog.isOpen()){
                    talkToKaqemeex();
                    return;
                }
                selectDialogOptions("I'm in search of a quest.", "Okay, I will try and help.");
                return;
            }

            case 1:{
                if (!Dialog.isOpen()){
                    talkToSanfew();
                    return;
                }
                selectDialogOptions("I've been sent to help purify the Varrock stone circle.", "Ok, I'll do that then.");
                return;
            }

            case 2:{
                if (isInCauldronArea()){
                    if (!hasAllEnchantedFood()){
                        createEnchantedFood();
                        return;
                    }
                    talkToSanfew();
                }
                if (hasAllRawFood()){
                    walkToCauldron();
                    return;
                }
                if (!hasAllEnchantedFood()){
                    Log.severe("Do not have all the required raw meat.");
                    Main.updateScriptState(ScriptState.BANKING);
                    return;
                }
                talkToSanfew();
                return;
            }

            case 3:{
                talkToKaqemeex();
                return;
            }

            case 4:{
                InterfaceComponent Close = Interfaces.getComponent(277,16);
                if (Close != null){
                    if (Close.click())
                        Time.sleepUntil(()->Interfaces.getComponent(277,16) == null, 2000);
                    return;
                }
                Log.fine("Completed Druadic Ritual.");
                Log.info("Now going to check if we can afford to train herblore");
                Main.updateScriptState(ScriptState.HERBLORE_TRAINING);
            }
        }
    }

    private static void createEnchantedFood(){
        SceneObject Cauldron = SceneObjects.getNearest("Cauldron of Thunder");
        if (Cauldron == null){
            Log.severe("Could not find Cauldron");
            return;
        }
        if (!useSingleFoodOnCauldron("Raw bear meat", "Enchanted bear", Cauldron))
            return;
        if (!useSingleFoodOnCauldron("Raw rat meat", "Enchanted rat", Cauldron))
            return;
        if (!useSingleFoodOnCauldron("Raw beef", "Enchanted beef", Cauldron))
            return;
        useSingleFoodOnCauldron("Raw chicken", "Enchanted chicken", Cauldron);
    }

    private static boolean useSingleFoodOnCauldron(String rawName, String enchantedName, SceneObject Cauldron){
        if (Inventory.contains(enchantedName))
            return true;
        if (Inventory.use(x->x.getName().equalsIgnoreCase(rawName), Cauldron))
            Time.sleepUntil(()->Inventory.contains(enchantedName), 5000);

        return Inventory.contains(enchantedName);
    }

    private static boolean hasAllEnchantedFood() {
        if (!Inventory.contains("Enchanted bear"))
            return false;
        if (!Inventory.contains("Enchanted rat"))
            return false;
        if (!Inventory.contains("Enchanted beef"))
            return false;
        if (!Inventory.contains("Enchanted chicken"))
            return false;
        return true;
    }

    private static boolean hasAllRawFood() {
        if (!Inventory.contains("Raw bear meat"))
            return false;
        if (!Inventory.contains("Raw rat meat"))
            return false;
        if (!Inventory.contains("Raw beef"))
            return false;
        if (!Inventory.contains("Raw chicken"))
            return false;
        return true;
    }

    //region Chat Processing

    private static void selectDialogOptions(String... validResponses){
        if (validResponses.length == 0){
            Log.severe("No valid responses provided.");
            return;
        }
        if (Dialog.isProcessing())
            return;
        if (Dialog.process(validResponses))
            Time.sleepUntil(Dialog::isProcessing, Random.nextInt(135, 500));
    }

    //endregion

    //region Quest Travelling

    private static void walkToCauldron(){
        if (isInCauldronArea())
            return;

        if (Players.getLocal().getFloorLevel() == 1){
            Climb("Climb-down", "Staircase", SANFEW_STAIRS_TILE);
            return;
        }
        if (!isInDungeon()){
            Climb("Climb-down", "Ladder", DUNGEON_LADDER_TILE);
            return;
        }
        if (!Movement.isRunEnabled() && Movement.getRunEnergy() > 10)
            Movement.toggleRun(true);

        if (Players.getLocal().distance(PRIZON_DOOR_TILE) > 3){
            Main.walkTo(PRIZON_DOOR_TILE);
            return;
        }
        SceneObject Door = SceneObjects.getNearest(x->x.getPosition().equals(PRIZON_DOOR_TILE)
                && x.getName().equalsIgnoreCase("Prison door"));
        if (Door == null){
            Log.severe("Could not find door to the cauldron");
            Main.walkTo(PRIZON_DOOR_TILE);
            return;
        }
        SceneObject[] Guard = SceneObjects.getLoaded(x->x.getId()==818
                && (x.getPosition().equals(ARMOUR_TILE_1) || x.getPosition().equals(ARMOUR_TILE_2)));

        if (Door.interact("Open")){
            if (Guard.length > 0) {
                Time.sleep(300, 1000);
                return;
            }
            Time.sleepUntil(Druidic_Ritual::isInCauldronArea, 2000);
        }
    }

    private static void talkToSanfew(){
        if (Players.getLocal().distance(SANFEW_STAIRS_TILE) > 10){
            if (isInDungeon() || !isInWalkingDistance()){
                teleportToFalador();
                return;
            }
            Main.walkTo(SANFEW_STAIRS_TILE);
            return;
        }

        if (Players.getLocal().getFloorLevel() == 0){
            Climb("Climb-up", "Staircase", SANFEW_STAIRS_TILE);
            return;
        }

        Npc Sanfew = Npcs.getNearest("Sanfew");
        if (Sanfew == null){
            Log.severe("Could not find Sanfew, walking to him.");
            Main.walkTo(SANFEW_STAIRS_TILE);
            return;
        }
        if (Sanfew.interact("Talk-to"))
            Time.sleepUntil(Dialog::isOpen, 8000);
    }

    private static void talkToKaqemeex(){
        if (Players.getLocal().distance(KAQEMEEX_TILE) > 10){
            if (Players.getLocal().getFloorLevel() == 1){
                Climb("Climb-down", "Staircase", SANFEW_STAIRS_TILE);
                return;
            }
            if (isInDungeon() || !isInWalkingDistance()){
                teleportToFalador();
                return;
            }
            Main.walkTo(KAQEMEEX_TILE);
            return;
        }

        Npc Kaqemeex = Npcs.getNearest("Kaqemeex");
        if (Kaqemeex == null){
            Log.severe("Could not find Kaqemeex, walking to him.");
            Main.walkTo(KAQEMEEX_TILE);
            return;
        }
        if (Kaqemeex.interact("Talk-to"))
            Time.sleepUntil(Dialog::isOpen, 8000);
    }

    private static void Climb(String Action, String objectName, Position objectTile){
        SceneObject Object = SceneObjects.getNearest(x->x.getName().equalsIgnoreCase(objectName)
                && x.getPosition().equals(objectTile)
                && x.containsAction(Action));

        if (Object == null){
            Log.info("Could not find " +  objectName + ", walking to it.");
            Main.walkTo(objectTile);
            return;
        }

        if (Object.interact(Action)){
            Time.sleepUntil(()->Players.getLocal().distance(objectTile) > 50
                    || Players.getLocal().getFloorLevel() != objectTile.getFloorLevel(), 8000);
        }
    }

    private static void teleportToFalador(){
        Item Teleport = Inventory.getFirst("Falador teleport");
        if (Teleport == null){
            Log.severe("Could not find Falador teleport in invent.");
            Main.updateScriptState(ScriptState.BANKING);
            return;
        }
        if (Teleport.interact("Break")){
            Time.sleepUntil(Druidic_Ritual::isInFallySquare, 7500);
        }
    }

    //endregion

    //region Quest Areas

    private static boolean isInCauldronArea(){
        return CAULDRON_AREA.contains(Players.getLocal());
    }

    private static boolean isInDungeon(){
        return Players.getLocal().distance(PRIZON_DOOR_TILE) < 500;
    }

    private static boolean isInWalkingDistance(){
        return QUEST_AREA.contains(Players.getLocal());
    }

    private static boolean isInFallySquare(){
        return FALADOR_SQUARE_AREA.contains(Players.getLocal());
    }

    //endregion

    public static RequiredItem[] getRequiredItems(){
        return REQUIRED_ITEMS;
    }

    public static boolean questComplete(){
        return Varps.get(80) == 4;
    }


    public static boolean hasAllRequiredItems(){
        return Main.hasItems(REQUIRED_ITEMS);
    }

}
