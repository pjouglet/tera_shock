package tera.gameserver.network.serverpackets;

import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет, отображающий откат скилов.
 * 
 * @author Ronn
 */
public class SkillReuse extends ServerPacket
{
	private static final ServerPacket instance = new SkillReuse();
	
	public static SkillReuse getInstance(int skillId, int reuseDelay)
	{
		SkillReuse packet = (SkillReuse) instance.newInstance();
		
		packet.skillId = skillId;
		packet.reuseDelay = reuseDelay;
		
		return packet;
	}
	
	/** ид скила */
	private int skillId;
	/** время отката */
	private int reuseDelay;

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.SKILL_REUSE;
	}

	@Override
	protected final void writeImpl()
	{
        writeOpcode();
        writeInt(skillId);
        writeInt(reuseDelay);
	}
}