package tera.gameserver.model.worldobject;

import tera.Config;
import tera.gameserver.model.TObject;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.serverpackets.DeleteWorldObject;
import tera.gameserver.network.serverpackets.WorldObjectInfo;

/**
 * Модель объектов  которые находятся в мире со спицефическими функциями.
 * 
 * @author Ronn
 */
public abstract class WorldObject extends TObject
{
	/**
	 * @param objectId уникальный ид объекта.
	 */
	public WorldObject(int objectId)
	{
		super(objectId);
	}

	@Override
	public void addMe(Player player)
	{
		player.sendPacket(WorldObjectInfo.getInstance(this), true);
	}

	@Override
	public int getSubId()
	{
		return Config.SERVER_OBJECT_SUB_ID;
	}

	@Override
	public final boolean isWorldObject()
	{
		return true;
	}

	@Override
	public void removeMe(Player player, int type)
	{
		player.sendPacket(DeleteWorldObject.getInstance(this), true);
	}
}
