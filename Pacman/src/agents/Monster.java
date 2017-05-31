package agents;

import org.json.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.gson.Gson;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

import agents.MonsterAgent.SuscribeBehaviour;
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


public class Monster extends Agent {
	protected AID[] receiver;
	protected int value;
	public Cell position;
	public Cell oldPosition;


	protected void setup() {
		Utils.register(this, this.getLocalName());
		System.out.println("### " + getLocalName() + " is now ... Installed !");
		// set value to agent
		Object[] args = getArguments();
		this.value = (int) args[0];
		// setup random position in grid 
		Random rand = new Random();
		this.oldPosition = null;
		this.position = new Cell(0, rand.nextInt(Constants.DIM_GRID_X - 0 + 1) + 0, rand.nextInt(Constants.DIM_GRID_Y - 0 + 1) + 0);
		// add behaviours
		addBehaviour(new SubscribeToEngineBehaviour());
		addBehaviour(new MoveBehaviour());
	}
	
	public void setValue(int newValue) {
		this.value = newValue;
	}
	
	public int getValue() {
		return this.value;
	}
	
	// to improve
	public void move() {
		int i;
		int j;
		this.oldPosition = null;
		this.oldPosition = this.position;
		// erasing monster at its previous position
		this.oldPosition.setValue(0);
		// remember old value
		this.oldPosition.setOldValue(this.value);
		// moving to a new random position
		Cell newPosition = new Cell(this.getValue(), 
				(this.oldPosition.nligne + this.getValue())%Constants.DIM_GRID_X, 
				(this.oldPosition.ncolonne + this.getValue()-1)%Constants.DIM_GRID_Y
				);
		this.position = newPosition;
		//System.out.print("\nAgent " + myAgent.getLocalName() + " has just received a request to move ---> " + newPosition.nligne + "," + newPosition.ncolonne);
	}
	
	
	/**
	 * Behaviour to subscribe to Engine Agent
	 */
	private class SubscribeToEngineBehaviour extends OneShotBehaviour {
		
		@Override
		public void action() {
			// search for simulater = only SimulationAgent 
			AID engine = Utils.searchForAgent(myAgent, Constants.ENGINE_DESCRIPTION);
			// should send a subscribe message to simulation Agent
			ACLMessage subscribeMessage = new ACLMessage();
			// add performative
			subscribeMessage.setPerformative(ACLMessage.SUBSCRIBE);
			// add engine  as a receiver
			subscribeMessage.addReceiver(engine);
			// send message to engine
			send(subscribeMessage);
			System.out.print("\nAgent " + myAgent.getLocalName() + " has just sent a SubscribeToSimulater message to " + engine.getName());
		}
	}
	
	/**
	 * Behaviour to move from one step to another
	 * This behaviour will continuously wait for receiving a message from environment.
	 * On its reception, the monster will randomly move according to the grid received and to its position.
	 * Then the new position is sent to the environment.
	 */
	private class MoveBehaviour extends CyclicBehaviour {
		
		@Override
		public void action() {
			// should receive a message that match console jade template : REQUEST and conversationId
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST).MatchConversationId(Constants.MONSTER_ENV_CONVERSATION_ID);
			ACLMessage message = myAgent.receive(mt);
			
			if (message != null) {
				try {
					//System.out.print("\nAgent " + myAgent.getLocalName() + " has just received a request to move --- ");
					String jsonMessage = message.getContent(); // chaîne JSON
					// parse grid received to move
					((Monster)myAgent).move();
					
					ACLMessage updatedPositionReply = message.createReply();
					// add performative
					updatedPositionReply.setPerformative(ACLMessage.INFORM);
					// add new position as content in json
					ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
					CellsBag cellsbag = new CellsBag(((Monster)myAgent).oldPosition, ((Monster)myAgent).position);
					String jsonCellsbag = ow.writeValueAsString(cellsbag);
					updatedPositionReply.setContent(jsonCellsbag);
					// replying with new cellsbag
					send(updatedPositionReply);
				} catch (JsonProcessingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				block();
			}
		}
			
	}
	
}
		
		

