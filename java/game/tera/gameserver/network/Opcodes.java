package tera.gameserver.network;

import java.io.File;

import rlib.util.VarTable;
import tera.Config;
import tera.gameserver.document.DocumentSnifferOpcode;

/**
 * Подготовитель опкодов пакетов.
 *
 * @author Ronn
 */
public abstract class Opcodes
{
	/**
	 * Подготовить опкоды.
	 */
	public static void prepare()
	{
		if(Config.SERVER_USE_SNIFFER_OPCODE)
		{
			VarTable opcodes = new DocumentSnifferOpcode(new File("./config/Opcode.xml")).parse();

			for(ClientPacketType packet : ClientPacketType.values())
				packet.setOpcode(opcodes.getInteger("CLIENT_PACKET_" + packet.name(), packet.getOpcode()));

			for(ServerPacketType packet : ServerPacketType.values())
				packet.setOpcode(opcodes.getInteger("SERVER_PACKET_" + packet.name(), packet.getOpcode()));
		}

		ClientPacketType.init();
		ServerPacketType.init();
	}
}
