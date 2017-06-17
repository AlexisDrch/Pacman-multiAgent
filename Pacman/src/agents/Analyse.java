package agents;

import java.io.IOException;
import java.security.acl.Acl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.gson.Gson;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import models.Cell;
import models.Constants;
import models.Utils;

public class Analyse extends Agent {
	public static String ASK_ENVIRONMENT_MONSTER_POSITION_CONVID = "askEnvMonstPos";

	protected int value;
	protected Cell monsterLastPosition; 

	protected void setup() {
		Utils.register(this, this.getLocalName());
		System.out.println("### " + getLocalName() + " is now ... Installed !");
		// set value to agent
		Object[] args = getArguments();
		this.value = (int) args[0];
		//		this.monsterLastPosition = new Cell(-1, -1, -1);
		// add behaviours
		addBehaviour(new SubscribeToEngineBehaviour());
		addBehaviour(new ReceiveCallForProposalBehaviour());

	}

	public void setValue(int newValue) {
		this.value = newValue;
	}

	public int getValue() {
		return this.value;
	}

	public Cell[] getPossiblePosition(){
		Cell[] tab = new Cell[49];
		int index = 0;
		int x = this.monsterLastPosition.nligne;
		int y = this.monsterLastPosition.ncolonne;
		for (int i= x-3; i<x+3; i++){
			if (i<0 || i> Constants.DIM_GRID_X){i = i%(Constants.DIM_GRID_X);}
			for (int j = y-3; j<y+3 ; j++){
				if (j<0 || j> Constants.DIM_GRID_Y){j = j%(Constants.DIM_GRID_Y);}
				Cell cell = new Cell(0,i,j);
				tab[index++] = cell;  
			}
		}
		return tab;
	}

	/**
	 * TODO
	 * Behaviour to subscribe to IA Agent
	 */
	private class SubscribeToEngineBehaviour extends OneShotBehaviour {

		@Override
		public void action() {
			AID engine = Utils.searchForAgent(myAgent, Constants.AI_DESCRIPTION);
			// should send a subscribe message to simulation Agent
			ACLMessage subscribeMessage = new ACLMessage(ACLMessage.SUBSCRIBE);
			// add performative
			//			subscribeMessage.setPerformative(ACLMessage.SUBSCRIBE);
			// add engine  as a receiver
			subscribeMessage.addReceiver(engine);
			subscribeMessage.setContent(myAgent.getLocalName());
			// send message to engine
			send(subscribeMessage);
			//			System.out.print("\nAgent " + myAgent.getLocalName() + " has just sent a SubscribeToSimulater message to " + engine.getName());
		}
	}

	/**
	 * Behaviour to ask the environment the position of the monster related.
	 * The analyse agent send a message to the environment to execute a fonction find_position()
	 * On its reception, The analyse will have to run a fonction to predicate a critique zone
	 * Then the new position is sent to the environment.
	 */
	private class AskForMonsterPositionBehaviour extends OneShotBehaviour {

		@Override
		public void action() {
			AID environment = Utils.searchForAgent(myAgent, Constants.ENVIRONMENT_DESCRIPTION);
			// should send a subscribe message to simulation Agent
			ACLMessage subscribeMessage = new ACLMessage(ACLMessage.QUERY_REF);
			// add conversation ID
			subscribeMessage.setConversationId(Analyse.ASK_ENVIRONMENT_MONSTER_POSITION_CONVID+value);
			// add engine  as a receiver
			subscribeMessage.addReceiver(environment);
			subscribeMessage.setContent(Integer.toString(value));
			System.out.println("AskForMonsterPosition");
			// send message to engine
			send(subscribeMessage);
			//			System.out.print("\nAgent " + myAgent.getLocalName() + " has just sent a SubscribeToSimulater message to " + environment.getName());
		}	
	}

	private class WaitForMonsterPositionBehaviour extends OneShotBehaviour {

		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM)
					.MatchConversationId(Analyse.ASK_ENVIRONMENT_MONSTER_POSITION_CONVID+value);
			ACLMessage message ;
			if((message= receive(mt)) ==null)
			{
				block();
				return;
			}

			
			System.out.print("WaitForMonsterPositionBehaviour " );
			System.out.println(message.getContent());
				ObjectMapper mapper = new ObjectMapper();
			
			Cell monsterLastPosition;
			try {
				monsterLastPosition = mapper.readValue(message.getContent(), Cell.class);
				((Analyse)myAgent).monsterLastPosition = monsterLastPosition;	
				
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}


			Cell[] cellsTab = new Cell[49];
			cellsTab = ((Analyse) myAgent).getPossiblePosition();
			try {
				ACLMessage possibleNextPosition = new ACLMessage(ACLMessage.INFORM);
				possibleNextPosition.addReceiver(Utils.searchForAgent(myAgent, Constants.AI_DESCRIPTION));
				// add performative
				possibleNextPosition.setPerformative(ACLMessage.INFORM);
				
				String msg_content= mapper.writeValueAsString(cellsTab);
				System.out.println("Analyseur propose un mouv à l'AI "+msg_content);

				possibleNextPosition.setContent(msg_content);
				// replying with new best position
				send(possibleNextPosition);
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}



		}



	}



	private class ReceiveCallForProposalBehaviour extends CyclicBehaviour {

		@Override
		public void action() {
			//First we wait for a message of type "Call for Proposal" from IAAgent
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
			ACLMessage message;

			if ((message = myAgent.receive(mt)) == null) {
				block();
				return;
			}

			myAgent.addBehaviour(new AskForMonsterPositionBehaviour());
			myAgent.addBehaviour(new WaitForMonsterPositionBehaviour());




		}
	}
}




