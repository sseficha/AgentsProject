import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import sun.awt.windows.ThemeReader;

import javax.swing.*;
import java.awt.*;
import java.util.*;

public class Player extends Agent {
    private static final int UNEXPLORED_VALUE = 5;
    private static final int AGENT_VALUE = -1;
    private static final int NONE_VALUE = -3;
    private static final int OBJECT_VALUE = -5;
    private static final int X_VALUE = 10;

    private ArrayList<DFAgentDescription> teammates;
    private int id;
    private String team;
    private Point position;
    private int sight;  //optiko pedio
    private MyMap map;
    protected void setup() {

        teammates = new ArrayList<>();

        // all next are hardcoded for now
        position = new Point(0,0);
        sight=2;        //can see 2 boxes away
        map = new MyMap();        //hardcoded path+name for now
        //must be initialized in game launcher and passed as parameter to Player


        id = Integer.parseInt(getArguments()[1].toString());
        System.out.println("ID == "+id);
        team = getArguments()[0].toString();
        
        //add agent to Yellow Pages
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("playing");
        sd.setName(getName());
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (
                FIPAException fe) {
            fe.printStackTrace();
        }

        //get a list of other agents in yellow pages (teammates)
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
        for(int i=0;i<result.length;i++)
        {
            if(!result[i].getName().equals(this.getName()))
                teammates.add(result[i]);
        }
        //test
//        for (int i=0;i<teammates.size();i++)
//            System.out.println("Teammate of player "+getName()+" is: "+teammates.get(i).getName());


        //add Behaviors

        //Communication Behavior (Must update known/unknown map)
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ArrayList<Point> surroundings;
                ACLMessage msg = myAgent.receive();
                if (msg != null) {
                    surroundings=unformat(msg.getContent());
                    for (int i=0;i<surroundings.size();i++) {
                        map.explore(surroundings.get(i).x, surroundings.get(i).y);
                        System.out.print("("+surroundings.get(i).x+","+surroundings.get(i).y+") ");
                    }
                    System.out.println("-------------"+myAgent.getName());

                }
                else {
                    block();
                }
            }
        });

        //Look/Send/Evaluate/Move Behavior
        addBehaviour(new TickerBehaviour(this,1500) {
            String msg;
            @Override
            protected void onTick() {
                msg=look();
                send(msg);
                Point nextPos = new Point(evaluate());
                move(nextPos);
            }
        });
    }

    public String look(){
        ArrayList<Point> surroundings = new ArrayList<>();
//        int num=(int)Math.pow(2*sight+1,2);
        for (int i=-sight;i<=sight;i++)
        {
            if (position.x+i<0 || position.x+i>=map.lengthX())
                continue;
            for (int j=-sight;j<=sight;j++)
            {
                if (position.y+j<0 || position.y+j>=map.lengthY())
                    continue;
                surroundings.add(new Point(position.x+i,position.y+j));
            }
        }
        for (int i=0;i<surroundings.size();i++) {
            map.explore(surroundings.get(i).x, surroundings.get(i).y);
//            System.out.println("("+surroundings.get(i).x+","+surroundings.get(i).y+") ");
        }


//        for (int i=0;i<map.lengthX();i++) {
//            for (int j = 0; j < map.lengthY(); j++) {
//                System.out.print(map.getBox(new Point(i, j)).explored ? 1 : 0 + "");
//            }
//            System.out.println();
//        }


        return format(surroundings);

    }

    public String format(ArrayList<Point> surroundings){
        StringBuilder msg=new StringBuilder();
        String x,y;
        for (int i=0;i<surroundings.size();i++){
            x=String.valueOf(surroundings.get(i).x);
            y=String.valueOf(surroundings.get(i).y);
            msg.append(x+y);
        }
        return msg.toString();
    }

    public ArrayList<Point> unformat(String surroundings) {
        int x, y;
        ArrayList<Point> u_surroundings = new ArrayList<>();
        for (int i = 0; i < surroundings.length(); i += 2) {
            x = Character.getNumericValue(surroundings.charAt(i));
            y = Character.getNumericValue(surroundings.charAt(i + 1));
            u_surroundings.add(new Point(x, y));
        }
        return u_surroundings;
    }

    public void send(String content){

        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        for(int i=0;i<teammates.size();i++)
            msg.addReceiver(teammates.get(i).getName());    //+ agent with gui
        msg.setContent(content);
        send(msg);
    }

    /**
     * The distance between the two points in map.
     * The function calculates how many steps the agent have to do.
     * @param pos1 The first position
     * @param pos2 The second position
     * @return The distance between the two Points
     */
    private int dist(Point pos1, Point pos2) {

        int distX = Math.abs(pos1.x - pos2.x);
        int distY = Math.abs(pos1.y - pos2.y);

        return Math.max(distX, distY);
    }

    private int estimateDistToTarget (Point pos) {

        int rand = new Random().nextInt(3);

        switch (rand) {
            case 0: return estimateDistToTargetMinValue(pos);
            case 1: return estimateDistToTargetMeanValue(pos);
            case 2: return estimateDistToTargetMaxValue(pos);
            default:
                throw new IllegalStateException("Unexpected value: " + rand);
        }
    }

    /**
     * Calculates an estimation for the distance to the target (Mean Value)
     * @return The estimation distance
     */
    private int estimateDistToTargetMeanValue (Point pos) {

        return (dist(pos, new Point(0, 0)) + dist(pos, new Point(map.lengthX()-1, map.lengthY()-1)))/2;
    }

    /**
     * Calculates an estimation for the distance to the target (Min Value)
     * @return The estimation distance
     */
    private int estimateDistToTargetMinValue (Point pos) {

        return Integer.min(dist(pos, new Point(0, 0)), dist(pos, new Point(map.lengthX()-1, map.lengthY()-1)));
    }

    /**
     * Calculates an estimation for the distance to the target (Max Value)
     * @return The estimation distance
     */
    private int estimateDistToTargetMaxValue (Point pos) {

        return Integer.max(dist(pos, new Point(0, 0)), dist(pos, new Point(map.lengthX()-1, map.lengthY()-1)));
    }

    /**
     * Check if a given point belongs to the map
     * @param value The given point
     * @param sizeX The size for the first dimension
     * @param sizeY The size for the second dimension
     * @return True if it can belong, false otherwise
     */
    private boolean checkPointInMap(Point value, int sizeX, int sizeY) {
        return value.x>=0 && value.y>=0 && value.x<sizeX && value.y<sizeY;
    }


    /**
     * Evaluates the possible moves from a box in the map
     * @param parent The parent box
     * @return An ArrayList with all the possible moves
     */
    private ArrayList<Point> expand(Point parent) {

        ArrayList<Point> expand = new ArrayList<>();

        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                if (i==0 && j==0)
                    continue;
                Point child = new Point(parent.x + i, parent.y + j);

                if (checkPointInMap(child, map.lengthX(), map.lengthY())
                        && !Objects.equals(map.getBox(child).getContent(), 'O')) {
                    expand.add(child);
                }
            }
        }

        return expand;
    }


    /**
     * The main evaluate method that is called after every update of the agents and the map.
     * It implements a slightly different A* and Best First Algorithm
     * @return The next box that the agent has to go.
     */
    private Point evaluate () {

        ArrayList<Point> closedSet = new ArrayList<>();
        ArrayList<Point> openSet = new ArrayList<>();
        ArrayList<Point> openSetNextMove = new ArrayList<>();
        ArrayList<Integer> openSetDistance = new ArrayList<>();
        ArrayList<Integer> openSetValue = new ArrayList<>();
        openSet.add(position);
        openSetNextMove.add(null);
        openSetDistance.add(0);
        openSetValue.add(0);

        // Find if the solution have found
        boolean solutionFound = false;
        Point solution = null;
        for (int i=0; i<map.lengthX(); i++) {
            for (int j = 0; j < map.lengthY(); j++) {
                if (map.getBox(new Point(i, j)).getExplored()
                        && Objects.equals(map.getBox(new Point(i, j)).getContent(), 'X')) {
                    solutionFound = true;
                    solution = new Point(i, j);
                }
            }
        }


        while (openSet.size()!=0) {

            // Find the min value
            int minIndex = openSetValue.indexOf(Collections.min(openSetValue));

            // If point exists in closedSet, remove and continue
            if (closedSet.contains(openSet.get(minIndex))) {
                openSet.remove(minIndex);
                openSetDistance.remove(minIndex);
                openSetNextMove.remove(minIndex);
                openSetValue.remove(minIndex);
                continue;
            }

            // If this is the solution, return the next box to move
            if (solutionFound && openSet.get(minIndex).equals(solution)) {
                return openSetNextMove.get(minIndex);
            }

            // If this is an unreached box, return the next box to move
            if (!map.getBox(openSet.get(minIndex)).getExplored()) {
                return openSetNextMove.get(minIndex);
            }

            // Produce possible moves
            ArrayList<Point> children = expand(openSet.get(minIndex));

            // Check if there are not new moves
            if (children.size() == 0) {/////////////////////////////////////////////////////
                closedSet.add(openSet.get(minIndex));
                openSet.remove(minIndex);
                openSetDistance.remove(minIndex);
                openSetNextMove.remove(minIndex);
                openSetValue.remove(minIndex);

                continue;
            }

            // Evaluate moves
            ArrayList<Integer> childrenDistance = new ArrayList<>();
            ArrayList<Integer> childrenValue = new ArrayList<>();
            ArrayList<Point> childrenNextMove = new ArrayList<>();

            for (int i=0; i<children.size(); i++) {
                childrenDistance.add(dist(position, children.get(i)));
                int valueToTarget = childrenDistance.get(i);
                if (solutionFound)
                    valueToTarget += dist(children.get(i), solution);
                else
                    valueToTarget += estimateDistToTarget(children.get(i));
                childrenValue.add(valueToTarget);
                Point nextMove = openSetNextMove.get(minIndex);
                if (childrenDistance.get(i) == 1)
                    nextMove = children.get(i);
                childrenNextMove.add(nextMove);
            }


            // Put moves for checking
            openSet.addAll(children);
            openSetDistance.addAll(childrenDistance);
            openSetNextMove.addAll(childrenNextMove);
            openSetValue.addAll(childrenValue);



            // Put current box to closedSet and remove it from everywhere
            closedSet.add(openSet.get(minIndex));
            openSet.remove(minIndex);
            openSetDistance.remove(minIndex);
            openSetNextMove.remove(minIndex);
            openSetValue.remove(minIndex);
        }

        return closedSet.get(closedSet.size()-1);
    }

    /**The main evaluate method that is called after every update of the agents and the map.
     * It implements a random choice for the agent
     * @return The next box that the agent has to go.
     */
    public Point evaluateRandom() {

        Point nextPos = new Point();

        while (true) {
            Random rand = new Random();

            int posX = rand.nextInt(3) - 1 + (int) position.getX();
            int posY = rand.nextInt(3) - 1 + (int) position.getY();

            nextPos.setLocation(posX, posY);

            if (nextPos.getX() < 0)
                continue;

            if (nextPos.getY() < 0)
                continue;

            if (nextPos.getX() > map.lengthX()-1)
                continue;

            if (nextPos.getY() > map.lengthY()-1)
                continue;

            if (Objects.equals(map.getBox(nextPos).getContent(), 'O'))
                continue;

            if (Objects.equals(map.getBox(nextPos).getContent(), 'X'))
                System.out.println("Found");

            break;
        }
        return nextPos;
    }


    /**
     * Makes the move of the agent according to the evaluation and updates the map.
     * @param pos The next box that the agent has to go
     */
    private void move(Point pos) {
        position.setLocation(pos);
//        System.out.println("Position is: ("+pos.x+","+pos.y+")");
//        System.out.println("--------------------------------------------------------------");

        gameLauncher.map.setAgentPositions(id,pos);
        gameLauncher.map.explore(pos.x,pos.y);
        map.explore(pos.x,pos.y);
        gameLauncher.map.repaint();

        if (Objects.equals(map.getBox(position).getContent(), 'X')) {
            System.out.println("Found");
            JOptionPane.showMessageDialog(null, "The "+ team + " won!", "Winner", JOptionPane.INFORMATION_MESSAGE);
            System.exit(0);
        }
    }

}
