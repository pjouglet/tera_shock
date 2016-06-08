package tera.gameserver.network.clientpackets;

import rlib.geom.Angles;
import tera.Config;
import tera.gameserver.model.Character;
import tera.gameserver.model.World;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.skillengine.Skill;

/**
 * Клиентский пакет старта мили скила.
 * 
 * @author Ronn
 */
public class RequestUseShortSkill extends ClientPacket
{
	/** игрок */
	private Player player;

	/** скил ид */
	private int skillId;
	/** нужный разворот */
	private int heading;
	/** ид цели */
	private int targetId;
	/** саб ид цели */
	private int targetSubId;

	/** целевые координаты */
	private float targetX;
	private float targetY;
	private float targetZ;

	/** точка каста скила */
	private float startX;
	private float startY;
	private float startZ;

	@Override
	public void finalyze()
	{
		setPlayer(null);
		setTargetId(0);
	}

	/**
	 * @return направление каста.
	 */
	public int getHeading()
	{
		return heading;
	}

	/**
	 * @return игрок.
	 */
	public final Player getPlayer()
	{
		return player;
	}

	/**
	 * @return стартовая координата.
	 */
	public float getStartX()
	{
		return startX;
	}

	/**
	 * @return стартовая координата.
	 */
	public float getStartY()
	{
		return startY;
	}

	/**
	 * @return стартовая координата.
	 */
	public float getStartZ()
	{
		return startZ;
	}

	/**
	 * @return уникальный ид цели.
	 */
	public int getTargetId()
	{
		return targetId;
	}

	/**
	 * @return уникальный под ид цели.
	 */
	public int getTargetSubId()
	{
		return targetSubId;
	}

	/**
	 * @return целевая координата.
	 */
	public float getTargetX()
	{
		return targetX;
	}

	/**
	 * @return целевая координата.
	 */
	public float getTargetY()
	{
		return targetY;
	}

	/**
	 * @return целевая координата.
	 */
	public float getTargetZ()
	{
		return targetZ;
	}

	@Override
	public void readImpl()
	{
		if(buffer.remaining() < 40)
			return;

		setPlayer(getOwner().getOwner());

		setSkillId(readInt());
		setHeading(readShort());

		setStartX(readFloat());
		setStartY(readFloat());
		setStartZ(readFloat());

		setTargetX(readFloat());
		setTargetY(readFloat());
		setTargetZ(readFloat());

		readShort();
		readByte();

		setTargetId(readInt());
		setTargetSubId(readInt());
	}

	@Override
	public void runImpl()
	{
		Player player = getPlayer();

		if(player == null)
			return;

		Skill skill = player.getSkill(skillId);

		if(skill == null)
		{
			player.sendMessage("You don't have this skill (maybe update ?)");
			return;
		}

		float startX = getStartX();
		float startY = getStartY();
		float startZ = getStartZ();

		Character target = null;

		if(player.getSquareDistance(startX, startY) > Config.WORLD_MAX_SKILL_DESYNC)
		{
			startX = player.getX();
			startY = player.getY();
		}

		int targetId = getTargetId();
		int targetSubId = getTargetSubId();

		if(targetId > 0)
			target = World.getAroundById(Character.class, player, targetId, targetSubId);

		player.setTarget(target);

		float targetX = getTargetX();
		float targetY = getTargetY();
		float targetZ = getTargetZ();

		int heading = getHeading();

		if(target != null && skill.isCorrectableTarget() && targetSubId == Config.SERVER_PLAYER_SUB_ID)
		{
			targetX = target.getX();
			targetY = target.getY();
			targetZ = target.getZ() + target.getGeomHeight() * 0.5F;

			heading = Angles.calcHeading(startX, startY, targetX, targetY);
		}

		player.getAI().startCast(startX, startY, startZ, skill, 0, heading, targetX, targetY, targetZ);
	}

	/**
	 * @param heading направление каста.
	 */
	public void setHeading(int heading)
	{
		this.heading = heading;
	}

	public void setPlayer(Player player)
	{
		this.player = player;
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

	public void setTargetId(int targetId)
	{
		this.targetId = targetId;
	}

	public void setTargetSubId(int targetSubId)
	{
		this.targetSubId = targetSubId;
	}

	public void setTargetX(float targetX)
	{
		this.targetX = targetX;
	}

	public void setTargetY(float targetY)
	{
		this.targetY = targetY;
	}

	public void setTargetZ(float targetZ)
	{
		this.targetZ = targetZ;
	}
}