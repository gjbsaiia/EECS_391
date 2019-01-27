
import java.io.InputStream;
import java.io.OutputStream;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.action.ActionFeedback;
import edu.cwru.sepia.action.ActionResult;
import edu.cwru.sepia.action.ActionType;
import edu.cwru.sepia.action.TargetedAction;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.environment.model.history.History.HistoryView;
import edu.cwru.sepia.environment.model.state.ResourceNode.Type;
import edu.cwru.sepia.environment.model.state.ResourceType;
import edu.cwru.sepia.environment.model.state.State.StateView;
import edu.cwru.sepia.environment.model.state.Template.TemplateView;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;
import edu.cwru.sepia.util.Direction;
import edu.cwru.sepia.environment.*;
import edu.cwru.sepia.util.*;
import java.util.*;
import edu.cwru.sepia.environment.model.history.History.HistoryView;
import edu.cwru.sepia.environment.model.state.State.StateView;

public class ECombatAgent extends Agent {

        private int enemyPlayerNum = 1;

        public ECombatAgent(int playernum, String[] otherargs) {
                super(playernum);

                if(otherargs.length > 0)
                {
                        enemyPlayerNum = new Integer(otherargs[0]);
                }

                System.out.println("Constructed MyCombatAgent");
        }

        @Override
        public Map<Integer, Action> initialStep(StateView newstate,
                        HistoryView statehistory) {
                // This stores the action that each unit will perform
                // if there are no changes to the current actions then this
                // map will be empty
                Map<Integer, Action> actions = new HashMap<Integer, Action>();

                // This is a list of all of your units
                // Refer to the resource agent example for ways of
                // differentiating between different unit types based on
                // the list of IDs
                List<Integer> myUnitIDs = newstate.getUnitIds(playernum);

                // This is a list of enemy units
                List<Integer> enemyUnitIDs = newstate.getUnitIds(enemyPlayerNum);

               // List<Integer> footmenIds = new ArrayList<Integer>();
                List<Integer> towerIds = new ArrayList<Integer>();
                List<Integer> archerIds = new ArrayList<Integer>();
                List<Integer> ballistaIds = new ArrayList<Integer>();
                List<Integer> footmanIds = new ArrayList<Integer>();
                List<Integer> scoutTowerIds = new ArrayList<Integer>();
                
                List<Integer> EtowerIds = new ArrayList<Integer>();
                List<Integer> EarcherIds = new ArrayList<Integer>();
                List<Integer> EballistaIds = new ArrayList<Integer>();
                List<Integer> EfootmanIds = new ArrayList<Integer>();
                List<Integer> EscoutTowerIds = new ArrayList<Integer>();
                
                
                
                if(enemyUnitIDs.size() == 0)
                {
                        // Nothing to do because there is no one left to attack
                        return actions;
                }
                for(Integer unitID : myUnitIDs)
                {

                        UnitView unit = newstate.getUnit(unitID);
                        String unitTypeName = unit.getTemplateView().getName();
                        System.out.println(unitTypeName);
                        if(unitTypeName.equals("Tower"))
                        	towerIds.add(unitID);
                        else if(unitTypeName.equals("Archer"))
                        	archerIds.add(unitID);
                        else if(unitTypeName.equals("Ballista"))
                        	ballistaIds.add(unitID);
                        else if(unitTypeName.equals("Footman"))
                        	footmanIds.add(unitID);
                     
                        else if(unitTypeName.equals("ScoutTower"))
                        	scoutTowerIds.add(unitID);
                        
                        else
                            System.err.println("Unexpected Unit type: " + unitTypeName);
                }
                
                for(Integer unitID : enemyUnitIDs)
                {

                        UnitView unit = newstate.getUnit(unitID);
                        String unitTypeName = unit.getTemplateView().getName();
                        System.out.println(unitTypeName);
                        if(unitTypeName.equals("Tower"))
                        	EtowerIds.add(unitID);
                        else if(unitTypeName.equals("Archer"))
                        	EarcherIds.add(unitID);
                        else if(unitTypeName.equals("Ballista"))
                        	EballistaIds.add(unitID);
                        else if(unitTypeName.equals("ScoutTower"))
                        	EscoutTowerIds.add(unitID);
                        else if(unitTypeName.equals("Footman"))
                        	EfootmanIds.add(unitID);
                        else
                            System.err.println("Unexpected Unit type: " + unitTypeName);
                }
                int min=1000;
                Integer minID=0;
                int dist;
                
                //Find closest footman to attack
                //Uses 7,10 as averge location of myunits
                for(Integer unitID : EfootmanIds)
                {
                	dist=newstate.getUnit(EfootmanIds.get(unitID)).getXPosition()-7+10-newstate.getUnit(EfootmanIds.get(unitID)).getYPosition();
                	if(dist<min)
                	{
                		min=dist;
                		minID=unitID;
                	}
                }
               
                for(Integer myUnitID : archerIds)
                {
                        // Command all of my units to attack the first enemy unit in the list
                	  actions.put(myUnitID, Action.createCompoundAttack(myUnitID, minID));
                                       
                }
                for(Integer myUnitID : ballistaIds)
                {
                        // Command all of my units to attack the first enemy unit in the list
                	  actions.put(myUnitID, Action.createCompoundAttack(myUnitID, minID));
                	 // EscoutTowerIds
                }
               
                return actions;
        }

        @Override
        public Map<Integer, Action> middleStep(StateView newstate,
                        HistoryView statehistory) {
                // This stores the action that each unit will perform
                // if there are no changes to the current actions then this
                // map will be empty
                Map<Integer, Action> actions = new HashMap<Integer, Action>();

                // This is a list of enemy units
                List<Integer> enemyUnitIDs = newstate.getUnitIds(enemyPlayerNum);

                if(enemyUnitIDs.size() == 0)
                {
                        // Nothing to do because there is no one left to attack
                        return actions;
                }

                int currentStep = newstate.getTurnNumber();

                // go through the action history
                for(ActionResult feedback : statehistory.getCommandFeedback(playernum, currentStep-1).values())
                {
                        // if the previous action is no longer in progress (either due to failure or completion)
                        // then add a new action for this unit
                        if(feedback.getFeedback() != ActionFeedback.INCOMPLETE)
                        {
                        	 List<Integer> myUnitIDs = newstate.getUnitIds(playernum);

                             // This is a list of enemy units
                             List<Integer> towerIds = new ArrayList<Integer>();
                             List<Integer> archerIds = new ArrayList<Integer>();
                             List<Integer> ballistaIds = new ArrayList<Integer>();
                             List<Integer> footmanIds = new ArrayList<Integer>();
                             List<Integer> scoutTowerIds = new ArrayList<Integer>();
                             
                             List<Integer> EtowerIds = new ArrayList<Integer>();
                             List<Integer> EarcherIds = new ArrayList<Integer>();
                             List<Integer> EballistaIds = new ArrayList<Integer>();
                             List<Integer> EfootmanIds = new ArrayList<Integer>();
                             List<Integer> EscoutTowerIds = new ArrayList<Integer>();
                             
                             
                             
                             if(enemyUnitIDs.size() == 0)
                             {
                                     // Nothing to do because there is no one left to attack
                                     return actions;
                             }
                             //creates arrayLists of all different types of units to control independently
                             for(Integer unitID : myUnitIDs)
                             {

                                     UnitView unit = newstate.getUnit(unitID);
                                     String unitTypeName = unit.getTemplateView().getName();
                                     System.out.println(unitTypeName);
                                     if(unitTypeName.equals("Tower"))
                                     	towerIds.add(unitID);
                                     else if(unitTypeName.equals("Archer"))
                                     	archerIds.add(unitID);
                                     else if(unitTypeName.equals("Ballista"))
                                     	ballistaIds.add(unitID);
                                     else if(unitTypeName.equals("Footman"))
                                     	footmanIds.add(unitID);
                                  
                                     else if(unitTypeName.equals("ScoutTower"))
                                     	scoutTowerIds.add(unitID);
                                     
                                     else
                                         System.err.println("Unexpected Unit type: " + unitTypeName);
                             }
                             //creates arrayLists of all different types of enemy units to control independently
                             for(Integer unitID : enemyUnitIDs)
                             {

                                     UnitView unit = newstate.getUnit(unitID);
                                     String unitTypeName = unit.getTemplateView().getName();
                                     System.out.println(unitTypeName);
                                     if(unitTypeName.equals("Tower"))
                                     	EtowerIds.add(unitID);
                                     else if(unitTypeName.equals("Archer"))
                                     	EarcherIds.add(unitID);
                                     else if(unitTypeName.equals("Ballista"))
                                     	EballistaIds.add(unitID);
                                     else if(unitTypeName.equals("ScoutTower"))
                                     	EscoutTowerIds.add(unitID);
                                     else if(unitTypeName.equals("Footman"))
                                     	EfootmanIds.add(unitID);
                                     else
                                         System.err.println("Unexpected Unit type: " + unitTypeName);
                             }
                             
                             //finds closest footman to attack, also stores unitID
                             int min=1000;
                             Integer minID=0;
                             int dist;
                             for(Integer unitID : EfootmanIds)
                             {
                           
                            	 dist=newstate.getUnit(EfootmanIds.get(unitID)).getXPosition()-7+10-newstate.getUnit(EfootmanIds.get(unitID)).getYPosition();
                             	if(dist<min)
                             	{
                             		min=dist;
                             		minID=unitID;
                               	}
                             	
                             }
                   
                             for(Integer myUnitID : archerIds)
                             {
                                     // Command all of my units to attack the first enemy unit in the list
                             	  actions.put(myUnitID, Action.createCompoundAttack(myUnitID, minID));
                              }
                             for(Integer myUnitID : ballistaIds)
                             {
                                     // Command all of my units to attack the first enemy unit in the list
                             	  actions.put(myUnitID, Action.createCompoundAttack(myUnitID, minID));
                             	 // EscoutTowerIds
                   
                             }
                             for(Integer myUnitID : footmanIds)
                             {
                                     // Command all of my units to attack the first enemy unit in the list
                             	  actions.put(myUnitID, Action.createCompoundAttack(myUnitID, minID));
                                 
                             }
                            
                        }
                }

                return actions;
        }

        @Override
        public void terminalStep(StateView newstate, HistoryView statehistory) {
                System.out.println("Finished the episode");
        }

        public void savePlayerData(OutputStream os) {
                // TODO Auto-generated method stub

        }

        public void loadPlayerData(InputStream is) {
                // TODO Auto-generated method stub

        }

}