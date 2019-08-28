package Chin_Hunter.Hunter.Trap_Admin;

import org.jetbrains.annotations.Nullable;

public enum TrapType {

    BOX_TRAP("Box trap", "Lay", 50),
    BIRD_SNARE("Bird snare", "Lay", 50),
    DEADFALL("Deadfall", "Set-trap", 80);

    private final long TimeoutMs;
    public long getTimeoutMs(){
        return TimeoutMs;
    }

    private final String Name;
    public String getName() {
        return Name;
    }

    private final String Action;
    public String getAction() {
        return Action;
    }

    TrapType(String name, String action, long timeoutSeconds) {
        this.Name = name;
        this.Action = action;
        this.TimeoutMs = timeoutSeconds * 1000;
    }


    @Nullable
    public static TrapType getTypeFromName(String trapName){
        for (TrapType type : TrapType.values()){
            if (type.getName().equalsIgnoreCase(trapName))
                return type;
        }
        return null;
    }
}
