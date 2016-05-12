package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;

import tera.gameserver.model.Character;
import tera.gameserver.network.ServerPacketType;
import tera.util.Location;

/**
 * Серверный пакет уведомляющий о старте каста скила
 *
 * @author Ronn
 * @created 31.03.2012
 */
public class SkillStart extends ServerPacket
{
	private static final ServerPacket instance = new SkillStart();

	public static SkillStart getInstance(Character caster, int skillId, int castId, int state)
	{
		SkillStart packet = (SkillStart) instance.newInstance();

		if(caster == null)
			log.warning(packet, new Exception("not found caster"));

		packet.casterId = caster.getObjectId();
		packet.casterSubId = caster.getSubId();
		packet.modelId = caster.getModelId();
		packet.atkSpd = caster.getAtkSpd() / 100F;
		packet.skillId = skillId;
		packet.state = state;
		packet.castId = castId;

		caster.getLoc(packet.loc);

		return packet;
	}

	/** позиция кастующего */
	private final Location loc;

	/** ид кастующего */
	private int casterId;
	/** под ид кастующего */
	private int casterSubId;
	/** ид модели кастующего */
	private int modelId;

	/** ид скила */
	private int skillId;
	/** состояние скила */
	private int state;
	/** ид каста */
	private int castId;

	/** скорость атаки кастующего */
	private float atkSpd;

	public SkillStart()
	{
		super();

		this.loc = new Location();
	}

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.SKILL_START;
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

		writeInt(buffer, 0);

		writeInt(buffer, casterId);
		writeInt(buffer, casterSubId);

		writeFloat(buffer, loc.getX());
		writeFloat(buffer, loc.getY());
		writeFloat(buffer, loc.getZ());
		writeShort(buffer, loc.getHeading());

		writeInt(buffer, modelId);

		writeInt(buffer, skillId);

		writeInt(buffer, state);

		writeFloat(buffer, atkSpd);

		/*writeShort(buffer, 0);
		writeByte(buffer, atkSpd);
		writeByte(buffer, 0x3F);*/

		writeInt(buffer, castId);

		/*
		 *  writeUid(skillProcessor.creature)
		 *
    writeF(skillProcessor.args.startPosition.x)
    writeF(skillProcessor.args.startPosition.y)
    writeF(skillProcessor.args.startPosition.z)
    writeH(skillProcessor.args.startPosition.heading)

    writeD(skillProcessor.creature.templateId)

    writeD(skillProcessor.args.skillId + 0x4000000)

    writeD(skillProcessor.stage)

    writeF(skillProcessor.speed)
    writeD(skillProcessor.uid)
		 */

		/*if(true)
		{
			writeInt(buffer, 0); // 11 57 30 04 тут бывает какойто скилл ид
			writeInt(buffer, 50);
			writeInt(buffer, 200); // 200-2100 у однотипных мобов вроде одинакого
			writeInt(buffer, 0x6666a63F);// бывает ноль и разные цифры
			writeShort(buffer, 0);
			writeByte(buffer, atkSpd);
			writeByte(buffer, 0x3F);
		}*/
	}
}