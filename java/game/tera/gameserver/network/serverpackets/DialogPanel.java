package tera.gameserver.network.serverpackets;

import tera.gameserver.model.playable.Player;
import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет панели для каких-то действий.
 *
 * @author Ronn
 * @created 26.02.2012
 */
public class DialogPanel extends ServerPacket
{
	public static enum PanelType
	{
		SKILL_LEARN(27),
		ENCHANT_ITEM(34);

		/** ид идалога */
		private int id;

		private PanelType(int id)
		{
			this.id = id;
		}

		public int getId()
		{
			return id;
		}
	}

	private static final ServerPacket instance = new DialogPanel();

	public static DialogPanel getInstance(Player player, PanelType type)
	{
		DialogPanel packet = (DialogPanel) instance.newInstance();

		packet.player = player;
		packet.type = type;

		return packet;
	}

	/** игрок */
	private Player player;

	/** тип панели */
	private PanelType type;

	@Override
	public void finalyze()
	{
		player = null;
	}

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.NPC_DIALOG_SKILL_LEARN_PANEL;
	}

	@Override
	protected void writeImpl()
	{
		writeOpcode();
		writeShort(44);
        writeShort(46 + (player.getName().length() * 2));
        writeShort(48 + (player.getName().length() * 2));
        writeShort(0);
		writeInt(player.getObjectId()); //f7 05 00 10
		writeInt(player.getSubId());  //00 80 00 06
		writeLong(0);   //00 00 00 00 00 00 00 00
		writeInt(type.getId());//1D 00 00 00
		writeInt(0x000E10EA);
		writeInt(0);//00 00 00 00 00 00 00 00
		writeShort(0);
		writeByte(0);
		writeString(player.getName());//32 00 32 00 32 00 00 00
		writeShort(0);//00 00                  c.z.x....*/
	}
}
