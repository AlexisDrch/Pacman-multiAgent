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


public class ArtificialIntelligenceAgent extends Agent {
	Grid grid = new Grid();

	public boolean positionCorrect(int i, int j, Grid grid) {
		return grid.getObtacles(i,j);
	}
	
	protected void setup() {
		Utils.register(this, this.getLocalName());
		System.out.println("### " + getLocalName() + " is now ... Installed !");
		addBehaviour(new GetRequestedFromTravelerBehaviour());
	}
	
	public Cell chooseBestMove(Cell[] cells){
		Cell position = cells[0];
		int randomI = Utils.randomNumber();
		int randomJ = Utils.randomNumber();
		if(randomI == 0) {
			randomJ = 1;
		}
		int i = (position.nligne + randomI)%Constants.DIM_GRID_X;
		int j = (position.ncolonne + randomJ)%Constants.DIM_GRID_Y;
		while(positionCorrect(i, j, grid)) {
			randomI = Utils.randomNumber();
			randomJ = Utils.randomNumber();
			if(randomI == 0) {
				randomJ = 1;
			}
			i = (position.nligne + randomI)%Constants.DIM_GRID_X;
			j = (position.ncolonne + randomJ)%Constants.DIM_GRID_Y;
		}
		position.nligne = i;
		position.ncolonne = j;
		return position;
	}

	
	/**
	 * GetRequestedFromTravelerBehaviour to wait for traveler request ofbest position
	 * On each request, AI will call for proposal the analyser.
	 * Chose the best option, based on traveler position.
	 */
	private class GetRequestedFromTravelerBehaviour extends CyclicBehaviour {
		
		@Override
		public void action() {
			// should receive a message that match console jade template : REQUEST and conversationId
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST).MatchConversationId(Constants.TRAVELER_AI_CONVERSATION_ID);
			ACLMessage message = myAgent.receive(mt);
			
			if (message != null) {
				try {
					//System.out.print("\nAgent " + myAgent.getLocalName() + " has just received a request  --- ");
					String jsonMessage = message.getContent(); // chaîne JSON
					// parse json message with Traveler cellsbag
					Gson gson = new Gson();
					Cell travelerCurrentPosition = gson.fromJson(jsonMessage, Cell.class);
					// ask for analysers @fake
					Cell[] cells = new Cell[1];
					// chose best options @fake
					cells[0] = travelerCurrentPosition;
					Cell bestMove = ((ArtificialIntelligenceAgent)myAgent).chooseBestMove(cells);
					
					ACLMessage updatedPositionReply = message.createReply();
					// add performative
					updatedPositionReply.setPerformative(ACLMessage.INFORM);
					// add new position as content in json
					ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
					String jsonCell = ow.writeValueAsString(bestMove);
					updatedPositionReply.setContent(jsonCell);
					// replying with new best position
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
		
		

