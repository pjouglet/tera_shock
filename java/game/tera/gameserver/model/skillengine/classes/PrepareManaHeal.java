package tera.gameserver.model.skillengine.classes;

import rlib.util.array.Array;
import tera.gameserver.model.Character;
import tera.gameserver.network.serverpackets.SkillStart;
import tera.gameserver.templates.SkillTemplate;
import tera.util.LocalObjects;

/**
 * Модель скила восстанавливающего мп.
 *
 * @author Ronn
 */
public class PrepareManaHeal extends AbstractSkill
{
	private int state;

	/**
	 * @param template темплейт скила.
	 */
	public PrepareManaHeal(SkillTemplate template)
	{
		super(template);
	}

	@Override
	public void startSkill(Character attacker, float targetX, float targetY, float targetZ)
	{
		super.startSkill(attacker, targetX, targetY, targetZ);

		state = template.getStartState();
	}

	@Override
	public void useSkill(Character character, float targetX, float targetY, float targetZ)
	{
		if(state <= template.getEndState())
		{
			character.broadcastPacket(SkillStart.getInstance(character, getIconId(), castId, state++));
			return;
		}

		// сила хила скила
		int power = getPower();

		if(power < 1)
			return;

		// получаем локальные объекты
		LocalObjects local = LocalObjects.get();

		Array<Character> targets = local.getNextCharList();

		addTargets(targets, character, targetX, targetY, targetZ);

		Character[] array = targets.array();

		for(int i = 0, length = targets.size(); i < length; i++)
		{
			Character target = array[i];

			if(target.isDead() || target.isInvul() || target.isEvasioned())
				continue;

			addEffects(character, target);

			// хилим таргет
			target.skillHealMp(getDamageId(), power, character);
		}
	}
}
