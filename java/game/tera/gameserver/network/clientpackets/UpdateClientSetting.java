package tera.gameserver.network.clientpackets;

import tera.gameserver.model.playable.Player;

/**
 * Клиентский пакет с настройками клиента.
 *
 * @author Ronn
 */
public class UpdateClientSetting extends ClientPacket
{
	/** игрок */
	private Player player;

	/** настройки клиента */
	private byte[] settings;

	@Override
	public void finalyze()
	{
		player = null;
		settings = null;
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
			byte[] old = player.getSettings();

			if(old != null && old.length == size)
				settings = old;
			else
				settings = new byte[size];

			buffer.get(settings);
		}
	}

	@Override
	public void runImpl()
    {
		if(player != null && settings != null)
			player.setSettings(settings, true);
    }
}