package tera.gameserver.model.ai.npc.taskfactory;

import org.w3c.dom.Node;

import rlib.geom.Coords;
import rlib.util.Rnd;
import rlib.util.VarTable;
import rlib.util.array.Array;

import tera.gameserver.manager.GeoManager;
import tera.gameserver.manager.PacketManager;
import tera.gameserver.model.Character;
import tera.gameserver.model.World;
import tera.gameserver.model.ai.npc.ConfigAI;
import tera.gameserver.model.ai.npc.NpcAI;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.skillengine.Skill;
import tera.gameserver.model.skillengine.SkillGroup;
import tera.gameserver.network.serverpackets.NotifyCharacter.NotifyType;
import tera.util.LocalObjects;
import tera.util.Location;

/**
 * Дефолтная модель реализации фабрики заданий в режиме ожидания.
 *
 * @author Ronn
 */
public class DefaultWaitTaskFactory extends AbstractTaskFactory
{
	/** шансы каста скилов */
	protected final int[] groupChance;

	/** минимальный радиус случайного брождения */
	protected final int randomWalkMinRange;
	/** максимальный радиус случайного брождения */
	protected final int randomWalkMaxRange;

	/** дистанция обращения внимания */
	protected final int noticeRange;

	/** минимальный интервал случайного движения */
	protected final int randomWalkMinDelay;
	/** максимальный интервал случайного движения */
	protected final int randomWalkMaxDelay;

	public DefaultWaitTaskFactory(Node node)
	{
		super(node);

		try
		{
			// получаем список всех групп скилов
			SkillGroup[] groups = SkillGroup.values();

			// парсим параметры
			VarTable vars = VarTable.newInstance(node, "set", "name", "val");

			this.randomWalkMinRange = vars.getInteger("randomWalkMinRange", ConfigAI.DEFAULT_RANDOM_MIN_WALK_RANGE);
			this.randomWalkMaxRange = vars.getInteger("randomWalkMaxRange", ConfigAI.DEFAULT_RANDOM_MAX_WALK_RANGE);

			this.randomWalkMinDelay = vars.getInteger("randomWalkMinDelay", ConfigAI.DEFAULT_RANDOM_MIN_WALK_DELAY);
			this.randomWalkMaxDelay = vars.getInteger("randomWalkMaxDelay", ConfigAI.DEFAULT_RANDOM_MAX_WALK_DELAY);

			this.noticeRange = vars.getInteger("noticeRange", ConfigAI.DEFAULT_NOTICE_RANGE);

			// создаем таблицу шансов групп скилов
			this.groupChance = new int[groups.length];

			// заполняем таблицу
			for(int i = 0, length = groupChance.length; i < length; i++)
				groupChance[i] = vars.getInteger(groups[i].name(), 0);
		}
		catch(Exception e)
		{
			log.warning(this, e);
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public <A extends Npc> void addNewTask(NpcAI<A> ai, A actor, LocalObjects local, ConfigAI config, long currentTime)
	{
		// получаем флаг нахождения нпс в боевой стойке
		boolean battle = actor.isBattleStanced();

		// получаем текущую цель игрока
		Character target = ai.getTarget();

		// получаем дистанцию наблюдения
		int noticeRange = getNoticeRange();

		// если нпс не в боевой стойке либо его цель вышла за пределы ренже внимания
		if(!battle || target == null || actor.getGeomDistance(target) > noticeRange)
		{
			// зануляем текущую цель
			target = null;

			// получаем список персонажей
			Array<Character> charList = local.getNextCharList();

			// собираем сведения о целях вокруг
			World.getAround(Character.class, charList, actor, noticeRange);

			// если есть потенциальные цели
			if(!charList.isEmpty())
			{
				// получаем массив целей
				Character[] array = charList.array();

				// перебираем их
				for(int i = 0, length = charList.size(); i < length; i++)
				{
					// получаем потенциальную цель
					Character character = array[i];

					// если цель враждебная
					if(actor.checkTarget(character))
					{
						// запоминаем
						target = character;
						// выходим из цикла
						break;
					}
				}
			}

			// если найдена новая цель для внимания либо нпс в боевой стойке
			if(target != null)
			{
				// отображаем иконку уделния внимания цели
				PacketManager.showNotifyIcon(actor, NotifyType.NOTICE);
				// обновляем время
				ai.setLastNotifyIcon(currentTime);
				// добавляем задание на обновление фокуса внимания
				ai.addNoticeTask(target, false);
				// выходим
				return;
			}
		}

		// если нпс в боевой стойке
		if(battle)
		{
			// если НПС уже разворачивается
			if(actor.isTurner())
			{
				// если целевой разворот не подходит
				if(!actor.isInTurnFront(target))
					// обновляем внимание
					ai.addNoticeTask(target, false);
			}
			// если фокусная цель не спереди
			else if(!actor.isInFront(target))
				// обновляем внимание
				ai.addNoticeTask(target, false);

			// выходим
			return;
		}

		// если выпал случай использования бафа
		if(chance(SkillGroup.HEAL))
		{
			// получаем скил для хила
			Skill skill = actor.getRandomSkill(SkillGroup.HEAL);

			// если он есть и не в откате
			if(skill != null && !actor.isSkillDisabled(skill))
			{
				// если скил можно на себя юзать и не фул хп у себя
				if(!skill.isNoCaster() && actor.getCurrentHp() < actor.getMaxHp())
				{
					// добавляем задание [bkmyenmcz
					ai.addCastTask(skill, actor);
					return;
				}
				// если скил не только на себя и есть фракция у НПС
				else if(!skill.isTargetSelf() && actor.getFractionRange() > 0)
				{
					// получаем фракцию НПС
					String fraction = actor.getFraction();

					// получаем окружающих НПС
					Array<Npc> npcs = World.getAround(Npc.class, local.getNextNpcList(), actor, actor.getFractionRange());

					// если НПС в радиусе фракции есть
					if(!npcs.isEmpty())
						// перебираем их
						for(Npc npc : npcs.array())
						{
							if(npc == null)
								break;

							// если НПС принадлежит его фракции и не имеет полного хп
							if(fraction.equals(npc.getFraction()) && npc.getCurrentHp() < npc.getMaxHp())
							{
								// добавляем задание хильнуть его
								ai.addCastTask(skill, npc);
								return;
							}
						}
				}
			}
		}

		// если выпал случай использования бафа
		if(chance(SkillGroup.BUFF))
		{
			// получаем скил для бафа
			Skill skill = actor.getRandomSkill(SkillGroup.BUFF);

			// если он есть и не в откате
			if(skill != null && !actor.isSkillDisabled(skill))
			{
				// если баф не висит на НПС
				if(!skill.isNoCaster() && !actor.containsEffect(skill))
				{
					// добавляем задание бафнуться
					ai.addCastTask(skill, actor);
					return;
				}
				// если скил не только на себя и есть фракция у НПС
				else if(!skill.isTargetSelf() && actor.getFractionRange() > 0)
				{
					// получаем фракцию НПС
					String fraction = actor.getFraction();

					// получаем окружающих НПС
					Array<Npc> npcs = World.getAround(Npc.class, local.getNextNpcList(), actor, actor.getFractionRange());

					// если НПС в радиусе фракции есть
					if(!npcs.isEmpty())
						// перебираем их
						for(Npc npc : npcs.array())
						{
							if(npc == null)
								break;

							// если НПС принадлежит его фракции и не имеет этого эффекта
							if(fraction.equals(npc.getFraction()) && !npc.containsEffect(skill))
							{
								// добавляем задание бафнуть его
								ai.addCastTask(skill, npc);
								return;
							}
						}
				}
			}
		}

		// если нпс должен бродить и наступило время следующего хождения
		if(getRandomWalkMaxRange() > 0 && currentTime > ai.getNextRandomWalk())
		{
			// если последний раз иконка блистала более 5 сек назад
			if(currentTime - ai.getLastNotifyIcon() > 5000)
			{
				// отображаем иконку думания
				PacketManager.showNotifyIcon(actor, NotifyType.NOTICE_THINK);
				// обновляем время
				ai.setLastNotifyIcon(currentTime);
			}

			// обновляем время следующего
			ai.setNextRandomWalk(currentTime + Rnd.nextInt(getRandomWalkMinDelay(), getRandomWalkMaxDelay()));

			// получаем точку спавна НПС.
			Location loc = actor.getSpawnLoc();

			// расчитываем расстояние брождения
			int distance = Rnd.nextInt(getRandomWalkMinRange(),getRandomWalkMaxRange());
			// рассчитываем случацное направление
			int newHeading = Rnd.nextInt(65000);

			// рассчитываем целевую точку
			float newX = Coords.calcX(loc.getX(), distance, newHeading);
			float newY = Coords.calcY(loc.getY(), distance, newHeading);

			// получаем менеджер геодаты
			GeoManager geoManager = GeoManager.getInstance();

			// добавляем задание пойти в эту точку
			ai.addMoveTask(newX, newY, geoManager.getHeight(actor.getContinentId(), newX, newY, loc.getZ()), true);
		}
	}

	/**
	 * @return радиус обращения на цели.
	 */
	protected final int getNoticeRange()
	{
		return noticeRange;
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

	/**
	 * @return минимальный радиус брождения.
	 */
	protected final int getRandomWalkMinRange()
	{
		return randomWalkMinRange;
	}

	/**
	 * @return сработала ли указанная группа.
	 */
	protected boolean chance(SkillGroup group)
	{
		return Rnd.chance(groupChance[group.ordinal()]);
	}
}
