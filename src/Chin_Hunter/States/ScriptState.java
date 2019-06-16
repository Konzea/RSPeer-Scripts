package Chin_Hunter.States;


import Chin_Hunter.Executes.*;
import Chin_Hunter.Executes.Hunting.*;
import org.rspeer.runetek.api.commons.Time;

public enum ScriptState {
    //TODO Look at inheriting from a 'State' class instead of enums pointing to classes

    STARTING{
        @Override
        public void execute() {
            //Wait for login bot to finish it's shit before accessing potentially unloaded objects
            Time.sleep(3000);
            Starting.execute();
        }

        @Override
        public void onStart() {

        }
    },

    BANKING{
        @Override
        public void execute() {
            Banking.execute();
        }

        @Override
        public void onStart() {

        }
    },

    PURCHASE_ITEMS{
        @Override
        public void execute() {
            PurchaseItems.execute();
        }

        @Override
        public void onStart() {

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
    },

    LONGTAILS{
        @Override
        public void execute() {
            Longtails.execute();
        }

        @Override
        public void onStart() {

        }
    },

    BUTTERFLIES{
        @Override
        public void execute() {
            Butterflies.execute();
        }

        @Override
        public void onStart() {

        }
    },

    TRAPFALL_KEBBITS{
        @Override
        public void execute() { DeadfallKebbits.execute(); }

        @Override
        public void onStart() {

        }
    },

    FALCON_KEBBITS{
        @Override
        public void execute() { FalconKebbits.execute(); }

        @Override
        public void onStart() {

        }
    },

    EAGLES_PEAK_QUEST{
        @Override
        public void execute() { EaglesPeakQuest.execute(); }

        @Override
        public void onStart() {

        }
    },

    CHINCHOMPAS{
        @Override
        public void execute() { Chinchompas.execute(); }

        @Override
        public void onStart() {

        }
    };


    public abstract void execute();

    public abstract void onStart();
}






