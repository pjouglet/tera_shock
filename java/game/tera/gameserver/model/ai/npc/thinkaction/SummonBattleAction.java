package tera.gameserver.model.ai.npc.thinkaction;

import org.w3c.dom.Node;

import rlib.util.VarTable;

import tera.gameserver.manager.PacketManager;
import tera.gameserver.model.Character;
import tera.gameserver.model.NpcAIState;
import tera.gameserver.model.ai.npc.ConfigAI;
import tera.gameserver.model.ai.npc.MessagePackage;
import tera.gameserver.model.ai.npc.NpcAI;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.network.serverpackets.NotifyCharacter.NotifyType;
import tera.gameserver.tables.MessagePackageTable;
import tera.util.LocalObjects;

/**
 * Базовая модель генератора действий в бою.
 *
 * @author Ronn
 */
public class SummonBattleAction extends AbstractThinkAction
{
	/** пакет сообщений при смене цели боя */
	protected final MessagePackage switchTargetMessages;

	/** радиус ведения боя */
	protected final int battleMaxRange;
	/** радиус реакции суммона на врагов */
	protected final int reactionMaxRange;

	public SummonBattleAction(Node node)
	{
		super(node);

		try
		{
			// парсим параметры
			VarTable vars = VarTable.newInstance(node, "set", "name", "val");

			this.battleMaxRange = vars.getInteger("battleMaxRange", ConfigAI.DEFAULT_BATTLE_MAX_RANGE);
			this.reactionMaxRange = vars.getInteger("reactionMaxRange", ConfigAI.DEFAULT_REACTION_MAX_RANGE);

			// получаем таблицу сообщений
			MessagePackageTable messageTable = MessagePackageTable.getInstance();

			this.switchTargetMessages = messageTable.getPackage(vars.getString("switchTargetMessages", ConfigAI.DEFAULT_SWITCH_TARGET_MESSAGES));
		}
		catch(Exception e)
		{
			log.warning(this, e);
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * @return радиус ведения боя.
	 */
	protected final int getBattleMaxRange()
	{
		return battleMaxRange;
	}

	/**
	 * @return радиус реакции НПС на врагов.
	 */
	protected final int getReactionMaxRange()
	{
		return reactionMaxRange;
	}

	@Override
	public <A extends Npc> void think(NpcAI<A> ai, A actor, LocalObjects local, ConfigAI config, long currentTime)
	{
		// получаем владельца суммона
		Character owner = actor.getOwner();

		// если нет владельца либо суммон мертв
		if(owner == null || actor.isDead())
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

		// если суммон щас что-то делает, выходим
		if(actor.isTurner() || actor.isCastingNow() || actor.isStuned() || actor.isOwerturned())
			return;

		// если суммон вышел за пределы максимального радиуса атаки
		if(!actor.isInRangeZ(owner, getReactionMaxRange()))
		{
			// зануляем цель
			ai.setTarget(null);
			// очищаем задания
			ai.clearTaskList();
			// очищаем агр лист
			actor.clearAggroList();
			// переводим в режим возврпщения домой
			ai.setNewState(NpcAIState.RETURN_TO_HOME);
			// отправляем иконку думания
			PacketManager.showNotifyIcon(actor, NotifyType.NOTICE_THINK);
			// обновляем время отправки иконки
			ai.setLastNotifyIcon(currentTime);
			// выходим
			return;
		}

		// получаем цель суммона
		Character target = ai.getTarget();

		// если цель отсутствует, выходим
		if(target == null)
		{
			// очищаем задания
			ai.clearTaskList();
			// очищаем агр лист
			actor.clearAggroList();
			// переводим в режим ожидания
			ai.setNewState(NpcAIState.WAIT);
			// отправляем иконку думания
			PacketManager.showNotifyIcon(actor, NotifyType.NOTICE_THINK);
			// обновляем время отправки иконки
			ai.setLastNotifyIcon(currentTime);
			// выходим
			return;
		}

		// если цель выходит за радиус боевых действий
		if(target.isDead() || !target.isInRange(owner, getBattleMaxRange()))
		{
			// отменяем его как цель
			ai.abortAttack();
			// выходим
			return;
		}

		// если есть ожидающие задания
		if(ai.isWaitingTask())
			// выполняем и если это небыло последнее задание
			if(ai.doTask(actor, currentTime, local))
				// выходим
				return;

		// если нпс щас что-то делает, выходим
		if(actor.isTurner() || actor.isCastingNow() || actor.isStuned() || actor.isOwerturned() || actor.isMoving())
			return;

		// если давно не оторбажали иконку думания
		if(currentTime - ai.getLastNotifyIcon() > 15000)
		{
			// отправляем иконку думания
			PacketManager.showNotifyIcon(actor, NotifyType.YELLOW_QUESTION);
			// обновляем время отправки иконки
			ai.setLastNotifyIcon(currentTime);
		}

		// создаем новое задание
		ai.getCurrentFactory().addNewTask(ai, actor, local, config, currentTime);

		// если есть ожидающие задания
		if(ai.isWaitingTask())
			// выполняем
			ai.doTask(actor, currentTime, local);
	}
}
