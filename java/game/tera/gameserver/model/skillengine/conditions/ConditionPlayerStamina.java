package tera.gameserver.model.skillengine.conditions;

import tera.gameserver.model.Character;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.skillengine.Skill;

/**
 * Кондишен для проверки ,есть ли столько стамины у игрока.
 * 
 * @author Ronn
 */
public final class ConditionPlayerStamina extends AbstractCondition
{
	private int stamina;
	
	public ConditionPlayerStamina(int stamina)
	{
		this.stamina = stamina;
	}

	@Override
	public boolean test(Character attacker, Character attacked, Skill skill, float val)
	{
		Player player = attacker.getPlayer();
		
		if(player == null)
			return false;
		
		return player.getStamina() > stamina;
	}
}
