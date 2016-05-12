package tera.gameserver.model.ai.npc.thinkaction;

import org.w3c.dom.Node;

import rlib.util.VarTable;
import rlib.util.array.Array;

import tera.gameserver.manager.PacketManager;
import tera.gameserver.model.Character;
import tera.gameserver.model.NpcAIState;
import tera.gameserver.model.World;
import tera.gameserver.model.WorldRegion;
import tera.gameserver.model.ai.CharacterAI;
import tera.gameserver.model.ai.npc.ConfigAI;
import tera.gameserver.model.ai.npc.NpcAI;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.network.serverpackets.NotifyCharacter.NotifyType;
import tera.util.LocalObjects;
import tera.util.Location;

public class DefaultPatrolAction extends AbstractThinkAction
{
	/** время ожидания диалога после завершения последнего */
	protected final int waitDialog;
	/** максимум для скольких нпс цель может быть топ агром */
	protected final int maxMostHated;

	public DefaultPatrolAction(Node node)
	{
		super(node);

		try
		{
			// парсим параметры
			VarTable vars = VarTable.newInstance(node, "set", "name", "val");

			this.waitDialog = vars.getInteger("waitDialog", 0);
			this.maxMostHated = vars.getInteger("maxMostHated", ConfigAI.DEFAULT_MAX_MOST_HATED);
		}
		catch(Exception e)
		{
			log.warning(this, e);
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public <A extends Npc> void think(NpcAI<A> ai, A actor, LocalObjects local, ConfigAI config, long currentTime)
	{
		// если нпс щас что-то делает, выходим
		if(ai.isActiveDialog() || actor.isDead() || actor.isCastingNow() || actor.isStuned() || actor.isOwerturned())
			return;

		// определяем наличие агрессора
		Character hated = actor.getMostHated();

		// если такой имеется
		if(hated != null)
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

		// если нпс агрессивен
		if(actor.isAggressive())
		{
			// получаем текущий регион НПС
			WorldRegion region = actor.getCurrentRegion();

			boolean find = false;

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
					{
						// получаем агро лист цели
						Array<Npc> hateList = target.getHateList();

						// если агр лист у него больше максимума, значит есть смысл проверять
						if(!actor.isMinion() && hateList.size() > getMaxMostHated())
						{
							// создаем счетчик нпс
							int counter = 0;

							// получаем массив агр нпс
							Npc[] hatenpcs = hateList.array();

							// перебираем
							for(int g = 0, size = hateList.size(); g < size; g++)
							{
								// получаем НПС
								Npc npc = hatenpcs[g];

								// если это другого типа НПС, пропускаем
								if(npc == null || npc.getTemplate() != actor.getTemplate())
									continue;

								// получаем АИ нпс
								CharacterAI npcAI = npc.getAI();

								// если оно левого типа, пропускаем
								if(npcAI.getClass() != ai.getClass())
									continue;

								@SuppressWarnings("unchecked")
								NpcAI<A> targetAI = (NpcAI<A>) npcAI;

								// если у того НПС эта цель активная, считаем
								if(targetAI.getTarget() == target)
									counter++;
							}

							// если у цели больше имита востов
							if(counter > getMaxMostHated())
								// пропускаем цель
								continue;
						}


						// добавляем в агр лист
						actor.addAggro(target, 1, false);
						// ставим флаг найденности цели
						find = true;
					}
				}

				if(find)
				{
					// останавливаем нпс
					actor.stopMove();
					// очищаем задания
					ai.clearTaskList();
					// переводим в состояние боя
					ai.setNewState(NpcAIState.IN_BATTLE);
					// отправляем иконку о том что он собирается нападать
					PacketManager.showNotifyIcon(actor, NotifyType.NOTICE_AGRRESION);
					// обновляем время
					ai.setLastNotifyIcon(currentTime);
					return;
				}
			}
		}

		Location[] route = actor.getRoute();

		if(route == null || route.length < 2)
		{
			// останавливаем нпс
			actor.stopMove();
			// очищаем задания
			ai.clearTaskList();
			// переводим в состояние ожидания
			ai.setNewState(NpcAIState.WAIT);
			// отправляем иконку о том что он переходит в агрессию
			PacketManager.showNotifyIcon(actor, NotifyType.YELLOW_QUESTION);
			// обновляем время
			ai.setLastNotifyIcon(currentTime);
			// выходим
			return;
		}

		// если НПС в движении, выходим
		if(actor.isMoving())
			return;

		// есть ли на очереди задания
		if(ai.isWaitingTask())
		{
			// выполняем задание
			ai.doTask(actor, currentTime, local);
			// выходим
			return;
		}

		// создаем новое задание
		ai.getCurrentFactory().addNewTask(ai, actor, local, config, currentTime);

		// есть ли на очереди задания
		if(ai.isWaitingTask())
			// выполняем задание
			ai.doTask(actor, currentTime, local);
	}

	/**
	 * @return максимальное число топ агров для цели.
	 */
	protected final int getMaxMostHated()
	{
		return maxMostHated;
	}
}
