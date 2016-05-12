package tera.gameserver.model.skillengine.funcs.chance;

import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.Skill;
import tera.gameserver.model.skillengine.funcs.Func;

/**
 * Интерфейс для реализации шансовых функций.
 * 
 * @author Ronn
 */
public interface ChanceFunc extends Func
{
	/**
	 * Приминение функции.
	 * 
	 * @param attacker атакующий.
	 * @param attacked атакуемый.
	 * @param skill используемый скил.
	 * @return применилась ли функция.
	 */
	public boolean apply(Character attacker, Character attacked, Skill skill);
	
	/**
	 * @return шанс срабатывания.
	 */
	public int getChance();
	
	/**
	 * @return шансовый скил.
	 */
	public Skill getSkill();
	
	/**
	 * @return срабатывает ли при атаке.
	 */
	public boolean isOnAttack();
	
	/**
	 * @return срабатывает ли под атакой.
	 */
	public boolean isOnAttacked();
	
	/**
	 * @return срабатывает при крит атаке.
	 */
	public boolean isOnCritAttack();
	
	/**
	 * @return срабатывает ли под крит атакой.
	 */
	public boolean isOnCritAttacked();
	
	/**
	 * @return срабатывает ли при опрокидывании.
	 */
	public boolean isOnOwerturn();
	
	/**
	 * @return срабатывает ли от опрокидывания.
	 */
	public boolean isOnOwerturned();
	
	/**
	 * @return срабатывает ли от блокировки.
	 */
	public boolean isOnShieldBlocked();
	
	/**
	 * Инициализация функции.
	 */
	public void prepare();
}
