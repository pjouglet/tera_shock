package tera.gameserver.network.serverpackets;

import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.Skill;
import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет, обрисовывающий обычный выстрел.
 * 
 * @author Ronn
 */
public class StartFastShot extends ServerPacket
{
	private static final ServerPacket instance = new StartFastShot();
	
	public static StartFastShot getInstance(Character caster, Character target, Skill skill, int castId)
	{
		StartFastShot packet = (StartFastShot) instance.newInstance();
		
		packet.caster = caster;
		packet.target = target;
		packet.skill = skill;
		packet.castId = castId;
		
		return packet;
	}
	
	public static StartFastShot getInstance(Character caster, Skill skill, int castId, float targetX, float targetY, float targetZ)
	{
		StartFastShot packet = (StartFastShot) instance.newInstance();
		
		packet.caster = caster;
		packet.skill = skill;
		packet.targetX = targetX;
		packet.targetY = targetY;
		packet.targetZ = targetZ;
		
		return packet;
	}
	
	/** кастер */
	private Character caster;
	/** цель */
	private Character target;
	
	/** скил, откуда вылетел */
	private Skill skill;
	
	/** ид каста */
	private int castId;
	
	/** целевая точка */
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
		return ServerPacketType.SKILL_FAST_SHOT;
	}

	@Override
	protected final void writeImpl()
	{
		writeOpcode();
		
		if(target != null)
		{
			writeInt(0x00200001);
			writeInt(0x00300001);
			
			writeInt(caster.getObjectId());
			writeInt(caster.getSubId());
			writeInt(caster.getModelId());
			writeInt(skill.getIconId());
			writeInt(castId);
			
			writeInt(32);
			
			writeInt(0);
			writeInt(target.getObjectId());
			writeInt(target.getSubId());
			
			writeInt(48);
			
			writeFloat(target.getX());
			writeFloat(target.getY());
			writeFloat(target.getZ());
		}
		else
		{
			writeInt(0);
			writeInt(0x00200001);
			
			writeInt(caster.getObjectId());
			writeInt(caster.getSubId());
			writeInt(caster.getModelId());
			writeInt(skill.getIconId());
			writeInt(castId);
			
			writeInt(32);
			
			writeFloat(targetX); // шарик куда летит X
			writeFloat(targetY); // шарик куда летит X
			writeFloat(targetZ); // шарик куда летит X
		}
	}
}