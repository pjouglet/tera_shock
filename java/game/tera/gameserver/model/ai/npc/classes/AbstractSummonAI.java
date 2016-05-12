package tera.gameserver.model.ai.npc.classes;

import tera.gameserver.model.Character;
import tera.gameserver.model.ai.npc.ConfigAI;
import tera.gameserver.model.npc.summons.Summon;
import tera.gameserver.model.skillengine.Skill;

/**
 * Базовая модель АИ суммона.
 *
 * @author Ronn
 */
public abstract class AbstractSummonAI extends AbstractNpcAI<Summon>
{
	public AbstractSummonAI(Summon actor, ConfigAI config)
	{
		super(actor, config);
	}

	@Override
	public void startAttack(Character target)
	{
		setTarget(target);

		Summon actor = getActor();

		if(actor == null || actor.isDead())
			return;

		actor.abortCast(true);
		actor.stopMove();
	}

	@Override
	public void abortAttack()
	{
		setTarget(null);

		Summon actor = getActor();

		if(actor == null || actor.isDead())
			return;

		actor.abortCast(true);
		actor.stopMove();
	}

	@Override
	public void notifyAttacked(Character attacker, Skill skill, int damage)
	{
		Character target = getTarget();

		if(target == null)
			setTarget(attacker);
	}

	@Override
	public void notifyDead(Character killer)
	{
		setTarget(null);
	}
}
