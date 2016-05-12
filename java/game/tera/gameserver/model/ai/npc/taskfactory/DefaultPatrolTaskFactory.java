package tera.gameserver.model.ai.npc.taskfactory;

import org.w3c.dom.Node;

import rlib.util.Rnd;
import rlib.util.VarTable;
import rlib.util.array.Array;
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
 * Модель генератора задач для патрулирование территории НПС.
 *
 * @author Ronn
 */
public class DefaultPatrolTaskFactory extends AbstractTaskFactory
{
	/** шансы каста скилов */
	protected final int[] groupChance;

	/** дистанция обращения внимания */
	protected final int noticeRange;
	/** интервал между переходами точек маршрута */
	protected final int patrolInterval;

	public DefaultPatrolTaskFactory(Node node)
	{
		super(node);

		try
		{
			// получаем список всех групп скилов
			SkillGroup[] groups = SkillGroup.values();

			// парсим параметры
			VarTable vars = VarTable.newInstance(node, "set", "name", "val");

			this.noticeRange = vars.getInteger("noticeRange", ConfigAI.DEFAULT_NOTICE_RANGE);
			this.patrolInterval = vars.getInteger("patrolInterval", 0);

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
				// останавливаем НПС
				actor.stopMove();
				// очищаем задания
				ai.clearTaskList();
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

		// получаем маршрут патрулирования
		Location[] route = actor.getRoute();

		// получаем текущий индекс точки маршрута
		int currentIndex = ai.getRouteIndex();

		// получаем текущую точку маршрута
		Location point = route[currentIndex];

		// получаем время перехода к след. точке
		long nextRoutePoint = ai.getNextRoutePoint();

		// если мы к нужной точки еще не пришли
		if(actor.getDistance(point.getX(), point.getY(), point.getZ()) > 10)
			// добавляем задачу дойти до нее
			ai.addMoveTask(point, true);
		// если мы уже пришли
		else if(nextRoutePoint == -1)
			// ставим время, когдап надо начать идти к следующей
			ai.setNextRoutePoint(currentTime + getPatrolInterval());
		// если уже пора к след. двигаться
		else if(currentTime > nextRoutePoint)
		{
			currentIndex += 1;

			if(route.length >= currentIndex)
				currentIndex = 0;

			// обновляем индекс точки
			ai.setRouteIndex(currentIndex);

			// получаем след. точку
			point = route[currentIndex];

			// даем задание к ней идти
			ai.addMoveTask(point, true);

			// убераем время перехода к след точке
			ai.setNextRoutePoint(-1);
		}
	}

	/**
	 * @return сработала ли указанная группа.
	 */
	protected boolean chance(SkillGroup group)
	{
		return Rnd.chance(groupChance[group.ordinal()]);
	}

	/**
	 * @return радиус обращения внимаия на игроков.
	 */
	public int getNoticeRange()
	{
		return noticeRange;
	}

	/**
	 * @return интервал между переходами между точек.
	 */
	public int getPatrolInterval()
	{
		return patrolInterval;
	}
}
