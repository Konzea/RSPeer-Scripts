package Chin_Hunter.Executes.Questing.Puzzle_Rooms;

import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.adapter.scene.Player;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.Varps;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.api.scene.SceneObjects;
import org.rspeer.ui.Log;

public class GoldenFeather {

    public static boolean attemptToSolve(){

        switch (Varps.getBitValue(3089)){
            case 0:{
                if (Inventory.getCount(true,"Odd bird seed") < 6){
                    collectBirdseed();
                    return false;
                }
                operateLever(19948, "Pull-down");
                return false;
                //"Take-from" 6 "Odd bird seed" from "Birdseed holder" (9574, 8914, 2)
                //"Pull-down" "Eagle lever" (9559, 8919, 2)
                //Varpbit 3089: 0->4: 1 Level Pulled
                //Varpbit 3092: 0->1: ^
            }
            case 4:{
                placeBirdseed(19939, 3200, 6000);
                return false;
                //"Use" "Odd bird seed" on "Bird feeder" (9582, 8898,2)
                //Sleep 3-5 seconds
                //Varpbit 3089: 4->260: 2 Bird seed place
                //Varpbit 3098: 0->1: ^
            }
            case 260:{
                placeBirdseed(19938, 3500, 5000);
                return false;
                //"Use" "Odd bird seed" on "Bird feeder" (9578,8902,2)
                    //Sleep 3-5 seconds
                //Varpbit 3089: 260->388: 3 Bird seed place
                //Varpbit 3097: 0->1: ^
            }
            case 388:{
                operateLever(19949, "Pull-down");
                return false;
                //"Pull-down" "Eagle lever" (9594,8899,2)
                //Varpbit 3089: 388->396: 4 Level pulled
                //Varpbit 3093: 0->1: ^
            }
            case 396:{
                operateLever(19948, "Push-up");
                return false;
                //"Push-up" "Eagle lever" (9559, 8919, 2)
                //Varpbit 3089: 396->392: 5 Level pulled
                //Varpbit 3092: 1->0: ^
            }
            case 392:{
                placeBirdseed(19937, 7000, 8300);
                return false;
                //"Use" "Odd bird seed" on "Bird feeder" (9563, 8906, 2)
                //Sleep 7-8 seconds
                //Varpbit 3089: 392->424: 6 Bird seed place
                //Varpbit 3095: 0->1: ^
            }
            case 424:{
                operateLever(19946, "Pull-down");
                return false;
                //"Pull-down" "Eagle lever" (9551, 8910, 2)
                //Varpbit 3089: 424->425: 7 Level Pulled
                //Varpbit 3090: 0->1: ^
            }
            case 425:{
                placeBirdseed(19936, 5200, 7000);
                return false;
                //"Use" "Odd bird seed" on "Bird feeder" (9561, 8923, 2)
                    //Sleep 5-7 seconds
                //Varpbit 3089: 425->441: 8 Bird seed place
                //Varpbit 3094: 0->1: ^
            }
            case 441:{
                operateLever(19947, "Pull-down");
                return false;
                //"Pull-down" "Eagle lever" (9541, 8923, 2)
                //Varpbit 3089: 441->443: 9 Level Pulled
                //Varpbit 3091: 0->1: ^
            }
            case 443:{
                placeBirdseed(19941, 5300, 7000);
                return false;
                //"Use" "Odd bird seed" on "Bird feeder" (9551, 8905, 2)
                    //Sleep 5-7 seconds
                //Varpbit 3089: 443->411: 10 Bird seed place
                //Varpbit 3095: 0->1: ^c
            }
            case 411:{
                placeBirdseed(19937, 4500, 6500);
                return false;
                //"Use" "Odd bird seed" on "Bird feeder" (9563, 8906, 2)
                //Sleep 4-5 seconds
                //Varpbit 3089: 411->475: 11 Bird seed place
                //Varpbit 3096: 0->1: ^
            }
            case 475:{
                if (isComplete()){
                    leaveCave();
                    return !isInCave();
                }

                SceneObject Pedestal = SceneObjects.getNearest(19950);
                if (Pedestal == null){
                    Log.severe("Could not find golden feather pedestal.");
                    return false;
                }
                if (Pedestal.interact("Take-from"))
                    Time.sleepUntil(GoldenFeather::isComplete, 4000);
                return false;

                //Complete?
                //"Take-from" "Stone pedestal" (9544, 8915, 2)
                //Wait until invent contains golden feather
                //leave tunnel: "Enter" "Tunnel" (9573, 8917, 2)
            }
        }
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
        return true;
    }

    private static void operateLever(int ID, String Action){
        SceneObject Lever = SceneObjects.getNearest(ID);
        if (Lever == null){
            Log.severe("Could not find a lever with ID: " + ID);
            return;
        }
        int varpBit = Varps.getBitValue(3089);
        if (Lever.interact(Action)) {
            Time.sleepUntil(()-> Players.getLocal().isMoving(), 1500);
            if (Players.getLocal().isMoving()) {
                Time.sleepUntil(() -> !Players.getLocal().isMoving(), Random.nextInt(20000,30000));
                Time.sleepUntil(() -> Varps.getBitValue(3089) != varpBit, 6000);
            }
        }
    }

    private static void placeBirdseed(int ID, int sleepMin, int sleepMax){
        if (!Inventory.contains("Odd bird seed")) {
            collectBirdseed();
            return;
        }
        SceneObject birdFeeder = SceneObjects.getNearest(ID);
        if (birdFeeder == null){
            Log.severe("Could not find bird feeder with ID: " + ID);
            return;
        }
        if (Inventory.use(x->x.getName().equalsIgnoreCase("Odd bird seed"), birdFeeder)) {
            Time.sleepUntil(() -> Players.getLocal().isMoving(), 1500);
            if (Players.getLocal().isMoving()) {
                Time.sleepUntil(() -> !Players.getLocal().isMoving(), Random.nextInt(20000,30000));
                Time.sleep(sleepMin, sleepMax);
            }
        }
    }

    private static void collectBirdseed(){
        SceneObject birdseedHolder = SceneObjects.getNearest(19919);
        if (birdseedHolder == null) {
            Log.severe("Could not find birdseed holder");
            return;
        }
        int birdseedCount = Inventory.getCount(true,"Odd bird seed");
        if (birdseedHolder.interact("Take-from"))
            Time.sleepUntil(()->Inventory.getCount(true,"Odd bird seed") != birdseedCount, 4000);
    }

    public static void leaveCave() {
        SceneObject Exit = SceneObjects.getNearest(19894);
        if (Exit == null){
            Log.severe("Could not find exit to Golden feather room.");
            return;
        }
        if (Exit.interact("Enter"))
            Time.sleepUntil(()->!isInCave(), 4000);
    }

    public static boolean isInCave(){
        if (SceneObjects.getNearest(19919) != null)
            return true;
        if (SceneObjects.getNearest(19935) != null)
            return true;
        return false;
    }

    public static boolean isComplete(){
        return Inventory.contains("Golden feather");
    }

}
