package tera.gameserver.network.clientpackets;

import tera.gameserver.model.EmotionType;
import tera.gameserver.model.playable.Player;

/**
 * Клиентский пакет с запуском эмоции.
 *
 * @author Ronn
 */
public class RequestStartEmotion extends ClientPacket
{
	/** игрок */
	private Player player;
	/** ид эмоции */
	private int index;

	@Override
	public void finalyze()
	{
		player = null;
	}

	@Override
	public boolean isSynchronized()
	{
		return false;
	}

	@Override
	protected void readImpl()
	{
		player = owner.getOwner();
		index = readInt();
	}

	@Override
	protected void runImpl()
	{
		if(player != null)
			player.getAI().startEmotion(EmotionType.valueOf(index));
	}
}
