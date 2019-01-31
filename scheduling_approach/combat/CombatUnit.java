
public class CombatUnit implements Comparable<CombatUnit>{
	
	public final int id; // unitID
	
	private final int typeID; // 25=EnemyFootmen, 58=Footmen, 55=Archer, 35=Ballista, 1=EnemyTower
	
	private int tHealth; // total health
	private int cHealth; // current health
	
	private int range;
	private int bAttack; // base attack
	private int pAttack; // piercing attack
	private int armor;
	
	public int getcHealth() {
		return cHealth;
	}

	public void updateHealth(int ch) {
		this.cHealth += ch;
	}

	public int getRange() {
		return range;
	}

	public void setRange(int range) {
		this.range = range;
	}

	public int getbAttack() {
		return bAttack;
	}

	public void setbAttack(int bAttack) {
		this.bAttack = bAttack;
	}

	public int getpAttack() {
		return pAttack;
	}

	public void setpAttack(int pAttack) {
		this.pAttack = pAttack;
	}

	public int getArmor() {
		return armor;
	}

	public void setArmor(int armor) {
		this.armor = armor;
	}

	public int gettypeID() {
		return typeID;
	}
	
	public void setttHealth(int tHealth){
		this.tHealth = tHealth;
	}

	public int gettHealth() {
		return tHealth;
	}
	
	public CombatUnit(int iD, int typeID, int tHealth, int range, int bAttack, int pAttack, int armor) {
		this.id = iD;
		this.typeID = typeID;
		this.tHealth = tHealth;
		this.cHealth = tHealth;
		this.range = range;
		this.bAttack = bAttack;
		this.pAttack = pAttack;
		this.armor = armor;
	}
	
	/*
	 * method used for upgrades --> means that whenever there's an upgrade, this method can be called on all units
	 */
	public void upgrade(int effectsID, int chHealth, int chRange, int chbAttack, int chpAttack, int chArmor){
		if(effectsID == this.typeID){
			this.tHealth += chHealth;
			this.range += chRange;
			this.bAttack += chbAttack;
			this.pAttack += chpAttack;
			this.armor += chArmor;
		}
	}
	
	// equals compares unit type
	public boolean equals(Object o){
		if(!(o instanceof CombatUnit)) return false;
		CombatUnit toCombat = (CombatUnit) o;
		return (this.id == toCombat.id);
	}
	
	/*
	 *  Used to sort for formations:
	 *  	Units with the furthest range are placed farthest back
	 *  		Units with less armor are placed further back
	 *  			Units with less health are placed further back
	 * 	In this order.
	 * 
	 */
	public int compareTo(CombatUnit o){
		if(this.getRange() > o.getRange()){
			return -1;
		}
		else if(this.getRange() < o.getRange()){
			return 1;
		}
		else{
			if(this.getArmor() > o.getArmor()){
				return 1;
			}
			else if(this.getArmor() < o.getArmor()){
				return -1;
			}
			else{
				if(this.getcHealth() > o.getcHealth()){
					return 1;
				}
				else if(this.getcHealth() < o.getcHealth()){
					return -1;
				}
				else{
					return 0;
				}
			}
		}
	}
	
}
