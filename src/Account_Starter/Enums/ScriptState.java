package Account_Starter.Enums;


import Account_Starter.Executes.*;
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

    EQUIPPING_GEAR{
        @Override
        public void execute() {
            Equipping_Gear.execute();
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
        public void execute() { Walking.execute(); }
    };


    public abstract void execute();
}






