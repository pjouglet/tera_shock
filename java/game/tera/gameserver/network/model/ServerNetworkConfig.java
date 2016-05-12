package tera.gameserver.network.model;

import rlib.network.NetworkConfig;
import tera.Config;
import tera.gameserver.ServerThread;

/**
 * Конфигурирование асинхронной сети под сервер теры.
 * 
 * @author Ronn
 */
public class ServerNetworkConfig implements NetworkConfig
{
	private static ServerNetworkConfig instance;
	
	public static ServerNetworkConfig getInstance()
	{
		if(instance == null)
			instance = new ServerNetworkConfig();
		
		return instance;
	}
	
	@Override
	public String getGroupName()
	{
		return "Network";
	}

	@Override
	public int getGroupSize()
	{
		return Config.NETWORK_GROUP_SIZE;
	}

	@Override
	public int getReadBufferSize()
	{
		return Config.NETWORK_READ_BUFFER_SIZE;
	}

	@Override
	public Class<? extends Thread> getThreadClass()
	{
		return ServerThread.class;
	}

	@Override
	public int getThreadPriority()
	{
		return Config.NETWORK_THREAD_PRIORITY;
	}

	@Override
	public int getWriteBufferSize()
	{
		return Config.NETWORK_WRITE_BUFFER_SIZE;
	}

	@Override
	public boolean isVesibleReadException()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isVesibleWriteException()
	{
		// TODO Auto-generated method stub
		return false;
	}
}
