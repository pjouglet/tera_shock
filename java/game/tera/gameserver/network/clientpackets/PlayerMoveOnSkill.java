package tera.gameserver.network.clientpackets;

import tera.gameserver.model.playable.Player;

/**
 * Клиентский пакет, указывающий на сколько двигается в процессе каста игрок
 *
 * @author Ronn
 */
public class PlayerMoveOnSkill extends ClientPacket
{
	/** игрок */
	private Player player;

	/** целевая точка */
	private float targetX;
	private float targetY;
	private float targetZ;

	/** направление */
	private int heading;

	@Override
	public void finalyze()
	{
		player = null;
	}

	@Override
	public void readImpl()
	{
    	player = owner.getOwner();

    	readInt();
    	readInt();

    	targetX = readFloat();
    	targetY = readFloat();
    	targetZ = readFloat();

    	heading = readShort();
	}

	@Override
	public void runImpl()
    {
		if(player == null)
			return;

		player.setHeading(heading);

		if(!player.isDefenseStance() && !player.isSkillMoved() && player.getDistance(targetX, targetY) > 64)
			return;

		player.setXYZ(targetX, targetY, targetZ);
    }
}