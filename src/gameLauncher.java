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
import java.util.Random;
import java.util.Scanner;

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

        map = new MyMap(numTeam1); //////////////////////////////////////

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

        } catch (StaleProxyException ignored) {
        }

        DFAgentDescription[] result = null;
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
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
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
    public static void killAgents() {
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
        catch (Exception ignored) {}
    }


    private Box[][] createMap () {

        // Set dimensions for map
        int w, h;
        System.out.println("Give Dimensions width, height: ");
        w = new Scanner(System.in).nextInt();
        h = new Scanner(System.in).nextInt();

        Box[][] map = new Box[h][w];

        // Random generate map
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                int choice = new Random().nextInt(2);
                if (choice == 0)
                    map[i][j] = new Box('O');
                else
                    map[i][j] = new Box('N');
            }
        }

        // Set (0, 0) to 'N' to be able to be starting point
        map[0][0].setContent('N');

        // Set destination point
        int posX = new Random().nextInt(w);
        int posY = new Random().nextInt(h);
        map[posY][posX].setContent('X');


        // Check if there is a path to destination
        if (!checkPath(map, new Point(0, 0)))
            invert(map);

        return map;
    }

    private void invert (Box[][] map) {

        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[0].length; j++) {
                Point z = new Point(i, j);
                if (map[z.x][z.y].getContent() == 'O')
                    map[z.x][z.y].setContent('N');
                else if (map[z.x][z.y].getContent() == 'N')
                    map[z.x][z.y].setContent('O');


            }
        }

        map[0][0].setContent('N');
    }

    private boolean checkPath (Box[][] mapForCheck, Point position) {

        ArrayList<Point> closedSet = new ArrayList<>();
        ArrayList<Point> openSet = new ArrayList<>();
        ArrayList<Integer> openSetValue = new ArrayList<>();
        openSet.add(position);
        openSetValue.add(0);

        while (openSet.size() != 0) {

            // Find the min value
            int minIndex = openSetValue.indexOf(Collections.min(openSetValue));

            // If point exists in closedSet, remove and continue
            if (closedSet.contains(openSet.get(minIndex))) {
                openSet.remove(minIndex);
                openSetValue.remove(minIndex);
                continue;
            }

            // If this is the solution, return the next box to move
            Point sol = openSet.get(minIndex);
            if (mapForCheck[sol.x][sol.y].getContent() == 'X') {
                return true;
            }

            // Produce possible moves
            ArrayList<Point> children = expand(mapForCheck, openSet.get(minIndex));

            // Check if there are not new moves
            if (children.size() == 0) {
                closedSet.add(openSet.get(minIndex));
                openSet.remove(minIndex);
                openSetValue.remove(minIndex);

                continue;
            }

            // Evaluate moves
            ArrayList<Integer> childrenValue = new ArrayList<>();
            ArrayList<Point> childrenNextMove = new ArrayList<>();

            for (Point child : children) {
                int realDist = dist(position, child);
                childrenValue.add(realDist);
            }


            // Put moves for checking
            openSet.addAll(children);
            openSetValue.addAll(childrenValue);


            // Put current box to closedSet and remove it from everywhere
            closedSet.add(openSet.get(minIndex));
            openSet.remove(minIndex);
            openSetValue.remove(minIndex);
        }

        return false;

    }

    private ArrayList<Point> expand (Box[][] mapForCheck, Point parent) {

        ArrayList<Point> expand = new ArrayList<>();

        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                if (i == 0 && j == 0)
                    continue;
                Point child = new Point(parent.x + i, parent.y + j);

                if (pointInMap(child, mapForCheck.length, mapForCheck[0].length)
                    && mapForCheck[child.x][child.y].getContent() != 'O') {
                    expand.add(child);
                }
            }
        }

        return expand;

    }

    private boolean pointInMap (Point value, int sizeX, int sizeY) {
        return value.x >= 0 && value.y >= 0 && value.x < sizeX && value.y < sizeY;
    }

    private int dist (Point pos1, Point pos2) {

        int distX = Math.abs(pos1.x - pos2.x);
        int distY = Math.abs(pos1.y - pos2.y);

        return Math.max(distX, distY);
    }
}

