package tera.remotecontrol.handlers;

import tera.Config;
import tera.remotecontrol.Packet;
import tera.remotecontrol.PacketHandler;
import tera.remotecontrol.PacketType;
import tera.remotecontrol.ServerControl;

/**
 * Обработчик авторизации контролера
 *
 * @author Ronn
 * @created 26.03.2012
 */
public class AuthHandler implements PacketHandler
{
	@Override
	public Packet processing(Packet packet)
	{
		String login = packet.nextString();
		String password = packet.nextString();
		
		if(Config.DIST_CONTROL_LOGIN.equals(login) && Config.DIST_CONTROL_PASSWORD.equals(password))
		{
			ServerControl.authed = true;
			
			return new Packet(PacketType.REQUEST_AUTH, true);
		}
		
		return new Packet(PacketType.REQUEST_AUTH, false);
	}
}
