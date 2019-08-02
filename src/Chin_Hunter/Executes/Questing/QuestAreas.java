package Chin_Hunter.Executes.Questing;

import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Players;

public class QuestAreas {

    private static final Area FENCED_VARROCK_AREA = Area.polygonal(
            new Position(3264, 3408, 0),
            new Position(3264, 3380, 0),
            new Position(3265, 3380, 0),
            new Position(3265, 3376, 0),
            new Position(3274, 3376, 0),
            new Position(3274, 3377, 0),
            new Position(3287, 3377, 0),
            new Position(3289, 3380, 0),
            new Position(3289, 3383, 0),
            new Position(3288, 3384, 0),
            new Position(3288, 3407, 0),
            new Position(3287, 3408, 0),
            new Position(3274, 3409, 0),
            new Position(3273, 3408, 0));

    //Close enough to ardy that we shouldn't tele
    private static final Area NEAR_ARDY_AREA = Area.polygonal(
            new Position(2442, 3382, 0),
            new Position(2401, 3334, 0),
            new Position(2470, 3256, 0),
            new Position(2612, 3231, 0),
            new Position(2723, 3292, 0),
            new Position(2654, 3421, 0));

    private static final Area PASSED_STONE_DOOR_AREA = Area.polygonal(
            new Position(2000, 4963, 3),
            new Position(2003, 4949, 3),
            new Position(2003, 4946, 3),
            new Position(2019, 4942, 3),
            new Position(2033, 4950, 3),
            new Position(2031, 4972, 3));

    private static final Area VARROCK_AREA = Area.rectangular(3297, 3373, 3123, 3520);

    private static final Area BASECAMP_AREA = Area.rectangular(2302, 3518, 2328, 3499);

    private static final Area EAGLES_PEAK_AREA = Area.polygonal(
            new Position(2315, 3495, 0),
            new Position(2327, 3502, 0),
            new Position(2337, 3499, 0),
            new Position(2356, 3473, 0),
            new Position(2352, 3462, 0),
            new Position(2316, 3475, 0));

    private static final Area ARDY_ZOO_AREA = Area.rectangular(2592, 3293, 2638, 3251);

    private static final Area CENTRAL_CAVE_AREA = Area.rectangular(1972, 4998, 2044, 4935, 3);


    private static final Position OUTPOST_TILE = new Position(2431, 3350, 0);
    private static final Position EAGLE_NEST_TILE = new Position(2006, 4959, 3);


    static boolean isInFencedVarrockArea(){
        return FENCED_VARROCK_AREA.contains(Players.getLocal());
    }

    static boolean isInVarrock(){
        return VARROCK_AREA.contains(Players.getLocal());
    }


    static boolean isNearArdy(){
        return NEAR_ARDY_AREA.contains(Players.getLocal());
    }

    static boolean isInArdyZoo(){
        return ARDY_ZOO_AREA.contains(Players.getLocal());
    }

    static boolean isAtOutpost(){
        return Players.getLocal().getPosition().distance(OUTPOST_TILE) < 10;
    }


    static boolean isAtBasecamp(){
        return BASECAMP_AREA.contains(Players.getLocal());
    }

    static boolean isAtPeak(){
        return EAGLES_PEAK_AREA.contains(Players.getLocal());
    }

    static boolean isInCentralCave(){
        return CENTRAL_CAVE_AREA.contains(Players.getLocal());
    }


    static boolean isPassedStoneDoor(){
        return PASSED_STONE_DOOR_AREA.contains(Players.getLocal());
    }

    static boolean isAtEaglesNest(){
        return EAGLE_NEST_TILE.isPositionInteractable();
    }

}
