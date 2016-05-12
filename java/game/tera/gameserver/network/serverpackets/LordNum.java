package tera.gameserver.network.serverpackets;

import tera.gameserver.network.ServerPacketType;

public class LordNum extends ServerPacket
{	
	
	private LordNum()
	{
		super();
	}
	
	@Override
	public ServerPacketType getPacketType()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void writeImpl()
	{
		/*writeOpcode(ServerPacketType.PLAYER_LORD_NUM);
		writeDS(0x06004000);
		writeDS(0x36003000);
		writeDS(0x31000000);// DE 82 06 00 40 00 36 00 30 00 31 00 00 00
		
		// шлётся при смерти
		writeDS(0x06004000);
		writeDS(0x36003500);
		writeDS(0x35000B00);
		writeDS(0x55007300);
		writeDS(0x65007200);
		writeDS(0x4E006100);
		writeDS(0x6D006500);
		writeHS(0x0B00);
		// writeS(_player.getName());
		writeDS(0x0E00ECA3);
		writeDS(0x1E000000);
		writeDS(0xE6320000);
		writeHS(0x0000);*/
		
	}
}
