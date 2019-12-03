import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.Map;

public class Player extends Agent {
    private static final int EXPLORED_VALUE = 0;
    private static final int AGENT_VALUE = 0;
    private static final int NONE_VALUE = 0;
    private static final int OBJECT_VALUE = 0;
    private static final int X_VALUE = 0;

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
     * The distance between the current position and some other.
     * The function calculates how many steps the agent have to do.
     * @param pos The second position
     * @return The distance between the two Points
     */
    private int dist(Point pos) {

        int distX = Math.abs(pos.x - position.x);
        int distY = Math.abs(pos.y - position.y);

        return Math.max(distX, distY);
    }

    public int evaluateBox(Box box) {

        if (!box.getExplored())
            return EXPLORED_VALUE;

        /*if (!box.AGENT_VALUE())
            return AGENT_VALUE;*/

        switch (box.getContent()) {

            case 'N':
                return NONE_VALUE;
            case 'O':
                return OBJECT_VALUE;
            case 'X':
                return X_VALUE;
            default:
                throw new IllegalStateException("Unexpected value: " + box.getContent());
        }
    }


    /**
     * N(x-1, y)
     * NE(x-1, y+1)
     * E(x, y+1)
     * SE(x+1, y+1)
     * S(x+1, y)
     * SW(x+1, y-1)
     * W(x, y-1)
     * NW(x-1, y-1)
     *
     * @return
     */
    private HashMap<String, Double> createHash () {

        HashMap<String, Double> direction = new HashMap<>();
        int last = map.lengthX()-1;

        if (position.x!=0
                && !Objects.equals(map.getBox(new Point(position.x-1, position.y)).getContent(), 'O'))
            direction.put("N", 0.0);

        if (position.x!=0 && position.y!=last
                && !Objects.equals(map.getBox(new Point(position.x-1, position.y-1)).getContent(), 'O'))
            direction.put("NE", 0.0);

        if (position.y!=last
                && !Objects.equals(map.getBox(new Point(position.x, position.y+1)).getContent(), 'O'))
            direction.put("E", 0.0);

        if (position.x!=last && position.y!=last
                && !Objects.equals(map.getBox(new Point(position.x+1, position.y+1)).getContent(), 'O'))
            direction.put("SE", 0.0);

        if (position.x!=last
                && !Objects.equals(map.getBox(new Point(position.x+1, position.y)).getContent(), 'O'))
            direction.put("S", 0.0);

        if (position.x!=last && position.y!=0
                && !Objects.equals(map.getBox(new Point(position.x+1, position.y-1)).getContent(), 'O'))
            direction.put("SW", 0.0);

        if (position.y!=0
                && !Objects.equals(map.getBox(new Point(position.x, position.y-1)).getContent(), 'O'))
            direction.put("W", 0.0);

        if (position.x!=0 && position.y!=0
                && !Objects.equals(map.getBox(new Point(position.x-1, position.y-1)).getContent(), 'O'))
            direction.put("NW", 0.0);

        return direction;
    }



    private void updateHashScore(HashMap<String, Double> direction, Point curPoint, double curValue) {

        /*if (direction.containsKey("N")
            && ) {
            direction.replace("N", direction.get("N")+curValue);
        }*/

        if (direction.containsKey("NE")
                && curPoint.x<position.x && curPoint.y>position.y) {
            direction.replace("NE", direction.get("NE")+curValue);
        }

        /*if (direction.containsKey("E")
                && ) {
            direction.replace("E", direction.get("E")+curValue);
        }*/

        if (direction.containsKey("SE")
                && curPoint.x>position.x && curPoint.y>position.y) {
            direction.replace("SE", direction.get("SE")+curValue);
        }

        /*if (direction.containsKey("S")
                && ) {
            direction.replace("S", direction.get("S")+curValue);
        }*/

        if (direction.containsKey("SW")
                && curPoint.x>position.x && curPoint.y<position.y) {
            direction.replace("SW", direction.get("SW")+curValue);
        }

        /*if (direction.containsKey("W")
                && ) {
            direction.replace("W", direction.get("W")+curValue);
        }*/

        if (direction.containsKey("NW")
                && curPoint.x<position.x && curPoint.y<position.y) {
            direction.replace("NW", direction.get("NW")+curValue);
        }
    }


    private Point finalPoint(String str) {

        switch (str) {
            case "N": return new Point(position.x-1, position.y);
            case "NE": return new Point(position.x-1, position.y+1);
            case "E": return new Point(position.x, position.y+1);
            case "SE": return new Point(position.x+1, position.y+1);
            case "S": return new Point(position.x+1, position.y);
            case "SW": return new Point(position.x+1, position.y-1);
            case "W": return new Point(position.x, position.y-1);
            case "NW": return new Point(position.x-1, position.y-1);
            default:
                throw new IllegalStateException("Unexpected value: " + str);
        }
    }


    public Point evaluate() {

        // Initialize hash with every available next position
        HashMap<String, Double> direction = createHash();

        // For each box in map, computes its value and updates the hash
        for (int i=0; i<map.lengthX(); i++) {
            for (int j=0; j<map.lengthY(); j++) {

                if (i==position.x && j==position.y)
                    continue;

                Point curPoint = new Point(i, j);
                double distanceFactor = 1 - (double) dist(curPoint) / map.lengthX();
                double curValue = distanceFactor * evaluateBox(map.getBox(curPoint));

                updateHashScore(direction, curPoint, curValue);
            }
        }

        // If there are many max values, returns randomly
        double maxValue = Collections.max(direction.values());
        ArrayList<String> possibleActions = new ArrayList<>();
        for (Map.Entry entry : direction.entrySet())
            if ((double) entry.getValue() == maxValue)
                possibleActions.add((String) entry.getKey());

        int rand = new Random().nextInt(possibleActions.size()-1);


        return finalPoint(possibleActions.get(rand));
    }




    public Point evaluate1() {

        Point nextPos = new Point();
        ArrayList<Integer> moves = new ArrayList<>();

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

    public void move(Point pos) {
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
