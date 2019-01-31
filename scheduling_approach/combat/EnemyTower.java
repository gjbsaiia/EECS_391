
public class EnemyTower extends CombatUnit {

	public EnemyTower(int iD, int typeID, int tHealth, int range, int bAttack,
			int pAttack, int armor) {
		super(iD, typeID, tHealth, range, bAttack, pAttack, armor);
		// TODO Auto-generated constructor stub
	}
	
	public EnemyTower(int iD) {
		super(iD, 1, 150, 6, 15, 20, 20);
		// TODO Auto-generated constructor stub
	}
	
	public EnemyTower(int iD, int tHealth) {
		super(iD, 1, tHealth, 6, 15, 20, 20);
		// TODO Auto-generated constructor stub
	}

}
