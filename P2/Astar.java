//package edu.cwru.sepia.agent;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.util.Direction;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

public class Astar extends Agent {



	class MapLocation
    {
        public int x, y;
        public float cost;
       // public MapLocation cameFrom;
        
        public MapLocation(int x, int y, MapLocation cameFrom, float cost)
        {
            this.x = x;
            this.y = y;
            this.cost=cost;
          //  this.cameFrom=cameFrom;
        }
    }

    Stack<MapLocation> path;
    int footmanID, townhallID, enemyFootmanID;
    MapLocation nextLoc;

    private long totalPlanTime = 0; // nsecs
    private long totalExecutionTime = 0; //nsecs

    public Astar(int playernum)
    {
        super(playernum);

        System.out.println("Constructed AstarAgent");
    }

    @Override
    public Map<Integer, Action> initialStep(State.StateView newstate, History.HistoryView statehistory) {
        // get the footman location
        List<Integer> unitIDs = newstate.getUnitIds(playernum);

        if(unitIDs.size() == 0)
        {
            System.err.println("No units found!");
            return null;
        }

        footmanID = unitIDs.get(0);

        // double check that this is a footman
        if(!newstate.getUnit(footmanID).getTemplateView().getName().equals("Footman"))
        {
            System.err.println("Footman unit not found");
            return null;
        }

        // find the enemy playernum
        Integer[] playerNums = newstate.getPlayerNumbers();
        int enemyPlayerNum = -1;
        for(Integer playerNum : playerNums)
        {
            if(playerNum != playernum) {
                enemyPlayerNum = playerNum;
                break;
            }
        }

        if(enemyPlayerNum == -1)
        {
            System.err.println("Failed to get enemy playernumber");
            return null;
        }

        // find the townhall ID
        List<Integer> enemyUnitIDs = newstate.getUnitIds(enemyPlayerNum);

        if(enemyUnitIDs.size() == 0)
        {
            System.err.println("Failed to find enemy units");
            return null;
        }

        townhallID = -1;
        enemyFootmanID = -1;
        for(Integer unitID : enemyUnitIDs)
        {
            Unit.UnitView tempUnit = newstate.getUnit(unitID);
            String unitType = tempUnit.getTemplateView().getName().toLowerCase();
            if(unitType.equals("townhall"))
            {
                townhallID = unitID;
            }
            else if(unitType.equals("footman"))
            {
                enemyFootmanID = unitID;
            }
            else
            {
                System.err.println("Unknown unit type");
            }
        }

        if(townhallID == -1) {
            System.err.println("Error: Couldn't find townhall");
            return null;
        }

        long startTime = System.nanoTime();
        path = findPath(newstate);
        totalPlanTime += System.nanoTime() - startTime;

        return middleStep(newstate, statehistory);
    }

    @Override
    public Map<Integer, Action> middleStep(State.StateView newstate, History.HistoryView statehistory) {
        long startTime = System.nanoTime();
        long planTime = 0;

        Map<Integer, Action> actions = new HashMap<Integer, Action>();

        if(shouldReplanPath(newstate, statehistory, path)) {
            long planStartTime = System.nanoTime();
            path = findPath(newstate);
            planTime = System.nanoTime() - planStartTime;
            totalPlanTime += planTime;
        }

        Unit.UnitView footmanUnit = newstate.getUnit(footmanID);

        int footmanX = footmanUnit.getXPosition();
        int footmanY = footmanUnit.getYPosition();

        if(!path.empty() && (nextLoc == null || (footmanX == nextLoc.x && footmanY == nextLoc.y))) {

            // stat moving to the next step in the path
            nextLoc = path.pop();

            System.out.println("Moving to (" + nextLoc.x + ", " + nextLoc.y + ")");
        }

        if(nextLoc != null && (footmanX != nextLoc.x || footmanY != nextLoc.y))
        {
            int xDiff = nextLoc.x - footmanX;
            int yDiff = nextLoc.y - footmanY;

            // figure out the direction the footman needs to move in
            Direction nextDirection = getNextDirection(xDiff, yDiff);

            actions.put(footmanID, Action.createPrimitiveMove(footmanID, nextDirection));
        } else {
            Unit.UnitView townhallUnit = newstate.getUnit(townhallID);

            // if townhall was destroyed on the last turn
            if(townhallUnit == null) {
                terminalStep(newstate, statehistory);
                return actions;
            }

            if(Math.abs(footmanX - townhallUnit.getXPosition()) > 1 ||
                    Math.abs(footmanY - townhallUnit.getYPosition()) > 1)
            {
                System.err.println("Invalid plan. Cannot attack townhall");
                System.out.println("nextLoc "+ nextLoc.x + " "+ nextLoc.y);
                totalExecutionTime += System.nanoTime() - startTime - planTime;
                return actions;
            }
            else {
                System.out.println("Attacking TownHall");
                // if no more movements in the planned path then attack
                actions.put(footmanID, Action.createPrimitiveAttack(footmanID, townhallID));
            }
        }

        totalExecutionTime += System.nanoTime() - startTime - planTime;
        return actions;
    }

    @Override
    public void terminalStep(State.StateView newstate, History.HistoryView statehistory) {
        System.out.println("Total turns: " + newstate.getTurnNumber());
        System.out.println("Total planning time: " + totalPlanTime/1e9);
        System.out.println("Total execution time: " + totalExecutionTime/1e9);
        System.out.println("Total time: " + (totalExecutionTime + totalPlanTime)/1e9);
    }

    @Override
    public void savePlayerData(OutputStream os) {

    }

    @Override
    public void loadPlayerData(InputStream is) {

    }

    /**
     * You will implement this method.
     *
     * This method should return true when the path needs to be replanned
     * and false otherwise. This will be necessary on the dynamic map where the
     * footman will move to block your unit.
     * 
     * You can check the position of the enemy footman with the following code:
     * state.getUnit(enemyFootmanID).getXPosition() or .getYPosition().
     * 
     * There are more examples of getting the positions of objects in SEPIA in the findPath method.
     *
     * @param state
     * @param history
     * @param currentPath
     * @return
     */
    private boolean shouldReplanPath(State.StateView state, History.HistoryView history, Stack<MapLocation> currentPath)
    {
        return false;
    }

    /**
     * This method is implemented for you. You should look at it to see examples of
     * how to find units and resources in Sepia.
     *
     * @param state
     * @return
     */
    private Stack<MapLocation> findPath(State.StateView state)
    {
        Unit.UnitView townhallUnit = state.getUnit(townhallID);
        Unit.UnitView footmanUnit = state.getUnit(footmanID);

        MapLocation startLoc = new MapLocation(footmanUnit.getXPosition(), footmanUnit.getYPosition(), null, 0);

        MapLocation goalLoc = new MapLocation(townhallUnit.getXPosition(), townhallUnit.getYPosition(), null, 0);

        MapLocation footmanLoc = null;
        if(enemyFootmanID != -1) {
            Unit.UnitView enemyFootmanUnit = state.getUnit(enemyFootmanID);
            footmanLoc = new MapLocation(enemyFootmanUnit.getXPosition(), enemyFootmanUnit.getYPosition(), null, 0);
        }

        // get resource locations
        List<Integer> resourceIDs = state.getAllResourceIds();
        Set<MapLocation> resourceLocations = new HashSet<MapLocation>();
        for(Integer resourceID : resourceIDs)
        {
            ResourceNode.ResourceView resource = state.getResourceNode(resourceID);

            resourceLocations.add(new MapLocation(resource.getXPosition(), resource.getYPosition(), null, 0));
            //MapLocation nextMove= new MapLocation(2,0 ,null, 0);
            // System.out.println("X: "+ resource.getXPosition() + " Y "+ resource.getYPosition());
           // System.out.println(resourceLocations.contains(nextMove)
        }
        return AstarSearch(startLoc, goalLoc, state.getXExtent(), state.getYExtent(), footmanLoc, resourceLocations);
    }
    /**
     * This is the method you will implement for the assignment. Your implementation
     * will use the A* algorithm to compute the optimum path from the start position to
     * a position adjacent to the goal position.
     *
     * Therefore your you need to find some possible adjacent steps which are in range 
     * and are not trees or the enemy footman.
     * Hint: Set<MapLocation> resourceLocations contains the locations of trees
     *
     * You will return a Stack of positions with the top of the stack being the first space to move to
     * and the bottom of the stack being the last space to move to. If there is no path to the townhall
     * then return null from the method and the agent will print a message and do nothing.
     * The code to execute the plan is provided for you in the middleStep method.
     *
     * As an example consider the following simple map
     *
     * F - - - -
     * x x x - x
     * H - - - -
     *
     * F is the footman
     * H is the townhall
     * x's are occupied spaces
     *
     * xExtent would be 5 for this map with valid X coordinates in the range of [0, 4]
     * x=0 is the left most column and x=4 is the right most column
     *
     * yExtent would be 3 for this map with valid Y coordinates in the range of [0, 2]
     * y=0 is the top most row and y=2 is the bottom most row
     *
     * resourceLocations would be {(0,1), (1,1), (2,1), (4,1)}
     *
     * The path would be
     *
     * (1,0)
     * (2,0)
     * (3,1)
     * (2,2)
     * (1,2)
     *
     * Notice how the initial footman position and the townhall position are not included in the path stack
     *
     * @param start Starting position of the footman
     * @param goal MapLocation of the townhall
     * @param xExtent Width of the map
     * @param yExtent Height of the map
     * @param resourceLocations Set of positions occupied by resources
     * @return Stack of positions with top of stack being first move in plan
     */
    private Stack<MapLocation> AstarSearch(MapLocation start, MapLocation goal, int xExtent, int yExtent, MapLocation enemyFootmanLoc, Set<MapLocation> resourceLocations)
    {
       	 ArrayList<MapLocation> openSet = new ArrayList<MapLocation>();
    	   ArrayList<MapLocation> closedSet = new ArrayList<MapLocation>();
    	// Set<MapLocation> openSet = new HashSet<MapLocation>();
    	// Set<MapLocation> closedSet = new HashSet<MapLocation>();
    	 Stack <MapLocation> path=new Stack<MapLocation>();
    	 int h;
     	MapLocation parent= new MapLocation(start.x,start.y ,null, 0);
    	openSet.add(parent);
    	int cost=0;
      	while(!openSet.isEmpty())
    	{
    		parent=findMove(openSet);    		
    		//This loop is designed to find all spots adjacent to the current location
    		for(int x=-1;x<2;x++)
    		{
    			for(int y=-1;y<2;y++)
    			{
    				MapLocation sucessor= new MapLocation(parent.x+x,parent.y+y, parent, parent.cost+1);
    				//we return the path if we are at the goal.
    				if(parent.x!=goal.x||parent.y!=goal.y)
    				{
    				
    					//checks to see if there is a resource, or if the move is off the map
    					//the contains method returns false always, even if resource there
    					if(checkValidMove(parent.x+x,parent.y+y,xExtent,yExtent,resourceLocations)&&!(x==0&&y==0))
    					{
    						h=heuristic(parent.x+x,parent.y+y,goal);
    						//if spot is not visited with lower cost, add to open set
    						if(!setContains(sucessor,openSet)&&!setContains(sucessor,closedSet))
    							openSet.add(new MapLocation(parent.x+x,parent.y+y,parent, cost+h));
    					}      			
    				}
    				else
    				{
       					path.push(sucessor);
       			      	System.out.println("Done");
       					return path;
    				}
    			}	
    		}
    		cost++;
    		openSet.remove(parent);
    		parent.cost=cost;
    		closedSet.add(parent);
    		path.push(parent);     
    	}    	
      	System.out.println("Done");
    	return path;        		
    }
    
    //checks to see if a set contains a mapLocation
    //Used to see if a spot is already visited at a lower cost
    private boolean setContains(MapLocation sucessor,ArrayList<MapLocation> set) {
	
    	int x=sucessor.x;
    	int y=sucessor.y;
     	for(int i=0; i<set.size();i++)
    	{
    		if(set.get(i).x==x&&set.get(i).y==y)
    		{
    			if(set.get(i).cost<sucessor.cost)
    			return true;
    		}
      	}
		return false;
	}

    //finds lowest cost move in openSet
	public MapLocation findMove(ArrayList<MapLocation> openSet)
    {
    	float minCost=(float) Integer.MAX_VALUE;
    	int minIndex=0;
    	for(int i=0; i<openSet.size();i++)
    	{
    		if(openSet.get(i).cost<minCost)
    		{
    			minIndex=i;
    			
    			minCost=openSet.get(i).cost;
    		}
      	}
    	  MapLocation parent=openSet.get(minIndex);
    	  openSet.remove(minIndex);
    	  return parent;
    	
    }
	//This h functions uses the recommended heuristic, 
	   public int heuristic(int x,int y, MapLocation goal)
	    {
	    	int distance=Math.max((Math.abs(x-goal.x)), (Math.abs(y-goal.y)));
	    	return distance;	
	    }
	/*
    //Used for error checking to display the costs and h value of current expanded state
    public void dispPositions(int[][] positions)
    {
    	
    	for(int x=0;x<3;x++)
 	   {
 		   for(int y=0;y<3; y++)
 		   {
 			  System.out.print(positions[y][x]+ "     ");
 		   }
 		   System.out.println();
 	   }    	
    }
    //not used, was used for the recursive version
    private Stack<MapLocation> merge (Stack<MapLocation> stack, Stack<MapLocation> output)
    {
    	   System.out.println("PUSHED");

    	//while(!output.empty())
    	//{
    		stack.push(output.pop());
    		
       	//}
    	return stack;
    }
    */
	
    //finds best move based on the h values
    //should be modified to reflect the cost+ h values 
	
  
	
   
   
   public boolean checkValidMove(int startX,int startY,int xExtent, int yExtent, Set<MapLocation> resourceLocations)
   {	  
	   MapLocation move = new MapLocation(startX, startY, null, 0);	
	   boolean valid=true;
	   if(startX==xExtent||startX<0)
		   {
		   valid=false;		   
		   }
	   else if(startY==yExtent||startY<0)
	   	{
		   valid=false;
	   	}
	   //this isn't working right
	  else if (resourceLocations.contains(move)) 
		  valid=false;	 
	   return valid;		   
   }
     
    /**
     * Primitive actions take a direction (e.g. Direction.NORTH, Direction.NORTHEAST, etc)
     * This converts the difference between the current position and the
     * desired position to a direction.
     *
     * @param xDiff Integer equal to 1, 0 or -1
     * @param yDiff Integer equal to 1, 0 or -1
     * @return A Direction instance (e.g. SOUTHWEST) or null in the case of error
     */
    private Direction getNextDirection(int xDiff, int yDiff) {

        // figure out the direction the footman needs to move in
        if(xDiff == 1 && yDiff == 1)
        {
            return Direction.SOUTHEAST;
        }
        else if(xDiff == 1 && yDiff == 0)
        {
            return Direction.EAST;
        }
        else if(xDiff == 1 && yDiff == -1)
        {
            return Direction.NORTHEAST;
        }
        else if(xDiff == 0 && yDiff == 1)
        {
            return Direction.SOUTH;
        }
        else if(xDiff == 0 && yDiff == -1)
        {
            return Direction.NORTH;
        }
        else if(xDiff == -1 && yDiff == 1)
        {
            return Direction.SOUTHWEST;
        }
        else if(xDiff == -1 && yDiff == 0)
        {
            return Direction.WEST;
        }
        else if(xDiff == -1 && yDiff == -1)
        {
            return Direction.NORTHWEST;
        }

        System.err.println("Invalid path. Could not determine direction");
        return null;
    }
}
