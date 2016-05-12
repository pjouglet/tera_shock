package tera.gameserver.model.skillengine;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import rlib.logging.Loggers;
import tera.gameserver.model.skillengine.classes.AbsorptionHp;
import tera.gameserver.model.skillengine.classes.Aggro;
import tera.gameserver.model.skillengine.classes.AutoSingleShot;
import tera.gameserver.model.skillengine.classes.Buff;
import tera.gameserver.model.skillengine.classes.CancelCast;
import tera.gameserver.model.skillengine.classes.CancelOwerturn;
import tera.gameserver.model.skillengine.classes.ChanceEffect;
import tera.gameserver.model.skillengine.classes.Charge;
import tera.gameserver.model.skillengine.classes.ChargeComplexStrike;
import tera.gameserver.model.skillengine.classes.ChargeManaHeal;
import tera.gameserver.model.skillengine.classes.ChargeRailFastManaShot;
import tera.gameserver.model.skillengine.classes.ChargeSingleShot;
import tera.gameserver.model.skillengine.classes.ChargeSingleSlowShot;
import tera.gameserver.model.skillengine.classes.ChargeStrike;
import tera.gameserver.model.skillengine.classes.ChargeVampStrike;
import tera.gameserver.model.skillengine.classes.CharmBuff;
import tera.gameserver.model.skillengine.classes.ClearBuff;
import tera.gameserver.model.skillengine.classes.ClearDebuff;
import tera.gameserver.model.skillengine.classes.ComplexModStrike;
import tera.gameserver.model.skillengine.classes.ComplexStrike;
import tera.gameserver.model.skillengine.classes.ConterStrike;
import tera.gameserver.model.skillengine.classes.Cyclone;
import tera.gameserver.model.skillengine.classes.Debuff;
import tera.gameserver.model.skillengine.classes.Defense;
import tera.gameserver.model.skillengine.classes.Effect;
import tera.gameserver.model.skillengine.classes.Heal;
import tera.gameserver.model.skillengine.classes.HealPercent;
import tera.gameserver.model.skillengine.classes.ItemBuff;
import tera.gameserver.model.skillengine.classes.Jump;
import tera.gameserver.model.skillengine.classes.LancerDefense;
import tera.gameserver.model.skillengine.classes.Leash;
import tera.gameserver.model.skillengine.classes.LockOn;
import tera.gameserver.model.skillengine.classes.LockOnEffect;
import tera.gameserver.model.skillengine.classes.LockOnHeal;
import tera.gameserver.model.skillengine.classes.LockOnStrike;
import tera.gameserver.model.skillengine.classes.LockOnStrikePartyBuff;
import tera.gameserver.model.skillengine.classes.ManaGainStrike;
import tera.gameserver.model.skillengine.classes.ManaHeal;
import tera.gameserver.model.skillengine.classes.ManaHealOnAbsorptionHp;
import tera.gameserver.model.skillengine.classes.ManaHealPercent;
import tera.gameserver.model.skillengine.classes.ManaSingleShot;
import tera.gameserver.model.skillengine.classes.ManaStrike;
import tera.gameserver.model.skillengine.classes.Mount;
import tera.gameserver.model.skillengine.classes.NpcSingleFastShot;
import tera.gameserver.model.skillengine.classes.NpcSingleShot;
import tera.gameserver.model.skillengine.classes.NpcSingleSlowShot;
import tera.gameserver.model.skillengine.classes.OwerturnedStrike;
import tera.gameserver.model.skillengine.classes.PartySummon;
import tera.gameserver.model.skillengine.classes.Passive;
import tera.gameserver.model.skillengine.classes.PrepareManaHeal;
import tera.gameserver.model.skillengine.classes.PrepareStrike;
import tera.gameserver.model.skillengine.classes.PvPMode;
import tera.gameserver.model.skillengine.classes.RestoreStamina;
import tera.gameserver.model.skillengine.classes.Resurrect;
import tera.gameserver.model.skillengine.classes.SingleShot;
import tera.gameserver.model.skillengine.classes.SingleSlowShot;
import tera.gameserver.model.skillengine.classes.SlayerFuryStrike;
import tera.gameserver.model.skillengine.classes.SpawnBonfire;
import tera.gameserver.model.skillengine.classes.SpawnBuffTrap;
import tera.gameserver.model.skillengine.classes.SpawnItem;
import tera.gameserver.model.skillengine.classes.SpawnSmokeSummon;
import tera.gameserver.model.skillengine.classes.SpawnSummon;
import tera.gameserver.model.skillengine.classes.SpawnTrap;
import tera.gameserver.model.skillengine.classes.StageStrike;
import tera.gameserver.model.skillengine.classes.Strike;
import tera.gameserver.model.skillengine.classes.SummonAbort;
import tera.gameserver.model.skillengine.classes.SummonAttack;
import tera.gameserver.model.skillengine.classes.TeleportJump;
import tera.gameserver.model.skillengine.classes.TeleportNearBonfire;
import tera.gameserver.model.skillengine.classes.TeleportTown;
import tera.gameserver.model.skillengine.classes.Transform;
import tera.gameserver.model.skillengine.classes.Trigger;
import tera.gameserver.model.skillengine.classes.UnAggro;
import tera.gameserver.model.skillengine.classes.UpdateBonfire;
import tera.gameserver.model.skillengine.classes.WarriorFuryStrike;
import tera.gameserver.templates.SkillTemplate;

/**
 * Перечисление типов скилов.
 *
 * @author Ronn
 */
public enum SkillType
{
	/** аггр скил */
	AGGRO(Aggro.class),
	/** скил для очистки агро */
	UNAGGRO(UnAggro.class),

	/** скил для восстановления стамины */
	RESTORE_STAMINA(RestoreStamina.class),

	/** одиночный выстрел */
	SINGLE_SHOT(SingleShot.class),
	/** выстрел с авто навидением */
	AUTO_SINGLE_SHOT(AutoSingleShot.class),
	/** одиночный выстрел после зарядки */
	CHARHE_SINGLE_SHOT(ChargeSingleShot.class),
	/** одиночный выстрел с поглащением маны */
	MANA_SINGLE_SHOT(ManaSingleShot.class),
	/** одиночный выстрел от нпс */
	NPC_SINGLE_SHOT(NpcSingleShot.class),
	/** быстрый прямой выстрел */
	NPC_SINGLE_FAST_SHOT(NpcSingleFastShot.class),
	/** выстрел после зарядки с поглощением мп*/
	CHARGE_RAIL_FAST_MANA_SHOT(ChargeRailFastManaShot.class),

	/** ближний удар */
	STRIKE(Strike.class),
	/** ближний удар со стодиями */
	STAGE_STRIKE(StageStrike.class),
	/** ближний удар */
	CONTER_STRIKE(ConterStrike.class),
	/** ближний удар с fury уроном */
	SLAYER_FURY_STRIKE(SlayerFuryStrike.class),
	/** ближний удар с fury уроном */
	WARRIOR_FURY_STRIKE(WarriorFuryStrike.class),
	/** ближний удар с поглощением мп */
	MANA_STRIKE(ManaStrike.class),
	/** ближний удар с поглощением мп */
	MANA_GAIN_STRIKE(ManaGainStrike.class),
	/** комплексный удар состоящий из 2х стейтов */
	COMPLEX_STRIKE(ComplexStrike.class),
	/** комплексный удар состоящий из 2х стейтов */
	COMPLEX_MOD_STRIKE(ComplexModStrike.class),
	/** подготовленный удар */
	PREPARE_STRIKE(PrepareStrike.class),
	/** удар при опрокинутом состоянии */
	OWERTURNED_STRIKE(OwerturnedStrike.class),
	/** заржающаяся атака с отправкой пакетов во время промежуточных юзов скила */
	CHARGE_COMPLEX_STRIKE(ChargeComplexStrike.class),
	/** атака после зарядки */
	CHARGE_STRIKE(ChargeStrike.class),
	/** атака после зарядки с вампириком хп*/
	CHARGE_VAMP_STRIKE(ChargeVampStrike.class),

	/** удочка */
	LEASH(Leash.class),

	/** скил спавна сумона */
	SPAWN_SUMMON(SpawnSummon.class),
	/** сапавн теневого сумона */
	SPAWN_SMOKE_SUMMON(SpawnSmokeSummon.class),
	/** спавн ловушки */
	SPAWN_TRAP(SpawnTrap.class),

	/** атака сумоном */
	SUMMON_ATTACK(SummonAttack.class),
	/** отмена атаки сумона */
	SUMMON_ABORT(SummonAbort.class),

	/** заржающаяся атака */
	CHARGE(Charge.class),

	/** циклон берса */
	CYCLONE(Cyclone.class),

	/** восстановление мп после зарядки */
	CHARGE_MANA_HEAL(ChargeManaHeal.class),

	/** оборонитиельный */
	DEFENSE(Defense.class),
	/** оборонитиельный с поглощением мп при дефе */
	LANCER_DEFENSE(LancerDefense.class),

	/** лок он выделение */
	LOCK_ON(LockOn.class),
	/** только накладывание эффекта */
	LOCK_ON_EFFECT(LockOnEffect.class),
	/** обычная лок он атака */
	LOCK_ON_STRIKE(LockOnStrike.class),
	/** обычная лок он атака */
	LOCK_ON_HEAL(LockOnHeal.class),
	/** лок он удар с последующим бафом группы */
	LOCK_ON_STRIKE_PARTY_BUFF(LockOnStrikePartyBuff.class),

	/** поглощение хп целей */
	ABSORPTION_HP(AbsorptionHp.class),
	/** восстановление МП за счет поглощенного хп */
	MANA_HEAL_ON_ABSORPTION_HP(ManaHealOnAbsorptionHp.class),

	/** прижок */
	JUMP(Jump.class),
	/** создание итема */
	SPAWN_ITEM(SpawnItem.class),
	/** скил для хила */
	HEAL(Heal.class),
	/** процентный хил хп */
	HEAL_PERCENT(HealPercent.class),
	/** хил мп */
	MANAHEAL(ManaHeal.class),
	/** ступенчатый хил мп */
	PREPARE_MANAHEAL(PrepareManaHeal.class),
	/** процентный хил мп */
	MANAHEAL_PERCENT(ManaHealPercent.class),
	/** скил для наложения эффектов */
	EFFECT(Effect.class),
	/** баф скилом */
	BUFF(Buff.class),
	/** баф скилом */
	CHARM_BUFF(CharmBuff.class),
	/** дебаф скила */
	DEBUFF(Debuff.class),
	/** эффект */
	CHANCE_EFFECT(ChanceEffect.class),
	/** баф для итема */
	ITEM_BUFF(ItemBuff.class),
	/** пассивка */
	PASSIVE(Passive.class),
	/** объектный выстрел */
	SINGLE_SLOW_SHOT(SingleSlowShot.class),
	/** объектный выстрел после зарядки */
	CHARGE_SINGLE_SLOW_SHOT(ChargeSingleSlowShot.class),
	/** объектный выстрел с поглощением мп */
	MANA_SINGLE_SLOW_SHOT(SingleSlowShot.class),
	/** выстрел нпс слоу шотом */
	NPC_SINGLE_SLOW_SHOT(NpcSingleSlowShot.class),
	/**тригеры */
	TRIGGER(Trigger.class),
	/** спавн костров */
	SPAWN_BONFIRE(SpawnBonfire.class),
	/** спввн баф лорвушки */
	SPAWN_BUFF_TRAP(SpawnBuffTrap.class),
	/** обновление костров */
	RESTART_BONFIRE(UpdateBonfire.class),
	/** трансформация */
	TRANSFORM(Transform.class),
	/** скил залезания на маунта */
	MOUNT(Mount.class),
	/** скил для снития дебафов */
	CLEAR_DEBUFF(ClearDebuff.class),
	/** скил для снития бафов */
	CLEAR_BUFF(ClearBuff.class),
	/** скил для отмены опрокинутости цели */
	CANCEL_OWERTURN(CancelOwerturn.class),
	/** скил прерывания каста скила цели */
	CANCEL_CAST(CancelCast.class),
	/** суммон пати */
	PARTY_SUMMON(PartySummon.class),
	/** воскрешение */
	RESURRECT(Resurrect.class),
	/** локальный телепорт */
	TELEPORT_JUMP(TeleportJump.class),
	/** скил для телепортации в город */
	TELEPORT_TOWN(TeleportTown.class),
	/** телепорт к ближайшему костру */
	TELEPORT_NEAR_BONFIRE(TeleportNearBonfire.class),
	/** скил для входа в пвп режим */
	PVP_MODE(PvPMode.class);

	/** конструктор скила */
	private Constructor<? extends Skill> constructor;

	/**
	 * @param type класс скила
	 */
	private SkillType(Class<? extends Skill> type)
	{
		try
		{
			constructor = type.getConstructor(SkillTemplate.class);
		}
		catch(NoSuchMethodException | SecurityException e)
		{
			Loggers.warning(this, e);
		}
	}

	/**
	 * Создет новый экземпляр скила с соответствующим типом.
	 *
	 * @param template темплейт скила.
	 */
	public Skill newInstance(SkillTemplate template)
	{
		try
		{
			return constructor.newInstance(template);
		}
		catch(InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
		{
			Loggers.warning(this, e);
		}

		return null;
	}
}
