package tera.gameserver.model.ai.npc.taskfactory;

import org.w3c.dom.Node;

import rlib.geom.Angles;
import rlib.util.Rnd;
import rlib.util.VarTable;
import rlib.util.array.Array;

import tera.gameserver.model.Character;
import tera.gameserver.model.World;
import tera.gameserver.model.ai.npc.ConfigAI;
import tera.gameserver.model.ai.npc.NpcAI;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.skillengine.OperateType;
import tera.gameserver.model.skillengine.Skill;
import tera.gameserver.model.skillengine.SkillGroup;
import tera.gameserver.model.skillengine.SkillType;
import tera.util.LocalObjects;

/**
 * Дефолтная модель фабрики заданий в бою НПС.
 *
 * @author Ronn
 */
public class DefaultBattleTaskFactory extends AbstractTaskFactory
{
	/** шансы каста скилов */
	protected final int[] groupChance;

	/** шанс отправки сообщения */
	protected final int shortRange;

	/** использовать ли быстрый поворот */
	protected final boolean fastTurn;

	public DefaultBattleTaskFactory(Node node)
	{
		super(node);

		try
		{
			// парсим атрибуты
			VarTable vars = VarTable.newInstance(node);

			// получаем шанс групп скилов по умолчанию
			int def = vars.getInteger("groupChance", ConfigAI.DEFAULT_GROUP_CHANCE);

			// получаем список всех групп скилов
			SkillGroup[] groups = SkillGroup.values();

			// парсим параметры
			vars = VarTable.newInstance(node, "set", "name", "val");

			// создаем таблицу шансов групп скилов
			this.groupChance = new int[groups.length];
			this.shortRange = vars.getInteger("shortRange", ConfigAI.DEFAULT_SHORT_RATE);
			this.fastTurn = vars.getBoolean("fastTurn", false);

			// заполняем таблицу
			for(int i = 0, length = groupChance.length; i < length; i++)
				groupChance[i] = vars.getInteger(groups[i].name(), def);
		}
		catch(Exception e)
		{
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public <A extends Npc> void addNewTask(NpcAI<A> ai, A actor, LocalObjects local, ConfigAI config, long currentTime)
	{
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

		// если выпал случай на установку ловушки
		if(chance(SkillGroup.TRAP))
		{
			// получаем скил ловушки
			Skill skill = actor.getRandomSkill(SkillGroup.TRAP);

			// если он есть и не в откате
			if(skill != null && !actor.isSkillDisabled(skill))
			{
				// добавляем на каст
				ai.addCastTask(skill, actor);
				return;
			}
		}

		// получаем текущую цель
		Character target = ai.getTarget();

		// если ее нет, выходим
		if(target == null)
			return;

		// если выпал случай использования бафа
		if(chance(SkillGroup.DEBUFF))
		{
			// получаем скил для бафа
			Skill skill = actor.getRandomSkill(SkillGroup.DEBUFF);

			// если он есть и не в откате
			if(skill != null && !actor.isSkillDisabled(skill))
			{
				// добавляем на каст
				ai.addCastTask(skill, target);
				return;
			}
		}

		// если НПС не в боевой стойке
		if(!actor.isBattleStanced())
		{
			// отправляем в боевую стойку
			ai.addNoticeTask(target, true);
			return;
		}

		if(!isFastTurn())
		{
			// если цель не спереди и НПС не в движении
			if(!actor.isInFront(target) && !actor.isMoving())
			{
				// поворачиваемся в сторону цели
				ai.addNoticeTask(target, false);
				return;
			}

			// если нпс не в движении
			if(!actor.isMoving())
			{
				if(actor.isTurner())
				{
					if(!actor.isInTurnFront(target))
					{
						// обновляем внимание
						ai.addNoticeTask(target, false);
						return;
					}
				}
				// если фокусная цель не спереди
				else if(!actor.isInFront(target))
				{
					// обновляем внимание
					ai.addNoticeTask(target, false);
					return;
				}
			}
		}

		// получаем кастующий скил цели
		Skill castingSkill = target.getCastingSkill();

		// если цель что-то кастует TODO
		if(castingSkill != null && castingSkill.getOperateType() == OperateType.ACTIVE && castingSkill.getSkillType() != SkillType.BUFF)
		{
			// если срабатывает случай на использование блока
			if(chance(SkillGroup.SHIELD))
			{
				// получаем скил блока
				Skill skill = actor.getRandomSkill(SkillGroup.SHIELD);

				// если он есть и не в откате
				if(skill != null && !actor.isSkillDisabled(skill))
				{
					// получаем итоговую зону поражения скила
					int range = castingSkill.getRange() + castingSkill.getRadius();

					// если НПС входит в зону поражения и цель находится с переди
					if(actor.isInRange(target, range) && target.isInFront(actor))
					{
						// добавляем на каст
						ai.addCastTask(skill, target);
						return;
					}
				}
			}

			// если срабатывает шанс на использование скила прыжка
			if(chance(SkillGroup.JUMP))
			{
				// пробуем получить прыжковый скил
				Skill skill = actor.getRandomSkill(SkillGroup.JUMP);

				// если такой скил есть и он не в откате
				if(skill != null && !actor.isSkillDisabled(skill))
				{
					// получаем дистанцию прыжка
					int range = skill.getMoveDistance();

					// определяем направление прыжка
					boolean positive = range > 0;

					// определяем боковое ли это движение
					boolean isSide = skill.getHeading() != 0;

					// если это боковое движение
					if(isSide)
					{
						// если кастуемый скил нацеленный
						if(castingSkill.isOneTarget())
						{
							// добавляем в задание скастануть боковое движение
							ai.addCastTask(skill, actor, Angles.calcHeading(actor.getX(), actor.getY(), target.getX(), target.getY()));
							return;
						}
					}
					// если цель находится в оборонительной стойке, а мы в зоне обороны
					else if(target.isDefenseStance() && target.isInFront(actor))
					{
						// нужный разворот
						int newHeading = 0;

						// определеям нужный разворот для запрыгивания заспину
						if(positive)
							newHeading = actor.calcHeading(target.getX(), target.getY());
						else
							newHeading = target.calcHeading(actor.getX(), actor.getY());

						// добавляем задание запрыгнуть за спину
						ai.addCastTask(skill, actor, newHeading);
						return;
					}
					// если прыжок - отпрыгивание назад
					else if(!positive)
					{
						// если мы находимся под ударом и имеем возможность отскочить
						if(castingSkill.getRange() < getShortRange() && actor.getGeomDistance(target) < getShortRange())
						{
							// добавляем отскок от него
							ai.addCastTask(skill, actor, actor.calcHeading(target.getX(), target.getY()));
							return;
						}
					}
					// если это укланение
					else if(skill.isEvasion())
					{
						// добавляем задание использовать укланение
						ai.addCastTask(skill, target);
						return;
					}
				}
			}
		}

		// получаем мили скил
		Skill shortSkill = actor.getRandomSkill(SkillGroup.SHORT_ATTACK);

		// получаем дистанционный скил
		Skill longSkill = actor.getRandomSkill(SkillGroup.LONG_ATTACK);

		// если цель находится в ближнем радиусе
		if(actor.getGeomDistance(target) < getShortRange())
		{
			// если полученный скил в откате
			if(shortSkill != null && actor.isSkillDisabled(shortSkill))
				shortSkill = actor.getFirstEnabledSkill(SkillGroup.SHORT_ATTACK);

			// если ближний скил доступен
			if(shortSkill != null)
			{
				// используем ближний скил
				ai.addCastTask(shortSkill, target);
				return;
			}
			// иначе если доступен дальний скил
			else if(longSkill != null && !actor.isSkillDisabled(longSkill))
			{
				// используем дальний скил
				ai.addCastTask(longSkill, target);
				return;
			}
		}
		// если цель в дальнем радиусе
		else
		{
			// если полученный скил в откате
			if(longSkill != null && actor.isSkillDisabled(longSkill))
				shortSkill = actor.getFirstEnabledSkill(SkillGroup.LONG_ATTACK);

			// если дальний скил доступен
			if(longSkill != null)
			{
				// используем дальний скил
				ai.addCastTask(longSkill, target);
				return;
			}
			// иначе если ближний доступен
			else if(shortSkill != null && !actor.isSkillDisabled(shortSkill))
			{
				// используем ближний скил
				ai.addCastTask(shortSkill, target);
				return;
			}
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
	 * @return дистанция, считающася ближней.
	 */
	protected final int getShortRange()
	{
		return shortRange;
	}

	/**
	 * @return использовать ли быстрый разворот.
	 */
	public boolean isFastTurn()
	{
		return fastTurn;
	}
}
