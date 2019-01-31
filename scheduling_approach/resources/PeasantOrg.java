
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
	
	private final int woodCap = 5000;
	private final int goldCap = 2000;
	
	private final int player;
	private List<Integer> townHall;
	private List<Integer> barracks;
	private List<Integer> farm;
	
	
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
	
	
	public PeasantOrg(int playernum, StateView initialState){
		this.player = playernum;
		this.currentGold = initialState.getResourceAmount(this.player, ResourceType.GOLD);
		this.currentWood = initialState.getResourceAmount(this.player, ResourceType.GOLD);
		buildArch(initialState);
	}
	
	private void buildArch(StateView initialState){
		townHall = new ArrayList<Integer>();
		barracks = new ArrayList<Integer>();
		farm = new ArrayList<Integer>();
		
		manifest = new ArrayList<Peasant>();
		goldList = new ArrayList<Resource>();
		woodList = new ArrayList<Resource>();
		
		resources = new ArrayList<List<Resource>>();
		resources.add(goldList);
		resources.add(woodList);
		
		List<Integer> allUnitIds = initialState.getUnitIds(this.player);
		for(Integer unitID : allUnitIds){
			UnitView unit = initialState.getUnit(unitID);
			String unitTypeName = unit.getTemplateView().getName();
            if(unitTypeName.equals("TownHall")){
            	townHall.add(unitID);
            }
            else if(unitTypeName.equals("Peasant"))
            	manifest.add(new Peasant(unitID, 100));
		}
		if(townHall.size() > 0){
			this.x = initialState.getUnit(townHall.get(0)).getXPosition();
			this.y = initialState.getUnit(townHall.get(0)).getYPosition();
					
		}
		List<Integer> tmpGold = initialState.getResourceNodeIds(Type.GOLD_MINE);
		List<Integer> tmpWood = initialState.getResourceNodeIds(Type.TREE);
		int i = 0;
		for(Integer gResource : tmpGold){
			ResourceNode.ResourceView ptr = initialState.getResourceNode(gResource);
			goldList.add(new Resource(ptr.getID(), ptr.getXPosition(), ptr.getYPosition(), this.goldCap));
			goldList.get(i).setDist(x, y);
			i++;
		}
		i = 0;
		for(Integer wResource : tmpWood){
			ResourceNode.ResourceView ptr = initialState.getResourceNode((int)wResource);
			woodList.add(new Resource(ptr.getID(), ptr.getXPosition(), ptr.getYPosition(), this.woodCap));
			woodList.get(i).setDist(x, y);
			i++;
		}
		int maxDist = ((int)Math.sqrt(Math.pow(((double)initialState.getXExtent()), 2.0)+Math.pow(((double)initialState.getYExtent()), 2.0)))+1;
		initialResourceSort(maxDist, 0); //sort gold first
		initialResourceSort(maxDist, 1); //sort wood
		i = 0;
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
	
	public Map<Integer, Action> assignWork(StateView newstate, Map<Integer, Action> turnActions){
		Map<Integer, Action> actions = new HashMap<Integer, Action>();
		// some ptrs so we don't have to reconfigure the lists each time
		int gi = 0;
		int wi = 0;
		for( Peasant ptr : manifest){
			Action action = null;
			if(!turnActions.containsKey(ptr.id)){
				switch(ptr.getStatus()){
				case(0):
					// Gold seems to be more important than wood, prioritizing gold
					if(goldGatherers > woodGatherers){
						ptr.setStatus(2); //peasant is  mining wood
						ptr.setType(2);
						while(!(woodList.get(wi).checkCap() && woodList.get(wi).isActive()))	wi++;	
						if(wi > woodList.size()) wi = 0;
						action = new TargetedAction(ptr.id, ActionType.COMPOUNDGATHER, woodList.get(wi).id);
						woodList.get(wi).incWork();
						ptr.setResource(woodList.get(wi));
						woodGatherers ++;
					}
					else{
						ptr.setStatus(1); //peasant is  mining gold
						ptr.setType(1);
						while(!(goldList.get(gi).checkCap() && goldList.get(gi).isActive())) gi++;
						if(gi > goldList.size()) gi = 0;
						action = new TargetedAction(ptr.id, ActionType.COMPOUNDGATHER, goldList.get(gi).id);
						goldList.get(gi).incWork();
						ptr.setResource(goldList.get(gi));
						goldGatherers ++;
					}
					System.out.println("Worker ID: "+ptr.id+", status: "+ptr.getStatus()+", type: "+ptr.getType()+", capacity: "+ptr.getCapacity());
					System.out.println("     Resource ID: "+ptr.getResource().id+", Cargo type: "+newstate.getUnit(ptr.id).getCargoType()+", Amount: "+newstate.getUnit(ptr.id).getCargoAmount());
					break;
				case(1):
					if(ptr.getResource() != null && ptr.getResource().isActive()){
						if(newstate.getUnit(ptr.id).getCargoAmount() >= ptr.getCapacity()){
							ptr.getResource().updateAmount(newstate.getUnit(ptr.id).getCargoAmount());
							action = new TargetedAction(ptr.id, ActionType.COMPOUNDDEPOSIT, townHall.get(0));
							ptr.setStatus(3);						
						}
						else{
							action = new TargetedAction(ptr.id, ActionType.COMPOUNDGATHER, ptr.getResource().id);
						}
					}
					else ptr.setStatus(0);
					break;
				case(2):
					if(ptr.getResource() != null && ptr.getResource().isActive()){
						if(newstate.getUnit(ptr.id).getCargoAmount() >= ptr.getCapacity()){
							ptr.getResource().updateAmount(newstate.getUnit(ptr.id).getCargoAmount());
							action = new TargetedAction(ptr.id, ActionType.COMPOUNDDEPOSIT, townHall.get(0));
							ptr.setStatus(3);						
						}
						else{
							action = new TargetedAction(ptr.id, ActionType.COMPOUNDGATHER, ptr.getResource().id);
						}
					}
					else ptr.setStatus(0);
					break;
				case(3):
					if(newstate.getUnit(ptr.id).getCargoAmount() < ptr.getCapacity()){
						if(ptr.getResource() != null && ptr.getResource().isActive()){
							action = new TargetedAction(ptr.id, ActionType.COMPOUNDGATHER, ptr.getResource().id);
							ptr.setStatus(ptr.getType());
						}
						else{
							if(!(ptr.getResource().isActive())){
								System.out.println("*************************************************");
								System.out.println("Resource "+ptr.getResource().id+" has been exhausted.");
								System.out.println("*************************************************");
								ptr.setResource(null);
							}
							if(ptr.getType() == 1) goldGatherers --;
							else woodGatherers --;
							ptr.setType(0);
							ptr.setStatus(0);
						}
					}
					else action = new TargetedAction(ptr.id, ActionType.COMPOUNDDEPOSIT, townHall.get(0));
					break;
				}
				if(action != null) actions.put(ptr.id, action);
			}
		}
		return actions;
	}
	
	private Map<Integer, Action> itinerary(StateView newstate){
		Map<Integer, Action> turnActions = new HashMap<Integer, Action>();
        //builds peasants until have 5 peasants
        if(manifest.size() < 5){
            if(currentGold >= 400){
	            TemplateView peasantTemplate = newstate.getTemplate(this.player, "Peasant");
	            turnActions.put(townHall.get(0), Action.createCompoundProduction(townHall.get(0), peasantTemplate.getID()));
            }
        }
		//build a farm when you can
        if(currentGold >= 500 && currentWood >= 250 && farm.size()==0) {
        	TemplateView farmTemplate = newstate.getTemplate(this.player, "Farm");
            turnActions.put(manifest.get(0).id, Action.createCompoundBuild(manifest.get(0).id, farmTemplate.getID(), 5, 3));
        }
        
        //build a barracks when you can
        if(currentGold >= 700 && currentWood >= 400 && farm.size()>0 && barracks.size()==0 ) {
        	TemplateView barracksTemplate = newstate.getTemplate(this.player, "Barracks");
            turnActions.put(manifest.get(0).id, Action.createCompoundBuild(manifest.get(0).id, barracksTemplate.getID(), 10, 10));
        }
        
        //build footman
        if(barracks.size() > 0 && currentGold >= 600) {
        	TemplateView footmanTemplate = newstate.getTemplate(this.player, "Footman");
            turnActions.put(barracks.get(0), Action.createCompoundProduction(barracks.get(0), footmanTemplate.getID()));
        }
        return turnActions;
	}
	
	public Map<Integer, Action> update(Map<Integer, Action> turnActions, StateView newstate){
        this.currentGold = newstate.getResourceAmount(this.player, ResourceType.GOLD);
        this.currentWood = newstate.getResourceAmount(this.player, ResourceType.WOOD);
		List<Integer> allUnitIds = newstate.getUnitIds(this.player);
		for(Integer unitID : allUnitIds){
			UnitView unit = newstate.getUnit(unitID);
			String unitTypeName = unit.getTemplateView().getName();
            if(unitTypeName.equals("TownHall") && !(townHall.contains(unitID)))
            	townHall.add(unitID);
            else if(unitTypeName.equals("Peasant") && !(manifest.contains(new Peasant(unitID, 0))))
            	manifest.add(new Peasant(unitID, 100));
            else if(unitTypeName.equals("Farm") && !(farm.contains(unitID)))
            	farm.add(unitID);
            else if(unitTypeName.equals("Barracks") && !(barracks.contains(unitID)))
            	barracks.add(unitID);
		}
		turnActions.putAll(itinerary(newstate));
		if(manifest.size() > 0){
			turnActions.putAll(assignWork(newstate, turnActions));
		}
		return turnActions;
	}
	
	
}
