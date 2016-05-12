package tera.gameserver.model.skillengine.effects;

import tera.gameserver.manager.ExecutorManager;
import tera.gameserver.manager.ObjectEventManager;
import tera.gameserver.model.AttackInfo;
import tera.gameserver.model.Character;
import tera.gameserver.model.EffectList;
import tera.gameserver.model.listeners.DamageListener;
import tera.gameserver.model.skillengine.Skill;
import tera.gameserver.templates.EffectTemplate;
import tera.gameserver.templates.SkillTemplate;

/**
 * Баф для поглощения урона.
 * 
 * @author Ronn
 */
public class DamageAbsorption extends AbstractEffect implements DamageListener, Runnable {

	/** лимит поглощаемого урона */
	private int limit;
	/** потребление мп */
	private final int consume;

	public DamageAbsorption(EffectTemplate template, Character effector, Character effected, SkillTemplate skill) {
		super(template, effector, effected, skill);

		this.consume = (int) template.getValue();
	}

	@Override
	public boolean onActionTime() {
		return true;
	}

	@Override
	public void onDamage(Character attacker, Character attacked, AttackInfo info, Skill skill) {

		if(limit < 1 || info.isNoDamage()) {
			return;
		}

		ExecutorManager executor = ExecutorManager.getInstance();

		int current = attacked.getCurrentMp();

		if(current < 1 && consume > 0) {
			executor.execute(this);
			return;
		}

		int damage = info.getDamage();
		int abs = damage > limit ? limit : damage;

		if(consume > 0) {
			int mp = Math.max(1, abs / consume);

			if(current < mp) {
				mp = current;
				abs = mp * consume;
			}

			attacked.setCurrentMp(current - mp);

			ObjectEventManager eventManager = ObjectEventManager.getInstance();
			eventManager.notifyMpChanged(attacked);
		}

		info.setDamage(Math.max(damage - abs, 0));

		limit -= abs;

		if(limit < 1) {
			executor.execute(this);
		}
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

		this.limit = template.getPower();

		super.onStart();
	}

	@Override
	public void run() {

		Character effector = getEffector();

		if(effector == null)
			return;

		EffectList effectList = effector.getEffectList();

		effectList.lock();
		try {
			exit();
		} finally {
			effectList.unlock();
		}
	}
}
