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
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.SequentialBehaviour;
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
import jade.util.leap.HashMap;


public class ArtificialIntelligenceAgent extends Agent {
	public int numberProposeReceived;
	ArrayList<Cell> predictedMonsterPositionsList = new ArrayList<>();
	ArrayList<AID> analysersSubscriptionsList = new ArrayList<>();

	Grid grid = new Grid();

	protected void setup() {
		Utils.register(this, this.getLocalName());
		System.out.println("### " + getLocalName() + " is now ... Installed !");
		numberProposeReceived = 0;
		addBehaviour(new MySequentialBehaviour(analysersSubscriptionsList));
	}

	public boolean positionCorrect(int i, int j, Grid grid) {
		return grid.getObtacles(i,j);
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
	 * MySequentialBehaviour construit la logique entière de cet agent.
	 * De manière séquentiel, il attend que les analyser soient inscris.
	 * Puis, il déclenche les Cyclicls behaviour en parallele
	*/
	private class MySequentialBehaviour extends SequentialBehaviour {
		
		public MySequentialBehaviour(ArrayList agentSubscriptions) {
			System.out.println("### Beginning the main game logic ...  ");
			addSubBehaviour(new AcceptNewSubscriptionBehaviour(agentSubscriptions));
			addSubBehaviour(new MyParallelLogicBehaviour());
		}
		
	}

	/**
	 * AcceptNewSubscriptionBehaviour accepte et inscrit les nouveaux agents Monster lorsqu'il est notifié
	 * par eux. 
	*/
	private class AcceptNewSubscriptionBehaviour extends Behaviour {
		public ArrayList myAgentSubscriptions;
		
		public AcceptNewSubscriptionBehaviour(ArrayList agentSubscriptions) {
			System.out.println("### Waiting for analyser' subscription ...  ");
			this.myAgentSubscriptions =  agentSubscriptions;
		}
		
		@Override
		public boolean done() {
			if (myAgentSubscriptions.size() >= Constants.MONSTER_NUMBER) {
				System.out.println("\n### Waiting for analyser' subscription ... done !" + "\n ==> Size is " + myAgentSubscriptions.size());
				return true;
			} else return false;
		}

		@Override
		public void action() {
			// should receive a message that match console jade template : SUBSCRIBE and conversationID 
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE);
			ACLMessage message = myAgent.receive(mt);
			
			if (message != null) {
				System.out.print("\nAgent " + myAgent.getLocalName() + " has just received message --- " + message.getContent());
				AID senderAID = message.getSender();
				// inserting new AID in Simulation Agent
				if (this.insertNewSubscription(senderAID)) {
					System.out.print("\nAgent " + myAgent.getLocalName() + " has just subscribed --- " + message.getSender().getName());
				} else {
					// simulater capacity reached
					System.out.print("\nERROR --- on" + myAgent.getLocalName() +  ": subscriptions max capacity reached");
					block();
				}
			} else {
				block();
			}
		}
		public boolean insertNewSubscription(AID agentAID) {
			if (analysersSubscriptionsList.size() > Constants.MONSTER_NUMBER) {
				return false;
			}
			analysersSubscriptionsList.add(agentAID);
			return true;
		}
	}
	
	/**
	 * Behaviour to trigger both cyclic behaviour
	*/
	private class MyParallelLogicBehaviour extends ParallelBehaviour {
		
		public MyParallelLogicBehaviour() {
			System.out.println("### Beginning the main game logic ...  ");
			//addSubBehaviour(new GetRequestedFromTravelerBehaviour());
			//addSubBehaviour(new GetProposalFromAnalyserBehaviour());
		}
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
					//System.out.print("\nAgent " + myAgent.getLocalName() + " has just received a request  --- ");
					String jsonMessage = message.getContent(); 
					Gson gson = new Gson();
					Cell travelerPosition = gson.fromJson(jsonMessage, Cell.class);
					
					//CONTRACT-NET to Analysers
					analysersSubscriptionsList.forEach(cle->{
						ACLMessage message_to_analyzer= new ACLMessage(ACLMessage.CFP);
						message_to_analyzer.addReceiver(cle);
						send(message_to_analyzer);
					});
				} else {
					block();
				}
			}
				
		}
	
	/**
	 * GetProposalFromAnalyserBehaviour
	 * receive all position and stock it in list
	 * Chose the best option, based on traveler position and predicted position.
	*/
	public class GetProposalFromAnalyserBehaviour  extends CyclicBehaviour{
		@Override
		public void action() {
			if (numberProposeReceived < 5){
				MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
				ACLMessage message = myAgent.receive(mt);
				if (message != null){
					try {
						String jsonMessage = message.getContent(); // chaîne JSON
						//TODO : to get a particular object (int : number of the Analyser sender and the Cell[]) and put it in the Map
						
						
						Gson gson = new Gson();
						Cell[] monsterPossiblePosition = (Cell[]) gson.fromJson(jsonMessage, Cell[].class);

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
						
					}
					catch(Exception e){e.printStackTrace();}
				}
				else{block();}
			}
		}
	}
	
}
		
		

