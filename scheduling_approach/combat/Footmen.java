
public class Footmen extends CombatUnit {

	public Footmen(int iD, int type, int tHealth, int range, int bAttack,
			int pAttack, int armor) {
		super(iD, type, tHealth, range, bAttack, pAttack, armor);
		// TODO Auto-generated constructor stub
	}

	public Footmen(int iD) {
		super(iD, 58, 60, 1, 8, 3, 2);
	}
	
	public Footmen(int iD, int tHealth) {
		super(iD, 58, tHealth, 1, 8, 3, 2);
	}

}
