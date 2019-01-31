
import java.io.InputStream;
import java.io.OutputStream;
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
 *  Combat organizer 
 *  	- Methods to efficiently distribute and coordinate attack
 */
public class CombatOrg {
	
	private final int player;
	private final int enemy;
	
	private List<List<CombatUnit>> allUnits;
	
	private List<CombatUnit> manifest; // allUnits.get(0)	
	private List<CombatUnit> enemyManifest; // allUnits.get(1)
	
	// x,y location of unit closest to enemy
	private int x;
	private int y;
	
	// x,y location of enemy unit closest
	private int ex;
	private int ey;
	
	// formations units can take --> impacts attack strategy
	private boolean turtle;
	private boolean spear;
	
	private Map<String, Integer> dictionary;
	private Map<Integer, Integer> unitCount;
	private Map<Integer, Integer> enemyCount;
	
	public CombatOrg(int playernum, int enemynum, StateView initialState){
		this.player = playernum;
		this.enemy = enemynum;
		buildArch(initialState);
	}
	
	private void buildArch(StateView initialState){
		
		manifest = new ArrayList<CombatUnit>();
		enemyManifest = new ArrayList<CombatUnit>();
		
		allUnits = new ArrayList<List<CombatUnit>>();
		allUnits.add(manifest);
		allUnits.add(enemyManifest);
		
		dictionary = new HashMap<String, Integer>();
		dictionary.put("EnemyFootman", 25);
		dictionary.put("Footman", 58);
		dictionary.put("Archer", 55);
		dictionary.put("Ballista", 35);
		dictionary.put("EnemyTower", 1);
		/*
		 * Build out when necessary
		 */
		
		unitCount = new HashMap<Integer, Integer>();
		enemyCount = new HashMap<Integer, Integer>();


        accountForUnits(initialState);
		
		rangeSort(unitCount.size(), 0); //sort my units
		rangeSort(enemyCount.size(), 1); //sort enemy units
		
		if(manifest.get(0).getRange() > enemyManifest.get(0).getRange()) turtleFormation();
		else spearFormation();
		
		// gets the position of my shortest range unit (should be closest to enemy)
		UnitView unit = initialState.getUnit(manifest.get(manifest.size()-1).id);
		this.x = unit.getXPosition();
		this.y = unit.getYPosition();
		
		// gets the position of enemy's shortest range unit (should be closest to me)
		unit = initialState.getUnit(enemyManifest.get(enemyManifest.size()-1).id);
		this.ex = unit.getXPosition();
		this.ey = unit.getYPosition();
		
	}
	
	/*
	 * Updates unit manifests/unit counts
	 */
	private void accountForUnits(StateView newstate){
        List<Integer> enemyUnitIDs = newstate.getUnitIds(this.enemy);
		List<Integer> allUnitIds = newstate.getUnitIds(this.player);
		
		for(Integer unitID : allUnitIds){
			int key;
			UnitView unit = newstate.getUnit(unitID);
			String unitTypeName = unit.getTemplateView().getName();
			if(dictionary.containsKey(unitTypeName)){
	            if(unitTypeName.equals("Footman") && !(manifest.contains(new Footmen(unitID)))){
	            	manifest.add(new Footmen(unitID,unit.getHP()));
	            }
	            else if(unitTypeName.equals("Archer") && !(manifest.contains(new Footmen(unitID)))){
	            	manifest.add(new Archer(unitID,unit.getHP()));
	            }
	            else if(unitTypeName.equals("Ballista") && !(manifest.contains(new Footmen(unitID)))){
	            	manifest.add(new Ballista(unitID,unit.getHP()));
	            }
				key = dictionary.get(unitTypeName);
            	if(!unitCount.containsKey(key)) unitCount.put(key, 1);
            	else unitCount.replace(key, unitCount.get(key)+1);
			}
		}
		
        for(Integer unitID : enemyUnitIDs)
        {
        	int key;
			UnitView unit = newstate.getUnit(unitID);
			String unitTypeName = unit.getTemplateView().getName();
			if(dictionary.containsKey("Enemy"+unitTypeName)){
	            if(unitTypeName.equals("Footman") && !(enemyManifest.contains(new Footmen(unitID)))){
	            	enemyManifest.add(new EnemyFootmen(unitID,unit.getHP()));
	            	if(!unitCount.containsKey(25)) unitCount.put(25, 1);
	            	else unitCount.replace(25, unitCount.get(25)+1);
	            }
	            else if(unitTypeName.equals("Tower") && !(enemyManifest.contains(new Footmen(unitID)))){
	            	enemyManifest.add(new EnemyTower(unitID,unit.getHP()));
	            	if(!unitCount.containsKey(1)) unitCount.put(1, 1);
	            	else unitCount.replace(1, unitCount.get(1)+1);
	            }
				key = dictionary.get("Enemy"+unitTypeName);
            	if(!enemyCount.containsKey(key)) enemyCount.put(key, 1);
            	else enemyCount.replace(key, enemyCount.get(key)+1);
			}
        }
	}
	
	/*
	 * Bucket sorts units from most range to least
	 * 		-> bucket for each unit type
	 * 
	 */
	private boolean rangeSort(int bucketNum, int r){
		int len = allUnits.get(r).size();
		
        if (len <= 1) return true;
        int max = (int)allUnits.get(r).get(0).getRange();
        int min = (int)allUnits.get(r).get(0).getRange();
        
        int ptr;
        for (int i = 1; i < len; i++) {
        	ptr = (int)allUnits.get(r).get(i).getRange();
            if (ptr > max) max = ptr;
            if (ptr < min) min = ptr;
        }
        double range = ((double)(max - min + 1))/bucketNum;

        ArrayList<ArrayList<CombatUnit>> buckets = new ArrayList<ArrayList<CombatUnit>>();
        int ii = 0;
        while(ii<bucketNum) { //initialize buckets
            buckets.add(new ArrayList<CombatUnit>());
            ii++;
        }

        for (int i = 0; i < len; i++) { //partition the input array
            buckets.get((int)((allUnits.get(r).get(i).getRange() - min)/range)).add((allUnits.get(r).get(i)));
        }

        ptr = 0;
        for (int i = 0; i < buckets.size(); i++) {
            Collections.sort(buckets.get(i)); //mergeSort
            for (int j = 0; j < buckets.get(i).size(); j++) { //merge the buckets
                allUnits.get(r).set(ptr, buckets.get(i).get(j));
                ptr++;
            }
        }
        return true;
	}
	
	private void turtleFormation(){
		
	}
	
	private void spearFormation(){
		
	}

}
