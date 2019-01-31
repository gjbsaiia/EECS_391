
public class Ballista extends CombatUnit {

	public Ballista(int iD, int type, int tHealth, int range, int bAttack,
			int pAttack, int armor) {
		super(iD, type, tHealth, range, bAttack, pAttack, armor);
		// TODO Auto-generated constructor stub
	}
	
	public Ballista(int iD) {
		super(iD, 35, 10, 8, 12, 0, 0);
		// TODO Auto-generated constructor stub
	}
	
	public Ballista(int iD, int tHealth) {
		super(iD, 35, tHealth, 8, 12, 0, 0);
		// TODO Auto-generated constructor stub
	}

}
