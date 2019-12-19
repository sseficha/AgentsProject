import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class gameLauncher extends Agent {

    public static final int SIZE = 100;
    public static final int SIGHT = 5;
    public static final int TIME = 10;
    public static final int DEFAULTPERCENTAGEOFOBSTACLES = 30;

    public static MyMap map;
    public ArrayList<Point> agents = new ArrayList<>();
    public JFrame gameGraphics;
    public Runtime runtime = Runtime.instance();
    public Profile config = new ProfileImpl("localhost", 8080, null);
    public AgentContainer mainContainer = runtime.createMainContainer(config);
    public int numTeam1;
    public int numTeam2;
    public static AgentController[] ctrl1; //prosorina static
    public static AgentController[] ctrl2; //prosorina static
    public static Stats stats;


    @Override
    protected void setup () {
        config.setParameter("gui", "true");
        System.out.println("START");

        // Register gameLauncher
        //====???====
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("master");
        sd.setName(getName());
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
            //System.out.println("register ok");
        } catch (FIPAException fe) {
            System.out.println("register not ok");
            fe.printStackTrace();
        }
        //====???====

        Object[] arguments = getArguments();
        numTeam1 = Integer.parseInt(arguments[0].toString());
        numTeam2 = Integer.parseInt(arguments[1].toString());

        ctrl1 = new AgentController[numTeam1];
        ctrl2 = new AgentController[numTeam2];

        map = new MyMap(numTeam1, SIZE, DEFAULTPERCENTAGEOFOBSTACLES);

        try {
            for (int i = 0; i < numTeam1 + numTeam2; i++)
                agents.add(new Point(0, 0));

            map.initializeAgentPos(agents);
            for (int i = 0; i < numTeam1; i++) {
                String[] arg1 = new String[6];
                arg1[0] = "team1";
                arg1[1] = Integer.toString(i);
                arg1[2] = Integer.toString(i);
                arg1[3] = String.valueOf(SIGHT);
                arg1[4] = "0,0";
                arg1[5] = Integer.toString((i + 1) * TIME);
                ctrl1[i] = mainContainer.createNewAgent("player1-" + i, Player.class.getName(), arg1);
            }

            for (int i = 0; i < numTeam2; i++) {
                String[] arg2 = new String[6];
                arg2[0] = "team2";
                arg2[1] = Integer.toString(i + numTeam1);
                arg2[2] = Integer.toString(i);
                arg2[3] = String.valueOf(SIGHT);
                arg2[4] = "0,0";
                arg2[5] = Integer.toString((i + 1) * TIME);
                ctrl2[i] = mainContainer.createNewAgent("player2-" + i, Player.class.getName(), arg2);
            }

            // Start agents
            for (int i = 0; i < numTeam1; i++)
                ctrl1[i].start();

            for (int i = 0; i < numTeam2; i++)
                ctrl2[i].start();

            stats = new Stats("output.txt");


            // Initialize graphics
            gameGraphics = new JFrame("TREASURE HUNT!");
            gameGraphics.setExtendedState(JFrame.MAXIMIZED_BOTH);
            gameGraphics.pack();
            gameGraphics.setLocationRelativeTo(null);
            gameGraphics.add(map);
            gameGraphics.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            gameGraphics.setVisible(true);

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } catch (StaleProxyException ignored) {
        }


        // Send to all agents a message that the game has started
        DFAgentDescription[] result;
        DFAgentDescription template = new DFAgentDescription();
        sd = new ServiceDescription();
        sd.setType("playing");
        template.addServices(sd);
        try {
            result = DFService.search(this, template);

            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.setContent("GAME STARTED");
            for (DFAgentDescription dfAgentDescription : result) {
                msg.addReceiver(dfAgentDescription.getName());
                send(msg);
            }
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }


        // If the treasure has been found, stop
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action () {
                ACLMessage msg = myAgent.receive();

                if (msg != null) {
                    String s = msg.getContent();
                    if (s.equals("FOUND TREASURE")) {
                        JOptionPane.showMessageDialog(gameGraphics, s, "WINNER!", JOptionPane.INFORMATION_MESSAGE);
                        System.exit(1);
                    }
                } else {
                    block();
                }
            }
        });
    }


    //prosorina static
    public static void killAgents () {

        stats.save();
        System.out.println(stats.calculateStats());

        for (AgentController agentController : ctrl1) {
            try {
                agentController.kill();
            } catch (StaleProxyException ex) {
                System.out.println("Something wrong when killing the agent!!!");
            }
        }

        for (AgentController agentController : ctrl2) {
            try {
                agentController.kill();
            } catch (StaleProxyException ex) {
                System.out.println("Something wrong when killing the agent!!!");
            }
        }

    }

    @Override
    protected void takeDown () {
        try {
            DFService.deregister(this);
            //killAgents();
        } catch (Exception ignored) {
        }
    }

}