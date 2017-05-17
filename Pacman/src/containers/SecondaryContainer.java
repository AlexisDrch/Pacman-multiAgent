package containers;

import containers.models.Constants;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

public class SecondaryContainer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		startWithProfile();
	}
	
	public static void startWithProfile() {
	 
		Runtime rt = Runtime.instance();
		ProfileImpl p = null;
		ContainerController cc;
		AgentController ac;
		int i;
	  	  try{
	  		/**
	  		 * host : null value means use the default (i.e. Localhost)
             * port - is the port number. A negative value should be used for using the default port number.
             * platformID - is the symbolic name of the platform, 
             * isMain : boolean
	  		 */
	  		p  =  new  ProfileImpl(null,-1,"tdia04",false);
	  		
			cc = rt.createAgentContainer(p);
			
			// generate 1 Environment Agent
			ac = cc.createNewAgent(Constants.ENVIRONMENT_DESCRIPTION, "sudoku.Agents.EnvAgent", null);
			ac.start();
			
			// generate 1 Simulation Agent 
			ac = cc.createNewAgent(Constants.SIMULATER_DESCRIPTION, "sudoku.Agents.SimulationAgent", null);
			ac.start();
			
			// generate 27 different Analyse Agents
			for(i = 0 ; i < Constants.ANALYSE_AGENT_NUMBER ; i++) {
				String agent_name = String.valueOf("Analyser" + (i+1));
				ac = cc.createNewAgent(agent_name, "sudoku.Agents.AnalyseAgent", null);
				ac.start();
			}
			
	  	  } catch(Exception ex) {
	  		  ex.printStackTrace();
	  	  }
	}
}
