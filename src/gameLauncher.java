import jade.core.AID;
import jade.core.Agent;
import javax.swing.*;

public class gameLauncher {

    public static void main(String[] args)
    {
        Map map = new Map();
        JFrame gameGraphics = new JFrame("TREASURE HUNT!");
        gameGraphics.setSize(200,200);
        gameGraphics.add(map);
        gameGraphics.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameGraphics.pack();
        gameGraphics.setVisible(true);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
