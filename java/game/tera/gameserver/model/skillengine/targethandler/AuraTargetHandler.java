package tera.gameserver.model.skillengine.targethandler;

import rlib.util.array.Array;
import tera.Config;
import tera.gameserver.manager.DebugManager;
import tera.gameserver.model.Character;
import tera.gameserver.model.World;
import tera.gameserver.model.skillengine.Skill;

/**
 * Обработчик целей вокруг кастующего.
 *
 * @author Ronn
 */
public class AuraTargetHandler extends AbstractTargetHandler
{
	/**
	 * Добавление в список всех потенциальных целей.
	 */
	protected void addAllTargets(Array<Character> targets, Character caster, int radius)
	{
		World.getAround(Character.class, targets, caster, radius);
	}

	@Override
	public void addTargetsTo(Array<Character> targets, Character caster, Skill skill, float targetX, float targetY, float targetZ)
	{
		// получаем радиус каста
		int radius = skill.getRadius();

		// получаем точку каста
		float x = caster.getX();
		float y = caster.getY();
		float z = caster.getZ();

		// обновляем если это надо, точку приминения скила
		updateImpact(skill, targetX, targetY, targetZ);

		// отображаем дебаг зоны поражения
		if(Config.DEVELOPER_DEBUG_TARGET_TYPE)
			DebugManager.showAreaDebug(caster.getContinentId(), x, y, z, radius);

		// вносим потенциальных целей
		addAllTargets(targets, caster, radius);

		// обновляем если это надо, точку приминения скила
		updateImpact(skill, x, y, z);

		// если список не пуст
		if(!targets.isEmpty())
		{
			Character[] array = targets.array();

			// перебираем цели
			for(int i = 0, length = targets.size(); i < length; i++)
			{
				Character target = array[i];

				// если цель не подходит, пропускаем
				if(!checkTarget(caster, target))
				{
					targets.fastRemove(i--);
					length--;
					continue;
				}

				// если до цели не достаем, пропускаем
				if(!target.isHit(x, y, z, 100, radius))
				{
					targets.fastRemove(i--);
					length--;
					continue;
				}
			}
		}
	}
}
