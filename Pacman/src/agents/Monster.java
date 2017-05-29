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
	protected Cell position;


	protected void setup() {
		Utils.register(this, this.getLocalName());
		System.out.println("### " + getLocalName() + " is now ... Installed !");
		// set value to agent
		Object[] args = getArguments();
		// int value = (int) args[0];
		// setup random position in grid 
		Random rand = new Random();
		this.position = new Cell(0, rand.nextInt(9 - 0 + 1) + 0, rand.nextInt(9 - 0 + 1) + 0);
		// add behaviours
		addBehaviour(new SubscribeToEngineBehaviour());
		addBehaviour(new MoveBehaviour(this.position));
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
		Cell superPosition;
		Cell oldPosition;
		
		public MoveBehaviour(Cell position) {
			this.superPosition = position;
			this.oldPosition = this.superPosition;
		}

		@Override
		public void action() {
			// should receive a message that match console jade template : REQUEST and conversationId
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST).MatchConversationId(Constants.MONSTER_ENV_CONVERSATION_ID);
			ACLMessage message = myAgent.receive(mt);
			
			if (message != null) {
				try {
					System.out.print("\nAgent " + myAgent.getLocalName() + " has just received a request to move --- ");
					String jsonMessage = message.getContent(); // chaîne JSON
					// parse grid received to move
					Gson gson = new Gson();
					Grid grid = gson.fromJson(jsonMessage, Grid.class);
					this.move();
					// updating grid
					grid.updateCell(this.oldPosition);
					grid.updateCell(this.superPosition);
					ACLMessage updatedGridReply = message.createReply();
					// add performative
					updatedGridReply.setPerformative(ACLMessage.INFORM);
					// add updated grid as content in json
					ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
					String jsonGrid = ow.writeValueAsString(grid);
					updatedGridReply.setContent(jsonGrid);
					// replying with new grid
					send(updatedGridReply);
				} catch (JsonProcessingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				// send(this.superPosition)
				
			} else {
				block();
			}
		}

		// todo
		public void move() {
			int i;
			int j;
			this.oldPosition = this.superPosition;
			// erasing monster at its previous position
			this.oldPosition.setValue(0);
			// moving to a new random position
			Cell newPosition = this.oldPosition;
			newPosition.ncolonne = (newPosition.ncolonne +1)%9 ;
			newPosition.nligne = (newPosition.nligne +1)%9 ;
			newPosition.setValue(1);
			this.superPosition = newPosition;
			
			
			System.out.print("\nAgent " + myAgent.getLocalName() + " has just received a request to move ---> " + newPosition.nligne + "," + newPosition.ncolonne);
		}
			
	}
	
}
		
		

