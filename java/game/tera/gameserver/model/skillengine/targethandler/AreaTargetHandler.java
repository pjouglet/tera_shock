package tera.gameserver.model.skillengine.targethandler;

import rlib.geom.Angles;
import rlib.geom.Coords;
import rlib.util.array.Array;
import tera.Config;
import tera.gameserver.manager.DebugManager;
import tera.gameserver.manager.GeoManager;
import tera.gameserver.model.Character;
import tera.gameserver.model.World;
import tera.gameserver.model.skillengine.Skill;

/**
 * Модель для реализации рассчета целей в области от точки применения.
 *
 * @author Ronn
 */
public class AreaTargetHandler extends AbstractTargetHandler
{
	/**
	 * Добавление в список всех потенциальных целей.
	 */
	protected void addAllTargets(Array<Character> targets, Character caster, float targetX, float targetY, float targetZ, int radius)
	{
		World.getAround(Character.class, targets, caster.getContinentId(), targetX, targetY, targetZ, caster.getObjectId(), caster.getSubId(), radius);
	}

	@Override
	public void addTargetsTo(Array<Character> targets, Character caster, Skill skill, float targetX, float targetY, float targetZ)
	{
		// определяем радиус скила
		int radius = skill.getRadius();

		// определяем направление приминения
		float radians = Angles.headingToRadians(caster.getHeading() + skill.getHeading());

		// рассчитываем центр области
		targetX = Coords.calcX(caster.getX(), skill.getRange(), radians);
		targetY = Coords.calcY(caster.getY(), skill.getRange(), radians);

		// получаем менеджер геодаты
		GeoManager geoManager = GeoManager.getInstance();

		targetZ = geoManager.getHeight(caster.getContinentId(), targetX, targetY, caster.getZ());

		// обновляем если это надо, точку приминения скила
		updateImpact(skill, targetX, targetY, targetZ);

		// отображаем дебаг зоны поражения
		if(Config.DEVELOPER_DEBUG_TARGET_TYPE)
			DebugManager.showAreaDebug(caster.getContinentId(), targetX, targetY, targetZ, radius);

		// добавляем все потенциальные цели
		addAllTargets(targets, caster, targetX, targetY, targetZ, radius);

		// если потенциальные цели есть
		if(!targets.isEmpty())
		{
			// получаем массив целей
			Character[] array = targets.array();

			// перебираем их
			for(int i = 0, length = targets.size(); i < length; i++)
			{
				// получаем цель
				Character target = array[i];

				// если цель не является возможной, удаляем
				if(!checkTarget(caster, target))
				{
					targets.fastRemove(i--);
					length--;
					continue;
				}

				// если цель не входит в зону удара, удаляем
				if(!target.isHit(targetX, targetY, targetZ, 100, radius))
				{
					targets.fastRemove(i--);
					length--;
				}
			}
		}
	}
}
