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


    @Override
    protected void setup() {
        config.setParameter("gui", "true");
        System.out.println("START");

        //====???====
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("master");
        sd.setName(getName());
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
            System.out.println("register ok");
        } catch (
                FIPAException fe) {
            System.out.println("register not ok");
            fe.printStackTrace();
        }
        //====???====

        Object[] arguments=getArguments();
        numTeam1 = Integer.parseInt(arguments[0].toString());
        numTeam2 = Integer.parseInt(arguments[1].toString());

        ctrl1 = new AgentController[numTeam1];
        ctrl2 = new AgentController[numTeam2];

        map = new MyMap(numTeam1);

        int id=0;

        try {
            for(int i=0;i<numTeam1+numTeam2;i++)
                agents.add(new Point(0,0));

            map.initializeAgentPos(agents);
            for(int i=0;i<numTeam1;i++){
                System.out.println("sssss");
                String[] arg1 = new String[2];
                arg1[0] = "team1";
                arg1[1] = Integer.toString(id);
                id++;
                ctrl1[i] = mainContainer.createNewAgent("player1-"+ Integer.toString(i), Player.class.getName(),arg1);
            }
            for(int i=0;i<numTeam2;i++) {
                System.out.println("tttt");
                String[] arg2 = new String[2];
                arg2[0] = "team2";
                arg2[1] = Integer.toString(id);
                id++;
                ctrl2[i] = mainContainer.createNewAgent("player2-"+ Integer.toString(i), Player.class.getName(), arg2);
            }
            System.out.println("AGENTS OK");

            for(int i=0;i<numTeam1;i++)
                ctrl1[i].start();

            for(int i=0;i<numTeam2;i++)
                ctrl2[i].start();


            gameGraphics = new JFrame("TREASURE HUNT!");
            gameGraphics.setExtendedState(JFrame.MAXIMIZED_BOTH);
            gameGraphics.pack();
            gameGraphics.setLocationRelativeTo(null);
            gameGraphics.add(map);
            gameGraphics.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            gameGraphics.setVisible(true);

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } catch (StaleProxyException ex) {
        }

        DFAgentDescription[] result = null;
        DFAgentDescription template = new DFAgentDescription();
        sd = new ServiceDescription();
        sd.setType("playing");
        template.addServices(sd);
        try {
            result = DFService.search(this, template);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }


        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setContent("GAME STARTED");
        for(int i=0;i<result.length;i++){
            msg.addReceiver(result[i].getName());
            send(msg);
        }

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = myAgent.receive();

                if (msg != null) {
                    String s = msg.getContent();
                    System.out.println(s);
                    if(s.equals("FOUND TREASURE"))
                    {
                        JOptionPane.showMessageDialog(gameGraphics, s, "WINNER!",JOptionPane.INFORMATION_MESSAGE);
                        System.exit(1);
                    }
                }else {
                    System.out.println("NULL");
                    block();
                }
            }
        });
    }


    //prosorina static
    public static void killAgents()
    {
        for(int i=0;i<ctrl1.length;i++) {
            try {
                ctrl1[i].kill();
            } catch (StaleProxyException ex) {
                System.out.println("!!!");
            }
        }

        for(int i=0;i<ctrl2.length;i++) {
            try {
                ctrl2[i].kill();
            } catch (StaleProxyException ex) {
                System.out.println("!!!");
            }
        }

    }

    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
            //killAgents();
        }
        catch (Exception e) {}
    }
}

