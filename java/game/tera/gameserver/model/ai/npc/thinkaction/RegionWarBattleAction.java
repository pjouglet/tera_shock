package tera.gameserver.model.ai.npc.thinkaction;

import org.w3c.dom.Node;

import rlib.util.VarTable;
import rlib.util.array.Array;

import tera.gameserver.manager.PacketManager;
import tera.gameserver.model.Character;
import tera.gameserver.model.NpcAIState;
import tera.gameserver.model.World;
import tera.gameserver.model.WorldRegion;
import tera.gameserver.model.ai.npc.ConfigAI;
import tera.gameserver.model.ai.npc.MessagePackage;
import tera.gameserver.model.ai.npc.NpcAI;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.network.serverpackets.NotifyCharacter.NotifyType;
import tera.gameserver.tables.MessagePackageTable;
import tera.util.LocalObjects;

/**
 * Модель генератора действий в бою при битве за регион.
 *
 * @author Ronn
 */
public class RegionWarBattleAction extends AbstractThinkAction
{
	/** пакет сообщений при смене цели боя */
	protected final MessagePackage switchTargetMessages;

	/** радиус ведения боя */
	protected final int battleMaxRange;
	/** радиус реакции НПС на врагов */
	protected final int reactionMaxRange;

	public RegionWarBattleAction(Node node)
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
		if(actor.isTurner() || actor.isCastingNow() || actor.isStuned() || actor.isOwerturned())
			return;

		// если актор вышел за пределы максимального радиуса атаки
		if(!actor.isInRangeZ(actor.getSpawnLoc(), getReactionMaxRange()))
		{
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

		// получаем топовый агр
		Character mostHated = actor.getMostHated();

		if(mostHated == null && actor.isAggressive())
		{
			// получаем текущий регион НПС
			WorldRegion region = actor.getCurrentRegion();

			// если там есть игроки
			if(region != null)
			{
				// получаем список персонажей
				Array<Character> charList = local.getNextCharList();

				// собираем сведения о целях вокруг
				World.getAround(Character.class, charList, actor, actor.getAggroRange());

				// получаем массив целей
				Character[] array = charList.array();

				// перебираем потенциальные цели
				for(int i = 0, length = charList.size(); i < length; i++)
				{
					// получаем потенциальную цель
					Character target = array[i];

					// если на цель возможна агрессия и цель в зоне агра
					if(ai.checkAggression(target))
						// добавляем в агр лист
						actor.addAggro(target, 1, false);
				}
			}
		}

		mostHated = actor.getMostHated();

		// если главная цель отсутствует, выходим
		if(mostHated == null)
		{
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

		// получаем текущую цель АИ
		Character target = ai.getTarget();

		// если цель выходит за радиус боевых действий
		if(mostHated.isDead() || !mostHated.isInRange(actor.getSpawnLoc(), getBattleMaxRange()))
		{
			// удаляем его с агр листа
			actor.removeAggro(mostHated);
			// очищаем задания
			ai.clearTaskList();
			// выходим
			return;
		}

		// если топ агр не является текущей целью АИ
		if(mostHated != target)
			// обновляем текущую цель АИ
			ai.setTarget(mostHated);

		// если есть ожидающие задания
		if(ai.isWaitingTask())
			// выполняем и если это небыло последнее задание
			if(ai.doTask(actor, currentTime, local))
				// выходим
				return;

		// если нпс щас что-то делает, выходим
		if(actor.isTurner() || actor.isCastingNow() || actor.isStuned() || actor.isOwerturned())
			return;

		if(currentTime - ai.getLastNotifyIcon() > 15000)
		{
			// отправляем иконку думания
			PacketManager.showNotifyIcon(actor, NotifyType.YELLOW_QUESTION);
			// обновляем время отправки иконки
			ai.setLastNotifyIcon(currentTime);
		}

		// создаем новое
		ai.getCurrentFactory().addNewTask(ai, actor, local, config, currentTime);

		// если есть ожидающие задания
		if(ai.isWaitingTask())
			// выполняем
			ai.doTask(actor, currentTime, local);
	}
}
