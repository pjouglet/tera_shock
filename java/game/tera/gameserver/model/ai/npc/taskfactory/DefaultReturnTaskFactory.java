package tera.gameserver.model.ai.npc.taskfactory;

import org.w3c.dom.Node;

import rlib.util.Strings;
import tera.gameserver.model.ai.npc.ConfigAI;
import tera.gameserver.model.ai.npc.NpcAI;
import tera.gameserver.model.npc.Npc;
import tera.util.LocalObjects;

/**
 * Дефолтная модель реализации фабрики заданий в режиме возвращения домой.
 *
 * @author Ronn
 */
public class DefaultReturnTaskFactory extends AbstractTaskFactory
{
	public DefaultReturnTaskFactory(Node node)
	{
		super(node);
	}

	@Override
	public <A extends Npc> void addNewTask(NpcAI<A> ai, A actor, LocalObjects local, ConfigAI config, long currentTime)
	{
		// если актор может нормально перемещаться
		if(actor.getRunSpeed() > 10)
			// добавляем задание бежать в указанную точку
			ai.addMoveTask(actor.getSpawnLoc(), true, Strings.EMPTY);
		else
			// иначе телепортируем его на месте
			actor.teleToLocation(actor.getSpawnLoc());
	}
}
