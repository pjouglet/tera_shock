package tera.gameserver.model.ai.npc.classes;

import tera.gameserver.model.ai.npc.ConfigAI;
import tera.gameserver.model.npc.summons.Summon;

/**
 * Дефолтная модель АИ суммона.
 * 
 * @author Ronn
 */
public class DefaultSummonAI extends AbstractSummonAI
{
	public DefaultSummonAI(Summon actor, ConfigAI config)
	{
		super(actor, config);
	}
}
