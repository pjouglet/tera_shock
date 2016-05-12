package tera.gameserver.model.skillengine.classes;

import rlib.util.array.Array;
import tera.gameserver.model.AttackInfo;
import tera.gameserver.model.Character;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.network.serverpackets.Damage;
import tera.gameserver.templates.SkillTemplate;
import tera.util.LocalObjects;

/**
 * Модель скила для вешания дебафа на цель.
 *
 * @author Ronn
 */
public class Debuff extends Effect
{
	/**
	 * @param template темплейт скила.
	 */
	public Debuff(SkillTemplate template)
	{
		super(template);
	}

	@Override
	public AttackInfo applySkill(Character attacker, Character target)
	{
		// получаем локальные объекты
		LocalObjects local = LocalObjects.get();

		// получаем информацию о атаке
		AttackInfo info = local.getNextAttackInfo();

		// рассчитывам блокированность дебафа
		info.setBlocked(target.isBlocked(attacker, impactX, impactY, this));

		// если дебаф не заблокирован
		if(!info.isBlocked())
		{
			// добавляем эффекты
			addEffects(attacker, target);

			// если цель нпс, агрмм его на себя
			if(target.isNpc())
			{
				Npc npc = target.getNpc();
				// добавялем агр поинты
				npc.addAggro(attacker, attacker.getLevel() * attacker.getLevel(), false);
			}
		}

		// если цель в ПвП режиме и не ПК, а атакующей н в ПвП режиме
		if(target.isPvPMode() && !attacker.isPvPMode())
			attacker.setPvPMode(true);

		// отображаем анимацию приминения дебафа
		target.broadcastPacket(Damage.getInstance(attacker, target, template.getDamageId(), getPower(), false, false, Damage.EFFECT));

		return info;
	}

	@Override
	public void useSkill(Character character, float targetX, float targetY, float targetZ)
	{
		// получаем локальные объекты
		LocalObjects local = LocalObjects.get();

		// набор целей
		Array<Character> targets = local.getNextCharList();

		// добавление в набор целей подходящих
		addTargets(targets, character, targetX, targetY, targetZ);

		Character[] array = targets.array();

		// перебор
		for(int i = 0, length = targets.size(); i < length; i++)
    	{
			// цель
			Character target = array[i];

			// если мертва или в инвуле или в уклоне, не подходит
    		if(target.isDead() || target.isInvul() || target.isEvasioned())
    			continue;

    		// применяем скил
    		applySkill(character, target);
    	}
	}
}
