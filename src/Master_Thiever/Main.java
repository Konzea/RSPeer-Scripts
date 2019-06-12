package Master_Thiever;

import Master_Thiever.Enums.ScriptState;
import Master_Thiever.Enums.Target;
import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.tab.EquipmentSlot;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.component.tab.Skill;
import org.rspeer.runetek.api.component.tab.Skills;
import org.rspeer.runetek.api.local.Health;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.event.listeners.ChatMessageListener;
import org.rspeer.runetek.event.types.ChatMessageEvent;
import org.rspeer.runetek.event.types.ChatMessageType;
import org.rspeer.script.Script;
import org.rspeer.script.ScriptCategory;
import org.rspeer.script.ScriptMeta;
import org.rspeer.ui.Log;

@ScriptMeta(desc = "Thieving", developer = "Shteve", name = "Master Thiever", category = ScriptCategory.THIEVING, version = 0.1)
public class Main extends Script implements ChatMessageListener {

    private static ScriptState currentState = ScriptState.STARTING;
    private static ScriptState previousState;

    private static Target currentTarget;

    private static final int foodHealAmount = 11;
    private static final String foodAction = "Drink";
    private static final int eatAnimation = 829;

    private static final String necklaceName = "Dodgy necklace";
    public static String getNecklaceName(){
        return necklaceName;
    }

    private static final String foodName = "Jug of wine";
    public static String getFoodName() {
        return foodName;
    }

    private static final String[] seedsToKeep = {"Snape grass", "Ranarr", "Snapdragon", "Torstol", "Avantoe", "Lantadyme"};
    public static String[] getSeedsToKeep(){
        return seedsToKeep;
    }

    private static final String mulePhrase = "boiii call me daddy";

    private static final int lowestAllowedHP = 4;
    public static int getLowestAllowedHP(){ return lowestAllowedHP; }




    @Override
    public void onStart() {
        Log.fine("Running Master Thiever by Shteve");
        super.onStart();
    }

    @Override
    public int loop() {
        if (currentState == null) {
            Log.severe("Null script state, stopping.");
            setStopping(true);
        }else
            currentState.execute();

        //Log.info("Current state: " + currentState.name());
        return 150;
    }

    @Override
    public void onStop() {
        super.onStop();
    }


    @Override
    public void notify(ChatMessageEvent chatMessageEvent) {
        String Message = chatMessageEvent.getMessage();
        if (chatMessageEvent.getType() == ChatMessageType.SERVER){
            if (Message.contains("Congratulations, ")){
               onLevelUpEvent();
            }else if (Message.equals("Oh dear, you are dead!")){
                onDeathEvent();
            }
        }else if (chatMessageEvent.getType() == ChatMessageType.CLAN_CHANNEL){
            if (Message.contains(mulePhrase)){
                updateScriptState(ScriptState.MULING);
            }
        }
    }

    private static void onDeathEvent(){
        Log.info("You died, getting back to thieving.");
        if (currentTarget == Target.MASTER_FARMERS){
            Log.severe("You died at master farmers somehow. Stopping Script.");
            updateScriptState(null);
        }
    }

    //region Getters & Setters
    //
    /**
     * Handles updating current script state and updating previousState.
     * @param inState The state you wish to set or null to stop script.
     */
    public static void updateScriptState(ScriptState inState){
            previousState = currentState;
            if (inState == currentState)
                Log.severe("Error: New script state same as previous.");
            else
                currentState = inState;
    }

    public static ScriptState getPreviousScriptState(){
        return previousState;
    }

    public static Target getCurrentTarget(){
        return currentTarget;
    }

    private static void updateTarget(Target inTarget){
        currentTarget = inTarget;
    }
    //endregion

    //region Public Methods

    public static boolean necklaceEquipped(){
        Item equippedNecklace = EquipmentSlot.NECK.getItem();
        return equippedNecklace != null && equippedNecklace.getName().equals(Main.getNecklaceName());
    }

    public static void equipNecklace(){
        Item necklace = Inventory.getFirst(necklaceName);
        if (necklace != null && necklace.interact("Wear"))
            Time.sleepUntil(Main::necklaceEquipped, 2000);
    }

    public static boolean canEatFood(){
        return  (Skills.getLevel(Skill.HITPOINTS) - Health.getCurrent()) >= foodHealAmount;
    }


    public static boolean userHasFood(){
        return Inventory.contains(foodName);
    }

    /**
     * Gets the amount of food required to heal you to full without overhealing.
     * @return Returns the amount of food that will heal you to full.
     */
    public static int getFoodToFullHP(){
        return (Skills.getLevel(Skill.HITPOINTS) - Health.getCurrent()) / foodHealAmount;
    }

    //Eat food methods placed here and public as needed in both Thieving and Banking
    /**
     * Eats as much food as possible without over healing.
     * Or if low hp level will just eat a single food.
     */
    public static void eatToFull(){
        int amount = getFoodToFullHP();
        if (amount > 0)
            eatFood(amount);
        else if (Health.getCurrent() < Skills.getLevel(Skill.HITPOINTS))
            eatFood();
    }

    /**
     * Eats a single piece of food
     */
    public static void eatFood() {
        eatFood(1);
    }

    /**
     * Eats the specific amount of food or until no more left.
     * Also sleeps between each food
     * @param amount
     */
    public static void eatFood(int amount) {
        Item[] food = Inventory.getItems(x->x.getName().equals(foodName));
        for (int i = 0; i < amount && i < food.length; i++){
            if (food[i] != null && food[i].interact(foodAction)) {
                Time.sleep(500);
                Time.sleepUntil(() -> Players.getLocal().getAnimation() != eatAnimation, 2000);
                Time.sleep(50, 100);
            }
        }
    }

    /**
     * Checks for the best attack style and best target and updates if necessary.
     */
    public static void onLevelUpEvent(){
        //TODO Potentially rename method.

        //If we have some left over coin pouches. Loot before moving to next npc
        if (currentTarget != null && currentTarget.dropsCoinPouches()) {
            Item goldPouches = Inventory.getFirst("Coin pouch");
            if (goldPouches != null && goldPouches.getStackSize() == 28 && goldPouches.interact("Open-all"))
                Time.sleep(200, 600);
        }

        //Update target to give best xp based on levels
        Target bestTarget = Target.getBestTarget();
        if (bestTarget != currentTarget){
            //Walk to the new training area unless just starting the script
            //If just starting the script we need to do more checks before walking
            if (currentTarget != null)
                updateScriptState(ScriptState.WALKING);

            Log.info("New thieving target set: " + bestTarget.toString());
            updateTarget(bestTarget);
        }

        if (!Movement.isRunEnabled())
            Movement.toggleRun(true);
    }
    //endregion
}
