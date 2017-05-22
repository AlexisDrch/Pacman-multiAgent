package agents;

import java.io.IOException;
import java.io.Serializable;
import java.util.Random;
import models.*;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.leap.ArrayList;


public class Engine extends Agent {
	public ArrayList mySubscriptions = new ArrayList();

	protected void setup() {
		Utils.register(this, this.getLocalName());
		System.out.println(getLocalName() + "--> Installed");
	}

}

