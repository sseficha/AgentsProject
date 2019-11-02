package agents;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;

public class Agent1 extends Agent {
    int counter = 0;
    protected void setup() {
        // Printout a welcome message
        System.out.println("Agent is set up!");
        System.out.println("Agent's name is "+getAID().getName());
        addBehaviour(
                new TickerBehaviour(this,2000) {
                    @Override
                    protected void onTick() {
                        counter++;
                        System.out.println("Counter is: "+counter);
                    }
                }
        );
//        this.doDelete();
    }

    protected void takeDown(){
        System.out.println("Agent 1 is down!");
    }
}
