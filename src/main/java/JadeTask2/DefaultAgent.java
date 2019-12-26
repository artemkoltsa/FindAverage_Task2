package JadeTask2;

import jade.core.Agent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.TimeUnit;


public class DefaultAgent extends Agent {

    public String[] linkedAgents;
    private Double Number;

    public Double DNumber() {

        return this.Number;
    }

    public void packageNumber(double Number) {

        this.Number = Number;
    }

    @Override
    protected void setup() {
//        Random rand = new Random();
//        int myNumber = rand.nextInt(100);
        Number = Double.valueOf((String) getArguments()[0]);
        String Name = getAID().getLocalName();

        System.out.println("Agent Id: " + Name +" number = " + Number);

        linkedAgents = Arrays.copyOfRange(getArguments(), 1,
                getArguments().length, String[].class);

        addBehaviour(new FindAverage
                (this, TimeUnit.SECONDS.toMillis(1)));
    }
}