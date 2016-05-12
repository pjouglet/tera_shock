package tera.gameserver.model.npc.interaction.conditions;

import tera.gameserver.model.inventory.Inventory;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.quests.Quest;

/**
 * @author Ronn
 */
public class ConditionPlayerHasItem extends AbstractCondition
{
	/** ид итема */
	private int id;
	/** нужное кол-во итемов */
	private int count;
	
	public ConditionPlayerHasItem(Quest quest, int id, int count)
	{
		super(quest);
		
		this.id = id;
		this.count = count;
	}

	@Override
	public boolean test(Npc npc, Player player)
	{
		if(player == null)
			return false;
		
		// получаем игрока
		Inventory inventory = player.getInventory();
		
		// если инвенторя нет, возвращаем плохо
		if(inventory == null)
		{
			log.warning(this, "not found inventory");
			return false;
		}
	
		// проверяем, есть ли такой итем в инвенторе
		return inventory.containsItems(id, count);
	}

	@Override
	public String toString()
	{
		return "ConditionPlayerHasItem id = " + id + ", count = " + count;
	}
}
