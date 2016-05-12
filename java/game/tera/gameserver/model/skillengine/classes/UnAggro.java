package tera.gameserver.model.skillengine.classes;

import rlib.util.array.Array;
import tera.gameserver.model.AttackInfo;
import tera.gameserver.model.Character;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.templates.SkillTemplate;
import tera.util.LocalObjects;

/**
 * @author Ronn
 */
public class UnAggro extends Strike {

	public UnAggro(SkillTemplate template) {
		super(template);
	}

	@Override
	public AttackInfo applySkill(Character attacker, Character target) {

		LocalObjects local = LocalObjects.get();

		AttackInfo info = local.getNextAttackInfo();

		Array<Npc> hateList = local.getNextNpcList();
		hateList = attacker.getLocalHateList(hateList);

		Npc[] array = hateList.array();

		for(int i = 0, length = hateList.size(); i < length; i++)
			array[i].removeAggro(attacker);

		addEffects(attacker, target);

		return info;
	}
}
