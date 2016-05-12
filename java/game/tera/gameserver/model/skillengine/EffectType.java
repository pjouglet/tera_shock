package tera.gameserver.model.skillengine;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.effects.AuraManaDamOverTime;
import tera.gameserver.model.skillengine.effects.Buff;
import tera.gameserver.model.skillengine.effects.CancelEffect;
import tera.gameserver.model.skillengine.effects.CharmBuff;
import tera.gameserver.model.skillengine.effects.DamOverTime;
import tera.gameserver.model.skillengine.effects.DamOverTimePercent;
import tera.gameserver.model.skillengine.effects.DamageAbsorption;
import tera.gameserver.model.skillengine.effects.DamageTransfer;
import tera.gameserver.model.skillengine.effects.Debuff;
import tera.gameserver.model.skillengine.effects.Heal;
import tera.gameserver.model.skillengine.effects.HealMod;
import tera.gameserver.model.skillengine.effects.HealOverTime;
import tera.gameserver.model.skillengine.effects.Invul;
import tera.gameserver.model.skillengine.effects.ManaHealOverTime;
import tera.gameserver.model.skillengine.effects.ManaHealOverTimePercent;
import tera.gameserver.model.skillengine.effects.NoBattleEffect;
import tera.gameserver.model.skillengine.effects.NoOwerturnEffect;
import tera.gameserver.model.skillengine.effects.PercentHealOverTime;
import tera.gameserver.model.skillengine.effects.Pheonix;
import tera.gameserver.model.skillengine.effects.Root;
import tera.gameserver.model.skillengine.effects.SkillBlocking;
import tera.gameserver.model.skillengine.effects.Stun;
import tera.gameserver.model.skillengine.effects.Turn;
import tera.gameserver.templates.EffectTemplate;
import tera.gameserver.templates.SkillTemplate;

/**
 * Перечисление типов эффекта.
 * 
 * @author Ronn
 */
public enum EffectType {
	/** баф */
	BUFF(Buff.class),

	CHARM_BUFF(CharmBuff.class),
	/** хилещий эффект */
	HEAL(Heal.class),
	/** хил с модифицированым бонусом хила */
	HEAL_MOD(HealMod.class),
	/** хил в течении времени эффекта */
	HEAL_OVER_TIME(HealOverTime.class),
	/** процентный хил в течении времени */
	PERCENT_HEAL_OVER_TIME(PercentHealOverTime.class),
	/** дебафф */
	DEBUFF(Debuff.class),
	/** эффект оглушения */
	STUN(Stun.class),
	/** эффект блокирования скилов */
	SKILL_BLOCKING(SkillBlocking.class),
	/** эффект, наносящий урон в процессе работы */
	DAM_OVER_TIME(DamOverTime.class),
	/** эффект, который наносит процентный урон в процессе работы */
	DAM_OVER_TIME_PERCENT(DamOverTimePercent.class),
	/** эффект, прерывающиийся при входе в бой */
	NO_BATTLE_EFFECT(NoBattleEffect.class),
	/** эффект, прерывающиийся при опрокидывании */
	NO_OWERTURN_EFFECT(NoOwerturnEffect.class),
	/** эффект, периодического рестора мп */
	MANA_HEAL_OVER_TIME(ManaHealOverTime.class),
	/** эффект, который процентно ресторит мп */
	MANA_HEAL_OVER_TIME_PERCENT(ManaHealOverTimePercent.class),
	/** ара баф на поддержание которого требуется мп */
	AURA_MANA_DAM_OVER_TIME(AuraManaDamOverTime.class),
	/** эффект обездвиживания */
	ROOT(Root.class),
	/** поглощающий урон эффект */
	DAMAGE_ABSORPTION(DamageAbsorption.class),
	/** эффект самовоскрешения */
	PHEONIX(Pheonix.class),
	/** эффект неуязвимости */
	INVUL(Invul.class),
	/** эффект разворота */
	TURN(Turn.class),
	/** эффект трансфера урона */
	DAMAGE_TRANSFER(DamageTransfer.class),
	/** эффект кансела эффектов */
	CANCEL_EFFECT(CancelEffect.class);

	/** конструктор для создания эффекта */
	private Constructor<? extends Effect> constructor;

	/**
	 * @param constructor конструктор эффекта.
	 */
	private EffectType(Class<? extends Effect> effectClass) {
		try {
			this.constructor = effectClass.getConstructor(EffectTemplate.class, Character.class, Character.class, SkillTemplate.class);
		} catch(NoSuchMethodException | SecurityException e) {
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Создание нового эффекта.
	 * 
	 * @param template темплейт эффекта.
	 * @param effector тот, кто накладывает эффект.
	 * @param effected тот, на кого накладывают эффект.
	 * @param skill скил, которым накладывают эффект.
	 * @return новый экземпляр эффекта.
	 */
	public Effect newInstance(EffectTemplate template, Character effector, Character effected, SkillTemplate skill) {
		try {
			return constructor.newInstance(template, effector, effected, skill);
		} catch(InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			return null;
		}
	}
}
