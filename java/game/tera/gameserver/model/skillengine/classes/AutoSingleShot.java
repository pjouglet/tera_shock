package tera.gameserver.model.skillengine.classes;

import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.shots.FastAutoShot;
import tera.gameserver.model.skillengine.shots.FastShot;
import tera.gameserver.network.serverpackets.StartFastShot;
import tera.gameserver.templates.SkillTemplate;

/**
 * Модель быстро стреляющего скила.
 * 
 * @author Ronn
 */
public class AutoSingleShot extends Strike
{
	/** цель выстрела */
	private Character target;
	
	/**
	 * @param template темплейт скила.
	 */
	public AutoSingleShot(SkillTemplate template)
	{
		super(template);
	}

	@Override
	public void endSkill(Character attacker, float targetX, float targetY, float targetZ, boolean force)
	{
		// зануляем цель
		setTarget(null);
		// завершаем скил
		super.endSkill(attacker, targetX, targetY, targetZ, force);
	}
	
	/**
	 * @return цель выстрела.
	 */
	public Character getTarget()
	{
		return target;
	}

	/**
	 * @param target цель выстрела.
	 */
	public void setTarget(Character target)
	{
		this.target = target;
	}

	@Override
	public void startSkill(Character attacker, float targetX, float targetY, float targetZ)
	{
		// источник удара будет кастующий
		setImpactX(attacker.getX());
		setImpactY(attacker.getY());
		setImpactZ(attacker.getZ());
		// запускаем скил
		super.startSkill(attacker, targetX, targetY, targetZ);
		// получаем потенциальную цель
		Character target = attacker.getTarget();
		// применяем цель
		setTarget(target);
		// если цель есть и она подходит
		if(target != null && attacker.checkTarget(target))
			// отправляем анимацию наводящегося выстрела
			attacker.broadcastPacket(StartFastShot.getInstance(attacker, target, this, castId));
		else
			// отправляем анимацию выстрела в точку
			attacker.broadcastPacket(StartFastShot.getInstance(attacker, this, castId, targetX, targetY, targetZ));
	}

	@Override
	public void useSkill(Character character, float targetX, float targetY, float targetZ)
	{
		// потенциальный таргет
		Character target = getTarget();
		// если таргет есть и он подходит нам
		if(target != null && character.checkTarget(target))
			// запускаем самоноводящийся выстрел
			FastAutoShot.startShot(character, target, this);
		else
			// иначе запускаем прямой выстрел
			FastShot.startShot(character, this, targetX, targetY, targetZ);
	}
}
