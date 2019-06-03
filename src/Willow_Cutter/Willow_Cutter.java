package Willow_Cutter;

import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.adapter.scene.Player;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.input.Keyboard;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.api.scene.SceneObjects;
import org.rspeer.script.Script;
import org.rspeer.script.ScriptCategory;
import org.rspeer.script.ScriptMeta;
import org.rspeer.ui.Log;

import java.util.regex.Pattern;

@ScriptMeta(desc = "Cuts Willow trees, duh.", developer = "Shteve", name = "Willow Cutter", category = ScriptCategory.WOODCUTTING, version = 1)
public class Willow_Cutter extends Script {

    private static Area area = Area.rectangular(2983, 3191, 2994, 3181);

    private static Player localPlayer = Players.getLocal();



    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public int loop() {


        if (area.contains(localPlayer.getPosition())) {
            if (!Inventory.isFull()) {
                if (localPlayer.getAnimation() == -1) {
                    if (!localPlayer.isMoving())
                        chopTree();
                    else
                        Time.sleepUntil(() -> !localPlayer.isMoving(), 1500);
                }else
                    Time.sleepUntil(() -> localPlayer.getAnimation() == -1, 3000);
            } else {
                dropLogs();
            }
        } else {
            Log.severe("Not in Area!");
            if (Movement.walkTo(area.getCenter())){
                Log.info("Attempting to walk back.");
                Time.sleep(500);
                Time.sleepUntil(() -> !localPlayer.isMoving(),2500);
            }
        }

        return 1000;
    }

    @Override
    public void onStop() {
        super.onStop();
    }


    private void chopTree() {
        //Get nearest Willow tree and if not null chop it and wait
        SceneObject tree = SceneObjects.getNearest("Willow");
        if (tree != null && tree.interact("Chop down")) {
            Time.sleepUntil(() -> localPlayer.getAnimation() != -1,2000);
        }
    }

    private void dropLogs() {
    //TODO Potentially hold down shift, if possible.

        //Drop from top to bottom left to right
        int[] order = {
                0, 4, 8, 12, 16, 20, 24,
                1, 5, 9, 13, 17, 21, 25,
                2, 6, 10, 14, 18, 22, 26,
                3, 7, 11, 15, 19, 23, 27
        };

        //If item isn't an axe, drop it and wait.
        for (int i : order) {
            Item item = Inventory.getItemAt(i);
            if (item != null && !item.getName().contains("axe")) {
                item.interact("Drop");
                Time.sleep(52, 213);
            }
        }
    }

}
