	public class Peasant{
		// unitID for this peasant
		public int id;
		
		// resource id : 0=idle, 1=gold, 2=wood, 3=task
		private int status;
		
		private int resourceID;
		
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
		
		public Peasant(int unitID, int capacity){
			this.id = unitID;
			this.status = 0; //peasant starts idle
			this.lifespan = 1;
			this.totalContribution = 0;
			this.resourceID = -1;
		}
		
		public void setStatus(int id){
			this.status = id;
		}
		
		public int getStatus(){
			return this.status;
		}
		
		public void setResourceID(int id){
			this.resourceID = id;
		}
		
		public int getResourceID(){
			return this.resourceID;
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