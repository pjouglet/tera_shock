package tera.gameserver.model.npc.interaction.conditions;

import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.quests.Quest;

/**
 * Проверка на уровень стамины.
 * 
 * @author Ronn
 */
public class ConditionPlayerHeart extends AbstractCondition
{
	/** нужное кол-во стамины */
	private int heart;
	
	public ConditionPlayerHeart(Quest quest, int heart)
	{
		super(quest);
		
		this.heart = heart;
	}

	@Override
	public boolean test(Npc npc, Player player)
	{
		if(player == null)
			return false;
		
		return player.getStamina() >= heart;
	}

	@Override
	public String toString()
	{
		return "ConditionPlayerHeart heart = " + heart;
	}
}
