package tera.gameserver.model.regenerations;

import tera.gameserver.model.Character;

/**
 * Модель регенерации мп у НПС.
 *
 * @author Ronn
 * @created 11.04.2012
 */
public class NpcRegenMp extends AbstractRegen<Character>
{
	public NpcRegenMp(Character actor)
	{
		super(actor);
	}

	@Override
	public boolean checkCondition()
	{
		Character actor = getActor();
		
		return actor.getCurrentMp() < actor.getMaxMp();
	}

	@Override
	public void doRegen()
	{
		Character actor = getActor();
		
		actor.setCurrentMp(actor.getCurrentMp() + actor.getRegenMp());
		actor.updateMp();
	}
}
