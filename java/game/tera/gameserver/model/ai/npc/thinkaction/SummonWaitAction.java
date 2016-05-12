package tera.gameserver.model.ai.npc.thinkaction;

import org.w3c.dom.Node;

import rlib.util.Rnd;
import rlib.util.VarTable;

import tera.gameserver.manager.PacketManager;
import tera.gameserver.model.Character;
import tera.gameserver.model.NpcAIState;
import tera.gameserver.model.ai.npc.ConfigAI;
import tera.gameserver.model.ai.npc.NpcAI;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.network.serverpackets.NotifyCharacter.NotifyType;
import tera.util.LocalObjects;

/**
 * Реализация генерации действий самона в режиме ожидания.
 *
 * @author Ronn
 */
public class SummonWaitAction extends AbstractThinkAction
{
	/** максимальный радиус от владельца */
	protected final int maxDistance;

	/** минимальный интервал случайного движения */
	protected final int randomWalkMinDelay;
	/** максимальный интервал случайного движения */
	protected final int randomWalkMaxDelay;

	public SummonWaitAction(Node node)
	{
		super(node);

		try
		{
			// парсим параметры
			VarTable vars = VarTable.newInstance(node, "set", "name", "val");

			this.maxDistance = vars.getInteger("maxDistance", ConfigAI.DEFAULT_RANDOM_MAX_WALK_RANGE);
			this.randomWalkMinDelay = vars.getInteger("randomWalkMinDelay", ConfigAI.DEFAULT_RANDOM_MIN_WALK_DELAY);
			this.randomWalkMaxDelay = vars.getInteger("randomWalkMaxDelay", ConfigAI.DEFAULT_RANDOM_MAX_WALK_DELAY);
		}
		catch(Exception e)
		{
			log.warning(this, e);
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * @return максимальный период брождения.
	 */
	protected final int getRandomWalkMaxDelay()
	{
		return randomWalkMaxDelay;
	}

	/**
	 * @return максимальный радиус от владельца.
	 */
	public int getMaxDistance()
	{
		return maxDistance;
	}

	/**
	 * @return минимальный период брождения.
	 */
	protected final int getRandomWalkMinDelay()
	{
		return randomWalkMinDelay;
	}

	@Override
	public <A extends Npc> void startAITask(NpcAI<A> ai, A actor, LocalObjects local, ConfigAI config, long currentTime)
	{
		// обновляем время следующего брождения
		ai.setNextRandomWalk(currentTime + Rnd.nextInt(getRandomWalkMinDelay(), getRandomWalkMaxDelay()));
		// обнуляем время очистки агр листа
		ai.setClearAggro(0);
	}

	@Override
	public <A extends Npc> void think(NpcAI<A> ai, A actor, LocalObjects local, ConfigAI config, long currentTime)
	{
		// получаем владельца суммона
		Character owner = actor.getOwner();

		// если нпс щас что-то делает, выходим
		if(owner == null || actor.isDead() || actor.isCastingNow() || actor.isStuned() || actor.isOwerturned())
			return;

		// определяем наличие агрессора
		Character target = ai.getTarget();

		// если такой имеется
		if(target != null)
		{
			// останавливаем нпс
			actor.stopMove();
			// очищаем задания
			ai.clearTaskList();
			// переводим в состояние боя
			ai.setNewState(NpcAIState.IN_BATTLE);
			// отправляем иконку о том что он переходит в агрессию
			PacketManager.showNotifyIcon(actor, NotifyType.NOTICE_SUB_AGRRESSION);
			// обновляем время
			ai.setLastNotifyIcon(currentTime);
			// выходим
			return;
		}

		// проверяем не отошел ли он на критическое расстояние от владельца
		if(!owner.isInRange(actor, getMaxDistance()))
		{
			// если уже активна стадия возвращения домой, выходим
			if(ai.getCurrentState() == NpcAIState.RETURN_TO_HOME)
				return;

			// зануляем цель
			ai.setTarget(null);
			// очищаем от лишних заданий
			ai.clearTaskList();
			// применяем новую стадию
			ai.setNewState(NpcAIState.RETURN_TO_HOME);
			// отправляем иконку о том что он собирается домой
			PacketManager.showNotifyIcon(actor, NotifyType.NOTICE_THINK);
			// обновляем время
			ai.setLastNotifyIcon(currentTime);
			// выходим
			return;
		}

		// есть ли на очереди задания
		if(ai.isWaitingTask())
		{
			// выполняем задание
			ai.doTask(actor, currentTime, local);
			return;
		}

		// создаем новое задание
		ai.getCurrentFactory().addNewTask(ai, actor, local, config, currentTime);

		// есть ли на очереди задания
		if(ai.isWaitingTask())
			// выполняем задание
			ai.doTask(actor, currentTime, local);
	}
}
