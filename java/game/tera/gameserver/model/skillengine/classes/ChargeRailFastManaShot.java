package tera.gameserver.model.skillengine.classes;

import tera.gameserver.manager.ObjectEventManager;
import tera.gameserver.model.AttackInfo;
import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.shots.FastShot;
import tera.gameserver.network.serverpackets.Damage;
import tera.gameserver.network.serverpackets.StartFastShot;
import tera.gameserver.templates.SkillTemplate;
import tera.util.LocalObjects;

/**
 * Модель стреляющего мана сосалку.
 *
 * @author Ronn
 */
public class ChargeRailFastManaShot extends ChargeDam
{
	/**
	 * @param template темплейт скила.
	 */
	public ChargeRailFastManaShot(SkillTemplate template)
	{
		super(template);
	}

	@Override
	public AttackInfo applySkill(Character attacker, Character target)
	{
		// получаем локальные объекты
		LocalObjects local = LocalObjects.get();

		// получаем контейнер инфы об атаке
		AttackInfo info = local.getNextAttackInfo();

		// получаем силу высасывания
		info.setDamage(getPower());

		// отображаем высасывание маны
		target.broadcastPacket(Damage.getInstance(attacker, target, getDamageId(), info.getDamage(), false, false, Damage.DAMAGE));

		// добавляем высасоное мп
		attacker.effectHealMp(info.getDamage(), target);

		// получаем менеджера событий
		ObjectEventManager eventManager = ObjectEventManager.getInstance();

		// уведомляем об факте атаки
		eventManager.notifyAttack(attacker, target, this, info.getDamage(), false);
		eventManager.notifyAttacked(target, attacker, this, info.getDamage(), false);

		return info;
	}

	@Override
	public void startSkill(Character attacker, float targetX, float targetY, float targetZ)
	{
		super.startSkill(attacker, targetX, targetY, targetZ);

		attacker.broadcastPacket(StartFastShot.getInstance(attacker, this, castId, targetX, targetY, targetZ));
	}

	@Override
	public void useSkill(Character character, float targetX, float targetY, float targetZ)
	{
		setImpactX(character.getX());
		setImpactY(character.getY());
		setImpactZ(character.getZ());

		FastShot.startShot(character, this, targetX, targetY, targetZ);
	}
}
