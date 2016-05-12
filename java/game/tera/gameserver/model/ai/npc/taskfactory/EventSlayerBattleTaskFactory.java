package tera.gameserver.model.ai.npc.taskfactory;

import org.w3c.dom.Node;

import rlib.geom.Angles;
import rlib.geom.Coords;
import tera.gameserver.model.Character;
import tera.gameserver.model.ai.npc.ConfigAI;
import tera.gameserver.model.ai.npc.NpcAI;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.skillengine.Skill;
import tera.gameserver.model.skillengine.SkillGroup;
import tera.util.LocalObjects;

/**
 * Модель боя ивентового слеера.
 *
 * @author Ronn
 */
public class EventSlayerBattleTaskFactory extends DefaultBattleTaskFactory
{
	public EventSlayerBattleTaskFactory(Node node)
	{
		super(node);
	}
/*
	@Override
	public <A extends Npc> void addNewTask(NpcAI<A> ai, A actor, LocalObjects local, ConfigAI config, long currentTime)
	{
		// получаем текущую цель
		Character target = ai.getTarget();

		// если ее нет, выходим
		if(target == null)
			return;

		// если НПС не в боевй стойке
		if(!actor.isBattleStanced())
		{
			// отправляем в боевую стойку
			ai.addNoticeTask(target, true);
			return;
		}

		// если цель не спереди
		if(!actor.isInFront(target))
		{
			// поворачиваемся в сторону цели
			ai.addNoticeTask(target, false);
			return;
		}

		// кастующий скил цели
		Skill castingSkill = target.getCastingSkill();

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

				// определяем направление
				boolean positive = range > 0;
				boolean isSide = skill.getHeading() != 0;

				// если скил дает уклонение и цель кастует скил
				if(skill.isEvasion() && castingSkill != null)
					// испольузем уклонение
					ai.addCastTask(skill, target);
				// если это боковое движение
				else if(isSide)
				{
					// если есть кастуемый им скил направленный
					if(castingSkill != null && castingSkill.isOneTarget())
					{
						float x = Coords.calcX(actor.getX(), range, actor.getHeading() + skill.getHeading());
						float y = Coords.calcY(actor.getY(), range, actor.getHeading() + skill.getHeading());

						// добавляем в задание скастануть боковое движение
						ai.addCastTask(skill, actor, Angles.calcHeading(x, y, target.getX(), target.getY()));
					}
				}
				// если цель находится в оборонительной стойке, а мы в зоне обороны
				else if(target.isDefenseStance() && target.getGeomDistance(actor.getX(), actor.getY()) < Math.abs(range) && target.isInFront(actor))
				{
					// нужный разворот
					int newHeading = 0;

					// определеям нужный разворот для запрыгивания заспину
					if(positive)
						newHeading = actor.calcHeading(target.getX(), target.getY());
					else
						newHeading = target.calcHeading(actor.getX(), actor.getY());

					//добавляем задание запрыгнуть за спину
					ai.addCastTask(skill, actor, newHeading);
					return;
				}
				// если прыжок - отпрыгивание назад
				else if(!positive)
				{
					// если есть кастуемый скил, и он ближнего действия и мы близко от игрока
					if(castingSkill != null && castingSkill.getRange() < getShortRange() && actor.getGeomDistance(target) < 150)
					{
						// добавляем отскок от него
						ai.addCastTask(skill, actor, actor.calcHeading(target.getX(), target.getY()));
						return;
					}
				}
				// если прыжок является прыжком в перед к цели и цель находится дальше прыжка
				else if(positive && !actor.isInRange(target, range))
				{
					//добавляем задание прыгнуть к цели
					ai.addCastTask(skill, target);
					return;
				}
			}
		}

		// получаем мили скил
		Skill shortSkill = actor.getRandomSkill(SkillGroup.SHORT_ATTACK);
		// получаем дистанционный скил
		Skill longSkill = actor.getRandomSkill(SkillGroup.LONG_ATTACK);

		// если есть мили скил и он не в откате
		if(shortSkill != null && !actor.isSkillDisabled(shortSkill))
		{
			// если мы близко у цели
			if(actor.getGeomDistance(target) < getShortRange())
			{
				// добавляем задание атаковать скилом
				ai.addCastTask(shortSkill, target);
				return;
			}

			// если дистанционного скила нет
			if(longSkill == null || actor.isSkillDisabled(longSkill))
			{
				// добавляем атаковать мили скилом
				ai.addCastTask(shortSkill, target);
				return;
			}
		}

		// если есть идстанционный скил и он не в откате
		if(longSkill != null && !actor.isSkillDisabled(longSkill))
			// добавляем скастануть им
			ai.addCastTask(longSkill, target);
	}*/
}
