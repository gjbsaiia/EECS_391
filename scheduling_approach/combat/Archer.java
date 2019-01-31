
public class Archer extends CombatUnit {
	
	public Archer(int iD, int type, int tHealth, int range, int bAttack,
			int pAttack, int armor) {
		super(iD, type, tHealth, range, bAttack, pAttack, armor);
		// TODO Auto-generated constructor stub
	}

	public Archer(int iD) {
		super(iD, 55, 40, 3, 10, 6, 0);
		// TODO Auto-generated constructor stub
	}
	
	public Archer(int iD, int tHealth) {
		super(iD, 55, tHealth, 3, 10, 6, 0);
		// TODO Auto-generated constructor stub
	}

}
