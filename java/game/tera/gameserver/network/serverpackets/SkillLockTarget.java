package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;

import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.Skill;
import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет, показывающий игроку, что он взял в цель объект.
 * 
 * @author Ronn
 */
public class SkillLockTarget extends ServerPacket
{
	private static final ServerPacket instance = new SkillLockTarget();
	
	public static SkillLockTarget getInstance(Character target, Skill skill, boolean locked)
	{
		SkillLockTarget packet = (SkillLockTarget) instance.newInstance();
		
		packet.id = target.getObjectId();
		packet.subId = target.getSubId();
		packet.skillId = skill.getIconId();
		packet.locked = locked? 1 : 0;
		
		return packet;
	}
	
	/** ид цели */
	private int id;
	/** саб ид цели */
	private int subId;
	/** ид скила */
	private int skillId;
	/** захвачен ли */
	private int locked;
	
	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.SKILL_LOCK_TARGET;
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
		writeInt(buffer, id);
		writeInt(buffer, subId);
		writeInt(buffer, skillId);
		writeByte(buffer, locked);//1 захват удался,0 захват не удался
	}
}
