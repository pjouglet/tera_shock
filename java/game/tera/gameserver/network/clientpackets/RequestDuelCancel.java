package tera.gameserver.network.clientpackets;

import tera.gameserver.model.Duel;
import tera.gameserver.model.playable.Player;

/**
 * Отмена дуэли.
 *
 * @author Ronn
 */
public class RequestDuelCancel extends ClientPacket
{
	/** игрок */
	private Player player;

	@Override
	public void finalyze()
	{
		player  = null;
	}

	@Override
	protected void readImpl()
	{
		player = owner.getOwner();
	}

	@Override
	protected void runImpl()
	{
		if(player == null)
			return;

		// получаем дуль игрока
		Duel duel = player.getDuel();

		// если такая есть
		if(duel != null)
			// отменяем
			duel.cancel(true, false);
	}
}
