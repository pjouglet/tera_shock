package tera.gameserver.model.ai.npc.taskfactory;

import org.w3c.dom.Node;

import rlib.geom.Angles;
import rlib.geom.Coords;
import tera.gameserver.manager.GeoManager;
import tera.gameserver.model.Character;
import tera.gameserver.model.ai.npc.ConfigAI;
import tera.gameserver.model.ai.npc.NpcAI;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.skillengine.Skill;
import tera.gameserver.model.skillengine.SkillGroup;
import tera.util.LocalObjects;

/**
 * Модель боя дальника ивентового.
 *
 * @author Ronn
 */
public class EventRangerBattleTaskFactory extends DefaultBattleTaskFactory
{
	public EventRangerBattleTaskFactory(Node node)
	{
		super(node);
	}

	/*@Override
	public <A extends Npc> void addNewTask(NpcAI<A> ai, A actor, LocalObjects local, ConfigAI config, long currentTime)
	{
		// если выпал случай использования бафа
		if(chance(SkillGroup.BUFF))
		{
			// получаем скил для бафа
			Skill skill = actor.getRandomSkill(SkillGroup.BUFF);

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

		if(chance(SkillGroup.TRAP))
		{
			Skill skill = actor.getRandomSkill(SkillGroup.TRAP);

			// если он есть и не в откате
			if(skill != null && !actor.isSkillDisabled(skill) && target.getGeomDistance(actor.getX(), actor.getY()) <= skill.getRadius())
			{
				// добавляем на каст
				ai.addCastTask(skill, target);
				return;
			}
		}

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

		// если цель находится в ближнем радиусе
		if(actor.getGeomDistance(target) < getShortRange())
		{
			// получаем мили скил
			Skill skill = actor.getRandomSkill(SkillGroup.SHORT_ATTACK);

			// если есть мили скил и он не в откате
			if(skill != null && !actor.isSkillDisabled(skill))
				// добавляем задание атаковать скилом
				ai.addCastTask(skill, target);
		}
		else
		{
			// получаем дистанционный скил
			Skill skill = actor.getRandomSkill(SkillGroup.LONG_ATTACK);

			// если есть идстанционный скил и он не в откате
			if(skill != null && !actor.isSkillDisabled(skill))
				// добавляем скастануть им
				ai.addCastTask(skill, target);
		}

		int dist = Math.max(getShortRange() + 30, 180);

		if(actor.getGeomDistance(target) < dist)
		{
			int heading = target.calcHeading(actor.getX(), actor.getY());

			// получаем координаты отступдения
			float x = Coords.calcX(actor.getX(), dist, heading);
			float y = Coords.calcY(actor.getY(), dist, heading);

			// получаем менеджер геодаты
			GeoManager geoManager = GeoManager.getInstance();

			// добавляем задание для отступления
			ai.addMoveTask(x, y, geoManager.getHeight(actor.getContinentId(), x, y, actor.getZ()), true);

			// добавляем задание разворота в сторону цели
			ai.addNoticeTask(target, true);
		}
	}*/
}
