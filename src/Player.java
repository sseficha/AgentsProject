import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

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
                ACLMessage msg = myAgent.receive();
                if (msg != null) {
                    System.out.println("Message content is: "+msg.getContent());
                }
                else {
                    block();
                }
            }
        });

        //Evaluate/Move Behavior
        addBehaviour(new TickerBehaviour(this,2000) {
            @Override
            protected void onTick() {
                Point nextPos = new Point(evaluate());
                move(nextPos);
            }
        });
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
        System.out.println(position);

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
