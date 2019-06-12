package Master_Thiever.Enums;


import Master_Thiever.Executes.*;
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

    THIEVING{
        @Override
        public void execute() {
            Thieving.execute();
        }
    },

    WALKING{
        @Override
        public void execute() { Walking.execute(); }
    },

    MULING{
        @Override
        public void execute() { Muling.execute(); }
    };


    public abstract void execute();
}






