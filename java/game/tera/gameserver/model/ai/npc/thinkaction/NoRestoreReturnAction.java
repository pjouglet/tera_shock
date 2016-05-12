package tera.gameserver.model.ai.npc.thinkaction;

import org.w3c.dom.Node;

import rlib.util.VarTable;
import tera.gameserver.model.NpcAIState;
import tera.gameserver.model.ai.npc.ConfigAI;
import tera.gameserver.model.ai.npc.NpcAI;
import tera.gameserver.model.npc.Npc;
import tera.util.LocalObjects;

/**
 * Базовая реализация генерации действий в режиме возвращения домой.
 *
 * @author Ronn
 */
public class NoRestoreReturnAction extends AbstractThinkAction
{
	/** максимальная дистанция до спавн точки, после которой считается что НПс на месте */
	protected final int distanceToSpawnLoc;
	/** дистанция при которой производить телепорт */
	protected final int distanceToTeleport;

	public NoRestoreReturnAction(Node node)
	{
		super(node);

		VarTable vars = VarTable.newInstance(node);

		this.distanceToSpawnLoc = vars.getInteger("distanceToSpawnLoc", ConfigAI.DEFAULT_DISTANCE_TO_SPAWN_LOC);
		this.distanceToTeleport = vars.getInteger("distanceToTeleport", ConfigAI.DEFAULT_DISTANCE_TO_TELEPORT);
	}

	/**
	 * @return дистанция от точки спавна, на которой считается что прибыл на место.
	 */
	public int getDistanceToSpawnLoc()
	{
		return distanceToSpawnLoc;
	}

	/**
	 * @return дистанция при которой производить телепорт.
	 */
	public int getDistanceToTeleport()
	{
		return distanceToTeleport;
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

		// обнуляем дату очистки агр листа
		ai.setClearAggro(0);

		// если НПС уже на точке респа
		if(actor.isInRange(actor.getSpawnLoc(), getDistanceToSpawnLoc()))
		{
			// переключаемся в режим ожидания
			ai.setNewState(NpcAIState.WAIT);
			// выходим
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
