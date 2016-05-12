package tera.gameserver.model.ai.npc.thinkaction;

import org.w3c.dom.Node;

import tera.gameserver.model.Character;
import tera.gameserver.model.NpcAIState;
import tera.gameserver.model.ai.npc.ConfigAI;
import tera.gameserver.model.ai.npc.NpcAI;
import tera.gameserver.model.npc.Npc;
import tera.util.LocalObjects;

/**
 * Реализация генерации действий суммона в режиме возвращения домой.
 *
 * @author Ronn
 */
public class SummonReturnAction extends DefaultReturnAction
{
	public SummonReturnAction(Node node)
	{
		super(node);
	}
	
	@Override
	public <A extends Npc> void think(NpcAI<A> ai, A actor, LocalObjects local, ConfigAI config, long currentTime)
	{
		// если нпс мертв
		if(actor.isDead())
		{
			// очищаем задания
			ai.clearTaskList();
			// очищаем агр лист
			actor.clearAggroList();
			// переводим в режим ожидания
			ai.setNewState(NpcAIState.WAIT);
			// выходим
			return;
		}
		
		// если нпс щас что-то делает, выходим
		if(actor.isTurner() || actor.isCastingNow() || actor.isMoving() || actor.isStuned() || actor.isOwerturned())
			return;

		// очищаем агро лист
		actor.clearAggroList();
		// очищаем задания
		ai.clearTaskList();

		// получаем владельца суммона
		Character owner = actor.getOwner();

		// если НПС уже на точке респа
		if(owner == null || actor.isInRange(owner, getDistanceToSpawnLoc()))
		{
			// переключаемся в режим ожидания
			ai.setNewState(NpcAIState.WAIT);
			// выходим
			return;
		}
		
		// если нужно телепортнуть 
		if(owner.getContinentId() != actor.getContinentId() || !owner.isInRange(actor, getDistanceToTeleport()) || actor.getRunSpeed() < 10)
		{
			actor.teleToLocation(owner.getContinentId(), owner.getX(), owner.getY(), owner.getZ(), 0);
			return;
		}

		// есть ли на очереди задания
		if(ai.isWaitingTask())
		{
			// выполняем задание
			ai.doTask(actor, currentTime, local);
			// выходим
			return;
		}

		// добавляем новые задания
		ai.getCurrentFactory().addNewTask(ai, actor, local, config, currentTime);

		// если есть ожидающиеся задания
		if(ai.isWaitingTask())
			// выполняем
			ai.doTask(actor, currentTime, local);
	}
}
