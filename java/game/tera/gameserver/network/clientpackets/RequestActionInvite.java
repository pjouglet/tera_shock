package tera.gameserver.network.clientpackets;

import tera.gameserver.model.actions.ActionType;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.model.UserClient;
import tera.gameserver.network.serverpackets.ActionStart;

/**
 * Приглашение на участие в акшене.
 * 
 * @author Ronn
 * @created 26.04.2012
 */
public class RequestActionInvite extends ClientPacket
{
	/** имя приглашаемого */
	private String name;

	/** тип акшена */
	private ActionType actionType;

	@Override
	public void finalyze()
	{
		name = null;
	}

	@Override
	protected void readImpl()
	{
		readInt();// 1A 00 24 00
		readShort();// 00 00

		actionType = ActionType.valueOf(readByte());

		readLong();// 00 00 00 00 00 00 00 00
		readInt();// 00 00 00 00
		readShort();// 00

		readByte();

		switch (actionType)
		{
			case CREATE_GUILD:
			{
				readShort();
				name = readString();
				break;
			}
			case BIND_ITEM:
			{
				readShort();
				name = String.valueOf(readInt());
				break;
			}
			default:
				name = readString();
		}
	}

	@Override
	protected void runImpl()
	{
		UserClient client = getOwner();

		if (client == null)
			return;

		Player actor = client.getOwner();

		if (actor == null || actor.getName().equals(name))
			return;

		ActionType actionType = getActionType();

		actor.sendPacket(ActionStart.getInstance(actionType), true);

		if (!actionType.isImplemented() || actor.hasLastAction())
			return;

		actor.getAI().startAction(actionType.newInstance(actor, name));
	}

	private ActionType getActionType()
	{
		return actionType;
	}
}
