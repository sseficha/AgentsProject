package agents;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

//oneshot, cyclic, generic, waker, ticker

public class Agent1 extends Agent {
    int counter = 0;
    protected void setup() {
        System.out.println("Agent "+getName()+" is up!");

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("example");
        sd.setName(getName());
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }

    addBehaviour(
                new TickerBehaviour(this,1500) {
                    @Override
                    protected void onTick() {
                        counter++;
                        if(counter % 4 == 0)
                        {
                            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                            msg.addReceiver(new AID("agent2", AID.ISLOCALNAME));
                            msg.setLanguage("English");
                            msg.setOntology("Weather-forecast-ontology");
                            msg.setContent("Today it’s raining");
                            send(msg);
                        }
                        System.out.println("Counter is: "+counter);
                    }
                }
        );
//        this.doDelete();
    }

    protected void takeDown(){
        System.out.println("Agent "+getName()+" is down!");
    }
}
