package tera.gameserver.network.serverpackets;

import tera.gameserver.network.ServerPacketType;

public class AuthAttempt extends ServerPacket
{
	public static final int SUCCESSFUL = 1;
	public static final int INCORRECT = 2;

	private static final ServerPacket instance = new AuthAttempt();

	public static AuthAttempt getInstance(int result)
	{
		AuthAttempt packet = (AuthAttempt) instance.newInstance();

		packet.result = result;

		return packet;
	}

	/** результат */
	private int result;

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.AUTH_ATTEMPT;
	}

	@Override
	protected void writeImpl()
	{
		switch(result)
		{
			case SUCCESSFUL:
			{
				writeOpcode();
				writeByte(1);

				owner.sendPacket(AuthSuccessful.getInstance(), true);
				owner.sendPacket(AuthSuccessful2.getInstance(), true);

				break;
			}
			case INCORRECT:	owner.sendPacket(AuthFailed.getInstance(), true);
		}
	}
}