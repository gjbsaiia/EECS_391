
public class EnemyFootmen extends CombatUnit {
	
	public EnemyFootmen(int iD, int type, int tHealth, int range, int bAttack,
			int pAttack, int armor) {
		super(iD, type, tHealth, range, bAttack, pAttack, armor);
		// TODO Auto-generated constructor stub
	}

	public EnemyFootmen(int iD) {
		super(iD, 25, 120, 1, 8, 3, 2);
	}
	
	public EnemyFootmen(int iD, int tHealth) {
		super(iD, 25, tHealth, 1, 8, 3, 2);
	}
}
