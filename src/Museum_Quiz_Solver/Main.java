package Museum_Quiz_Solver;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.rspeer.runetek.adapter.component.InterfaceComponent;
import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.Varps;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.Dialog;
import org.rspeer.runetek.api.component.Interfaces;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.api.scene.SceneObjects;
import org.rspeer.script.Script;
import org.rspeer.script.ScriptCategory;
import org.rspeer.script.ScriptMeta;
import org.rspeer.ui.Log;

@ScriptMeta(desc = "Solves the P2P Museum quiz for 9 Slayer and 9 Hunter", developer = "Shteve", name = "Museum Quiz Solver", category = ScriptCategory.QUESTING, version = 0.1)
public class Main extends Script {

    private static final int GLOBAL_TRACKER_VARP = 1010;
    private static final int INDIVIDUAL_TRACKER_VARP = 1014;

    private static final int QUIZ_INTERFACE_ID = 533;
    private static final int QUESTION_CHILD_ID = 28;
    private static final int[] ANSWER_CHILD_IDS = {29, 30, 31};

    private static final Area MUSEUM_BASEMENT = Area.rectangular(1724, 4993, 1794, 4927);
    private static final Position STAIRS_DOWN_TILE = new Position(3255, 3451, 0);
    private static final Position STAIRS_UP_TILE = new Position(1758, 4959, 0);

    private static JsonObject quizData;

    private static boolean setStopping = false;

    @Override
    public void onStart() {
        Log.fine("Running Museum Quiz Solver by Shteve");
        Gson gson = new Gson();
        String rawJson = "{\"MuseumBoxes\":[{\"Name\":\"Lizard\",\"Tile\":\"1743,4978,0\",\"Varpbit\":3675,\"Questions\":{\"How does a lizard regulate body heat?\":\"Sunlight\",\"Who discovered how to kill lizards?\":\"The Slayer Masters\",\"How many eyes does a lizard have?\":\"Three\",\"What order do lizards belong to?\":\"Squamata\",\"What happens when a lizard becomes cold?\":\"It becomes sleepy\",\"Lizard skin is made of the same substance as?\":\"Hair\"}},{\"Name\":\"Tortoise\",\"Tile\":\"1753,4978,0\",\"Varpbit\":3680,\"Questions\":{\"What is the name of the oldest tortoise ever recorded?\":\"Mibbiwocket\",\"What is a tortoise's favourite food?\":\"Vegetables\",\"Name the explorer who discovered the world's oldest tortoise.\":\"Admiral Bake\",\"How does the tortoise protect itself?\":\"Hard shell\",\"If a tortoise had twenty rings on its shell, how old would it be?\":\"Twenty years\",\"Which race breeds tortoises for battle?\":\"Gnomes\"}},{\"Name\":\"Dragon\",\"Tile\":\"1768,4978,0\",\"Varpbit\":3672,\"Questions\":{\"What is considered a delicacy by dragons?\":\"Runite\",\"What is the best defence against a dragon's attack?\":\"Anti dragon-breath shield\",\"How long do dragons live?\":\"Unknown\",\"Which of these is not a type of dragon?\":\"Elemental\",\"What is the favoured territory of a dragon?\":\"Old battle sites\",\"Approximately how many feet tall do dragons stand?\":\"Twelve\"}},{\"Name\":\"Wyvern\",\"Varpbit\":3681,\"Tile\":\"1778,4978,0\",\"Questions\":{\"How did the wyverns die out?\":\"Climate change\",\"How many legs does a wyvern have?\":\"Two\",\"Where have wyvern bones been found?\":\"Asgarnia\",\"Which genus does the wyvern theoretically belong to?\":\"Reptiles\",\"What are the wyverns' closest relations?\":\"Dragons\",\"What is the ambient temperature of wyvern bones?\":\"Below room temperature\"}},{\"Name\":\"Snail\",\"Tile\":\"1776,4963,0\",\"Varpbit\":3674,\"Questions\":{\"What is special about the shell of the giant Morytanian snail?\":\"It is resistant to acid\",\"How do Morytanian snails capture their prey?\":\"Spitting acid\",\"Which of these is a snail byproduct?\":\"Fireproof oil\",\"What does 'Achatina Acidia' mean?\":\"Acid-spitting snail\",\"How do snails move?\":\"Contracting and stretching\",\"What is the 'trapdoor', which snails use to cover the entrance to their shells called?\":\"An operculum\"}},{\"Name\":\"Snake\",\"Tile\":\"1783,4963,0\",\"Varpbit\":3677,\"Questions\":{\"What is snake venom adapted from?\":\"Stomach acid\",\"Aside from their noses, what do snakes use to smell?\":\"Tongue\",\"If a snake sticks its tongue out at you, what is it doing?\":\"Seeing how you smell\",\"If some snakes use venom to kill their prey, what do other snakes use?\":\"Constriction\",\"Lizards and snakes belong to the same order - what is it?\":\"Squamata\",\"Which habitat do snakes prefer?\":\"Anywhere\"}},{\"Name\":\"Sea slug\",\"Tile\":\"1781,4957,0\",\"Varpbit\":3682,\"Questions\":{\"We assume that sea slugs have a stinging organ on their soft skin - what is it called?\":\"Nematocysts\",\"Why has the museum never examined a live sea slug?\":\"The researchers keep vanishing\",\"What do we think the sea slug feeds upon?\":\"Seaweed\",\"What are the two fangs presumed to be used for?\":\"Defense or display\",\"Off of which coastline would you find sea slugs?\":\"Ardougne\",\"In what way are sea slugs similar to snails?\":\"They have a hard shell\"}},{\"Name\":\"Monkey\",\"Tile\":\"1774,4957,0\",\"Varpbit\":3676,\"Questions\":{\"Which type of primates do monkeys belong to?\":\"Simian\",\"Which have the lighter colour: Karamjan or Harmless monkeys?\":\"Harmless\",\"Monkeys love bananas. What else do they like to eat?\":\"Bitternuts\",\"There are two known families of monkeys. One is Karamjan, the other is...?\":\"Harmless\",\"What colour mohawk do Karamjan monkeys have?\":\"Red\",\"What have Karamjan monkeys taken a deep dislike to?\":\"Seaweed\"}},{\"Name\":\"Kalphite Queen\",\"Tile\":\"1762,4938,0\",\"Varpbit\":3684,\"Questions\":{\"Kalphites are ruled by a...?\":\"Pasha\",\"What is the lowest caste in kalphite society?\":\"Worker\",\"What are the armoured plates on a kalphite called?\":\"Lamellae\",\"Are kalphites carnivores, herbivores or omnivores?\":\"Carnivores\",\"What are kalphites assumed to have evolved from?\":\"Scarab beetles\",\"Name the prominent figure in kalphite mythology?\":\"Scabaras\"}},{\"Name\":\"Terrorbird\",\"Tile\":\"1755,4940,0\",\"Varpbit\":3683,\"Questions\":{\"What is a terrorbird's preferred food?\":\"Anything\",\"Who use terrorbirds as mounts?\":\"Gnomes\",\"Where do terrorbirds get most of their water?\":\"Eating plants\",\"How many claws do terrorbirds have?\":\"Four\",\"What do terrorbirds eat to aid digestion?\":\"Stones\",\"How many teeth do terrorbirds have?\":\"0\"}},{\"Name\":\"Penguin\",\"Tile\":\"1742,4957,0\",\"Varpbit\":3673,\"Questions\":{\"Which sense do penguins rely on when hunting?\":\"Sight\",\"Which skill seems unusual for the penguins to possess?\":\"Planning\",\"How do penguins keep warm?\":\"A layer of fat\",\"What is the preferred climate for penguins?\":\"Cold\",\"Describe the behaviour of penguins?\":\"Social\",\"When do penguins fast?\":\"During breeding\"}},{\"Name\":\"Mole\",\"Tile\":\"1735,4957,0\",\"Varpbit\":3678,\"Questions\":{\"What habitat do moles prefer?\":\"Subterranean\",\"Why are moles considered to be an agricultural pest?\":\"They dig holes\",\"Who discovered giant moles?\":\"Wyson the Gardener\",\"What would you call a group of young moles?\":\"A labour\",\"What is a mole's favourite food?\":\"Insects and other invertebrates\",\"Which family do moles belong to?\":\"The Talpidae family\"}},{\"Name\":\"Camel\",\"Tile\":\"1737,4963,0\",\"Varpbit\":3679,\"Questions\":{\"What is produced by feeding chilli to a camel?\":\"Toxic dung\",\"If an ugthanki has one, how many does a bactrian have?\":\"Two\",\"Camels: herbivore, carnivore or omnivore?\":\"Omnivore\",\"What is the usual mood for a camel?\":\"Annoyed\",\"Where would you find an ugthanki?\":\"Al Kharid\",\"Which camel byproduct is known to be very nutritious?\":\"Milk\"}},{\"Name\":\"Leech\",\"Tile\":\"1744,4963,0\",\"Varpbit\":3685,\"Questions\":{\"What is the favoured habitat of leeches?\":\"Water\",\"What shape is the inside of a leech's mouth?\":\"'Y'-shaped\",\"Which of these is not eaten by leeches?\":\"Apples\",\"What contributed to the giant growth of Morytanian leeches?\":\"Environment\",\"What is special about Morytanian leeches?\":\"They attack by jumping\",\"How does a leech change when it feeds?\":\"It doubles in size\"}}]}";
        quizData = gson.fromJson(rawJson, JsonObject.class);

        Time.sleep(3000);
        if (!Movement.isRunEnabled())
            Movement.toggleRun(true);

        super.onStart();
    }


    @Override
    public int loop() {
        if (setStopping)
            setStopping(true);

        if (!hasClaimedReward()) {
            if (isInBasement()) {
                if (!hasCompletedQuiz()) {
                    if (hasStartedQuiz())
                        solveAllQuizzes(quizData);
                    else
                        startQuiz();
                } else {
                    claimReward();
                    //Clear data, no longer needed
                    if (quizData != null)
                        quizData = null;
                }
            } else {
                //Enter basement
                SceneObject stairs = SceneObjects.getFirstAt(STAIRS_DOWN_TILE);
                if (stairs == null) {
                    Movement.walkTo(STAIRS_DOWN_TILE);
                    Time.sleep(219, 612);
                } else {
                    if (stairs.interact("Walk-down"))
                        Time.sleepUntil(Main::isInBasement, 4000);
                }
            }
        } else {
            //Done with the quiz
            if (isInBasement()) {
                //Leave
                SceneObject stairs = SceneObjects.getFirstAt(STAIRS_UP_TILE);
                if (stairs == null) {
                    Movement.walkTo(STAIRS_UP_TILE);
                    Time.sleep(219, 612);
                } else {
                    if (stairs.interact("Walk-up"))
                        Time.sleepUntil(() -> !isInBasement() || Dialog.isOpen(), 2000);
                }
            } else {
                Log.fine("Museum Quiz Complete");
                setStopping(true);
            }
        }

        return Random.nextInt(100,250);
    }

    private static boolean isInBasement() {
        return MUSEUM_BASEMENT.contains(Players.getLocal());
    }

    private static void startQuiz() {
        if (Dialog.isOpen()) {
            if (Dialog.canContinue()) {
                if (Dialog.processContinue()) {
                    Time.sleep(366, 566);
                    Time.sleepUntil(() -> !Dialog.isProcessing(), 2000);
                }
            } else {
                InterfaceComponent option = Dialog.getChatOption(x -> x.equals("Sure thing."));
                if (option != null) {
                    if (option.interact("Continue")) {
                        Time.sleep(366, 566);
                        Time.sleepUntil(() -> !Dialog.isProcessing(), 2000);
                    }
                }
            }
        } else {
            Npc rewardNPC = Npcs.getNearest("Orlando Smith");
            if (rewardNPC != null) {
                if (rewardNPC.interact("Talk-to"))
                    Time.sleepUntil(Dialog::isOpen, 4000);
            } else
                Log.severe("Could not find npc to start the quiz");

        }
    }

    private static void claimReward() {
        if (Dialog.isOpen()) {
            if (Dialog.processContinue()) {
                Time.sleep(366, 566);
                Time.sleepUntil(() -> !Dialog.isProcessing(), 2000);
            }
        } else {
            Npc rewardNPC = Npcs.getNearest("Orlando Smith");
            if (rewardNPC != null) {
                if (rewardNPC.interact("Talk-to"))
                    Time.sleepUntil(Dialog::isOpen, 4000);
            } else
                Log.severe("Could not find reward npc");

        }
    }


    private static void solveAllQuizzes(JsonObject data) {
        JsonArray allCases = data.getAsJsonArray("MuseumBoxes");
        JsonObject displayCaseData;
        for (JsonElement element : allCases) {
            displayCaseData = element.getAsJsonObject();
            if (Varps.getBitValue((displayCaseData.get("Varpbit").getAsInt())) != 3) {
                solveSingleQuiz(displayCaseData);
                break;
            }
        }
    }

    private static void solveSingleQuiz(JsonObject displayCaseData) {
        Position tile = parseStringTile(displayCaseData.get("Tile").getAsString());
        if (tile == null) {
            Log.severe("Unable to parse tile from json");
            return;
        }
        if (quizInterfaceIsOpen()) {
            InterfaceComponent questionInterface = Interfaces.getComponent(QUIZ_INTERFACE_ID, QUESTION_CHILD_ID);
            if (questionInterface == null)
                return;
            String question = questionInterface.getText();
            String answer = displayCaseData.get("Questions").getAsJsonObject().get(question).getAsString();
            if (answer == null){
                Log.severe("Unable to find answer for following question:");
                Log.info(question);
                setStopping = true;
                return;
            }
            //Loop through all answers and click the one that matches above string parsed from json
            for (int answerChildId : ANSWER_CHILD_IDS) {
                InterfaceComponent answerInterface = Interfaces.getComponent(QUIZ_INTERFACE_ID, answerChildId);
                //Have to use contains and not equals because the quizes have '.' at the end
                if (answerInterface != null && answerInterface.getText().contains(answer)) {
                    if (answerInterface.interact("Ok")) {
                        Time.sleepUntil(() -> !quizInterfaceIsOpen(), 2000);
                        break;
                    }
                }
            }
        } else if (Dialog.isOpen()) {
            if (Dialog.processContinue()) {
                Time.sleep(100, 433);
                Time.sleepUntil(() -> !Dialog.isProcessing(), 2000);
            }
        } else {
            SceneObject quizButton = SceneObjects.getFirstAt(tile);
            if (quizButton == null) {
                Movement.walkTo(tile);
                Time.sleep(300, 788);
            } else {
                if (quizButton.interact("Study"))
                    Time.sleepUntil(Main::quizInterfaceIsOpen, 4000);
            }
        }
    }

    private static Position parseStringTile(String tile) {
        String[] tileData = tile.split(",");
        if (tileData.length == 0)
            return null;
        return new Position(Integer.parseInt(tileData[0]),
                Integer.parseInt(tileData[1]),
                Integer.parseInt(tileData[2]));
    }

    private static boolean hasStartedQuiz() {
        return Varps.get(INDIVIDUAL_TRACKER_VARP) != 0;
    }

    private static boolean hasCompletedQuiz() {
        return Varps.get(GLOBAL_TRACKER_VARP) == 2076;
    }

    private static boolean hasClaimedReward() {
        return Varps.getBitValue(3688) == 1;
    }

    private static boolean quizInterfaceIsOpen() {
        return Interfaces.getComponent(QUIZ_INTERFACE_ID, QUESTION_CHILD_ID) != null;
    }
}
