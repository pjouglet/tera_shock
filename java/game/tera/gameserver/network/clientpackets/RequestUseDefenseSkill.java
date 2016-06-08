package tera.gameserver.network.clientpackets;

import tera.Config;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.skillengine.Skill;

/**
 * Запрос на юз деф скила.
 *
 * @author Ronn
 */
public class RequestUseDefenseSkill extends ClientPacket
{
	/** включить деф */
	public static final int DEFENSE_START = 1;
	/** выключить деф */
	public static final int DEFENSE_END = 0;

	/** игрок */
	private Player player;

	/** скил ид игрока */
	private int skillId;

	/** режим */
	private int state;

	/** направление */
	private int heading;

	/** точка каста скила */
	private float startX;
	private float startY;
	private float startZ;

	@Override
	public void finalyze()
	{
		player = null;
	}

	/**
	 * @return игрок.
	 */
	public final Player getPlayer()
	{
		return player;
	}

	public void setPlayer(Player player)
	{
		this.player = player;
	}

	@Override
	public void readImpl()
	{
		if(buffer.remaining() < 19)
			return;

		setPlayer(getOwner().getOwner());

		setSkillId(readInt());

		setState(readByte());

		setStartX(readFloat());
		setStartY(readFloat());
		setStartZ(readFloat());

		setHeading(readShort());
	}

	@Override
	public void runImpl()
	{
		// получаем игрока
		Player player = getPlayer();

		// если его нет, выходим
		if(player == null)
			return;

		// пробуем получить скил игрока
		Skill skill = player.getSkill(getSkillId());

		// если его у него нету
		if(skill == null)
		{
			// сообщаем и выходим
			player.sendMessage("You don't have this skill (maybe update ?)");
			return;
		}

		float startX = getStartX();
		float startY = getStartY();
		float startZ = getStartZ();

		if(player.getSquareDistance(startX, startY) > Config.WORLD_MAX_SKILL_DESYNC)
		{
			startX = player.getX();
			startY = player.getY();
		}

		player.getAI().startCast(startX, startY, startZ, skill, getState(), getHeading(), player.getX(), player.getY(), player.getZ());
	}

	@Override
	public String toString()
	{
		return "RequestUseDefenseSkill state = " + state;
	}

	public int getSkillId()
	{
		return skillId;
	}

	public int getHeading()
	{
		return heading;
	}

	public float getStartX()
	{
		return startX;
	}

	public float getStartY()
	{
		return startY;
	}

	public float getStartZ()
	{
		return startZ;
	}

	public int getState()
	{
		return state;
	}

	public void setHeading(int heading)
	{
		this.heading = heading;
	}

	public void setSkillId(int skillId)
	{
		this.skillId = skillId;
	}

	public void setStartX(float startX)
	{
		this.startX = startX;
	}

	public void setStartY(float startY)
	{
		this.startY = startY;
	}

	public void setStartZ(float startZ)
	{
		this.startZ = startZ;
	}

	public void setState(int state)
	{
		this.state = state;
	}
}