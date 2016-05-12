package tera.gameserver.network.clientpackets;

import tera.gameserver.model.playable.Player;

/**
 * Клиентский пакет с настройками клиента.
 *
 * @author Ronn
 */
public class UpdateHotKey extends ClientPacket
{
	/** игрок */
	private Player player;
	/** настройки клиента */
	private byte[] hotkey;

	@Override
	public void finalyze()
	{
		player = null;
		hotkey = null;
	}

	@Override
	public void readImpl()
    {
		player = owner.getOwner();

		// получаем размер настроек
		int size = buffer.limit() - buffer.position();

		// если клиентский буфер меньше 4 кб, предупреждаем
		if(buffer.capacity() < 4096)
		{
			log.warning(this, "this is small read buffer " + buffer.capacity() + ".");
			return;
		}

		if(player != null)
		{
			byte[] old = player.getHotkey();

			if(old != null && old.length == size)
				hotkey = old;
			else
				hotkey = new byte[size];

			buffer.get(hotkey);
		}
	}

	@Override
	public void runImpl()
    {
		if(player != null && hotkey != null)
			player.setHotkey(hotkey, true);
    }
}