import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import java.util.ArrayList;

public class Player extends Agent {
    private ArrayList<DFAgentDescription> teammates;
    private Coordinate position;
    private int sight;  //optiko pedio
    private Map map;
    protected void setup() {

        teammates = new ArrayList<>();

        // all next are hardcoded for now
        // Constructor of Coordinate sets (0, 0)
        sight=2;        //can see 2 boxes away

        map = new Map();        //hardcoded path+name for now


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
                //evaluate()    kali epitixia Theodosi
                //move()
            }
        });
    }
}
