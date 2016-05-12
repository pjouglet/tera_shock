package tera.gameserver.model.skillengine.targethandler;

import rlib.util.array.Array;
import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.Skill;

/**
 * Модель пустой цели.
 *
 * @author Ronn
 */
public final class NoneTargetHandler extends AbstractTargetHandler
{
	@Override
	public void addTargetsTo(Array<Character> targets, Character caster, Skill skill, float targetX, float targetY, float targetZ){}
}
