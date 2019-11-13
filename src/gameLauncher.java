import agents.Agent1;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

import javax.swing.*;

public class gameLauncher {

    public static void main(String[] args) {
        Runtime runtime = Runtime.instance();
        Profile config = new ProfileImpl("localhost", 8888, null);
        config.setParameter("gui", "true");
        AgentContainer mainContainer = runtime.createMainContainer(config);
        AgentController ctrl1,ctrl2;

        gameLauncher game = new gameLauncher();
        try {
            System.out.println("START");
            String[] arg1 = new String[1];
            arg1[0] = "team1";
            ctrl1 = mainContainer.createNewAgent("player1", Agent1.class.getName(),arg1);
            String[] arg2 = new String[1];
            arg2[0] = "team1";
            ctrl2 = mainContainer.createNewAgent("player2",Agent1.class.getName(),arg2);

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
            
            ctrl1.start();
            ctrl2.start();

        } catch (StaleProxyException ex) {
        }


    }


}
