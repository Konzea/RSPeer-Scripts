package Combat_Starter.Executes;

import Combat_Starter.Enums.ScriptState;
import Combat_Starter.Main;
import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.tab.*;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.ui.Log;

import java.util.function.Predicate;

public class Fighting {

    private Fighting(){
        //Private Default Constructor
    }

    public static void execute(){
//Fighting
//if gear equipped
//if in fight zone
//attack
//else go to fight zone
//else equip gear


            if (Main.getCurrentTarget().getZone().contains(Players.getLocal().getPosition())) {
                Main.updateScriptState(null);
            }else
                Main.updateScriptState(ScriptState.WALKING);

    }

    private static void fightNPC(Predicate<Npc> targetPredicate){

    }

    public static boolean gearEquipped(){
        return Equipment.isOccupied(EquipmentSlot.MAINHAND) && Equipment.isOccupied(EquipmentSlot.OFFHAND);
    }

    public static void equipGear(){
        int attempt = 0;
        while (!gearEquipped() && attempt < 3) {
            Item[] items = Inventory.getItems(x -> x.containsAction("Wield") || x.containsAction("Wear"));
            for (Item i : items) {
                i.click();
                Time.sleep(50, 300);
            }
            Time.sleepUntil(Inventory::isEmpty, 2000);
            attempt++;
        }
        if (!gearEquipped()){
            Log.info("Attempted to equip gear 3 times but failed.");
            Main.updateScriptState(null);
        }
    }

    /**
     * Calculates the lowest combat skill and returns the attack style that will train it.
     * @return Returns the most optimal attack style to train.
     */
    public static Combat.AttackStyle getBestAttackStyle(){
        int attackLevel = Skills.getLevel(Skill.ATTACK);
        int strengthLevel = Skills.getLevel(Skill.STRENGTH);
        int defenceLevel = Skills.getLevel(Skill.DEFENCE);

            int lowestCMBLevel = strengthLevel;
            Combat.AttackStyle bestAttackStyle = Combat.AttackStyle.AGGRESSIVE;

            if (attackLevel < lowestCMBLevel) {
                lowestCMBLevel = attackLevel;
                bestAttackStyle = Combat.AttackStyle.ACCURATE;
            }
            if (defenceLevel < lowestCMBLevel) {
                bestAttackStyle = Combat.AttackStyle.DEFENSIVE;
            }

        return bestAttackStyle;
    }


}
