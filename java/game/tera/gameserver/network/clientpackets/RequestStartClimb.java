package tera.gameserver.network.clientpackets;

import tera.gameserver.model.playable.Player;
import tera.gameserver.model.territory.ClimbTerritory;
import tera.gameserver.network.serverpackets.CharClimb;

/**
 * Клиентский пакет, запрашивающий лазание.
 *
 * @author Ronn
 */
public class RequestStartClimb extends ClientPacket
{
	/** игрок */
	private Player player;

	@Override
	public void finalyze()
	{
		player = null;
	}

	@Override
	public void readImpl()
	{
		player = owner.getOwner();
	}

	@Override
	public void runImpl()
	{
		if(player == null || player.isBattleStanced())
			return;

		ClimbTerritory territory = player.getTerritory(ClimbTerritory.class);

		if(territory == null)
			return;

		int heading = player.calcHeading(territory.getTargetX(), territory.getTargetY());

		player.broadcastPacket(CharClimb.getInstance(player, heading, territory.getTargetX(), territory.getTargetY(), territory.getTargetZ()));
	}
}