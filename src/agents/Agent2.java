package agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

public class Agent2 extends Agent {
    protected void setup(){
        System.out.println("Agent "+getName()+" is up!");
//        addBehaviour(new TickerBehaviour(this,2000) {
//            @Override
//            protected void onTick() {
//                ACLMessage msg = receive();
//                if (msg != null) {
//                    System.out.println("Message content is: "+msg.getContent());
//                }
//            }
//        });
        //recommended way to receive messages inside Behavior
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
        addBehaviour(new WakerBehaviour(this,2000) {
            @Override
            protected void onWake() {
                DFAgentDescription[] result = null;
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("example");
                template.addServices(sd);
                try {
                    result = DFService.search(myAgent, template);
                }
                catch (FIPAException fe) {
                    fe.printStackTrace();
                }
                for(int i=0;i<result.length;i++)
                    System.out.println("Found agent: "+result[i].getName()+" providing service example");
            }
        });

    }

}
