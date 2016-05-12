package tera.gameserver.model.skillengine.conditions;

import tera.gameserver.model.Character;
import tera.gameserver.model.equipment.Equipment;
import tera.gameserver.model.equipment.Slot;
import tera.gameserver.model.skillengine.Skill;

/**
 * Класс для проверки корректности пушки
 * 
 * @author Ronn
 */
public class ConditionUsingItem extends AbstractCondition
{
	/** необходимый тип */
	private Enum<?>[] types;
	
	/**
	 * @param npcType
	 */
	public ConditionUsingItem(Enum<?>[] types)
	{
		this.types = types;
	}

	@Override
	public boolean test(Character attacker, Character attacked, Skill skill, float val)
	{
		if(!attacker.isPlayer())
			return true;
		
		Equipment equipment = attacker.getEquipment();
		
		if(equipment == null)
			return false;

		Slot[] slots = equipment.getSlots();
		
		for(Enum<?> type : types)
		{
			for(Slot slot : slots)
			{
				if(slot.isEmpty())
					continue;
				
				if(type == slot.getItem().getType())
					return true;
			}
		}
		
		return false;
	}
}
