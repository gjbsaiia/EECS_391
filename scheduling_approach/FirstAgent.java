import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import edu.cwru.sepia.environment.model.*;
import edu.cwru.sepia.util.Direction;


/*
 *  Stuart resource agent
 * 
 */

public class FirstAgent extends Agent {
	
	PeasantOrg resourceSched;

	public FirstAgent(int arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Map<Integer, Action> initialStep(StateView newstate, HistoryView statehistory) {
		resourceSched = new PeasantOrg(playernum, newstate);
		return middleStep(newstate, statehistory);
	}

	@Override
	public void loadPlayerData(InputStream arg0) {
		// TODO Auto-generated method stub

	}


public Map middleStep(StateView newstate, HistoryView statehistory) {

        Map<Integer, Action> actions = new HashMap<Integer, Action>();
        
        actions = resourceSched.update(actions, newstate);

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

        int currentGold = newstate.getResourceAmount(playernum, ResourceType.GOLD);
        int currentWood = newstate.getResourceAmount(playernum, ResourceType.WOOD);

        /*
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
        */

        return actions;
}

	@Override
	public void savePlayerData(OutputStream arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void terminalStep(StateView arg0, HistoryView arg1) {
		System.out.println("Finsihed the episode");

	}

}
