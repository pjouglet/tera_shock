package tera.gameserver.model.skillengine.effects;

import tera.gameserver.model.AttackInfo;
import tera.gameserver.model.Character;
import tera.gameserver.model.listeners.DamageListener;
import tera.gameserver.model.skillengine.Skill;
import tera.gameserver.templates.EffectTemplate;
import tera.gameserver.templates.SkillTemplate;

/**
 * @author Ronn
 */
public class Invul extends AbstractEffect implements DamageListener {

	public Invul(EffectTemplate template, Character effector, Character effected, SkillTemplate skillTemplate) {
		super(template, effector, effected, skillTemplate);
	}

	@Override
	public boolean onActionTime() {
		return true;
	}

	@Override
	public void onDamage(Character attacker, Character attacked, AttackInfo info, Skill skill) {

		if(info.isNoDamage()) {
			return;
		}

		info.setDamage(0);
	}

	@Override
	public void onExit() {

		Character effected = getEffected();

		if(effected != null)
			effected.removeDamageListener(this);

		super.onExit();
	}

	@Override
	public void onStart() {

		Character effected = getEffected();

		if(effected != null)
			effected.addDamageListener(this);

		super.onStart();
	}
}
