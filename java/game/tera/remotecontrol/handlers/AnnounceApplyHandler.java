package tera.remotecontrol.handlers;

import java.util.ArrayList;
import java.util.List;

import rlib.util.array.Array;
import tera.gameserver.manager.AnnounceManager;
import tera.gameserver.tasks.AnnounceTask;
import tera.remotecontrol.Packet;
import tera.remotecontrol.PacketHandler;

/**
 * Принятие новых настроек
 *
 * @author Ronn
 * @created 08.04.2012
 */
public class AnnounceApplyHandler implements PacketHandler
{
	@Override
	@SuppressWarnings("unchecked")
	public Packet processing(Packet packet)
	{
		// получаем менеджер аннонсов
		AnnounceManager announceManager = AnnounceManager.getInstance();

		{
			List<String> startingAnnounces = packet.next(ArrayList.class);

			Array<String> current = announceManager.getStartAnnouncs();

			current.clear();

			for(String announce : startingAnnounces)
				current.add(announce);

			current.trimToSize();
		}

		{
			List<Object[]> runningAnnounces = packet.next(ArrayList.class);

			Array<AnnounceTask> current = announceManager.getRuningAnnouncs();

			for(AnnounceTask task : current)
				task.cancel();

			current.clear();

			for(Object[] announce : runningAnnounces)
				current.add(new AnnounceTask(announce[0].toString(), (int) announce[1]));
		}

		announceManager.save();

		return null;
	}
}
