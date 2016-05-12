package tera.gameserver.model.skillengine;

/**
 * Перечисление групп скилов.
 *
 * @author Ronn
 */
public enum SkillGroup
{
	/** скил ближнего боя */
	SHORT_ATTACK,
	/** скил дальнего боя */
	LONG_ATTACK,
	/** скил супер ближнего боя */
	SHORT_ULTIMATE,
	/** скил супер дальнего боя */
	LONG_ULTIMATE,
	/** ловушка */
	TRAP,
	/** щит */
	SHIELD,
	/** прыжок */
	JUMP,
	/** баф */
	BUFF,
	/** дебаф */
	DEBUFF,
	/** хил */
	HEAL,
	/** неиспользуемый */
	NONE;

	public static final SkillGroup[] values = values();

	public static final int length = values.length;
}
