package tera.gameserver.model.ai.npc;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import tera.gameserver.model.ai.npc.classes.BattleGuardAI;
import tera.gameserver.model.ai.npc.classes.DefaultNpcAI;
import tera.gameserver.model.ai.npc.classes.DefaultSummonAI;
import tera.gameserver.model.ai.npc.classes.EpicBattleAI;
import tera.gameserver.model.ai.npc.classes.EventMonsterAI;
import tera.gameserver.model.ai.npc.classes.RegionWarDefenseAI;
import tera.gameserver.model.npc.BattleGuard;
import tera.gameserver.model.npc.EventMonster;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.npc.RegionWarDefense;
import tera.gameserver.model.npc.playable.EventEpicBattleNpc;
import tera.gameserver.model.npc.summons.Summon;

/**
 * Перечисление классов АИ.
 *
 * @author Ronn
 */
public enum NpcAIClass
{
	/** АИ монстра для ивента */
	EVENT_MOSTER(EventMonsterAI.class, EventMonster.class),
	/** АИ боевого гварда */
	BATTLE_GUARD(BattleGuardAI.class, BattleGuard.class),
	/** АИ для защитника региона в битве */
	REGION_WAR_DEFENSE(RegionWarDefenseAI.class, RegionWarDefense.class),
	/** АИ для ивента Эпичных Боев */
	EPIC_BATTLE_EVENT(EpicBattleAI.class, EventEpicBattleNpc.class),
	/** базовое АИ суммона */
	DEFAULT_SUMMON(DefaultSummonAI.class, Summon.class),
	/** АИ по умолчанию */
	DEFAULT(DefaultNpcAI.class, Npc.class);

	/** конструктор АИ */
	private Constructor<? extends NpcAI<? extends Npc>> constructor;

	private NpcAIClass(Class<? extends NpcAI<?>> type, Class<? extends Npc> actorType)
	{
		try
		{
			this.constructor = (Constructor<? extends NpcAI<?>>) type.getConstructor(actorType, ConfigAI.class);
		}
		catch(NoSuchMethodException | SecurityException e)
		{
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Создание нового инстанса АИ.
	 *
	 * @param npc нпс, для которого создается АИ.
	 * @param config конфиг для АИ.
	 * @return новый инстанс АИ.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Npc> NpcAI<T> newInstance(T npc, ConfigAI config)
	{
		try
		{
			return (NpcAI<T>) constructor.newInstance(npc, config);
		}
		catch(InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
		{
			throw new IllegalArgumentException(e);
		}
	}
}
