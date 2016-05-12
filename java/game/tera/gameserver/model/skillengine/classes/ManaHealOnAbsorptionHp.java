package tera.gameserver.model.skillengine.classes;

import rlib.util.wraps.Wrap;
import rlib.util.wraps.WrapType;
import tera.gameserver.model.Character;
import tera.gameserver.templates.SkillTemplate;

/**
 * Модель скила восстанавливающего мп.
 *
 * @author Ronn
 */
public class ManaHealOnAbsorptionHp extends ManaHeal
{
	/** накопленная мощность */
	private int power;

	/**
	 * @param template темплейт скила.
	 */
	public ManaHealOnAbsorptionHp(SkillTemplate template)
	{
		super(template);
	}

	@Override
	public int getPower()
	{
		return power;
	}

	@Override
	public void startSkill(Character attacker, float targetX, float targetY, float targetZ)
	{
		super.startSkill(attacker, targetX, targetY, targetZ);

		// зануляем силу
		power = 0;

		// получаем переменную о накоплении поглащенного хп
		Wrap wrap = attacker.getSkillVariables().get(template.getId());

		// если она есть
		if(wrap != null && wrap.getWrapType() == WrapType.INTEGER)
		{
			// берем ее значение
			power = wrap.getInt();
			// опустащаем ее
			wrap.setInt(0);
		}
	}
}
