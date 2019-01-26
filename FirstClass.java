import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import edu.cwru.sepia.action.Action;
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

/*
 * Eric's experiment
 * 
 */

public class FirstClass extends Agent {

	public FirstClass(int playernum) {
		super(playernum);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Map initialStep(StateView newstate, HistoryView statehistory) {

		return middleStep(newstate, statehistory);
}


	@Override
	public void loadPlayerData(InputStream arg0) {
		// TODO Auto-generated method stub

	}
	/*
	@Override
	public Map middleStep(StateView newstate, HistoryView statehistory) {
        // This stores the action that each unit will perform
        // if there are no changes to the current actions then this
        // map will be empty.
        Map<Integer, Action> actions = new HashMap<Integer, Action>();

        // this will return a list of all of your units
        // You will need to check each unit ID to determine the unit's type
        List<Integer> myUnitIds = newstate.getUnitIds(playernum);

        // These will store the Unit IDs that are peasants and townhalls respectively
        List<Integer> peasantIds = new ArrayList<Integer>();
        List<Integer> townhallIds = new ArrayList<Integer>();

        // This loop will examine each of our unit IDs and classify them as either
        // a Townhall or a Peasant
        for(Integer unitID : myUnitIds)
        {
                // UnitViews extract information about a specified unit id
                // from the current state. Using a unit view you can determine
                // the type of the unit with the given ID as well as other information
                // such as health and resources carried.
                UnitView unit = newstate.getUnit(unitID);

                // To find properties that all units of a given type share
                // access the UnitTemplateView using the `getTemplateView()`
                // method of a UnitView instance. In this case we are getting
                // the type name so that we can classify our units as Peasants and Townhalls
                String unitTypeName = unit.getTemplateView().getName();

                if(unitTypeName.equals("TownHall"))
                        townhallIds.add(unitID);
                else if(unitTypeName.equals("Peasant"))
                        peasantIds.add(unitID);
                else
                        System.err.println("Unexpected Unit type: " + unitTypeName);
        }

        // Now that we know the unit types we can assign our peasants to collect resources
        for(Integer peasantID : peasantIds)
        {
                actions.put(peasantID, Action.createPrimitiveMove(peasantID, Direction.EAST));
        }

        return actions;
}
*/
	@Override
	public Map middleStep(StateView newstate, HistoryView statehistory) {

        Map<Integer, Action> actions = new HashMap<Integer, Action>();


        List<Integer> myUnitIds = newstate.getUnitIds(playernum);

        List<Integer> peasantIds = new ArrayList<Integer>();
        List<Integer> townhallIds = new ArrayList<Integer>();
        List<Integer> farmIds = new ArrayList<Integer>();
        List<Integer> barracksIds = new ArrayList<Integer>();
        List<Integer> footmanIds = new ArrayList<Integer>();

        for(Integer unitID : myUnitIds)
        {

                UnitView unit = newstate.getUnit(unitID);
                String unitTypeName = unit.getTemplateView().getName();

                if(unitTypeName.equals("TownHall"))
                	townhallIds.add(unitID);
                else if(unitTypeName.equals("Peasant"))
                	peasantIds.add(unitID);
                else if(unitTypeName.equals("Farm"))
                	farmIds.add(unitID);
                else if(unitTypeName.equals("Barracks"))
                	barracksIds.add(unitID);
                else if(unitTypeName.equals("Footman"))
                	footmanIds.add(unitID);
                else
                    System.err.println("Unexpected Unit type: " + unitTypeName);
        }
        
        int townHallX=newstate.getUnit(townhallIds.get(0)).getXPosition();
        int townHallY=newstate.getUnit(townhallIds.get(0)).getYPosition();
       // System.out.println(townHallX);
      //  System.out.println(townHallY);
        
        int currentGold = newstate.getResourceAmount(playernum, ResourceType.GOLD);
        int currentWood = newstate.getResourceAmount(playernum, ResourceType.WOOD);

        List<Integer> goldMines = newstate.getResourceNodeIds(Type.GOLD_MINE);
        List<Integer> trees = newstate.getResourceNodeIds(Type.TREE);

        //builds peasants until have 3 peasants
        if(peasantIds.size() < 3)
        {
                if(currentGold >= 400)
                {

                        TemplateView peasantTemplate = newstate.getTemplate(playernum, "Peasant");
                        int peasantTemplateID = peasantTemplate.getID();

                        int townhallID = townhallIds.get(0);

                        actions.put(townhallID, Action.createCompoundProduction(townhallID, peasantTemplateID));
                }
        }
        
        //tell peasants to mine resources
        for(Integer peasantID : peasantIds)
        {
                Action action = null;
                if(newstate.getUnit(peasantID).getCargoAmount() > 0)
                {
                        action = new TargetedAction(peasantID, ActionType.COMPOUNDDEPOSIT, townhallIds.get(0));
                }
                else
                {
                        if(currentGold < currentWood)
                        {
                                action = new TargetedAction(peasantID, ActionType.COMPOUNDGATHER, goldMines.get(0));
                        }
                        else
                        {
                                action = new TargetedAction(peasantID, ActionType.COMPOUNDGATHER, trees.get(0));
                        }
                }

                actions.put(peasantID, action);
        }
        
        
        
        //build a farm when you can
        if(currentGold >= 500 && currentWood >= 250 && farmIds.size()>=0) 
        {
        	//if(lastX>0)
        //System.out.println("AGGGGGGGGGGGGGGGGGGGGGGGGGG"+ townHallX);
        	TemplateView farmTemplate = newstate.getTemplate(playernum, "Farm");
            int farmTemplateID = farmTemplate.getID();
        	
        	if(farmIds.size()==0)
        	{
        		   actions.put(peasantIds.get(0), Action.createCompoundBuild(peasantIds.get(0), farmTemplateID, 9,9));
        	//	lastX=9;
        	//	lastY=9;
        	}
        	else if (farmIds.size()<=6)
        	{
        		int farmX=newstate.getUnit(farmIds.get(farmIds.size()-1)).getXPosition();
                int farmY=newstate.getUnit(farmIds.get(farmIds.size()-1)).getYPosition();
                if(farmX==townHallX+1)
                {
                	farmX=townHallX-2;
                	farmY+=2;
                }
                
        		//if(y>townHallY+1)
        		//{
        		//	y=townHallY-1;
        		//	x=lastX+1;
        		//}
        		actions.put(peasantIds.get(0), Action.createCompoundBuild(peasantIds.get(0), farmTemplateID, farmX+2,farmY));
        	   // lastX=lastX+1;
        	   //lastY=lastY+1;
        	}
        //	else 
        	//	actions.put(peasantIds.get(0), Action.createCompoundBuild(peasantIds.get(0), farmTemplateID, 9,10));
       	 
           }
        /*
        //build a barracks when you can
        if(currentGold >= 700 && currentWood >= 400 && farmIds.size()>0 && barracksIds.size()==0 ) {
        	TemplateView barracksTemplate = newstate.getTemplate(playernum, "Barracks");
            int barracksTemplateID = barracksTemplate.getID();

            actions.put(peasantIds.get(0), Action.createCompoundBuild(peasantIds.get(0), barracksTemplateID, 10, 10));
        }
     
        //build footman
        if(barracksIds.size() > 0 && currentGold >= 600) {
        	TemplateView footmanTemplate = newstate.getTemplate(playernum, "Footman");
            int footmanTemplateID = footmanTemplate.getID();
            
            actions.put(barracksIds.get(0), Action.createCompoundProduction(barracksIds.get(0), footmanTemplateID));

        }
*/
        return actions;
}

	@Override
	public void savePlayerData(OutputStream arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void terminalStep(StateView newstate, HistoryView statehistory) {
        System.out.println("Finsihed the episode");
}

}
