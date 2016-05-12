package tera.gameserver.model.skillengine.classes;

import rlib.util.array.Array;
import tera.gameserver.model.AttackInfo;
import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.Formulas;
import tera.gameserver.templates.SkillTemplate;
import tera.util.LocalObjects;

/**
 * Оснавная модель ударных скилов.
 * 
 * @author Ronn
 */
public class Strike extends AbstractSkill {

	/**
	 * @param template темплейт скила.
	 */
	public Strike(SkillTemplate template) {
		super(template);
	}

	@Override
	public AttackInfo applySkill(Character attacker, Character target) {

		LocalObjects local = LocalObjects.get();

		Formulas formulas = Formulas.getInstance();

		AttackInfo info = formulas.calcDamageSkill(local.getNextAttackInfo(), this, attacker, target);

		target.causingDamage(this, info, attacker);

		if(!info.isBlocked()) {
			addEffects(attacker, target);
		}

		return info;
	}

	@Override
	public void useSkill(Character character, float targetX, float targetY, float targetZ) {

		LocalObjects local = LocalObjects.get();

		Array<Character> targets = local.getNextCharList();

		addTargets(targets, character, targetX, targetY, targetZ);

		Character[] array = targets.array();

		for(int i = 0, length = targets.size(); i < length; i++) {

			Character target = array[i];

			if(target == null || target.isDead() || target.isInvul() || target.isEvasioned()) {
				continue;
			}

			applySkill(character, target);
		}

		super.useSkill(character, targetX, targetY, targetZ);
	}
}
