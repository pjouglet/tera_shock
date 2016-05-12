package tera.gameserver.model.skillengine.classes;

import rlib.util.array.Array;
import tera.gameserver.model.AttackInfo;
import tera.gameserver.model.Character;
import tera.gameserver.network.serverpackets.Damage;
import tera.gameserver.templates.SkillTemplate;
import tera.util.LocalObjects;

/**
 * Модель скила, служащего только для активирования бафа.
 *
 * @author Ronn
 */
public class Effect extends AbstractSkill
{
	/**
	 * @param template темплейт скила.
	 */
	public Effect(SkillTemplate template)
	{
		super(template);
	}

	@Override
	public AttackInfo applySkill(Character attacker, Character target)
	{
		// получаем локальные объекты
		LocalObjects local = LocalObjects.get();

		// инфа об атаке
		AttackInfo info = local.getNextAttackInfo();

		// рассчет блокировки щитом
		info.setBlocked(target.isBlocked(attacker, impactX, impactY, this));

		// отображем анимацию
		target.broadcastPacket(Damage.getInstance(attacker, target, template.getDamageId(), getPower(), false, false, Damage.EFFECT));

		return info;
	}

	@Override
	public void useSkill(Character character, float targetX, float targetY, float targetZ)
	{
		// получаем локальные объекты
		LocalObjects local = LocalObjects.get();

		// контейнер для целей
		Array<Character> targets = local.getNextCharList();

		// добавляем цели
		addTargets(targets, character, targetX, targetY, targetZ);

		Character[] array = targets.array();

		// перечисляем цели
		for(int i = 0, length = targets.size(); i < length; i++)
    	{
			Character target = array[i];

			// если цель мертва или в инву, пропускаем
    		if(target.isDead() || target.isInvul())
    			continue;

    		// применяем скил
    		applySkill(character, target);
    	}
	}
}
