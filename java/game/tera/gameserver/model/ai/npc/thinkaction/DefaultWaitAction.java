package tera.gameserver.model.ai.npc.thinkaction;

import org.w3c.dom.Node;

import rlib.util.Rnd;
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

/**
 * Базовая реализация генерации действий в режиме ожидания.
 *
 * @author Ronn
 */
public class DefaultWaitAction extends AbstractThinkAction
{
	/** максимальный радиус случайного брождения */
	protected final int randomWalkMaxRange;

	/** минимальный интервал случайного движения */
	protected final int randomWalkMinDelay;
	/** максимальный интервал случайного движения */
	protected final int randomWalkMaxDelay;
	/** максимум для скольких нпс цель может быть топ агром */
	protected final int maxMostHated;

	public DefaultWaitAction(Node node)
	{
		super(node);

		try
		{
			// парсим параметры
			VarTable vars = VarTable.newInstance(node, "set", "name", "val");

			this.randomWalkMaxRange = vars.getInteger("maxRandomWalkRange", ConfigAI.DEFAULT_RANDOM_MAX_WALK_RANGE);

			this.randomWalkMinDelay = vars.getInteger("randomWalkMinDelay", ConfigAI.DEFAULT_RANDOM_MIN_WALK_DELAY);
			this.randomWalkMaxDelay = vars.getInteger("randomWalkMaxDelay", ConfigAI.DEFAULT_RANDOM_MAX_WALK_DELAY);
			this.maxMostHated = vars.getInteger("maxMostHated", ConfigAI.DEFAULT_MAX_MOST_HATED);
		}
		catch(Exception e)
		{
			log.warning(this, e);
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * @return максимальное число топ агров для цели.
	 */
	protected final int getMaxMostHated()
	{
		return maxMostHated;
	}

	/**
	 * @return максимальный период брождения.
	 */
	protected final int getRandomWalkMaxDelay()
	{
		return randomWalkMaxDelay;
	}

	/**
	 * @return максимальный радиус брождения.
	 */
	protected final int getRandomWalkMaxRange()
	{
		return randomWalkMaxRange;
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
		// если нпс щас что-то делает, выходим
		if(actor.isDead() || actor.isCastingNow() || actor.isStuned() || actor.isOwerturned())
			return;

		// получаем время очистки агро листа
		long time = ai.getClearAggro();

		// если пришло время очистить агр лист
		if(time != 0 && currentTime > time)
		{
			// очищаем агр лист
			actor.clearAggroList();
			// обнуляем время очистки
			ai.setClearAggro(0);
		}

		// определяем наличие агрессора
		Character damager = actor.getMostHated();

		// если такой имеется
		if(damager != null)
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

		// проверяем не отошел ли он на критическое расстояние от точки спавна
		if(!actor.isInRangeZ(actor.getSpawnLoc(), getRandomWalkMaxRange()))
		{
			// если уже активна стадия возвращения домой, выходим
			if(ai.getCurrentState() == NpcAIState.RETURN_TO_HOME)
				return;

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
							// получаем массив агр нпс
							Npc[] hatenpcs = hateList.array();

							// создаем счетчик нпс
							int counter = 0;

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

		if(route != null && route.length > 1)
		{
			// останавливаем нпс
			actor.stopMove();
			// очищаем задания
			ai.clearTaskList();
			// переводим в состояние ожидания
			ai.setNewState(NpcAIState.PATROL);
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
}
