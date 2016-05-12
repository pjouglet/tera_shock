package tera.gameserver.network.clientpackets;

import tera.gameserver.model.actions.Action;
import tera.gameserver.model.playable.Player;

/**
 * Соглашение с началом трейда.
 *
 * @author Ronn
 */
public class AssentTrade extends ClientPacket
{
	/** игрок */
	private Player player;

	/** тип акшена */
	private int type;

	@Override
	public void finalyze()
	{
		player = null;
	}

	@Override
	protected void readImpl()
	{
		player = owner.getOwner();

		type = readInt();
	}

	@Override
	protected void runImpl()
	{
		if(player == null)
			return;

		Action action = player.getLastAction();

		if(action == null || action.getId() != type)
			return;

		action.assent(player);
	}
}
