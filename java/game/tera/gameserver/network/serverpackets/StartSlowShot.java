package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;

import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.Skill;
import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет для отображения выстрела.
 *
 * @author Ronn
 */
public class StartSlowShot extends ServerPacket
{
	private static final ServerPacket instance = new StartSlowShot();

	public static StartSlowShot getInstance(Character caster, Skill skill, int objectId, int subId, float targetX, float targetY, float targetZ)
	{
		StartSlowShot packet = (StartSlowShot) instance.newInstance();

		packet.caster = caster;
		packet.skill = skill;
		packet.objectId = objectId;
		packet.subId = subId;
		packet.targetX = targetX;
		packet.targetY = targetY;
		packet.targetZ = targetZ;

		return packet;
	}

	/** кастер */
	private Character caster;

	/** скил, откуда вылетел */
	private Skill skill;

	/** ид выстрела */
	private int objectId;
	/** тип выстрела */
	private int subId;

	/** цель выстрела */
	private float targetX;
	private float targetY;
	private float targetZ;

	@Override
	public void finalyze()
	{
		caster = null;
		skill = null;
	}

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.SKILL_SLOW_SHOT;
	}

	@Override
	public boolean isSynchronized()
	{
		return false;
	}

	@Override
	protected void writeImpl(ByteBuffer buffer)
	{
		writeOpcode(buffer);
		writeInt(buffer, caster.getObjectId()); // кастер обжект айди
		writeInt(buffer, caster.getSubId()); // кастер сабайди
		writeInt(buffer, caster.getModelId());
		writeInt(buffer, 0);
		writeInt(buffer, objectId); // обжект айди шарика
		writeInt(buffer, subId); // саб айди шарика
		writeInt(buffer, skill.getDamageId()); // код скила damageId из xml
		writeFloat(buffer, caster.getX()); // шарик откуда летит X
		writeFloat(buffer, caster.getY()); // шарик откуда летит Y
		writeFloat(buffer, caster.getZ() + caster.getGeom().getHeight() - 5F); // шарик откуда летит Z
		writeFloat(buffer, targetX); // шарик куда летит X
		writeFloat(buffer, targetY); // шарик куда летит X
		writeFloat(buffer, targetZ); // шарик куда летит X
		writeShort(buffer, 0x8000);
		writeShort(buffer, 17048 + skill.getSpeed() + skill.getSpeedOffset());
	}
}