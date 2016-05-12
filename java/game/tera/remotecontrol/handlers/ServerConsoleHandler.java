package tera.remotecontrol.handlers;

import java.util.ArrayList;

import rlib.logging.LoggerListener;
import tera.remotecontrol.Packet;
import tera.remotecontrol.PacketHandler;
import tera.remotecontrol.PacketType;

/**
 * Обработчик запроса на данные из консоли сервера.
 * 
 * @author Ronn
 */
public class ServerConsoleHandler implements PacketHandler, LoggerListener
{
	public static final ServerConsoleHandler instance = new ServerConsoleHandler();
	
	/** хранилище сообщений */
	private final ArrayList<String> messages;
	
	private ServerConsoleHandler()
	{
		messages = new ArrayList<String>();
	}

	@Override
	public void println(String text)
	{
		if(messages.size() > 1000)
			messages.clear();
		
		messages.add(text);
	}

	@Override
	public Packet processing(Packet packet)
	{
		ArrayList<String> buffer = new ArrayList<String>();
		
		buffer.addAll(messages);
		
		messages.clear();
		
		return new Packet(PacketType.RESPONSE, buffer);
	}
}
