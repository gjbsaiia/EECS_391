import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.action.ActionType;
import edu.cwru.sepia.action.TargetedAction;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.environment.model.history.History.HistoryView;
import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.environment.model.state.ResourceNode.Type;
import edu.cwru.sepia.environment.model.state.ResourceType;
import edu.cwru.sepia.environment.model.state.State.StateView;
import edu.cwru.sepia.environment.model.state.Template.TemplateView;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;
import edu.cwru.sepia.environment.model.*;
import edu.cwru.sepia.util.Direction;
/*
 *  Peasant organizer 
 *  	- Methods to efficiently distribute and coordinate resource collection
 */
public class PeasantOrg {
	
	private final int player;
	private List<Integer> townHall;
	private int x;
	private int y;
	
	private int currentGold;
	private int currentWood;
	
	private int goldGatherers;
	private int woodGatherers;
	private List<Peasant> manifest;
	
	private List<Resource> goldList;
	private List<Resource> woodList;
	private List<List<Resource>> resources;
	
	private List<Resource> working;
	
	public PeasantOrg(int playernum, StateView initialState){
		this.player = playernum;
		this.currentGold = initialState.getResourceAmount(this.player, ResourceType.GOLD);
		this.currentWood = initialState.getResourceAmount(this.player, ResourceType.GOLD);
		buildArch(initialState);
	}
	
	private void buildArch(StateView initialState){
		townHall = new ArrayList<Integer>();
		manifest = new ArrayList<Peasant>();
		goldList = new ArrayList<Resource>();
		woodList = new ArrayList<Resource>();
		working = new ArrayList<Resource>();
		
		resources = new ArrayList<List<Resource>>();
		resources.add(goldList);
		resources.add(woodList);
		
		List<Integer> allUnitIds = initialState.getUnitIds(this.player);
		for(Integer unitID : allUnitIds){
			UnitView unit = initialState.getUnit(unitID);
			String unitTypeName = unit.getTemplateView().getName();
            if(unitTypeName.equals("TownHall"))
            	townHall.add(unitID);
            else if(unitTypeName.equals("Peasant"))
            	manifest.add(new Peasant(unitID, 50));
		}
		if(townHall.size() > 0){
			this.x = initialState.getResourceNode((int)this.townHall.get(0)).getXPosition();
			this.y = initialState.getResourceNode((int)this.townHall.get(0)).getYPosition();
					
		}
		List<Integer> tmpGold = initialState.getResourceNodeIds(Type.GOLD_MINE);
		List<Integer> tmpWood = initialState.getResourceNodeIds(Type.TREE);
		int i = 0;
		for(Integer gResource : tmpGold){
			ResourceNode.ResourceView ptr = initialState.getResourceNode((int)gResource);
			goldList.add(new Resource(ptr.getID(), ptr.getXPosition(), ptr.getYPosition(), ptr.getAmountRemaining()));
			goldList.get(i).setDist(x, y);
			i++;
		}
		i = 0;
		for(Integer wResource : tmpWood){
			ResourceNode.ResourceView ptr = initialState.getResourceNode((int)wResource);
			woodList.add(new Resource(ptr.getID(), ptr.getXPosition(), ptr.getYPosition(), ptr.getAmountRemaining()));
			woodList.get(i).setDist(x, y);
			i++;
		}
		int maxDist = ((int)Math.sqrt(Math.pow(((double)initialState.getXExtent()), 2.0)+Math.pow(((double)initialState.getYExtent()), 2.0)))+1;
		initialResourceSort(maxDist, 0); //sort gold first
		initialResourceSort(maxDist, 1); //sort wood
		
	}
	
	private boolean initialResourceSort(int bucketNum, int r){
		int len = resources.get(r).size();
		
        if (len <= 1) return true;
        int max = (int)resources.get(r).get(0).dist;
        int min = (int)resources.get(r).get(0).dist;
        
        int ptr;
        for (int i = 1; i < len; i++) {
        	ptr = (int)resources.get(r).get(i).dist;
            if (ptr > max) max = ptr;
            if (ptr < min) min = ptr;
        }
        double range = ((double)(max - min + 1))/bucketNum;

        ArrayList<ArrayList<Resource>> buckets = new ArrayList<ArrayList<Resource>>();
        int ii = 0;
        while(ii<bucketNum) { //initialize buckets
            buckets.add(new ArrayList<Resource>());
            ii++;
        }

        for (int i = 0; i < len; i++) { //partition the input array
            buckets.get((int)((resources.get(r).get(i).dist - min)/range)).add((resources.get(r).get(i)));
        }

        ptr = 0;
        for (int i = 0; i < buckets.size(); i++) {
            Collections.sort(buckets.get(i)); //mergeSort
            for (int j = 0; j < buckets.get(i).size(); j++) { //merge the buckets
                resources.get(r).set(ptr, buckets.get(i).get(j));
                ptr++;
            }
        }
        return true;
	}
	
	public Map<Integer, Action> assignWork(Map<Integer, Action> actions){
		// some manuvering to get the closest resources that aren't already being farmed
		List<Resource> idleGold = new ArrayList<Resource>();
		List<Resource> idleWood = new ArrayList<Resource>();
		idleGold.addAll(goldList);
		idleGold.removeAll(working);
		idleWood.addAll(woodList);
		idleWood.removeAll(working);
		
		// some ptrs so we don't have to reconfigure the lists each time
		int gi = 0;
		int wi = 0;
		for( Peasant ptr : manifest){
			// if we don't have enough idle resources, just double up the assignments
			if(gi > idleGold.size()) gi = 0;
			if(wi > idleWood.size()) wi = 0;
			// checking to see if peasant is idle
			if(ptr.getStatus() == 0){
				Action action = null;
				// Gold seems to be more important than wood, shooting for 3/2 ratio there
				if(goldGatherers == 0 || (3*goldGatherers) < (2*woodGatherers)){
					ptr.setStatus(1); //peasant is  mining gold
					action = new TargetedAction(ptr.id, ActionType.COMPOUNDGATHER, idleGold.get(gi).id);
					working.add(idleGold.get(gi));
					ptr.setResourceID(idleGold.get(gi).id);
					goldGatherers ++;
					gi ++;
				}
				else{
					ptr.setStatus(2); //peasant is  mining wood
					action = new TargetedAction(ptr.id, ActionType.COMPOUNDGATHER, idleWood.get(wi).id);
					working.add(idleWood.get(wi));
					ptr.setResourceID(idleWood.get(wi).id);
					woodGatherers ++;
					wi ++;
				}
				actions.put(ptr.id, action);
			}
		}
		return actions;
	}
	
	
	/*
	 * This is where I left off
	 * 
	 */
	public void update(Map<Integer, Action> turnActions, StateView newstate){
		List<Integer> allUnitIds = newstate.getUnitIds(this.player);
		for(Integer unitID : allUnitIds){
			UnitView unit = newstate.getUnit(unitID);
			String unitTypeName = unit.getTemplateView().getName();
            if(unitTypeName.equals("TownHall"))
            	townHall.add(unitID);
            else if(unitTypeName.equals("Peasant"))
            	manifest.add(new Peasant(unitID, 50));
		}
        //builds peasants until have 5 peasants
        if(manifest.size() < 5)
        {
            if(currentGold >= 400)
            {

                    TemplateView peasantTemplate = newstate.getTemplate(this.player, "Peasant");
                    int peasantTemplateID = peasantTemplate.getID();

                    int townhallID = townHall.get(0);

                    turnActions.put(townhallID, Action.createCompoundProduction(townhallID, peasantTemplateID));
            }
        }
        
		if(manifest.size() > 0){
			turnActions = assignWork(turnActions);
		}
		
	}
	
	
}
