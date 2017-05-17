package agent;

import java.util.Random;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import massage.InformMessage;
import massage.SuscribeMessage;
import model.FonctionSpace;
import model.ListeValeur;
import models.Cellule;

public class MonsterAgent extends Agent {

	public Cellule position;  //Abscisse 
	
	public int index_monster_agent //Numero de l'agent 
	
	@Override
	protected void setup() {
		register();
		System.out.println(getLocalName() + "--> Installed");
		Object[] args = getArguments();
		position = new Cellule() {line = random.nextInt(9 - 0 + 1) + 0, column = random.nextInt(9 - 0 + 1) + 0};
		addBehaviour(new SuscribeBehaviour());	
		addBehaviour(new AnalyseBehaviour());
	}
	
	private void register() {
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("Analyse");
		sd.setName("Analyse");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
	}
		
	private AID searchAgent(String type, String name) {
		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType(type);
		sd.setName(name);
		template.addServices(sd);
		AID receiver = null;
		try {
			DFAgentDescription[] result = DFService.search(this, template);
			int n = result.length;
			Random r = new Random();
			if (n > 0) {
				int v = r.nextInt(n);
				receiver = result[v].getName();
			}
		} catch (FIPAException fe) {
			fe.printStackTrace();
		} 
		return receiver;
	}
	
	/*
	 * Ce behaviour permet à chaque agent analyse de s'enregistrer auprès de l'agent de Simulation
	 */
	public class SuscribeBehaviour extends OneShotBehaviour {	
		private static final long serialVersionUID = 1L; 		
		@Override 
		public void action() {
			ACLMessage messageSimulateur = new SuscribeMessage(searchAgent("Simulateur", "Simulateur"));
			messageSimulateur.setContent(analyse_agent); 
			send(messageSimulateur);		
		}	
	}
		
	/*
	 * Ce behaviour implémente les algorithmes permettant d'améliorer une liste de 9 cellules
	 * A chaque étape de la résolution, l'agent d'analyse reçoit une copie des cellules à traiter
	 * Après traitment de ces cellules, il retourne les cellules améliorées à l'agent environnement.
	 */
	public class AnalyseBehaviour extends CyclicBehaviour {	
		private static final long serialVersionUID = 1L; 				
		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);	
		
		@Override
		public void action() {			
			ACLMessage message = receive(mt);  
			if (message != null) {				
				stringCellule = message.getContent().substring(1, message.getContent().length());				
				cellule = FonctionSpace.changeToInt(stringCellule);									
				  	 				
				/* Algorithme 2 */
				ListeValeur.resetListeValeur(index_analyse_agent-1, cellule, ListeValeur.listevaleurs); 
										
				/* Algorithme 1 */
				valideCellule = ListeValeur.getVlideAgentListeAlgo1(index_analyse_agent-1, cellule, ListeValeur.listevaleurs); 					
						
				/* Algorithme 2 */
				ListeValeur.resetListeValeur(index_analyse_agent-1, valideCellule, ListeValeur.listevaleurs); 
				
				/* Algorithme 3 */
				valideCellule = ListeValeur.getVlideAgentListeAlgo3(index_analyse_agent-1, valideCellule, ListeValeur.listevaleurs); 				
				
				/* Algorithme 4 */
				ListeValeur.resetValeurDeValiderListeAlgo4(index_analyse_agent-1, valideCellule, ListeValeur.listevaleurs); 
						
				/*
				 * Chaque fois, on retourne les cellules à Agent Environnement pour mettre à jour le Sudoku
				 */
				ACLMessage messageEnvironnement = new InformMessage(searchAgent("Environnement", "Environnement"));
				messageEnvironnement.setContent(FonctionSpace.changeToString(valideCellule)); 
				messageEnvironnement.setConversationId(analyse_agent);
				send(messageEnvironnement);					
			}else{
				block();
			}
		}
	}
		
}


