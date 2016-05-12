package tera.gameserver.model.skillengine.classes;

import tera.gameserver.model.AttackInfo;
import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.Formulas;
import tera.gameserver.templates.SkillTemplate;
import tera.util.LocalObjects;

/**
 * @author Ronn
 */
public class SlayerFuryStrike extends Strike
{
	private static final float[] MOD = new float[101];

	static
	{
		for(int i = 0; i < MOD.length; i++)
			MOD[i] = Math.min(100 + 1900 / Math.max(i, 1) - 19, 300) /  100F;
	}

	/**
	 * @param template темплейт скила.
	 */
	public SlayerFuryStrike(SkillTemplate template)
	{
		super(template);
	}

	@Override
	public AttackInfo applySkill(Character attacker, Character target)
	{
		// получаем локальные объекты
		LocalObjects local = LocalObjects.get();

		// получаем формулы
		Formulas formulas = Formulas.getInstance();

		AttackInfo info = formulas.calcDamageSkill(local.getNextAttackInfo(), this, attacker, target);

		info.mulDamage(MOD[attacker.getCurrentHpPercent()]);

		target.causingDamage(this, info, attacker);

		if(!info.isBlocked())
			addEffects(attacker, target);

		return info;
	}
}
