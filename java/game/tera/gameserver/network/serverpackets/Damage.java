package tera.gameserver.network.serverpackets;

import tera.gameserver.model.AttackInfo;
import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.Skill;
import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет с отображением результата атаки.
 * 
 * @author Ronn
 */
public class Damage extends ServerPacket
{
	/** отображения заблокированного уронеа */
	public static final int BLOCK = 0;
	/** отображение обычного урона */
	public static final int DAMAGE = 1;
	/** отображение хила */
	public static final int HEAL = 2;
	/** отображение хила мп */
	public static final int MANAHEAL = 3;
	/** отображение наложения эффекта */
	public static final int EFFECT = 4;
	
	private static final ServerPacket instance = new Damage();
	
	public static Damage getInstance(Character attacker, Character attacked, AttackInfo info, Skill skill, int type)
	{
		Damage packet = (Damage) instance.newInstance();
		
		if(attacker == null || attacked == null)
			log.warning(packet, new Exception("not found attacker or attacked"));
		
		packet.attacker = attacker;
		packet.attacked = attacked;
		packet.damage = info.getDamage();
		packet.crit = info.isCrit();
		packet.owerturned = info.isOwerturn();
		packet.type = type;
		packet.damageId = skill.getDamageId();
		
		return packet;
	}
	
	public static Damage getInstance(Character attacker, Character attacked, int damageId, int damage, boolean crit, boolean owerturned, int type)
	{
		Damage packet = (Damage) instance.newInstance();
		
		if(attacker == null || attacked == null)
			log.warning(packet, new Exception("not found attacker or attacked"));
		
		packet.attacker = attacker;
		packet.attacked = attacked;
		packet.damageId = damageId;
		packet.damage = damage;
		packet.crit = crit;
		packet.owerturned = owerturned;
		packet.type = type;
		
		return packet;
	}
	
	/** атакер */
	private Character attacker;
	/** атакуемый */
	private Character attacked;
	
	/** ид урона */
	private int damageId;
	/** кол-во урона */
	private int damage;
	/** тип атаки */
	private int type;
	
	/** блы лиудар критом */
	private boolean crit;
	/** опрокинул ли удар цель */
	private boolean owerturned;

	@Override
	public void finalyze()
	{
		attacker = null;
		attacked = null;
	}

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.DAMAGE;
	}

	@Override
	protected final void writeImpl()
	{
        writeOpcode();
        writeInt(owerturned ? 0x00540003 : 0);
		writeInt(attacker.getObjectId());
		writeInt(attacker.getSubId());
		writeInt(attacked.getObjectId());
		writeInt(attacked.getSubId());
		writeInt(attacker.getModelId());
		writeInt(damageId);
		writeInt(0);
		writeInt(0);	
		writeInt(0); // думаю 2е 2 байта хп цели 0x058E3E50
		writeInt(0); // было ноль
		writeInt(damage); // Сколько надамажил
		writeShort(type);// ИД если 2 то будет лечить +80 и т.д.
		
		writeByte(crit? 1 : 0);  // 01 Крит
		writeByte(0); //електровсплеск        	
		writeByte(owerturned? 1 : 0);  //01 опрокинул	
        writeByte(owerturned? 1 : 0);
        
        if(owerturned)
        {
    		writeFloat(attacked.getX());
    		writeFloat(attacked.getY());
    		writeFloat(attacked.getZ());
    		writeShort(attacked.getHeading());//8 heading
    		writeInt(attacked.getOwerturnId());
    		
    		writeInt(0);
    		writeInt(0);
        }
        else
        {
        	writeInt(0);
    		writeInt(0);
    		writeInt(0);
    		writeInt(0);
    		writeInt(0);
    		writeInt(0);
    		writeShort(0);
        }
	}
}