import jade.core.AID;
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
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class Player extends Agent {
    private ArrayList<DFAgentDescription> teammates;
    private int id;
    private String team;
    private Point position;
    private int sight;  //optiko pedio
    private Map map;
    protected void setup() {

        teammates = new ArrayList<>();

        // all next are hardcoded for now
        position = new Point(0,0);
        sight=2;        //can see 2 boxes away
        map = new Map();        //hardcoded path+name for now
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


    public Point evaluate() {

        Point nextPos = new Point();
        ArrayList<Integer> moves = new ArrayList();

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
