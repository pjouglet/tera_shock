package tera.gameserver.model.npc;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.w3c.dom.Node;

import rlib.logging.Loggers;
import rlib.util.VarTable;
import tera.gameserver.model.ai.npc.ConfigAI;
import tera.gameserver.model.ai.npc.NpcAIClass;
import tera.gameserver.model.npc.playable.EventEpicBattleNpc;
import tera.gameserver.model.npc.playable.PlayerKiller;
import tera.gameserver.model.npc.spawn.BossSpawn;
import tera.gameserver.model.npc.spawn.NpcSpawn;
import tera.gameserver.model.npc.spawn.RegionWarSpawn;
import tera.gameserver.model.npc.spawn.Spawn;
import tera.gameserver.model.npc.summons.DefaultSummon;
import tera.gameserver.model.npc.summons.PlayerSummon;
import tera.gameserver.model.npc.summons.SmokeSummon;
import tera.gameserver.templates.NpcTemplate;
import tera.util.Location;

/**
 * Перечисление видов НПС.
 * 
 * @author Ronn
 */
public enum NpcType {
	/** --------------------------- OTHER NPC ------------------------- */

	/** дружественный нпс */
	FRIENDLY(FriendNpc.class),
	/** гвард */
	GUARD(Guard.class),
	/** боевой гвард */
	BATTLE_GUARD(BattleGuard.class),
	/** ивентовый монстер */
	EVENT_MONSTER(EventMonster.class),
	/** нпс для эпичной битвы */
	EPIC_BATTLE_NPC(EventEpicBattleNpc.class),
	/** НПС объект */
	NPC_OBJECT(NpcObject.class),

	/** --------------------------- MONSTERS ------------------------- */

	/** обычный монстр */
	MONSTER(Monster.class),
	/** социальный монстр */
	SOCIAL_MONSTER(SocialMonster.class),
	/** элитный монстр */
	ELITE(EliteMonster.class),
	/** рейд босс */
	RAID_BOSS(RaidBoss.class),
	/** минион */
	MINION(Minion.class),
	/** лидер минионов */
	MINION_LEADER(MinionLeader.class),

	/** --------------------------- REGION NPC ------------------------- */

	/** НПС контрола региона в битве */
	REGION_WAR_CONTROL(RegionWarControl.class),
	/** НПС для продажи чего-то в регионе */
	REGION_WAR_SHOP(RegionWarShop.class),
	/** НПС защитник региона в битве */
	REGION_WAR_DEFENSE(RegionWarDefense.class),
	/** НПС баррьер */
	REGION_WAR_BARRIER(RegionWarBarrier.class),

	/** --------------------------- PLAYABLE NPC ------------------------- */
	/** убийца игроков */
	PLAYER_KILLER(PlayerKiller.class),

	/** --------------------------- SUMMONS ------------------------- */

	/** стандартная модель суммона */
	DEFAULT_SUMMON(DefaultSummon.class),
	/** игрокоподобный суммон */
	PLAYER_SUMMON(PlayerSummon.class),
	/** реализация тени */
	SMOKE_SUMMON(SmokeSummon.class), ;

	/** кноструктор НПС */
	private Constructor<? extends Npc> constructor;

	private NpcType(Class<? extends Npc> type) {
		try {
			this.constructor = type.getConstructor(int.class, NpcTemplate.class);
		} catch(NoSuchMethodException | SecurityException e) {
			Loggers.warning(this, e);
		}
	}

	/**
	 * Создание нового экземпляра НПС.
	 * 
	 * @param objectId уникальный ид нпс.
	 * @param template шаблон нпс.
	 * @return новый нпс.
	 */
	public Npc newInstance(int objectId, NpcTemplate template) {
		try {
			return constructor.newInstance(objectId, template);
		} catch(InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Создание спавна НПс.
	 * 
	 * @param template шаблон НПС.
	 * @param location позиция спавна.
	 * @param respawn время респа.
	 * @param random случайная часть респа.
	 * @param minRadius минимальный радиус спавна от точки.
	 * @param maxRadius максимальный радиус спавна от точки.
	 * @param config конфиг АИ.
	 * @param aiClass исполняющий класс АИ.
	 * @return новый спавн.
	 */
	public Spawn newSpawn(Node node, VarTable vars, NpcTemplate template, Location location, int respawn, int random, int minRadius, int maxRadius, ConfigAI config, NpcAIClass aiClass) {
		switch(this) {
			case RAID_BOSS:
				return new BossSpawn(node, vars, template, location, respawn, random, minRadius, maxRadius, config, aiClass);
			case REGION_WAR_SHOP:
			case REGION_WAR_DEFENSE:
			case REGION_WAR_BARRIER:
			case REGION_WAR_CONTROL:
				return new RegionWarSpawn(node, vars, template, location, respawn, random, minRadius, maxRadius, config, aiClass);
			default:
				return new NpcSpawn(node, vars, template, location, respawn, random, minRadius, maxRadius, config, aiClass);
		}
	}
}
