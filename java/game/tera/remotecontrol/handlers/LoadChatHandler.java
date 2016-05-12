package tera.remotecontrol.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;

import rlib.concurrent.Locks;
import tera.remotecontrol.Packet;
import tera.remotecontrol.PacketHandler;
import tera.remotecontrol.PacketType;

/**
 * Загрузка чата
 *
 * @author Ronn
 * @created 09.04.2012
 */
public class LoadChatHandler implements PacketHandler
{
	public static final LoadChatHandler instance = new LoadChatHandler();
	
	/** список ожидающих сообщений */
	private static final List<String> messages = new ArrayList<String>();
	/** блокировщик */
	private static final Lock lock = Locks.newLock();
	
	/**
	 * Добавление нового сообщения
	 * 
	 * @param message
	 */
	public static void add(String message)
	{
		lock.lock();
		try
		{
			if(messages.size() > 200)
				messages.clear();
			
			messages.add(message);
		}
		finally
		{
			lock.unlock();
		}
	}
	
	@Override
	public Packet processing(Packet packet)
	{
		lock.lock();
		try
		{
			ArrayList<String> buffer = new ArrayList<String>(messages);
			
			messages.clear();
			
			return new Packet(PacketType.RESPONSE, buffer);
		}
		finally
		{
			lock.unlock();
		}
	}
}
