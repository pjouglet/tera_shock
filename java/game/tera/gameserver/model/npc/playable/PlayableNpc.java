package tera.gameserver.model.npc.playable;

import rlib.util.SafeTask;
import rlib.util.Strings;
import rlib.util.VarTable;

import tera.gameserver.manager.ExecutorManager;
import tera.gameserver.model.Character;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.serverpackets.CharDead;
import tera.gameserver.network.serverpackets.DeleteCharacter;
import tera.gameserver.network.serverpackets.NpcPlayableInfo;
import tera.gameserver.network.serverpackets.PlayerBattleStance;
import tera.gameserver.tables.NpcAppearanceTable;
import tera.gameserver.templates.NpcTemplate;

/**
 * Модель игрокоподобного НПС.
 *
 * @author Ronn
 */
public class PlayableNpc extends Npc
{
	/** задача по удалению трупа */
	private final SafeTask deleteTask;

	/** внешность НПС */
	private NpcAppearance appearance;

	public PlayableNpc(int objectId, NpcTemplate template)
	{
		super(objectId, template);

		VarTable vars = template.getVars();

		NpcAppearanceTable appearanceTable = NpcAppearanceTable.getInstance();

		setAppearance(appearanceTable.getAppearance(vars.getString("appearance")));
		setTitle(vars.getString("title", Strings.EMPTY));

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
	public NpcAppearance getAppearance()
	{
		return appearance;
	}

	/**
	 * @param appearance внешность НПС.
	 */
	public void setAppearance(NpcAppearance appearance)
	{
		if(appearance == null)
			throw new IllegalArgumentException("not found appearance.");

		this.appearance = appearance;
	}

	@Override
	public void addMe(Player player)
	{
		player.sendPacket(NpcPlayableInfo.getInstance(this), true);
	}

	@Override
	public int getModelId()
	{
		return getTemplate().getIconId();
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

		// отправляем пакет о смерти
		broadcastPacket(CharDead.getInstance(this, true));

		// запускаем задание по удалению
		ExecutorManager.getInstance().scheduleGeneral(deleteTask, 10000);
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

	@Override
	public boolean isBroadcastEndSkillForCollision()
	{
		return false;
	}
}
