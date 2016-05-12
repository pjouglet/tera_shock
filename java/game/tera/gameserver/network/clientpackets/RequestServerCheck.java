package tera.gameserver.network.clientpackets;

import tera.gameserver.network.serverpackets.CheckServerResult;

/**
 * Пакет клиентский, для проверки сервера
 *
 * @author Ronn
 * @created 25.03.2012
 */
public class RequestServerCheck extends ClientPacket
{
	/** присылаемое значение */
	private int[] vals;

	public RequestServerCheck()
	{
		this.vals = new int[3];
	}

	@Override
	public boolean isSynchronized()
	{
		return false;
	}

	@Override
	public void readImpl()
	{
		vals[0] = readInt();//01 00 00 00 число 1
		vals[1] = readInt();//01 00 00 00 число 2
		vals[2] = readInt();//01 00 00 00 число 3
	}

	@Override
	public void runImpl()
	{
		owner.sendPacket(CheckServerResult.getInstance(vals), true);
	}
}