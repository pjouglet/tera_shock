package tera.gameserver;

import tera.util.LocalObjects;

/**
 * Модель серверного потока.
 *
 * @author Ronn
 */
public class ServerThread extends Thread
{
	/**
	 * @return текущий серверный поток.
	 */
	public static ServerThread currentThread()
	{
		return (ServerThread) Thread.currentThread();
	}

	/** локальные объекты */
	private final LocalObjects local;

	public ServerThread()
	{
		this.local = new LocalObjects();
	}

	/**
	 * @param group группа потоков.
	 * @param target целевой таск.
	 * @param name название опотка.
	 */
	public ServerThread(ThreadGroup group, Runnable target, String name)
	{
		super(group, target, name);

		this.local = new LocalObjects();
	}

	/**
	 * @return локальные объекты.
	 */
	public final LocalObjects getLocal()
	{
		return local;
	}
}
