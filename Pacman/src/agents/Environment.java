package agents;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.Random;

import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.gson.Gson;

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

		this.myGrid = new Grid(Constants.GRID_LVL1);
		this.value = 0;

		addBehaviour(new GetInformedFromSimulationBehaviour(this.myGrid));
		addBehaviour(new GetInformedFromMonsterXBehaviour(this.myGrid));
		addBehaviour(new EndOfGameBehaviour());

		this.displayMyGrid();
	}
	
	public void setMyGrid(Grid newGrid) {
		this.myGrid = newGrid;
	}
	
	public Grid getMyGrid() {
		return this.myGrid;
	}
	
	public void displayMyGrid() {
		System.out.println("\n\n\n\n\n\n\n\n\n----------------------------" + this.value + "----------------------------\n\n");
		this.myGrid.display();

		System.out.println("\n\n----------------------------" + this.value + "----------------------------\n\n\n\n\n\n\n\n\n");
		this.value = this.value + 1 ;
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
				try {
					//display and refresh grid;
					((Environment)myAgent).displayMyGrid();
					Grid localGrid = ((Environment)myAgent).getMyGrid();
					this.superGrid = localGrid;
					String jsonMessage = message.getContent(); // chaîne JSON
					// parse json message with MonsterX information
					JSONObject obj = new JSONObject(jsonMessage);
					String monsterXLocalName = obj.getString("localName");
					String monsterXName = obj.getString("name");
					//System.out.println("\nAgent " + myAgent.getLocalName() + " has just received credentials of --- " + monsterXLocalName );
					// should send a request message to according monsterX with supergrid as content
					AID monsterX = Utils.searchForAgent(myAgent, monsterXLocalName);
					ACLMessage requestMessage = new ACLMessage();
					// add performative
					requestMessage.setPerformative(ACLMessage.REQUEST);
					// add receiver
					requestMessage.addReceiver(monsterX);
					// add conversationID
					requestMessage.setConversationId(Constants.MONSTER_ENV_CONVERSATION_ID);
					// add grid as content in json
					ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
					String jsonGrid = ow.writeValueAsString(localGrid);
					requestMessage.setContent(jsonGrid);
					// send message
					send(requestMessage);
				} catch (JsonProcessingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				block();
			}
		}
	}
	
	/**
	 * GetInformedFromMonsterXBehaviour get an updated grid from a monster (with its new position) and update UI.
	 */
	private class GetInformedFromMonsterXBehaviour extends Behaviour {
		Grid superGrid;
		
		public GetInformedFromMonsterXBehaviour(Grid grid) {
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
			// should receive a message that match console jade template : INFORM and ConversationId
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST).MatchConversationId(Constants.MONSTER_ENV_CONVERSATION_ID);
			ACLMessage message = myAgent.receive(mt);
			
			if (message != null) {
				String jsonMessage = message.getContent(); // chaîne JSON
				// parse json message with MonsterX grid
				Gson gson = new Gson();
				Grid grid = gson.fromJson(jsonMessage, Grid.class);
				((Environment)myAgent).setMyGrid(grid);
				this.superGrid = ((Environment)myAgent).getMyGrid();
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

		

