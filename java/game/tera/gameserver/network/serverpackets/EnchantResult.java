package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;

import tera.gameserver.network.ServerPacketType;

/**
 * Результат заточки предмета.
 * 
 * @author Ronn
 */
public class EnchantResult extends ServerConstPacket
{
	private static final EnchantResult SUCCESSFUL = new EnchantResult(1);
	private static final EnchantResult FAIL = new EnchantResult(0);

	public static final EnchantResult getSuccessful()
	{
		return SUCCESSFUL;
	}

	public static final EnchantResult getFail()
	{
		return FAIL;
	}

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.ENCHANT_RESULT;
	}

	/** результат */
	private final int result;

	public EnchantResult(int result)
	{
		this.result = result;
	}

	public EnchantResult()
	{
		this.result = 0;
	}

	@Override
	protected void writeImpl(ByteBuffer buffer)
	{
		writeOpcode(buffer);
		writeInt(buffer, result);
	}
}
