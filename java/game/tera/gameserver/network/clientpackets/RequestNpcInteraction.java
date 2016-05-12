package tera.gameserver.network.clientpackets;

import tera.Config;
import tera.gameserver.model.World;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.playable.Player;

/**
 * Запрос на взаимодействие с нпс.
 *
 * @author Ronn
 */
public class RequestNpcInteraction extends ClientPacket
{
	/** игрок */
	private Player player;

	/** обджект ид цели */
	private int targetId;

	@Override
	public void finalyze()
	{
		player  = null;
	}

	@Override
	public boolean isSynchronized()
	{
		return false;
	}

	@Override
	public void readImpl()
	{
    	player = owner.getOwner();

    	targetId = readInt();
	}

	@Override
	public void runImpl()
    {
		if(player == null)
			return;

		player.getAI().startNpcSpeak(World.getAroundById(Npc.class, player, targetId, Config.SERVER_NPC_SUB_ID));
	}
}