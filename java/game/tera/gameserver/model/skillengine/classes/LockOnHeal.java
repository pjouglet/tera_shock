package tera.gameserver.model.skillengine.classes;

import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.StatType;
import tera.gameserver.network.serverpackets.SkillLockAttack;
import tera.gameserver.templates.SkillTemplate;

/**
 * Модель исцеляющего скила.
 *
 * @author Ronn
 */
public class LockOnHeal extends LockOnStrike
{
	/**
	 * @param template темплейт скила.
	 */
	public LockOnHeal(SkillTemplate template)
	{
		super(template);
	}

	@Override
	public void useSkill(Character character, float targetX, float targetY, float targetZ)
    {

		if(applyOrder == 0)
			character.broadcastPacket(SkillLockAttack.getInstance(character, targets, this, castId));
		else if(applyOrder == 1)
		{
			//сила хила скила
			int power = getPower();

			//увеличиваем на процентный бонус хилера
			power = (int) (power + (power * character.calcStat(StatType.HEAL_POWER_PERCENT, 0, null, null) / 100F));
			//добавляем статичный бонус хилера
			power = power += character.calcStat(StatType.HEAL_POWER_STATIC, 0, null, null);

			if(power < 1)
				return;

			targets.readLock();
			try
			{
				Character[] array = targets.array();

				for(int i = 0, length = targets.size(); i < length; i++)
				{
					Character target = array[i];

					if(target.isDead() || target.isInvul())
		    			continue;

		    		addEffects(character, target);

		    		// хилим таргет
		    		target.skillHealHp(getDamageId(), power, character);

		    		// добавляем агр поинты
		    		addAggroTo(character, target, power + getAggroPoint());

		    		// если цель в ПвП режиме а кастер нет
		    		if(target.isPvPMode() && !character.isPvPMode())
		    		{
		    			// включаем пвп режим
		    			character.setPvPMode(true);

		    			// включаем боевую стойку
		    			character.startBattleStance(target);
		    		}
				}
			}
			finally
			{
				targets.readUnlock();
			}
		}

		applyOrder++;
    }
}
