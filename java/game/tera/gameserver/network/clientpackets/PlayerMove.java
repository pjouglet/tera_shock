package tera.gameserver.network.clientpackets;

import rlib.logging.Loggers;
import tera.gameserver.manager.GeoManager;
import tera.gameserver.model.MoveType;
import tera.gameserver.model.playable.Player;

/**
 * Клиентский пакет для указания типа и точку перемещения игрока.
 *
 * @author Ronn
 */
public class PlayerMove extends ClientPacket
{
	/** игрок */
	private Player player;

	/** тип перемещения */
	private MoveType type;

	/** направление */
	private int heading;

	/** целевая точка */
	private float targetX;
	private float targetY;
	private float targetZ;

	/** стартовая точка */
	private float startX;
	private float startY;
	private float startZ;

	@Override
	public void finalyze()
	{
		player = null;
	}

	@Override
	public void readImpl()
	{
		player = owner.getOwner();

		if(player == null || buffer.remaining() < 27)
		{
			player = null;
			Loggers.warning(this, "incorrect packet");
			return;
		}

		startX = readFloat();
		startY = readFloat();
		startZ = readFloat();

		heading = readShort();

		//целевая точка
		targetX = readFloat();
		targetY = readFloat();
		targetZ = readFloat();

		//тип перемещения
		type = MoveType.valueOf(readByte());
	}

	@Override
	public void runImpl()
	{
		if(player == null)
			return;

		if(type == MoveType.RUN)
		{
			GeoManager.write(player.getContinentId(), startX, startY, startZ);
			GeoManager.write(player.getContinentId(), targetX, targetY, targetZ);
		}

		/*if(player.getDistance(startX, startY) > 64)
		{
			player.sendMessage("Рассинхронизация!!!");
			player.stopMove();
			player.sendPacket(player.getMovePacket(MoveType.STOP, player.getX(), player.getY(), player.getZ()), true);
			return;
		}*/
		player.getAI().startMove(startX, startY, startZ, heading, type, targetX, targetY, targetZ, true, false);
	}
}