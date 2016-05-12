package tera.gameserver.model.skillengine.targethandler;

import rlib.util.array.Array;
import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.Skill;

/**
 * Интерфейс для реализации расчета таргетов.
 *
 * @author Ronn
 */
public interface TargetHandler
{
	/**
	 * Рассчет целей скила.
	 *
	 * @param targets контейнер целей.
	 * @param caster кастующий скил.
	 * @param skill кастуемый скил.
	 * @param targetX координата цели скила.
	 * @param targetY координата цели скила.
	 * @param targetZ координата цели скила.
	 */
	public void addTargetsTo(Array<Character> targets, Character caster, Skill skill, float targetX, float targetY, float targetZ);
}
