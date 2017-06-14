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


	ArrayList<AID> analysersSubscriptionsList = new ArrayList<>();

	protected void setup() {
		Utils.register(this, this.getLocalName());
		System.out.println("### " + getLocalName() + " is now ... Installed !");
		addBehaviour(new GetRequestedFromTravelerBehaviour());
		addBehaviour(new GetProposalFromAnalyserBehaviour());
	}
	
	public Cell chooseBestMove(Cell[] cells){
		Cell position = cells[0];
		position.nligne= (position.nligne + 1)%Constants.DIM_GRID_X;
		return position;
	}

	
	/**
	 * GetRequestedFromTravelerBehaviour to wait for traveler request ofbest position
	 * On each request, AI will call for proposal the analyser.
	 * Chose the best option, based on traveler position.
	 */

	public class GetProposalFromAnalyserBehaviour extends Behaviour {

		@Override
		public void action() {
			// TODO Auto-generated method stub
			MessageTemplate mTemplate = MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE);
			ACLMessage analyserMsg ;
			if((analyserMsg = receive(mTemplate)) == null)
			{
				block();
				return;
			}
			
			analysersSubscriptionsList.add(Utils.searchForAgent(myAgent, analyserMsg.getContent()));
			System.out.println("J'ai recu un message d'inscription de l'analyser \n"+analysersSubscriptionsList);
		}

		@Override
		public boolean done() {
			// TODO Auto-generated method stub
			return false;
		}

	}
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
					//TODO : Ask For analyser
					analysersSubscriptionsList.forEach(cle->{
						ACLMessage message_to_analyzer= new ACLMessage(ACLMessage.CFP);
						message_to_analyzer.addReceiver(cle);
						ObjectMapper mapper = new ObjectMapper();
						try {
							message_to_analyzer.setContent(mapper.writeValueAsString(travelerCurrentPosition));
							send(message_to_analyzer);
						} catch (JsonProcessingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					});
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
		
		

