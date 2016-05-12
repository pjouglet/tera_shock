package tera.gameserver.model.ai.npc.thinkaction;

import org.w3c.dom.Node;

import rlib.logging.Logger;
import rlib.logging.Loggers;
import tera.gameserver.model.ai.npc.ConfigAI;
import tera.gameserver.model.ai.npc.NpcAI;
import tera.gameserver.model.npc.Npc;
import tera.util.LocalObjects;

/**
 * Базовая модель генератора действий.
 *
 * @author Ronn
 */
public abstract class AbstractThinkAction implements ThinkAction
{
	protected static final Logger log = Loggers.getLogger(ThinkAction.class);

	public AbstractThinkAction(Node node){}

	@Override
	public <A extends Npc> void prepareState(NpcAI<A> ai, A actor, LocalObjects local, ConfigAI config, long currentTime){}

	@Override
	public <A extends Npc> void startAITask(NpcAI<A> ai, A actor, LocalObjects local, ConfigAI config, long currentTime){}
}
