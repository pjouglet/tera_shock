package tera.gameserver.model.skillengine.effects;

import rlib.logging.Logger;
import rlib.logging.Loggers;
import rlib.util.array.Arrays;
import tera.gameserver.model.Character;
import tera.gameserver.model.EffectList;
import tera.gameserver.model.Party;
import tera.gameserver.model.skillengine.Effect;
import tera.gameserver.model.skillengine.EffectState;
import tera.gameserver.model.skillengine.EffectType;
import tera.gameserver.model.skillengine.ResistType;
import tera.gameserver.model.skillengine.funcs.Func;
import tera.gameserver.network.serverpackets.AppledEffect;
import tera.gameserver.network.serverpackets.CancelEffect;
import tera.gameserver.taskmanager.EffectTaskManager;
import tera.gameserver.templates.EffectTemplate;
import tera.gameserver.templates.SkillTemplate;

/**
 * Базовая реализация эффектов.
 * 
 * @author Ronn
 */
public abstract class AbstractEffect implements Effect {

	protected static final Logger LOGGER = Loggers.getLogger(Effect.class);

	/** темплейт эффекта */
	protected final EffectTemplate template;
	/** скил, с которого был наложен эффект */
	protected final SkillTemplate skillTemplate;

	/** функции, которые будут применятся при старте эффекта */
	protected Func[] funcs;

	/** тот, кто наложил эффект */
	protected Character effector;
	/** тот, на ком эффект висит */
	protected Character effected;

	/** эффект лист, в котором находится эффект */
	protected EffectList effectList;

	/** время старта */
	protected long startTime;

	/** период эффекта */
	protected int period;
	/** счетчик */
	protected int count;

	/** заюзан ли эффект */
	protected boolean inUse;

	/** статус эффекта */
	protected volatile EffectState state;

	/**
	 * @param effectTemplate темплейт эффекта.
	 * @param effector тот, кто наложил эффект.
	 * @param effected тот, на кого наложили эффект,
	 * @param skillTemplate скил, которым был наложен эффект.
	 */
	public AbstractEffect(EffectTemplate template, Character effector, Character effected, SkillTemplate skillTemplate) {
		this.effector = effector;
		this.effected = effected;
		this.skillTemplate = skillTemplate;
		this.state = EffectState.CREATED;
		this.funcs = template.getFuncs();
		this.template = template;
		this.period = template.getTime();
		this.count = template.getCount();
		this.startTime = System.currentTimeMillis();
	}

	@Override
	public void exit() {

		EffectList effectList = getEffectList();

		if(effectList == null) {
			LOGGER.warning(this, new Exception("not found effect list."));
			return;
		}

		effectList.lock();
		try {

			if(getState() == EffectState.FINISHED) {
				return;
			}

			setState(EffectState.FINISHING);
			scheduleEffect();

		} finally {
			effectList.unlock();
		}
	}

	@Override
	public void finalyze() {
		effector = null;
		effected = null;
		effectList = null;
	}

	@Override
	public void fold() {
		template.put(this);
	}

	@Override
	public int getChance() {
		return template.getChance();
	}

	@Override
	public int getCount() {
		return count;
	}

	@Override
	public Character getEffected() {
		return effected;
	}

	@Override
	public int getEffectId() {
		return template.getId() < 0 ? skillTemplate.getId() : template.getId();
	}

	@Override
	public EffectList getEffectList() {
		return effectList;
	}

	@Override
	public Character getEffector() {
		return effector;
	}

	@Override
	public EffectType getEffectType() {
		return template.getConstructor();
	}

	@Override
	public Func[] getFuncs() {
		return funcs;
	}

	@Override
	public int getOrder() {
		return Arrays.indexOf(skillTemplate.getEffectTemplates(), template);
	}

	@Override
	public int getPeriod() {
		return period;
	}

	@Override
	public ResistType getResistType() {
		return template.getResistType();
	}

	@Override
	public int getSkillClassId() {
		return skillTemplate.getClassId();
	}

	@Override
	public int getSkillId() {
		return skillTemplate.getId();
	}

	@Override
	public SkillTemplate getSkillTemplate() {
		return skillTemplate;
	}

	@Override
	public String getStackType() {
		return template.getStackType();
	}

	@Override
	public long getStartTime() {
		return startTime;
	}

	@Override
	public EffectState getState() {
		return state;
	}

	@Override
	public EffectTemplate getTemplate() {
		return template;
	}

	@Override
	public int getTime() {
		return (int) ((System.currentTimeMillis() - startTime) / 1000);
	}

	@Override
	public int getTimeEnd() {
		return getTotalTime() - getTime();
	}

	@Override
	public int getTimeForPacket() {
		return getTotalTime() * 1000;
	}

	@Override
	public int getTotalTime() {
		return period * count;
	}

	@Override
	public int getUsingCount() {
		return template.getCount() - count;
	}

	@Override
	public boolean hasStackType() {
		return !template.getStackType().isEmpty();
	}

	@Override
	public boolean isAura() {
		return false;
	}

	@Override
	public boolean isDebuff() {
		return template.isDebuff();
	}

	@Override
	public boolean isEffect() {
		return true;
	}

	@Override
	public boolean isEnded() {
		return state == EffectState.FINISHED || state == EffectState.FINISHING;
	}

	@Override
	public boolean isFinished() {
		return state == EffectState.FINISHED;
	}

	@Override
	public boolean isInUse() {
		return inUse;
	}

	@Override
	public boolean isNoAttack() {
		return template.isNoAttack();
	}

	@Override
	public boolean isNoAttacked() {
		return template.isNoAttacked();
	}

	@Override
	public boolean isNoOwerturn() {
		return template.isNoOwerturn();
	}

	@Override
	public boolean onActionTime() {
		return false;
	}

	@Override
	public void onExit() {

		Character effected = getEffected();

		// если его нет, выходим
		if(effected == null) {
			LOGGER.warning(this, new Exception("not found effected"));
			return;
		}

		Func[] funcs = template.getFuncs();

		if(funcs.length < 1) {
			return;
		}

		for(int i = 0, length = funcs.length; i < length; i++)
			funcs[i].removeFuncTo(effected);

		effected.updateInfo();
	}

	@Override
	public void onStart() {

		Character effected = getEffected();

		if(effected == null) {
			LOGGER.warning(this, new Exception("not found effected"));
			return;
		}

		Func[] funcs = template.getFuncs();

		if(funcs.length < 1) {
			return;
		}

		for(int i = 0, length = funcs.length; i < length; i++) {
			funcs[i].addFuncTo(effected);
		}

		effected.updateInfo();
	}

	@Override
	public void reinit() {
		this.state = EffectState.CREATED;
		this.period = template.getTime();
		this.count = template.getCount();
		this.startTime = System.currentTimeMillis();
	}

	@Override
	public void scheduleEffect() {

		Character effected = getEffected();
		Character effector = getEffector();

		if(effected == null) {
			LOGGER.warning(this, new Exception("not found effected"));
			return;
		}

		if(effector == null) {
			LOGGER.warning(this, new Exception("not found effector"));
			return;
		}

		EffectList effectList = getEffectList();

		if(effectList == null) {
			LOGGER.warning(this, new Exception("not found effect list."));
			return;
		}

		effectList.lock();
		try {
			switch(getState()) {

				case CREATED: {

					onStart();
					setState(EffectState.ACTING);

					effected.broadcastPacket(AppledEffect.getInstance(effector, effected, this));

					Party party = effected.getParty();

					if(party != null) {
						party.updateEffects(effected.getPlayer());
					}

					EffectTaskManager effectManager = EffectTaskManager.getInstance();
					effectManager.addTask(this, period);
					break;
				}

				case ACTING: {

					if(count > 0) {

						count--;

						if(onActionTime() && count > 0) {
							break;
						}
					}

					setState(EffectState.FINISHING);
				}

				case FINISHING: {

					setState(EffectState.FINISHED);
					setInUse(false);
					onExit();

					effected.removeEffect(this);
					effected.broadcastPacket(CancelEffect.getInstance(effected, getEffectId()));

					Party party = effected.getParty();

					if(party != null) {
						party.updateEffects(effected.getPlayer());
					}

					break;
				}
				default:
					LOGGER.warning(this, new Exception("incorrect effect state " + state));
			}
		} finally {
			effectList.unlock();
		}
	}

	@Override
	public void setCount(int count) {
		this.count = count;
	}

	@Override
	public void setEffected(Character effected) {
		this.effected = effected;
	}

	@Override
	public void setEffectList(EffectList effectList) {
		this.effectList = effectList;
	}

	@Override
	public void setEffector(Character effector) {
		this.effector = effector;
	}

	@Override
	public void setInUse(boolean inUse) {
		this.inUse = inUse;
	}

	@Override
	public void setPeriod(int period) {
		this.period = period;
	}

	@Override
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	@Override
	public void setState(EffectState state) {
		this.state = state;
	}

	@Override
	public boolean isDynamicCount() {
		return template.isDynamicCount();
	}

	@Override
	public boolean isDynamicTime() {
		return template.isDynamicTime();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
