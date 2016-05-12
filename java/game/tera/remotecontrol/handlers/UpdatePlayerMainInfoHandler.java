package tera.remotecontrol.handlers;

import tera.gameserver.model.World;
import tera.gameserver.model.base.Sex;
import tera.gameserver.model.playable.Player;
import tera.remotecontrol.Packet;
import tera.remotecontrol.PacketHandler;
import tera.remotecontrol.PacketType;

/**
 * Запрос основной информации об игроке.
 *
 * @author Ronn
 */
public class UpdatePlayerMainInfoHandler implements PacketHandler
{
	public static final UpdatePlayerMainInfoHandler instance = new UpdatePlayerMainInfoHandler();

	@Override
	public Packet processing(Packet packet)
	{
		Player player = World.getPlayer(packet.nextString());

		if(player == null)
			return null;

		return new Packet(PacketType.RESPONSE, player.getLevel(), player.getPlayerClass().name(), player.getSex() == Sex.MALE? "Муж." : "Жен.", player.getExp(), player.getCurrentHp(), player.getCurrentMp());
	}
}
