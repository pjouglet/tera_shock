package tera.gameserver.network.clientpackets;

import tera.gameserver.model.playable.Player;

/**
 * Клиентский пакет для указания положения во время лазания.
 *
 * @author Ronn
 */
public class PlayerClimb extends ClientPacket
{
	/** игрок */
	private Player player;

	/** целевая точка */
	private float targetX;
	private float targetY;
	private float targetZ;

	@Override
	public void finalyze()
	{
		player = null;
	}

	@Override
	public void readImpl()
	{
		player = owner.getOwner();

		if(player == null)
			return;

		// целевая точка
		targetX = readFloat();
		targetY = readFloat();
		targetZ = readFloat();
	}

	@Override
	public void runImpl()
	{
		if(player == null)
			return;

		player.setXYZ(targetX, targetY, targetZ);
	}

	@Override
	public String toString()
	{
		return "PlayerClimb  targetX = " + targetX + ", targetY = " + targetY + ", targetZ = " + targetZ;
	}
}