package tera.gameserver.model.regenerations;

import tera.gameserver.model.Character;

/**
 * Модель регенерации хп у НПС.
 *
 * @author Ronn
 * @created 11.04.2012
 */
public class NpcRegenHp extends AbstractRegen<Character>
{
	public NpcRegenHp(Character actor)
	{
		super(actor);
	}

	@Override
	public boolean checkCondition()
	{
		Character actor = getActor();
		
		if(actor.isBattleStanced())
			return false;
		
		return actor.getCurrentHp() < actor.getMaxHp();
	}

	@Override
	public void doRegen()
	{
		Character actor = getActor();
		
		actor.setCurrentHp(actor.getCurrentHp() + actor.getRegenHp());
		actor.updateHp();
	}
}
