package tera.remotecontrol.handlers;

import tera.gameserver.model.World;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.skillengine.StatType;
import tera.remotecontrol.Packet;
import tera.remotecontrol.PacketHandler;
import tera.remotecontrol.PacketType;

/**
 * Запрос основной информации об игроке.
 * 
 * @author Ronn
 */
public class UpdatePlayerStatInfoHandler implements PacketHandler
{
	public static final UpdatePlayerStatInfoHandler instance = new UpdatePlayerStatInfoHandler();
	
	@Override
	public Packet processing(Packet packet)
	{
		Player player = World.getPlayer(packet.nextString());
		
		if(player == null)
			return null;

		int attack = player.getAttack(null, null);
		int defense = player.getDefense(null, null);
		int impact = player.getImpact(null, null);
		int balance = player.getBalance(null, null);
		int critRcpt = (int) player.getCritRateRcpt(null, null);
		int critRate = (int) player.getCritRate(null, null);
		int powerFactor = player.getPowerFactor();
		int defenseFactor = player.getDefenseFactor();
		int impactFactor = player.getImpactFactor();
		int balanceFactor = player.getBalanceFactor();
		int attackSpeed = player.getAtkSpd();
		int moveSpeed = player.getRunSpeed();
		int weakRcpt = (int) player.calcStat(StatType.WEAK_RECEPTIVE, 0, null, null);
		int damageRcpt = (int) player.calcStat(StatType.DAMAGE_RECEPTIVE, 0, null, null);
		int stunRcpt = (int) player.calcStat(StatType.STUN_RECEPTIVE, 0, null, null);
		
		float critDmg = player.getCritDamage(null, null);
		
		return new Packet(PacketType.RESPONSE, attack, defense, impact, balance, critRcpt, critRate, critDmg, powerFactor, defenseFactor, impactFactor, balanceFactor, attackSpeed, moveSpeed, weakRcpt, damageRcpt, stunRcpt);
	}
}
