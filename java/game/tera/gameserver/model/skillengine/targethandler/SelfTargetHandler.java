package tera.gameserver.model.skillengine.targethandler;

import rlib.util.array.Array;
import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.Skill;

/**
 * Модель цели на себя.
 *
 * @author Ronn
 */
public final class SelfTargetHandler extends AbstractTargetHandler
{
	@Override
	public void addTargetsTo(Array<Character> targets, Character caster, Skill skill, float targetX, float targetY, float targetZ)
	{
		targets.add(caster);
	}
}
