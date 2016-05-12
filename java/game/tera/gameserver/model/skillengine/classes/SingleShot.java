package tera.gameserver.model.skillengine.classes;

import tera.gameserver.model.AttackInfo;
import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.Formulas;
import tera.gameserver.model.skillengine.shots.ObjectShot;
import tera.gameserver.templates.SkillTemplate;
import tera.util.LocalObjects;

/**
 * Модель стреляющего скила.
 *
 * @author Ronn
 */
public class SingleShot extends Strike
{
	public SingleShot(SkillTemplate template)
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

		// рассчитываем урон
		AttackInfo info = formulas.calcDamageSkill(local.getNextAttackInfo(), this, attacker, target);

		// применяем влияние расстояния
		fineRange(attacker, target, info);

		// наносим урон
		target.causingDamage(this, info, attacker);

		// если удар был не заблокирован
		if(!info.isBlocked())
			// добавляем эффекты
			addEffects(attacker, target);

		return info;
	}

	/**
	 * Рассчет штрафа на дистанционный скил.
	 *
	 * @param attacker атакующий.
	 * @param attacked атакуемый.
	 * @param info инфа об атаке.
	 */
	protected void fineRange(Character attacker, Character attacked, AttackInfo info)
	{
		if(info.getDamage() < 2)
			return;

		// получаем максимальное расстояние
		float range = getRange() * getRange();

		// получаем текущее расстояние
		float current = Math.max(attacker.getSquareDistance(attacked.getX(), attacked.getY(), attacked.getZ()), 1F);

		info.divDamage(3F - Math.min(range / current, 2F));
	}

	@Override
	public void useSkill(Character character, float targetX, float targetY, float targetZ)
	{
		setImpactX(character.getX());
		setImpactY(character.getY());
		setImpactZ(character.getZ());

		ObjectShot.startShot(character, this, targetX, targetY, targetZ);
	}
}
