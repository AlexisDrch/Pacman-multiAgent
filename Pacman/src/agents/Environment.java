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
	protected int nshot;
	protected Grid myGrid;

	protected void setup() {
		Utils.register(this, this.getLocalName());
		System.out.println("### " + getLocalName() + " is now ... Installed !");

		this.myGrid = new Grid();
		this.nshot = 0;

		addBehaviour(new GetInformedFromEngineBehaviour(this.myGrid));
		addBehaviour(new GetInformedFromEntitiesBehaviour(this.myGrid));

		this.displayMyGrid();
	}
	
	public void setMyGrid(Grid newGrid) {
		this.myGrid = newGrid;
	}
	
	public Grid getMyGrid() {
		return this.myGrid;
	}
	
	public boolean validMove(Cell targetedCell, Cell oldPosition) {
		boolean inValid = (oldPosition.wasTraveler() && targetedCell.isMonster());
		boolean inValid2 = (oldPosition.wasMonster() && targetedCell.isTraveler());
		
		return (!inValid && !inValid2);
	}
	
	public void updateMyGrid(CellsBag cellsBag) {
		int newPositionLi = cellsBag.newPosition.nligne;
		int newPositionCol = cellsBag.newPosition.ncolonne;
		Cell targetedCell = this.myGrid.getCell(newPositionLi, newPositionCol);
		if (validMove(targetedCell, cellsBag.oldPosition)) {
			cellsBag.oldPosition.setOldValue(0);
			this.myGrid.updateCell(cellsBag.oldPosition);
			this.myGrid.updateCell(cellsBag.newPosition);
		} else {
			this.myGrid.endGame();
			addBehaviour(new EndOfGameBehaviour());
		}
	}

	public void displayMyGrid() {
		System.out.println("\n\n\n\n\n\n\n\n\n----------------------------" + this.nshot + "----------------------------\n\n");
		this.myGrid.display();

		System.out.println("\n\n----------------------------" + this.nshot + "----------------------------");
		this.nshot = this.nshot + 1 ;
	}

	/**
	 * GetInformedFromEngineBehaviour get an agent AID from engine and trigger a request to it directly.
	 */
	private class GetInformedFromEngineBehaviour extends Behaviour {
		Grid superGrid;
		
		public GetInformedFromEngineBehaviour(Grid grid) {
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
				//display and refresh grid;
				((Environment)myAgent).displayMyGrid();
				String jsonMessage = message.getContent(); // chaîne JSON
				// parse json message with MonsterX information
				JSONObject obj = new JSONObject(jsonMessage);
				String monsterXLocalName = obj.getString("localName");
				String monsterXName = obj.getString("name");
				// System.out.println("\nAgent " + myAgent.getLocalName() + " has just received credentials of --- " + monsterXLocalName );
				// should send a request message to according monsterX asking him to move
				AID monsterX = Utils.searchForAgent(myAgent, monsterXLocalName);
				ACLMessage requestMessage = new ACLMessage();
				// add performative
				requestMessage.setPerformative(ACLMessage.REQUEST);
				// add receiver
				requestMessage.addReceiver(monsterX);
				// add conversationID
				requestMessage.setConversationId(Constants.MONSTER_ENV_CONVERSATION_ID);
				// send message
				send(requestMessage);
				// refresh local grid
				this.superGrid = ((Environment)myAgent).getMyGrid();
			} else {
				block();
			}
		}
	}
	
	/**
	 * GetInformedFromEntitiesBehaviour get an updated grid from a monster or the traveler(with its new position) and update UI.
	 */
	private class GetInformedFromEntitiesBehaviour extends Behaviour {
		Grid superGrid;
		
		public GetInformedFromEntitiesBehaviour(Grid grid) {
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
			MessageTemplate mt1 = MessageTemplate.MatchPerformative(ACLMessage.REQUEST)
					.MatchConversationId(Constants.MONSTER_ENV_CONVERSATION_ID);
			MessageTemplate mt2 = MessageTemplate.MatchPerformative(ACLMessage.REQUEST)
					.MatchConversationId(Constants.TRAVELER_ENV_CONVERSATION_ID);
			
			// either traveler or monster conv_id
			MessageTemplate m1_or_m2 = MessageTemplate.or(mt1, mt2);
			ACLMessage message = myAgent.receive(m1_or_m2);
			
			if (message != null) {
				String jsonMessage = message.getContent(); // chaîne JSON
				// System.out.println(jsonMessage);
				// parse json message with entities cellsbag
				Gson gson = new Gson();
				CellsBag cellsBag = gson.fromJson(jsonMessage, CellsBag.class);
				//Check validity of new position
				if (myGrid.getObtacles(cellsBag.newPosition.nligne, cellsBag.newPosition.ncolonne)) {
					int value = 0;
					if (myGrid.getObtacles(cellsBag.oldPosition.nligne, cellsBag.oldPosition.ncolonne)) {
						value = -1;
					} 
					// remove old monster value from dirty position
					Cell dirtyCell = new Cell(value,cellsBag.oldPosition.nligne, cellsBag.oldPosition.ncolonne);
					((Environment)myAgent).myGrid.updateCell(dirtyCell);
					// prevent Monster from moving : has to move in a new random one
					ACLMessage errorReply = message.createReply();
					errorReply.setPerformative(ACLMessage.FAILURE);
					send(errorReply);
				} else {
					if (myGrid.getObtacles(cellsBag.oldPosition.nligne, cellsBag.oldPosition.ncolonne)) {
						cellsBag.oldPosition.setValue(-1);
					}
					((Environment)myAgent).updateMyGrid(cellsBag);
					this.superGrid = ((Environment)myAgent).getMyGrid();
				}
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

		

