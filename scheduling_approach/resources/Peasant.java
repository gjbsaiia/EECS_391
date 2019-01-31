

	public class Peasant{
		// unitID for this peasant
		public int id;
		
		// status: 0=idle, 1=gold, 2=wood, 3=depositing
		private int status;
		
		// id of resource peasant is farming
		private Resource res;
		
		// id of resource type: 1=gold, 2=wood
		private int type;
		
		// decides how much resource should be collected before peasant deposits
		private int capacity;
		
		/*
		 * used to generate metric that tracks how efficient this peasant is
		 * --> just (double)totalContribution/(double)lifespan
		 * 
		 */
		
		// how long this peasant has been in play (in rounds)
		private int lifespan;
		
		// how much this peasant has gathered in total
		private int totalContribution;
		
		public Peasant(int unitID, int cap){
			this.id = unitID;
			this.status = 0; //peasant starts idle
			this.lifespan = 1;
			this.totalContribution = 0;
			this.res = null;
			this.capacity = cap;
			this.type = 0;
		}
		
		public void setType(int t){
			this.type = t;
		}
		
		public int getType(){
			return this.type;
		}
		
		public void setStatus(int id){
			this.status = id;
		}
		
		public int getStatus(){
			return this.status;
		}
		
		public void setResource(Resource r){
			this.res = r;
		}
		
		public Resource getResource(){
			return this.res;
		}
		
		public void setCapacity(int cap){
			this.capacity = cap;
		}
		
		public int getCapacity(){
			return this.capacity;
		}
		
		public void incLifespan(){
			this.lifespan++;
		}
		
		public int getLifespan(){
			return this.lifespan;
		}
		
		public void updateContrib(int newDeposit){
			this.totalContribution += newDeposit;
		}
		
		public int getContrib(){
			return this.totalContribution;
		}
		
		public double getGatherRatio(){
			return ((double)this.totalContribution/(double)this.lifespan);
		}
		
		public boolean equals(Object o){
			if(!(o instanceof Peasant)) return false;
			Peasant toPeasant = (Peasant) o;
			return (this.id == toPeasant.id);
		}
	}