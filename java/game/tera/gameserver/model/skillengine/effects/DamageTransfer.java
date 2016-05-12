package tera.gameserver.model.skillengine.effects;

import tera.gameserver.manager.ExecutorManager;
import tera.gameserver.model.AttackInfo;
import tera.gameserver.model.Character;
import tera.gameserver.model.EffectList;
import tera.gameserver.model.listeners.DamageListener;
import tera.gameserver.model.skillengine.Skill;
import tera.gameserver.templates.EffectTemplate;
import tera.gameserver.templates.SkillTemplate;

/**
 * Баф для переноса урона.
 * 
 * @author Ronn
 */
public class DamageTransfer extends AbstractEffect implements DamageListener, Runnable {

	public DamageTransfer(EffectTemplate template, Character effector, Character effected, SkillTemplate skill) {
		super(template, effector, effected, skill);
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

		Character effector = getEffector();
		Character effected = getEffected();

		if(effected == effector) {
			return;
		}

		ExecutorManager executor = ExecutorManager.getInstance();

		if(effector.isDead()) {
			executor.execute(this);
			return;
		}

		info.setDamage(0);

		skill.applySkill(attacker, effector);
	}

	@Override
	public void onExit() {

		Character effected = getEffected();

		if(effected != null) {
			effected.removeDamageListener(this);
		}

		super.onExit();
	}

	@Override
	public void onStart() {

		Character effected = getEffected();

		if(effected != null) {
			effected.addDamageListener(this);
		}

		super.onStart();
	}

	@Override
	public void run() {

		Character effector = getEffector();

		if(effector == null) {
			return;
		}

		EffectList effectList = effector.getEffectList();

		effectList.lock();
		try {
			exit();
		} finally {
			effectList.unlock();
		}
	}
}
