package tera.gameserver.model.ai.npc.thinkaction;

import org.w3c.dom.Node;

import rlib.util.VarTable;
import tera.gameserver.model.Character;
import tera.gameserver.model.NpcAIState;
import tera.gameserver.model.ai.npc.ConfigAI;
import tera.gameserver.model.ai.npc.NpcAI;
import tera.gameserver.model.npc.Npc;
import tera.util.LocalObjects;

/**
 * Базовая реализация генерации действий в режиме убегания.
 *
 * @author Ronn
 */
public class DefaultRunAwayAction extends AbstractThinkAction
{
	/** время от последней атаки, сколько убегать */
	protected final int lastAttackedTime;
	/** максимальный радиус боевых действий */
	protected final int battleMaxRange;

	public DefaultRunAwayAction(Node node)
	{
		super(node);

		try
		{
			// парсим атрибуты
			VarTable vars = VarTable.newInstance(node, "set", "name", "val");

			this.lastAttackedTime = vars.getInteger("lastAttackedTime", ConfigAI.DEFAULT_LAST_ATTACKED_TIME);
			this.battleMaxRange = vars.getInteger("battleMaxRange", ConfigAI.DEFAULT_BATTLE_MAX_RANGE);
		}
		catch(Exception e)
		{
			log.warning(this, e);
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * @return максимальный радиус ведения боя.
	 */
	protected final int getBattleMaxRange()
	{
		return battleMaxRange;
	}

	/**
	 * @return время последней атаки.
	 */
	protected final int getLastAttackedTime()
	{
		return lastAttackedTime;
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

		// если прошло много времени с момента последнего удара
		if(currentTime - ai.getLastAttacked() > getLastAttackedTime())
		{
			// очищаем задания
			ai.clearTaskList();
			// очистка агр листа
			actor.clearAggroList();
			// переводим в режим ожидания
			ai.setNewState(NpcAIState.WAIT);
			// выходим
			return;
		}

		// получаем топовый агр
		Character mostHated = actor.getMostHated();

		// если его нет либо
		if(mostHated == null)
		{
			// очищаем задания
			ai.clearTaskList();
			// переводим в режим ожидания
			ai.setNewState(NpcAIState.WAIT);
			// выходим
			return;
		}

		// если агрессор мертв или слишком далеко
		if(mostHated.isDead() || !actor.isInRange(mostHated, getBattleMaxRange()))
		{
			// удаляем его из агр листа
			actor.removeAggro(mostHated);
			// выходим
			return;
		}

		// запоминаем новую цель
		ai.setTarget(mostHated);

		// создаем новое задание
		ai.getCurrentFactory().addNewTask(ai, actor, local, config, currentTime);

		// есть ли на очереди задания
		if(ai.isWaitingTask())
			// выполняем задание
			ai.doTask(actor, currentTime, local);
	}
}
