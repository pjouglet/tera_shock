package tera.gameserver.model.listeners;

import tera.gameserver.model.AttackInfo;
import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.Skill;

/**
 * Интерфейс для реализации слушателя получаемого урона.
 * 
 * @author Ronn
 */
public interface DamageListener
{
	/**
	 * @param attacker атакующий.
	 * @param attacked атакуемый.
	 * @param info инфа об атаке.
	 * @param skill атакующий скил.
	 */
	public void onDamage(Character attacker, Character attacked, AttackInfo info, Skill skill);
}
