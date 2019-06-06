package Combat_Starter.Helpers;

import org.rspeer.runetek.adapter.component.InterfaceComponent;
import org.rspeer.runetek.api.component.Interfaces;
import org.rspeer.runetek.api.component.tab.Combat;
import org.rspeer.ui.Log;

import java.util.ArrayList;
import java.util.List;

public class CombatStyle {

    //Interfaces for all 4 possible attack styles
    private final static InterfaceComponent[] attackStyleInterfaces = {
            Interfaces.getComponent(593, 4),
            Interfaces.getComponent(593, 8),
            Interfaces.getComponent(593, 12),
            Interfaces.getComponent(593, 16)
    };

    /**
     * Attempts to set the players attack style.
     * @param inAttStyle The attack style to attempt to switch to.
     * @return Returns true if successful, false if attack style is not available.
     */
    public static boolean setAttackStyle(Combat.AttackStyle inAttStyle){
        Combat.AttackStyle[] availableStyles = getAvailableAttackStyles();
        for (int i = 0; i < availableStyles.length; i++){
            if (availableStyles[i] == inAttStyle){
                Combat.select(i);
                return true;
            }
        }
        return false;
    }

    /**
     * Gets all your players currently available attack styles.
     * @return Returns an ordered AttackStyle enum array.
     */
    public static Combat.AttackStyle[] getAvailableAttackStyles(){
        List<Combat.AttackStyle> output = new ArrayList();
        for (InterfaceComponent i: attackStyleInterfaces) {
            //If it is a valid available attack style
            if (i != null && !i.isExplicitlyHidden()){
                //Parse what kind of attack style it is
                Object[] Listeners = i.getHoverListeners();
                output.add(parseStyleFromListeners(Listeners));
            }
        }
        return output.toArray(new Combat.AttackStyle[0]);
    }

    /**
     * Gets the attack style currently selected.
     * @return Returns the selected attack style as an AttackStyle enum.
     */
    public static Combat.AttackStyle getAttackStyle(){
        //This still works afaik
        return Combat.getAttackStyle();
    }


    //Parses the AttackStyle from interface hover listeners
    private static Combat.AttackStyle parseStyleFromListeners(Object[] inListeners){
        for (Object o: inListeners) {
            //Find the object that contains the info we need
            if (o.toString().contains("<br>")){
                //Format '(Defensive)<br>Stab<br>Trains Attack)' into just 'DEFENSIVE'
                String rawString = o.toString().split("<")[0];
                String processedString = rawString
                        .replace('(',' ')
                        .replace(')',' ').trim().toUpperCase();
                try {
                    //Converts the processed string into an AttackStyle enum and returns it
                    return Combat.AttackStyle.valueOf(processedString);
                }catch (IllegalArgumentException e){
                    //If could not find matching enum return null
                    Log.severe(e);
                    return null;
                }
            }
        }
        return null;
    }
}
