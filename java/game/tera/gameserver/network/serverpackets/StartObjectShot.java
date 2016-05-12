package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;

import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.Skill;
import tera.gameserver.model.skillengine.shots.Shot;
import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет для отображения выстрела.
 *
 * @author Ronn
 */
public class StartObjectShot extends ServerPacket
{
	private static final ServerPacket instance = new StartObjectShot();

	public static StartObjectShot getInstance(Character caster, Skill skill, Shot shot)
	{
		StartObjectShot packet = (StartObjectShot) instance.newInstance();

		packet.casterId = caster.getObjectId();
		packet.casterSubId = caster.getSubId();
		packet.casterTemplateId = caster.getModelId();
		packet.startX = caster.getX();
		packet.startY = caster.getY();
		packet.startZ = caster.getZ() + caster.getGeomHeight() - 20;
		packet.damageId = skill.getDamageId();
		packet.speed = skill.getSpeed() + skill.getSpeedOffset();
		packet.objectId = shot.getObjectId();
		packet.subId = shot.getSubId();
		packet.targetX = shot.getTargetX();
		packet.targetY = shot.getTargetY();
		packet.targetZ = shot.getTargetZ();
		packet.casterTemplateType = caster.getTemplateType();

		return packet;
	}

	/** ид кастующего */
	private int casterId;
	/** саб ид кастующего */
	private int casterSubId;
	/** темплейт кастующего */
	private int casterTemplateId;
	/** тип темплейта кастующего */
	private int casterTemplateType;

	/** ид выстрела */
	private int objectId;
	/** тип выстрела */
	private int subId;
	/** скорость выстрела */
	private int speed;
	/** ид урона */
	private int damageId;

	/** стартовые координаты */
	private float startX;
	private float startY;
	private float startZ;

	/** конечные координаты */
	private float targetX;
	private float targetY;
	private float targetZ;

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.SKILL_NPC_SLOW_SHOT;
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

		writeInt(buffer, objectId);//BC 11 06 00 ид
		writeInt(buffer, subId);//00 80 0D 00 саб ид
		writeInt(buffer, casterTemplateType);//00 00 00 00
		writeInt(buffer, damageId);//08 60 01 04
		writeFloat(buffer, startX);//61 5F 05 C7
		writeFloat(buffer, startY);//1D BB 94 C6
		writeFloat(buffer, startZ);//76 7C 40 43
		writeFloat(buffer, targetX);//61 5F 05 C7
		writeFloat(buffer, targetY);//1D BB 94 C6
		writeFloat(buffer, targetZ);//76 7C 40 43
		writeByte(buffer, 1);//00  у трапа ноль и лтящей хрени 1
		writeByte(buffer, 0);//00
		writeByte(buffer, 00);//00
		writeShort(buffer, 17048 + speed);
		writeInt(buffer, casterId);//CD 03 0D 00 ид хозяина
		writeInt(buffer, casterSubId);//00 80 00 01 саб ид хозяин
		writeInt(buffer, casterTemplateId);//0A 29 00 00  ид темлейта
	}
}