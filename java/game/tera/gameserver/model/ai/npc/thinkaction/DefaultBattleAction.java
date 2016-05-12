package tera.gameserver.model.ai.npc.thinkaction;

import org.w3c.dom.Node;

import rlib.util.Rnd;
import rlib.util.VarTable;
import rlib.util.array.Array;
import tera.gameserver.manager.PacketManager;
import tera.gameserver.model.Character;
import tera.gameserver.model.NpcAIState;
import tera.gameserver.model.World;
import tera.gameserver.model.WorldRegion;
import tera.gameserver.model.ai.npc.ConfigAI;
import tera.gameserver.model.ai.npc.MessagePackage;
import tera.gameserver.model.ai.npc.NpcAI;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.network.serverpackets.NotifyCharacter.NotifyType;
import tera.gameserver.tables.MessagePackageTable;
import tera.util.LocalObjects;

/**
 * Базовая модель генератора действий в бою.
 * 
 * @author Ronn
 */
public class DefaultBattleAction extends AbstractThinkAction {

	/** пакет сообщений при смене цели боя */
	protected final MessagePackage switchTargetMessages;

	/** радиус ведения боя */
	protected final int battleMaxRange;
	/** радиус реакции НПС на врагов */
	protected final int reactionMaxRange;
	/** уровень критического хп */
	protected final int criticalHp;
	/** шанс перехода в ярость */
	protected final int rearRate;
	/** шанс перехода в убегание */
	protected final int runAwayRate;
	/** максимум для скольких нпс цель может быть топ агром */
	protected final int maxMostHated;

	public DefaultBattleAction(Node node) {
		super(node);

		try {
			// парсим параметры
			VarTable vars = VarTable.newInstance(node, "set", "name", "val");

			this.battleMaxRange = vars.getInteger("battleMaxRange", ConfigAI.DEFAULT_BATTLE_MAX_RANGE);
			this.reactionMaxRange = vars.getInteger("reactionMaxRange", ConfigAI.DEFAULT_REACTION_MAX_RANGE);

			this.criticalHp = vars.getInteger("criticalHp", ConfigAI.DEFAULT_CRITICAL_HP);
			this.rearRate = vars.getInteger("rearRate", ConfigAI.DEFAULT_REAR_RATE);
			this.runAwayRate = vars.getInteger("runAwayRate", ConfigAI.DEFAULT_RUN_AWAY_RATE);
			this.maxMostHated = vars.getInteger("maxMostHated", ConfigAI.DEFAULT_MAX_MOST_HATED);

			// получаем таблицу сообщений
			MessagePackageTable messageTable = MessagePackageTable.getInstance();

			this.switchTargetMessages = messageTable.getPackage(vars.getString("switchTargetMessages", ConfigAI.DEFAULT_SWITCH_TARGET_MESSAGES));
		} catch(Exception e) {
			log.warning(this, e);
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * @return радиус ведения боя.
	 */
	protected final int getBattleMaxRange() {
		return battleMaxRange;
	}

	/**
	 * @return % хп, который является критическим.
	 */
	protected final int getCriticalHp() {
		return criticalHp;
	}

	/**
	 * @return максимальное число топ агров для цели.
	 */
	protected final int getMaxMostHated() {
		return maxMostHated;
	}

	/**
	 * @return радиус реакции НПС на врагов.
	 */
	protected final int getReactionMaxRange() {
		return reactionMaxRange;
	}

	/**
	 * @return шанс входа в состояние ярости.
	 */
	protected final int getRearRate() {
		return rearRate;
	}

	/**
	 * @return шанс входа в состояние убегания.
	 */
	protected final int getRunAwayRate() {
		return runAwayRate;
	}

	@Override
	public <A extends Npc> void think(NpcAI<A> ai, A actor, LocalObjects local, ConfigAI config, long currentTime) {

		if(actor.isDead()) {
			ai.clearTaskList();
			actor.clearAggroList();
			ai.setNewState(NpcAIState.WAIT);
			return;
		}

		if(actor.isTurner() || actor.isCastingNow()) {
			return;
		}

		if(actor.isStuned() || actor.isOwerturned()) {

			if(ai.isWaitingTask()) {
				ai.clearTaskList();
			}

			return;
		}

		if(!actor.isInRangeZ(actor.getSpawnLoc(), getReactionMaxRange())) {
			ai.clearTaskList();
			actor.clearAggroList();
			ai.setNewState(NpcAIState.RETURN_TO_HOME);
			PacketManager.showNotifyIcon(actor, NotifyType.NOTICE_THINK);
			ai.setLastNotifyIcon(currentTime);
			return;
		}

		Character mostHated = actor.getMostHated();

		if(mostHated == null && actor.isAggressive()) {

			WorldRegion region = actor.getCurrentRegion();

			if(region != null) {

				Array<Character> charList = local.getNextCharList();

				World.getAround(Character.class, charList, actor, actor.getAggroRange());

				Character[] array = charList.array();

				for(int i = 0, length = charList.size(); i < length; i++) {

					Character target = array[i];

					if(ai.checkAggression(target)) {
						actor.addAggro(target, 1, false);
					}
				}
			}
		}

		mostHated = actor.getMostHated();

		if(mostHated == null) {
			ai.clearTaskList();
			actor.clearAggroList();
			ai.setNewState(NpcAIState.RETURN_TO_HOME);
			PacketManager.showNotifyIcon(actor, NotifyType.NOTICE_THINK);
			ai.setLastNotifyIcon(currentTime);
			return;
		}

		Character target = ai.getTarget();

		if(mostHated.isDead() || !mostHated.isInRange(actor.getSpawnLoc(), getBattleMaxRange())) {
			actor.removeAggro(mostHated);
			ai.clearTaskList();
			return;
		}

		if(actor.getCurrentHpPercent() < getCriticalHp()) {

			int rate = Rnd.nextInt(0, 100000);

			if(rate < getRearRate()) {
				ai.clearTaskList();
				ai.setNewState(NpcAIState.IN_RAGE);
				PacketManager.showNotifyIcon(actor, NotifyType.READ_REAR);
				ai.setLastNotifyIcon(currentTime);
				return;
			}

			// if(rate < getRunAwayRate()) {
			// ai.clearTaskList();
			// ai.setNewState(NpcAIState.IN_RUN_AWAY);
			// return;
			// }
		}

		if(mostHated != target) {
			ai.setTarget(mostHated);
		}

		if(ai.isWaitingTask()) {
			if(ai.doTask(actor, currentTime, local)) {
				return;
			}
		}

		if(actor.isTurner() || actor.isCastingNow() || actor.isStuned() || actor.isOwerturned() || actor.isMoving()) {
			return;
		}

		if(currentTime - ai.getLastNotifyIcon() > 15000) {
			PacketManager.showNotifyIcon(actor, NotifyType.YELLOW_QUESTION);
			ai.setLastNotifyIcon(currentTime);
		}

		ai.getCurrentFactory().addNewTask(ai, actor, local, config, currentTime);

		if(ai.isWaitingTask()) {
			ai.doTask(actor, currentTime, local);
		}
	}
}
