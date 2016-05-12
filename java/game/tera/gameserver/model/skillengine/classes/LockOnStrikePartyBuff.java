package tera.gameserver.model.skillengine.classes;

import rlib.util.array.Array;
import tera.gameserver.model.AttackInfo;
import tera.gameserver.model.Character;
import tera.gameserver.model.Party;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.skillengine.Formulas;
import tera.gameserver.templates.SkillTemplate;
import tera.util.LocalObjects;

/**
 * Реализация скила приста Energy Stars.
 *
 * @author Ronn
 */
public class LockOnStrikePartyBuff extends LockOnStrike
{
	public LockOnStrikePartyBuff(SkillTemplate template)
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

		// применяем атаку
		target.causingDamage(this, info, attacker);

		// если не заблокирован
		if(!info.isBlocked())
		{
			// вешаем бафы на кастера
			addEffects(attacker, attacker);
			// добалвяем агр на баф
			addAggroTo(attacker, attacker, getAggroPoint());

			// получаем пати кастера
			Party party = attacker.getParty();

			// если пати есть
			if(party != null)
			{
				// получаем членов пати
				Array<Player> members = party.getMembers();

				members.readLock();
				try
				{
					// получаем массив членов пати
					Player[] array = members.array();

					// перебираем членов пати
					for(int i = 0, length = members.size(); i < length; i++)
					{
						// получаем члена пати
						Player member = array[i];

						// если его нет либо это кастер либо задалеко, пропускаем
						if(member == null || member == attacker || !attacker.isInRange(member, getRadius()))
							continue;

						// добавляем баф
						addEffects(attacker, member);
						// добалвяем агр на баф
						addAggroTo(attacker, member, getAggroPoint());
					}
				}
				finally
				{
					members.readUnlock();
				}
			}
		}

		return info;
	}
}
