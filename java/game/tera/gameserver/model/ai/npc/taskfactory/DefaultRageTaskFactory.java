package tera.gameserver.model.ai.npc.taskfactory;

import org.w3c.dom.Node;

import tera.gameserver.model.ai.npc.ConfigAI;
import tera.gameserver.model.ai.npc.NpcAI;
import tera.gameserver.model.npc.Npc;
import tera.util.LocalObjects;

public class DefaultRageTaskFactory extends AbstractTaskFactory
{

	public DefaultRageTaskFactory(Node node)
	{
		super(node);
		// TODO Автоматически созданная заглушка конструктора
	}

	@Override
	public <A extends Npc> void addNewTask(NpcAI<A> ai, A actor, LocalObjects local, ConfigAI config, long currentTime)
	{
		// TODO Автоматически созданная заглушка метода

	}
}
