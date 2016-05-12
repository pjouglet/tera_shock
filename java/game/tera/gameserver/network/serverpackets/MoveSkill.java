package tera.gameserver.network.serverpackets;

import tera.gameserver.model.Character;
import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет уведомляющий о старте бегущего скила
 *
 * @author Ronn
 * @created 31.03.2012
 */
public class MoveSkill extends ServerPacket
{
	private static final ServerPacket instance = new MoveSkill();

	public static MoveSkill getInstance(Character caster, Character target)
	{
		MoveSkill packet = (MoveSkill) instance.newInstance();

		packet.caster = caster;
		packet.target = target;

		return packet;
	}

	public static MoveSkill getInstance(Character caster, float targetX, float targetY, float targetZ)
	{
		MoveSkill packet = (MoveSkill) instance.newInstance();

		packet.caster = caster;
		packet.targetX = targetX;
		packet.targetY = targetY;
		packet.targetZ = targetZ;

		return packet;
	}

	/** тот кто кастует */
	private Character caster;
	/** цели атакующего */
	private Character target;

	/** целевая точка перемещения */
	private float targetX;
	private float targetY;
	private float targetZ;

	@Override
	public void finalyze()
	{
		caster = null;
		target = null;
	}

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.SKILL_MOVE;
	}

	@Override
	protected final void writeImpl()
	{
		writeOpcode();

		if(target != null)
		{
			writeInt(caster.getObjectId());
			writeInt(caster.getSubId());
			writeInt(target.getObjectId());
			writeInt(target.getSubId());
			writeFloat(target.getX());
			writeFloat(target.getY());
			writeFloat(target.getZ());
			writeShort(caster.calcHeading(caster == target? caster.getHeading() : target.getX(), target.getY()));//FD4 A0
		}
		else
		{
			writeInt(caster.getObjectId());
			writeInt(caster.getSubId());
			writeInt(0);
			writeInt(0);
			writeFloat(targetX);
			writeFloat(targetY);
			writeFloat(targetZ);
			writeShort(caster.calcHeading(targetX, targetY));//FD4 A0
		}
	}
}