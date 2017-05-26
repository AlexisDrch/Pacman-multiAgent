package agents;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.Random;

import org.json.JSONObject;

import models.*;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.util.leap.ArrayList;


public class Environment extends Agent {
	protected int value;
	protected Grid myGrid;

	protected void setup() {
		Utils.register(this, this.getLocalName());
		System.out.println("### " + getLocalName() + " is now ... Installed !");
		// set value to agent
		Object[] args = getArguments();
		// int value = (int) args[0];

		myGrid = new Grid(Constants.GRID_LVL1);

		addBehaviour(new GetInformedFromSimulationBehaviour(this.myGrid));
		addBehaviour(new EndOfGameBehaviour());

		myGrid.display();
	}
	
	/**
	 * GetInformedFromEngineBehaviour get an agent AID from engine and trigger a request to it directly.
	 */
	private class GetInformedFromSimulationBehaviour extends Behaviour {
		Grid superGrid;
		
		public GetInformedFromSimulationBehaviour(Grid grid) {
			this.superGrid = grid;
		}
		
		@Override
		public boolean done() {
			// TODO Auto-generated method stub
			if (this.superGrid  != null) {
				return (this.superGrid.isOver());	
			}
			return false;
		}

		@Override
		public void action() {
			// should receive a message that match console jade template : REQUEST
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
			ACLMessage message = myAgent.receive(mt);
			
			if (message != null) {
				String jsonMessage = message.getContent(); // chaîne JSON
				System.out.println("\nAgent " + myAgent.getLocalName() + " has just received message --- " + jsonMessage);
				JSONObject obj = new JSONObject(jsonMessage);
				String monsterXName = obj.getString("name");
				// should send a request message to according monsterX
				AID monsterX = Utils.searchForAgent(myAgent, monsterXName);
				ACLMessage requestMessage = new ACLMessage();
				// add performative
				requestMessage.setPerformative(ACLMessage.REQUEST);
				// add receiver
				requestMessage.addReceiver(monsterX);
				// send message 
				send(requestMessage);
				
			} else {
				block();
			}
		}
	}
	
	/**
	 * Behaviour to send an inform message to the engine when the game is over
	 */
	private class EndOfGameBehaviour extends OneShotBehaviour {
		@Override
		public void action() {
			// search for engine = only SimulationAgent 
			AID engine = Utils.searchForAgent(myAgent, Constants.ENGINE_DESCRIPTION);
			// should send a subscribe message to simulation Agent
			ACLMessage subscribeMessage = new ACLMessage();
			// add performative
			subscribeMessage.setPerformative(ACLMessage.INFORM);
			// add receiver
			subscribeMessage.addReceiver(engine);
			// send message 
			send(subscribeMessage);
			System.out.print("\nAgent " + myAgent.getLocalName() + " has just sent an enfOfGame message to " + engine.getName());
		}
	}
}

		

