package Chin_Hunter.Executes;

import Chin_Hunter.Main;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.rspeer.runetek.adapter.component.InterfaceComponent;
import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.adapter.scene.Player;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.Varps;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.Dialog;
import org.rspeer.runetek.api.component.Interfaces;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.api.scene.SceneObjects;
import org.rspeer.ui.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Paths;

public class MuseumQuiz {

    private static final int globalTrackerVarp = 1010;
    private static final int individualTrackerVarp = 1014;

    private static final int quizInterfaceID = 533;
    private static final int questionChildID = 28;
    private static final int[] answerChildIDs = {29, 30, 31};

    private static final Area museumBasement = Area.rectangular(1724, 4993, 1794, 4927);
    private static final Position stairsDown = new Position(3255,3451,0);
    private static final Position stairsUp = new Position(1758,4959,0);

    private static JsonObject quizData;


    private MuseumQuiz() {
        //Private default constructor
    }

    public static void onStart() {
        Gson gson = new Gson();
        try {

            File jsonFile = Paths.get("C:\\Users\\willc\\Desktop\\Bot Development\\RSPeer Scripts\\src\\Chin_Hunter\\Data\\MuseumData.json").toFile();
            quizData = gson.fromJson(new FileReader(jsonFile), JsonObject.class);

        } catch (FileNotFoundException e) {
            Log.severe(e);
            e.printStackTrace();
            Main.updateScriptState(null);
        }

    }

    public static void execute() {
        Player local = Players.getLocal();
        if (!hasClaimedReward()) {
            if (museumBasement.contains(local)) {
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
                SceneObject stairs = SceneObjects.getFirstAt(stairsDown);
                if (stairs == null) {
                    Movement.walkTo(stairsDown);
                    Time.sleep(219, 612);
                } else {
                    if (stairs.interact("Walk-down"))
                        Time.sleepUntil(()->museumBasement.contains(local), 4000);
                }
            }
        }else{
            //Done with the quiz
            if (museumBasement.contains(local)){
                //Leave
                SceneObject stairs = SceneObjects.getFirstAt(stairsUp);
                if (stairs == null) {
                    Movement.walkTo(stairsUp);
                    Time.sleep(219, 612);
                } else {
                    if (stairs.interact("Walk-up"))
                        Time.sleepUntil(()->!museumBasement.contains(local), 2000);
                }
            }else{
                Log.fine("Finished");
                Time.sleep(2000);
                //TODO Update State
            }
        }

    }

    private static void startQuiz(){
        if (Dialog.isOpen()){
            if (Dialog.canContinue()) {
                if (Dialog.processContinue()) {
                    Time.sleep(366, 566);
                    Time.sleepUntil(() -> !Dialog.isProcessing(), 2000);
                }
            }else{
                InterfaceComponent option = Dialog.getChatOption(x->x.equals("Sure thing."));
                if (option != null){
                    if (option.interact("Continue")){
                        Time.sleep(366, 566);
                        Time.sleepUntil(() -> !Dialog.isProcessing(), 2000);
                    }
                }
            }
        }else{
            Npc rewardNPC = Npcs.getNearest("Orlando Smith");
            if (rewardNPC != null){
                if (rewardNPC.interact("Talk-to"))
                    Time.sleepUntil(Dialog::isOpen, 4000);
            }else
                Log.severe("Could not find npc to start the quiz");

        }
    }

    private static void claimReward(){
        if (Dialog.isOpen()){
            if (Dialog.processContinue()){
                Time.sleep(366, 566);
                Time.sleepUntil(() -> !Dialog.isProcessing(), 2000);
            }
        }else{
            Npc rewardNPC = Npcs.getNearest("Orlando Smith");
            if (rewardNPC != null){
                if (rewardNPC.interact("Talk-to"))
                    Time.sleepUntil(Dialog::isOpen, 4000);
            }else
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
            InterfaceComponent questionInterface = Interfaces.getComponent(quizInterfaceID, questionChildID);
            if (questionInterface == null)
                return;
            String question = questionInterface.getText();
            String answer = displayCaseData.get("Questions").getAsJsonObject().get(question).getAsString();

            Boolean answerFound = false;
            //Loop through all answers and click the one that matches above string parsed from json
            for (int i = 0; i < answerChildIDs.length; i++) {
                InterfaceComponent answerInterface = Interfaces.getComponent(quizInterfaceID, answerChildIDs[i]);
                //Have to use contains and not equals because the quizes have '.' at the end
                if (answerInterface != null && answerInterface.getText().contains(answer)) {
                    if (answerInterface.interact("Ok")) {
                        Time.sleepUntil(() -> !quizInterfaceIsOpen(), 2000);
                        answerFound = true;
                        break;
                    }
                }
            }
            if (!answerFound){
                Log.severe("Could not find answer to the question: " + question);
                Main.updateScriptState(null);
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
                    Time.sleepUntil(MuseumQuiz::quizInterfaceIsOpen, 4000);
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
        return Varps.get(individualTrackerVarp) != 0;
    }

    private static boolean hasCompletedQuiz() {
        return Varps.get(globalTrackerVarp) == 2076;
    }

    private static boolean hasClaimedReward() {
        return Varps.getBitValue(3688) == 1;
    }

    private static boolean quizInterfaceIsOpen() {
        return Interfaces.getComponent(quizInterfaceID, questionChildID) != null;
    }
}
