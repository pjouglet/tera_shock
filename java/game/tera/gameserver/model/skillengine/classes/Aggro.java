package tera.gameserver.model.skillengine.classes;

import tera.gameserver.manager.ObjectEventManager;
import tera.gameserver.model.AttackInfo;
import tera.gameserver.model.Character;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.templates.SkillTemplate;

/**
 * Модель скила, увеличивающего аггрессию.
 *
 * @author Ronn
 */
public class Aggro extends Debuff
{
	public Aggro(SkillTemplate template)
	{
		super(template);
	}

	@Override
	public AttackInfo applySkill(Character attacker, Character target)
	{
		AttackInfo info = super.applySkill(attacker, target);

		if(!info.isBlocked() && target.isNpc())
		{
			Npc npc = target.getNpc();
			// добавляем агр поинт
			npc.addAggro(attacker, getPower(), false);
		}

		// получаем менеджера событий
		ObjectEventManager eventManager = ObjectEventManager.getInstance();

		// увндомляем о факте атаки
		eventManager.notifyAttacked(target, attacker, this, 0, false);
		eventManager.notifyAttack(attacker, target, this, 0, false);

		return info;
	}
}
