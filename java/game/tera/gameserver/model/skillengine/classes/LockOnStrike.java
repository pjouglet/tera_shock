package tera.gameserver.model.skillengine.classes;

import rlib.util.array.Array;
import rlib.util.array.Arrays;
import tera.gameserver.model.Character;
import tera.gameserver.network.serverpackets.SkillLockAttack;
import tera.gameserver.templates.SkillTemplate;

/**
 * Модель лок он скила.
 *
 * @author Ronn
 */
public class LockOnStrike extends Strike
{
	/** набор лок он таргетов */
	protected final Array<Character> targets;

	public LockOnStrike(SkillTemplate template)
	{
		super(template);

		this.targets = Arrays.toConcurrentArray(Character.class);
	}

	@Override
	public void endSkill(Character attacker, float targetX, float targetY, float targetZ, boolean force)
	{
		super.endSkill(attacker, targetX, targetY, targetZ, force);

		// очищаем список целей
		targets.clear();
	}


	@Override
	public void startSkill(Character attacker, float targetX, float targetY, float targetZ)
	{
		super.startSkill(attacker, targetX, targetY, targetZ);

		// формируем список целей
		addTargets(targets, attacker, targetX, targetY, targetZ);
	}

	@Override
	public void useSkill(Character character, float targetX, float targetY, float targetZ)
	{
		if(applyOrder == 0)
			character.broadcastPacket(SkillLockAttack.getInstance(character, targets, this, castId));
		else if(applyOrder == 1)
		{
			targets.writeLock();
			try
			{
				// получаем массив целей
				Character[] array = targets.array();

				// перебираем всех
				for(int i = 0, length = targets.size(); i < length; i++)
				{
					// получаем цель
					Character target = array[i];

					// если она не походит, пропускаем
					if(target == null || target.isDead() || target.isInvul() || target.isEvasioned())
						continue;

					// применяем скил
		    		applySkill(character, target);
				}
			}
			finally
			{
				targets.writeUnlock();
			}
		}

		applyOrder++;
	}
}
