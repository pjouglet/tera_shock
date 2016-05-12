package tera.gameserver.model.npc.summons;

import java.util.concurrent.ScheduledFuture;

import rlib.util.SafeTask;
import rlib.util.VarTable;
import tera.gameserver.manager.ExecutorManager;
import tera.gameserver.model.Character;
import tera.gameserver.model.EmotionType;
import tera.gameserver.model.npc.playable.NpcAppearance;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.serverpackets.CharDead;
import tera.gameserver.network.serverpackets.DeleteCharacter;
import tera.gameserver.network.serverpackets.NpcPlayableInfo;
import tera.gameserver.network.serverpackets.PlayerBattleStance;
import tera.gameserver.tables.NpcAppearanceTable;
import tera.gameserver.tasks.EmotionTask;
import tera.gameserver.templates.NpcTemplate;

/**
 * Базовая модель игрокоподобного суммона.
 * 
 * @author Ronn
 */
public class PlayableSummon extends Summon
{
	public static final int DEAD_TIME = 5000;

	/** задача по удалению трупа */
	private final SafeTask deleteTask;

	/** ссылка на задачу удаления */
	private volatile ScheduledFuture<SafeTask> schedule;

	/** внешность НПС */
	private NpcAppearance appearance;

	public PlayableSummon(int objectId, NpcTemplate template)
	{
		super(objectId, template);

		VarTable vars = template.getVars();

		NpcAppearanceTable appearanceTable = NpcAppearanceTable.getInstance();

		setAppearance(appearanceTable.getAppearance(vars.getString("appearance")));

		this.deleteTask = new SafeTask()
		{
			@Override
			protected void runImpl()
			{
				deleteMe(DeleteCharacter.DISAPPEARS);
			}
		};
	}

	@Override
	public void setOwner(Character owner)
	{
		super.setOwner(owner);

		if(owner != null)
			setTitle(owner.getName());
	}

	@Override
	public NpcAppearance getAppearance()
	{
		return appearance;
	}

	/**
	 * @param appearance внешность суммона..
	 */
	public void setAppearance(NpcAppearance appearance)
	{
		if(appearance == null)
			throw new IllegalArgumentException("not found appearance.");

		this.appearance = appearance;
	}

	@Override
	public int getModelId()
	{
		return getTemplate().getIconId();
	}

	@Override
	public void addMe(Player player)
	{
		player.sendPacket(NpcPlayableInfo.getInstance(this), true);
	}

	@Override
	public boolean startBattleStance(Character enemy)
	{
		if(enemy == null)
		{
			if(!isBattleStanced())
				return false;

			broadcastPacket(PlayerBattleStance.getInstance(this, PlayerBattleStance.STANCE_OFF));

			setBattleStanced(false);

			return true;
		}
		else
		{
			if(isBattleStanced())
				return false;

			broadcastPacket(PlayerBattleStance.getInstance(this, PlayerBattleStance.STANCE_ON));

			setBattleStanced(true);

			return true;
		}
	}

	@Override
	public void stopBattleStance()
	{
		broadcastPacket(PlayerBattleStance.getInstance(this, PlayerBattleStance.STANCE_OFF));
		setBattleStanced(false);
	}

	@Override
	public void doDie(Character attacker)
	{
		super.doDie(attacker);

		broadcastPacket(CharDead.getInstance(this, true));

		ExecutorManager executorManager = ExecutorManager.getInstance();
		setSchedule(executorManager.scheduleGeneral(deleteTask, DEAD_TIME));
	}

	@Override
	public void deleteMe(int type)
	{
		if(type == DeleteCharacter.DEAD)
			return;

		super.deleteMe(type);
	}

	@Override
	public int getOwerturnId()
	{
		return 0x080F6C72;
	}

	@Override
	public boolean isOwerturnImmunity()
	{
		return false;
	}

	@Override
	public int getTemplateType()
	{
		return 0;
	}

	/**
	 * @param schedule ссылка на задачу удаления сумона.
	 */
	public void setSchedule(ScheduledFuture<SafeTask> schedule)
	{
		this.schedule = schedule;
	}

	/**
	 * @return ссылка на задачу удаления сумона.
	 */
	public ScheduledFuture<SafeTask> getSchedule()
	{
		return schedule;
	}

	@Override
	public void finishDead()
	{
		boolean isRun = false;

		// получаем ссылку на задачу удаления
		ScheduledFuture<SafeTask> schedule = getSchedule();

		if(schedule != null)
			synchronized(this)
			{
				schedule = getSchedule();

				// если такая в итоге есть
				if(schedule != null)
				{
					// завершаем ее
					schedule.cancel(false);
					// зануляем ссылку
					setSchedule(null);
					// ставим флаг исполеия
					isRun = true;
				}
			}

		// если надо исполнить
		if(isRun)
			// исполняем
			deleteTask.run();
	}

	@Override
	public boolean isBroadcastEndSkillForCollision()
	{
		return false;
	}

	@Override
	protected EmotionType[] getAutoEmotions()
	{
		return EmotionTask.PLAYER_TYPES;
	}

	@Override
	public int getOwerturnTime()
	{
		return 3000;
	}
}
