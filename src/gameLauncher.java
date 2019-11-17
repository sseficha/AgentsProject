import agents.Agent1;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class gameLauncher {

    public static Map map = new Map();
    public static ArrayList<Point> agents = new ArrayList<>();
    public static JFrame gameGraphics;

    public static void main(String[] args) {
        Runtime runtime = Runtime.instance();
        Profile config = new ProfileImpl("localhost", 8080, null);
        config.setParameter("gui", "true");
        AgentContainer mainContainer = runtime.createMainContainer(config);
        AgentController ctrl1,ctrl2;

        ///gameLauncher game = new gameLauncher();
        try {
            System.out.println("START");
            String[] arg1 = new String[2];
            arg1[0] = "team1";
            arg1[1] = "0";
            ctrl1 = mainContainer.createNewAgent("player1", Player.class.getName(),arg1);
            String[] arg2 = new String[2];
            arg2[0] = "team1";
            arg2[1] = "1";
            ctrl2 = mainContainer.createNewAgent("player2",Player.class.getName(),arg2);

            System.out.println("AGENTS OK");


            JFrame gameGraphics = new JFrame("TREASURE HUNT!");
            gameGraphics.pack();
            gameGraphics.setSize(200,200);
            gameGraphics.add(map);
            gameGraphics.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            gameGraphics.setVisible(true);

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            ctrl1.start();
            ctrl2.start();
            
            agents.add(new Point(0,0));
            agents.add(new Point(0,0));

            map.initializeAgentPos(agents);

        } catch (StaleProxyException ex) {
        }


    }


}
