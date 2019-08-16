package Chin_Hunter.Helpers;

import Chin_Hunter.Executes.Hunting.DeadfallKebbits;
import Chin_Hunter.Main;
import org.rspeer.runetek.api.component.tab.Skill;
import org.rspeer.runetek.api.component.tab.Skills;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.event.types.RenderEvent;
import org.rspeer.ui.Log;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ConcurrentModificationException;

public class Paint {

    private static long startTime;
    private static int hunterStartXP;
    private static int hunterStartLevel;
    private static BufferedImage paintOn = null;
    private static BufferedImage paintOff = null;
    private static BufferedImage herbloreOff = null;
    private static BufferedImage prayerOff = null;

    public static boolean canDisplayPaint = false;


    public Paint(){
        startTime = System.currentTimeMillis();
        hunterStartXP = Skills.getExperience(Skill.HUNTER);
        hunterStartLevel = Skills.getLevel(Skill.HUNTER);
        try {
            paintOn = ImageIO.read(new URL("https://i.gyazo.com/fb7231025f8c3cb4b20927cbfce57f74.png"));
            paintOff = ImageIO.read(new URL("https://i.gyazo.com/8bd93deae5104ef7849bd172d9c6f981.png"));
            prayerOff = ImageIO.read(new URL("https://i.gyazo.com/ed8f60478cac178d2750b0adc5eec305.png"));
            herbloreOff = ImageIO.read(new URL("https://i.gyazo.com/74e84f9ab024158b54483cebdd71cb4d.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (paintOn == null || paintOff == null || prayerOff == null || herbloreOff == null)
            Log.severe("Unable to load paint. Honestly your not missing much.");
        else
            canDisplayPaint = true;
    }

    public void Render(RenderEvent e) {
        Graphics g = e.getSource();
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int y = 344;
        int x = 6;
        if (!canDisplayPaint){
            if (paintOff != null)
                g2.drawImage(paintOff, null, x,y);
            return;
        }

        g2.setColor(Color.WHITE);
        if (paintOn != null)
            g2.drawImage(paintOn, null, x,y);

        if (!Main.canBuryBones() && prayerOff != null)
            g2.drawImage(prayerOff, null, x,y);
        if (!Main.canTrainHerblore() && herbloreOff != null)
            g2.drawImage(herbloreOff, null, x,y);

        int xpGained = Skills.getExperience(Skill.HUNTER) - hunterStartXP;
        int levelsGained = Skills.getLevel(Skill.HUNTER) - hunterStartLevel;
        long runTime = System.currentTimeMillis() - startTime;

        g2.drawString("Traps Placed: " + Trapping.getPlacedTrapsCount(), x += 13, y += 52);
        g2.drawString("Our Deadfall: " + DeadfallKebbits.deadfallIsOurs, x, y += 15);
        g2.setColor(Color.YELLOW);
        g2.drawString("Chins Caught: 0", x, y += 15);
        g2.drawString("Chins Per Hour: 0", x, y += 15);

        g2.setColor(Color.WHITE);
        g2.drawString("XP Gained: " + xpGained, x +=157, y -= 45);
        g2.drawString("XP Per Hour: " + getXPPerHour(xpGained, runTime), x, y += 15);
        g2.drawString("Levels Gained: " + levelsGained, x, y += 15);

        g2.setColor(Color.CYAN);
        g2.drawString("Run Time: " + formatTime(runTime), x +=157,y -= 20);
        g2.drawString("XP Gained: " + xpGained, x, y += 16);

        String currentState = Main.getCurrentState() == null?"null":Main.getCurrentState().name();
        g2.drawString("State: " + currentState, x, y += 16);

        if (Trapping.getPlacedTrapsCount() > 0) {
            try {
                for (Position pos : Trapping.getTrapLocations())
                    pos.outline(g2);
            } catch (ConcurrentModificationException ignored) {
            }
        }
    }

    private String getXPPerHour(int inXPGained, long inMillisecondsRan){
        double xpPerMillisecond = (inXPGained / (double)(inMillisecondsRan));
        return NumberFormat.getIntegerInstance().format((int)(xpPerMillisecond * 1000 * 60 * 60));
    }

    private String formatTime(long inMilliseconds){
        long second = (inMilliseconds / 1000) % 60;
        long minute = (inMilliseconds / (1000 * 60)) % 60;
        long hour = (inMilliseconds / (1000 * 60 * 60)) % 24;

        return String.format("%02d:%02d:%02d", hour, minute, second);
    }

}
