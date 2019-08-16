package Chin_Hunter.Executes.Eagles_Peak.Puzzle_Rooms;

import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.Varps;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.scene.SceneObjects;
import org.rspeer.ui.Log;

import java.util.HashMap;
import java.util.Map;

public class BronzeFeather {

    private static final Map<Integer,Integer> WINCH_DATA = new HashMap<>();

    public static boolean attemptToSolve(){

        if (Varps.get(934) == 15){
            SceneObject Pedestal = SceneObjects.getNearest(19980);
            if (Pedestal == null){
                Log.severe("Could not find a pedestal.");
                return false;
            }
            if (Pedestal.interact("Take-from"))
                Time.sleepUntil(BronzeFeather::isComplete, 4000);
            return false;
        }

        if (WINCH_DATA.isEmpty()){
            //Winch 1 - Top Left
            WINCH_DATA.put(19976, 3101);
            //Winch 1 - Top Right
            WINCH_DATA.put(19977, 3102);
            //Winch 1 - Bottom Left
            WINCH_DATA.put(19978, 3103);
            //Winch 1 - Bottom Right
            WINCH_DATA.put(19979, 3104);
        }

        if (!allWinchesComplete()){
            operateAllWinches();
            return false;
        }
        if (!isComplete()){
            SceneObject Pedestal = SceneObjects.getNearest(19984);
            if (Pedestal == null){
                Log.severe("Could not find a pedestal with the Bronze feather on.");
                Time.sleep(1000,2500);
                return false;
            }
            if (Pedestal.interact("Take-from"))
                Time.sleepUntil(BronzeFeather::isComplete, 4000);
            return false;
        }

        leaveCave();
        return !isInCave();
    }



    public static boolean isInCave(){
        //Cave exit door.
        return SceneObjects.getNearest(19906) != null;
    }

    public static boolean isComplete(){
        return Inventory.contains("Bronze feather");
    }

    public static void leaveCave() {
        SceneObject Exit = SceneObjects.getNearest(19906);
        if (Exit == null){
            Log.severe("Could not find exit to Bronze feather room.");
            return;
        }
        if (Exit.interact("Enter")) {
            Time.sleepUntil(() -> !isInCave(), 4000);
            Time.sleep(500, 1800);
        }
    }

    private static boolean allWinchesComplete(){
        return Varps.getBitValue(3105) == 0;
    }

    private static void operateAllWinches(){
        for (Map.Entry<Integer, Integer> winchData : WINCH_DATA.entrySet()) {
            int ID = winchData.getKey();
            int varpValue = Varps.getBitValue(winchData.getValue());
            if (varpValue == 1)
                continue;

            SceneObject Winch = SceneObjects.getNearest(ID);
            if (Winch == null){
                Log.severe("Could not find Winch with ID of: " + ID);
                continue;
            }

            if (Winch.interact("Operate"))
                Time.sleepUntil(()->Varps.getBitValue(winchData.getValue()) != varpValue, 8000);
            break;
        }
    }
}
