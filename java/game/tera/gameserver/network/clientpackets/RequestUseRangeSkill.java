package tera.gameserver.network.clientpackets;

import rlib.geom.Angles;
import tera.Config;
import tera.gameserver.model.Character;
import tera.gameserver.model.World;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.skillengine.Skill;

/**
 * Клиентский пакет с юзом дальнего скила.
 *
 * @author Ronn
 */
public final class RequestUseRangeSkill extends ClientPacket
{
	/** игрок */
	private Player player;

	/** скил ид */
	private int skillId;

	/** нужный разворот */
	private int heading;

	/** ид возможной цели */
	private int targetId;
	/** саб ид возможной цели */
	private int targeSubId;

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
	 * @return ид кастуемого скила.
	 */
	public int getSkillId()
	{
		return skillId;
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
	 * @return под ид цели.
	 */
	public int getTargeSubId()
	{
		return targeSubId;
	}

	/**
	 * @return ид цели.
	 */
	public int getTargetId()
	{
		return targetId;
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
		if(buffer.remaining() < 27)
			return;

		setPlayer(getOwner().getOwner());

		boolean isHit = readInt() != 0; //4

		readInt(); //8

		setSkillId(readInt()); //12

		setStartX(readFloat());
		setStartY(readFloat());
		setStartZ(readFloat());

		setHeading(readShort());

		readByte(); //27

		if(isHit && buffer.remaining() > 31)
		{
			readInt(); //4
			readInt(); //8

			setTargetId(readInt());
			setTargeSubId(readInt());

			readInt();

			setTargetX(readFloat());
			setTargetY(readFloat());
			setTargetZ(readFloat());
		}
		else if(buffer.remaining() > 15)
		{
			readInt();

			setTargetX(readFloat());
			setTargetY(readFloat());
			setTargetZ(readFloat());
		}
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

		// ссылка на возможную цель
		Character target = null;

		// если десинхрон превышает допустимый
		if(player.getSquareDistance(startX, startY) > Config.WORLD_MAX_SKILL_DESYNC)
		{
			startX = player.getX();
			startY = player.getY();
		}

		int targetId = getTargetId();
		int targetSubId = getTargeSubId();

		// если есть конкретная цель
		if(targetId > 0 && targetSubId == Config.SERVER_PLAYER_SUB_ID)
			// ищем ее
			target = World.getAroundById(Character.class, player, targetId, targetSubId);

		// запоминаем цель
		player.setTarget(target);

		float targetX = getTargetX();
		float targetY = getTargetY();
		float targetZ = getTargetZ();

		int heading = getHeading();

		// если цель есть
		if(target != null && skill.isCorrectableTarget())
		{
			// правим координаты выстрела
			targetX = target.getX();
			targetY = target.getY();
			targetZ = target.getZ() + target.getGeomHeight() * 0.5F;

			// правим направление
			heading = Angles.calcHeading(startX, startY, targetX, targetY);
		}

		// отправляем на обработку запрос каста
		player.getAI().startCast(startX, startY, startZ, skill, 0, heading, targetX, targetY, targetZ);
	}

	/**
	 * @param heading направление каста.
	 */
	public void setHeading(int heading)
	{
		this.heading = heading;
	}

	/**
	 * @param player игрок.
	 */
	public void setPlayer(Player player)
	{
		this.player = player;
	}

	/**
	 * @param skillId ид кастуемого скила.
	 */
	public void setSkillId(int skillId)
	{
		this.skillId = skillId;
	}

	/**
	 * @param startX стартовая координата.
	 */
	public void setStartX(float startX)
	{
		this.startX = startX;
	}

	/**
	 * @param startY стартовая координата.
	 */
	public void setStartY(float startY)
	{
		this.startY = startY;
	}

	/**
	 * @param startZ стартовая координата.
	 */
	public void setStartZ(float startZ)
	{
		this.startZ = startZ;
	}

	/**
	 * @param targeSubId под ид цели.
	 */
	public void setTargeSubId(int targeSubId)
	{
		this.targeSubId = targeSubId;
	}

	/**
	 * @param targetId ид цели.
	 */
	public void setTargetId(int targetId)
	{
		this.targetId = targetId;
	}

	/**
	 * @param targetX целевая координата.
	 */
	public void setTargetX(float targetX)
	{
		this.targetX = targetX;
	}

	/**
	 * @param targetY целевая координата.
	 */
	public void setTargetY(float targetY)
	{
		this.targetY = targetY;
	}

	/**
	 * @param targetZ целевая координата.
	 */
	public void setTargetZ(float targetZ)
	{
		this.targetZ = targetZ;
	}
}