package tera.gameserver.model.ai.npc.classes;

import tera.gameserver.model.ai.npc.ConfigAI;
import tera.gameserver.model.npc.Npc;

/**
 * Базовая модель НПС АИ.
 *
 * @author Ronn
 */
public class DefaultNpcAI extends AbstractNpcAI<Npc>
{
	public DefaultNpcAI(Npc actor, ConfigAI config)
	{
		super(actor, config);
	}
}
