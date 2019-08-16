package Chin_Hunter.States;


import Chin_Hunter.Executes.*;
import Chin_Hunter.Executes.Herblore.Druidic_Ritual;
import Chin_Hunter.Executes.Herblore.Herblore_Training;
import Chin_Hunter.Executes.Hunting.*;
import Chin_Hunter.Executes.Eagles_Peak.QuestMain;
import Chin_Hunter.Helpers.RequiredItem;

public enum ScriptState {

    STARTING{
        @Override
        public void execute() {
            Starting.execute();
        }

        @Override
        public void onStart() {
        }

        @Override
        public RequiredItem[] getItemsToBuy() {
            return null;
        }
    },

    BANKING{
        @Override
        public void execute() {
            Banking.execute();
        }

        @Override
        public void onStart() {
            Banking.onStart();
        }

        @Override
        public RequiredItem[] getItemsToBuy() {
            return null;
        }
    },

    MUSEUM_QUIZ{
        @Override
        public void execute() {
            MuseumQuiz.execute();
        }

        @Override
        public void onStart() {
            MuseumQuiz.onStart();
        }

        @Override
        public RequiredItem[] getItemsToBuy() {
            return null;
        }
    },

    LONGTAILS{
        @Override
        public void execute() {
            Longtails.execute();
        }

        @Override
        public void onStart() {
            Longtails.onStart();
        }

        @Override
        public RequiredItem[] getItemsToBuy() {
            return RequiredItem.concat(DeadfallKebbits.getRequiredItems(),FalconKebbits.getRequiredItems());
        }
    },

    BUTTERFLIES{
        @Override
        public void execute() {
            Butterflies.execute();
        }

        @Override
        public void onStart() {
            Butterflies.onStart();
        }

        @Override
        public RequiredItem[] getItemsToBuy() {
            return RequiredItem.concat(DeadfallKebbits.getRequiredItems(),FalconKebbits.getRequiredItems());
        }
    },

    DEADFALL_KEBBITS {
        @Override
        public void execute() { DeadfallKebbits.execute(); }

        @Override
        public void onStart() {
            DeadfallKebbits.onStart();
        }

        @Override
        public RequiredItem[] getItemsToBuy() {
            return RequiredItem.concat(DeadfallKebbits.getRequiredItems(),FalconKebbits.getRequiredItems());
        }
    },

    FALCON_KEBBITS{
        @Override
        public void execute() { FalconKebbits.execute(); }

        @Override
        public void onStart() {
            FalconKebbits.onStart();
        }

        @Override
        public RequiredItem[] getItemsToBuy() {
            return RequiredItem.concat(DeadfallKebbits.getRequiredItems(),FalconKebbits.getRequiredItems());
        }
    },

    EAGLES_PEAK_QUEST{
        @Override
        public void execute() { QuestMain.execute(); }

        @Override
        public void onStart() {
            QuestMain.onStart();
        }

        @Override
        public RequiredItem[] getItemsToBuy() {
            return QuestMain.getRequiredItems();
        }
    },

    DRUIDIC_RITUAL_QUEST{
        @Override
        public void execute() {
            Druidic_Ritual.execute();
        }

        @Override
        public void onStart() {
            Druidic_Ritual.onStart();
        }

        @Override
        public RequiredItem[] getItemsToBuy() {
            return Druidic_Ritual.getRequiredItems();
        }
    },

    HERBLORE_TRAINING{
        @Override
        public void execute() {
            Herblore_Training.execute();
        }

        @Override
        public void onStart() {
            Herblore_Training.onStart();
        }

        @Override
        public RequiredItem[] getItemsToBuy() {
            return Herblore_Training.getRequiredItems();
        }
    },

    CHINCHOMPAS{
        @Override
        public void execute() { Chinchompas.execute(); }

        @Override
        public void onStart() {
            Chinchompas.onStart();
        }

        @Override
        public RequiredItem[] getItemsToBuy() {
            return Chinchompas.getRequiredItems();
        }
    };

    public abstract void execute();

    public abstract void onStart();

    public abstract RequiredItem[] getItemsToBuy();
}






