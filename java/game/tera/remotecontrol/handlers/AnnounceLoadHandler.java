package tera.remotecontrol.handlers;

import java.util.ArrayList;

import rlib.util.array.Arrays;
import tera.gameserver.manager.AnnounceManager;
import tera.gameserver.tasks.AnnounceTask;
import tera.remotecontrol.Packet;
import tera.remotecontrol.PacketHandler;
import tera.remotecontrol.PacketType;

/**
 * Отправляет все анонсы
 *
 * @author Ronn
 * @created 08.04.2012
 */
public class AnnounceLoadHandler implements PacketHandler
{
	@Override
	public Packet processing(Packet packet)
	{
		ArrayList<String> startingAnnounces = new ArrayList<String>();

		// получаем менеджер аннонсов
		AnnounceManager announceManager = AnnounceManager.getInstance();

		for(String announce : announceManager.getStartAnnouncs())
			startingAnnounces.add(announce);

		ArrayList<Object[]> runningAnnounces = new ArrayList<Object[]>();

		for(AnnounceTask announce : announceManager.getRuningAnnouncs())
			runningAnnounces.add(Arrays.toGenericArray(announce.getText(), announce.getInterval()));

		return new Packet(PacketType.REQUEST_LOAD_ANNOUBCES, startingAnnounces, runningAnnounces);
	}
}
