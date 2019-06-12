package Master_Thiever.Executes;

import Master_Thiever.Enums.ScriptState;
import Master_Thiever.Enums.Target;
import Master_Thiever.Main;
import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.adapter.scene.Player;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.tab.*;
import org.rspeer.runetek.api.local.Health;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.pathfinding.executor.custom.CustomPathExecutor;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.api.scene.SceneObjects;
import org.rspeer.ui.Log;

import java.util.function.Predicate;

public class Thieving {

    private static int STUNNED_GRAPHIC_ID = 245;

    private Thieving() {
        //Private Default Constructor
    }

    public static void execute() {
        Player local = Players.getLocal();
        Target target = Main.getCurrentTarget();

        //Handle coin pouches
        if (target.dropsCoinPouches()) {
            Item goldPouches = Inventory.getFirst("Coin pouch");
            if (goldPouches != null && goldPouches.getStackSize() == 28 && goldPouches.interact("Open-all"))
                Time.sleep(200, 600);
        }

        //Main Logic
        if (target != Target.MEN && mustEatFood()) {
            if (Main.userHasFood())
                Main.eatFood();
            else
                Main.updateScriptState(ScriptState.BANKING);
        }else if (local.getGraphic() == STUNNED_GRAPHIC_ID) {
            performStunnedChecks(target);
            Time.sleepUntil(() -> local.getGraphic() != STUNNED_GRAPHIC_ID, 5000);
        }else if (Inventory.isFull()){
            performDropping(target);
        } else {
            Thieve(target, local);
        }
    }

    /**
     * Drops all junk items and/or opens coin pouches. Junk dropped depends on currentTarget
     * @param target The current thieving target.
     */
    public static void performDropping(Target target){
        Item[] droppableItems = null;
        if (target == Target.TEA){
            //Drop all
            droppableItems = Inventory.getItems();
        }else if (target == Target.MASTER_FARMERS) {
            //Drop shit seeds
            droppableItems = Inventory.getItems(x -> !x.getName().equals(Main.getFoodName())
                    && !x.getName().equals(Main.getNecklaceName())
                    && !arrayContainsName(x, Main.getSeedsToKeep()));
        }else{
            Log.severe("Attempting to drop with no dropping list setup.");
            Main.updateScriptState(null);
        }

        if (droppableItems != null){
            for (Item i : droppableItems) {
                if (i != null) {
                    if (i.containsAction("Drop")) {
                        if (i.interact("Drop"))
                            Time.sleep(142, 553);
                    }else if (i.containsAction("Open-all")){
                        if (i.interact("Open-all"))
                            Time.sleep(142, 553);
                    }
                }
            }
        }

    }

    //Given an Item, checks if it's name is in the given array
    private static boolean arrayContainsName(Item i, String[] array){
        for (String s : array){
            if (i.getName().contains(s))
                return true;
        }
        return false;
    }

    //Check if we have to eat food or we'll fucking die
    private static boolean mustEatFood(){
        return Health.getCurrent() < Main.getLowestAllowedHP();
    }

    //Check if wearing a necklace and if can eat food
    private static void performStunnedChecks(Target target) {
        Time.sleep(400, 1000);

        if ( target != Target.MEN && Main.userHasFood()){
            if (mustEatFood() || Main.canEatFood()) {
                Main.eatToFull();
            }
        }

        if (Random.nextInt(0, 8) == 1)
            performDropping(target);

        //Handle coin pouches
        if (target.dropsCoinPouches()) {
            Item goldPouches = Inventory.getFirst("Coin pouch");
            if (goldPouches != null && goldPouches.getStackSize() >= 17 && goldPouches.interact("Open-all"))
                Time.sleep(200, 600);
        }


        //Handle necklaces
        if (target == Target.MASTER_FARMERS) {
            Item neckItem = EquipmentSlot.NECK.getItem();
            if (neckItem == null || !neckItem.getName().equals("Dodgy necklace")) {
                Item inventNecklace = Inventory.getFirst("Dodgy necklace");
                if (inventNecklace != null && inventNecklace.interact("Wear"))
                    Time.sleepUntil(()->EquipmentSlot.NECK.getItem() != null, 2000);
            }
        }
    }

    //Fights an npc that matches the given predicate
    private static void Thieve(Target target, Player local) {
        //If we can't find an NPC or Object we'll set the state to walking
        boolean unableToThieve = false;

        if (target.isNpc()) {
            //Thieving NPCs
            Npc npc = Npcs.getNearest(target::NpcMatches);
            if (npc != null) {
                if (npc.interact(target.getAction()))
                    Time.sleepUntil(() -> Players.getLocal().getTargetIndex() == -1, 1200);
            } else
                unableToThieve = true;
        } else {
            //Thieving Objects (Mainly designed for stalls atm)
            if (local.getPosition().equals(target.getLocation())) {
                SceneObject object = SceneObjects.getNearest(target::ObjectMatches);
                if (object != null) {
                    if (object.getId() == target.getLootableID()) {
                        if (object.interact(target.getAction()))
                            Time.sleepUntil(()->local.getAnimation() == -1, 2000);
                    } else {
                        //Sometimes while waiting we'll drop the invent
                        if (Random.nextInt(0, 100) <= 10)
                            performDropping(target);
                        Time.sleepUntil(() -> SceneObjects.getNearest(target.getLootableID()) != null, 5000);
                    }
                } else
                    unableToThieve = true;
            }else {
                //Custom bit of walking for stalls where you need to stand at a specific tile
                if (Movement.walkTo(target.getLocation())) {
                    Time.sleep(333, 863);
                    Time.sleepUntil(() -> !local.isMoving(), 5000);
                }
            }
        }

        if (unableToThieve) {
            Log.info("Unable to thieve, walking to thieving place.");
            Main.updateScriptState(ScriptState.WALKING);
        }
    }


}


