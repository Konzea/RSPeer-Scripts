package Chin_Hunter.Helpers;

import Chin_Hunter.Main;
import org.rspeer.runetek.adapter.Positionable;
import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.adapter.scene.Pickable;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.component.tab.Skill;
import org.rspeer.runetek.api.component.tab.Skills;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Pickables;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.api.scene.SceneObjects;

import java.util.ArrayList;
import java.util.List;

public class Hunter {

     enum TrapType {
         BOX_TRAP("Box trap", "Lay"),
         BIRD_SNARE("Bird snare", "Lay");

         private final String Name;
         private final String Action;

         TrapType(String name, String action){
            this.Name = name;
            this.Action = action;
         }
     }

    public static List<Positionable> trapLocations = new ArrayList<>();

    public static void teleportToPiscatoris(){
        Item piscTele = Inventory.getFirst("Piscatoris teleport");
        if (piscTele != null && piscTele.interact("Teleport")){
            Time.sleepUntil(()->!Main.isAtPiscatoris(), 8000);
        }
    }

    private static Position[] generateTrapTiles(Position centreTile){
        int[] xTransforms = {0, 1, 1, -1, -1, 0, 0, 2, -2, -1, 0, 1, 0};
        int[] yTransforms = {0, 1, -1, -1, 1, 2, -2, 0, 0, 0, 1, 0, -1};
        Position[] potentialTrapTiles = new Position[xTransforms.length];
        int x = centreTile.getX();
        int y = centreTile.getY();
        int z = centreTile.getFloorLevel();

        for (int i = 0; i < xTransforms.length; i++)
            potentialTrapTiles[i] = new Position(x + xTransforms[i], y + yTransforms[i], z);
        return potentialTrapTiles;
    }

    public static boolean layTrap(TrapType trapType, Position tile){
        Item[] inventTraps = Inventory.getItems(x->x.getName().equalsIgnoreCase(trapType.Name));
        //No traps in invent
        if (inventTraps.length == 0)
            return false;

        //Walk to tile
        if (!Players.getLocal().getPosition().equals(tile) && Movement.walkTo(tile)) {
            Time.sleepUntil(() -> Players.getLocal().getPosition().equals(tile), 4000);
            if (!Players.getLocal().getPosition().equals(tile))
                return false;
        }

        SceneObject[] objectOnTile = SceneObjects.getAt(tile);
        //Object already on tile
        if (objectOnTile.length > 0)
            return false;

        Pickable[] droppedTraps = Pickables.getLoaded(x->x.getPosition().equals(tile) && x.getName().equalsIgnoreCase(trapType.Name));
        int droppedTrapsCount = droppedTraps.length;
        int inventTrapsCount = inventTraps.length;

        if (inventTraps[0].interact(trapType.Action))
            Time.sleepUntil(()-> SceneObjects.getAt(tile).length > 0, 4000);
        else
            return false;

        //Update after placing a trap
        droppedTraps = Pickables.getLoaded(x->x.getPosition().equals(tile) && x.getName().equalsIgnoreCase(trapType.Name));
        inventTraps = Inventory.getItems(x->x.getName().equalsIgnoreCase(trapType.Name));

        //Number of traps on the tile changed, probably failed while placing it.
        if (droppedTraps.length != droppedTrapsCount)
            return false;

        //Didn't lose a trap from invent, probably didn't place it.
        if (inventTraps.length != inventTrapsCount - 1)
            return false;

        //1 Object on the tile, hopefully the trap we placed...
        if (SceneObjects.getAt(tile).length != 1)
            return false;

        trapLocations.add(tile);

        return true;
    }

    public static int getMaxTrapCount(){
        int hunterLevel = Skills.getLevel(Skill.HUNTER);
        if (hunterLevel < 20)
            return 1;
        if (hunterLevel < 40)
            return 2;
        if (hunterLevel < 60)
            return 3;
        if (hunterLevel < 80)
            return 4;
        return 5;
    }

}
