package Combat_Starter.Enums;


import Combat_Starter.Executes.*;
import org.rspeer.runetek.api.commons.Time;

public enum ScriptState {
    STARTING{
        @Override
        public void execute() {
            //Wait for login bot to finish it's shit before accessing potentially unloaded objects
            Time.sleep(3000);
            Starting.execute();
        }
    },

    BANKING{
        @Override
        public void execute() {
            Banking.execute();
        }
    },

    GETTING_GEAR{
        @Override
        public void execute() {
            Getting_Gear.execute();
        }
    },

    FIGHTING{
        @Override
        public void execute() {
            Fighting.execute();
        }
    },

    WALKING{
        @Override
        public void execute() { Walking.excecute(); }
    };


    public abstract void execute();
}






