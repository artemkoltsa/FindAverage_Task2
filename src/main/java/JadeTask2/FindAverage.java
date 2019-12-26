package JadeTask2;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.wrapper.StaleProxyException;
import org.graalvm.compiler.nodes.NodeView;
import JadeTask2.DefaultAgent;

import java.text.DecimalFormat;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;


public class FindAverage extends TickerBehaviour {


    // 1->2 Connection failures
    private final static double Connection_Failures = 0.2;
    // 6->9 Connection network delay
    private final static double Network_Delay = 0.15;
    private final static int MaxDelay = 10;
    private static final double Alpha = 0.3;

    private final DefaultAgent agent;
    private int currentStep = 0;
    private State state = State.SEND;

    public FindAverage(DefaultAgent agent, long period) {
        super(agent, period);
        this.setFixedPeriod(true);
        this.agent = agent;
    }

    @Override
    protected void onTick() {
        currentStep++;

        switch (state) {
            case SEND:
                send();
                break;
            case RECEIVE:
                receive();
                break;
            case OFF:
                off();
                break;
            default:
                block();
        }
    }

    private double generateNoise() {
        double noise = 0.1*Math.sin(Instant.now().toEpochMilli());
        return (agent.DNumber() - noise);
    }

    private void send() {
        double noisyContent=generateNoise();

        System.out.println("Agent Id: " + agent.getAID().getLocalName() +". Step "+
                        currentStep + ") Send " + noisyContent +
                " to " + Arrays.toString(agent.linkedAgents));

        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        for (String linkedAgent : agent.linkedAgents) {
            //if 1->2 connection failures 0.2
            if (agent.getAID().getLocalName().equals("1")
                    && linkedAgent.equals("2")) {
                double connectionExistParam = Math.random();
                if (connectionExistParam > Connection_Failures) {
                    continue;
                }
            }
            msg.addReceiver(new AID(linkedAgent, AID.ISLOCALNAME));

            //if 6->9 network delay = 0.15
            if (agent.getAID().getLocalName().equals("6")
                    && linkedAgent.equals("9")) {
                double networkDelay = Math.random();
                if (networkDelay > Network_Delay) {
                    //Generate delay
                    int delay = (int) (Math.random() * MaxDelay);

                    try {
                        TimeUnit.MILLISECONDS.sleep(delay);
                    } catch (InterruptedException e) {
                        System.out.println(e.toString());
                    }
                }
            }
        }
        msg.setContent(String.valueOf(noisyContent));
        agent.send(msg);
        state = State.RECEIVE;
    }

    private void receive() {
        double res = 0;
        Set<String> processed = new HashSet<>();
        double AgentNumber = agent.DNumber();
        while ((agent.receive()) != null) {
            ACLMessage msg = agent.receive();
            if (msg != null) {
                if (processed.isEmpty() || !processed.contains(msg.getSender().getLocalName())) {
                    double NumberReceived = Double.parseDouble(msg.getContent());

                    System.out.println("Agent Id: "+ agent.getAID().getLocalName()+". Step "+
                            currentStep + ") Received " + NumberReceived);

                    res += NumberReceived - AgentNumber;
                    processed.add(msg.getSender().getLocalName());

                }
                else{
                    if(processed.size()==agent.linkedAgents.length){
                        break;
                    }
                }
            }
        }
        agent.packageNumber(AgentNumber + Alpha * res);

        if (currentStep >= 50) {
            off();
        }
        state = State.SEND;
    }

    private void off() {
        String name = agent.getAID().getLocalName();
        DecimalFormat df = new DecimalFormat("#.######");

        System.out.println("The end! " + currentStep + ") " + "Agent Id: " + name
                + ". Count average : " + df.format(agent.DNumber()));

//End container
        jade.wrapper.AgentContainer container = agent.getContainerController();
        agent.doDelete();
        new Thread(() -> {
            try {
                container.kill();
            } catch (StaleProxyException ignored) {
            }
        }).start();
    }

    public enum State {
        SEND,
        RECEIVE,
        OFF;
    }
}
