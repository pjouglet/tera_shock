package tera.gameserver.model.skillengine.classes;

import tera.gameserver.model.Character;
import tera.gameserver.network.serverpackets.MoveSkill;
import tera.gameserver.network.serverpackets.SkillStart;
import tera.gameserver.templates.SkillTemplate;

/**
 * Модель составного удара заряжающего скила.
 *
 * @author Ronn
 */
public class Cyclone extends ChargeDam
{
	/** состояние скила */
	private int state;

	/**
	 * @param template темплейт скила.
	 */
	public Cyclone(SkillTemplate template)
	{
		super(template);
	}

	@Override
	public int getCastCount()
	{
		return chargeLevel + 1;
	}

	/**
	 * @return разница.
	 */
	protected int getDiff()
	{
		return Math.max(1, super.getCastCount() - chargeLevel);
	}

	@Override
	public int getHitTime()
	{
		return getDelay() + getInterval() * (getCastCount() + 2) + super.getHitTime();
	}

	@Override
	public int getIconId()
	{
		return super.getIconId();
	}

	@Override
	public int getMoveDistance()
	{
		return super.getMoveDistance() / getDiff();
	}

	@Override
	public int getMoveTime()
	{
		return Math.max(getHitTime() - super.getMoveTime(), 1);
	}

	@Override
	public int getPower()
	{
		return super.getPower() / getDiff();
	}

	@Override
	public void startSkill(Character attacker, float targetX, float targetY, float targetZ)
	{
		// ид каста */
		castId = attacker.getCastId();
		// уровень заряда
		chargeLevel = attacker.getChargeLevel();
		// рассчет стартового стейта скила
		state = template.getStartState() + template.getEndState() - chargeLevel;
		// отображаем начало каста
		attacker.broadcastPacket(SkillStart.getInstance(attacker, getIconId(), castId, state));

		// если раш, отображаем рывок
		if(isRush())
		{
			// цель рывка
			Character target = attacker.getTarget();

			// если цель есть, отображаем рывок за целью
			if(target != null)
				attacker.broadcastPacket(MoveSkill.getInstance(attacker, target));
			else
				// иначе рывок в точку
				attacker.broadcastPacket(MoveSkill.getInstance(attacker, targetX, targetY, targetZ));
		}
	}

	@Override
	public void useSkill(Character character, float targetX, float targetY, float targetZ)
	{
		state++;

		super.useSkill(character, targetX, targetY, targetZ);

		character.broadcastPacket(SkillStart.getInstance(character, template.getIconId(), castId, state));
	}
}
