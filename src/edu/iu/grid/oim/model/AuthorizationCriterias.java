package edu.iu.grid.oim.model;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.model.db.CertificateRequestUserModel;

public class AuthorizationCriterias {
    static Logger log = Logger.getLogger(AuthorizationCriterias.class);  
	//individual test, sort of.
	public abstract class AuthorizationCriteria {
		public Boolean pass;
		public String name; //like.. "Does this user provided x509 certificate" 
		public String help_id; //editable help doc to be shown if requested - set to non-null if you want to let support staff edit this.
		
		public AuthorizationCriteria(String name, String help_id) {
			this.name = name;
			this.help_id = help_id;
			try {
				pass = test(); //should I do lazy-testing instead? For now, I don't think we gain much
			} catch (Exception e)  {
				//if test throws exception, treat it as no-pass..
				log.error("Exception thrown while testing: "+name, e);
				pass = false;
			}
			criterias.add(this);
		}
		
		//put your test code here to return true or false
		abstract public Boolean test();	
	}
	
	ArrayList<AuthorizationCriteria> criterias = new ArrayList<AuthorizationCriteria>();
	//TODO - don't know how to implement Iterable interface..
	public ArrayList<AuthorizationCriteria> getCriterias() {
		return criterias;
	}
	
	public void addAll(AuthorizationCriterias others) {
		criterias.addAll(others.criterias);
	}

	public boolean passAll() {
		for(AuthorizationCriteria criteria : criterias) {
			if(!criteria.pass) return false; 
		}
		//all good
		return true; 
	}

	//call this to retest
	public void retestAll() {
		for(AuthorizationCriteria criteria : criterias) {
			criteria.pass = criteria.test();
		}
	}
}

