package tera.gameserver.network.clientpackets;

import tera.gameserver.model.actions.ActionType;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.serverpackets.ActionStart;

/**
 * Приглашение игрока в пати.
 *
 * @author Ronn
 * @created 26.04.2012
 */
public class RequestGuildInvite extends ClientPacket
{
	/** имя приглашаемого */
	private String name;
	/** создатель акшена */
	private Player actor;

	@Override
	public void finalyze()
	{
		actor = null;
		name = null;
	}

	@Override
	protected void readImpl()
	{
		actor = owner.getOwner();

		readInt();//1A 00 2C 00
		readLong();//00 00 0B 00 00 00 00 00
		readLong();//00 00 00 00 00 00 00 00
		readShort();//00 00
		readByte();
		name = readString();//61 00 75 00 73 00 74 00 00 00   ..AO..F.a.u.s.t.
	}

	@Override
	protected void runImpl()
	{
		ActionType actionType = ActionType.INVITE_GUILD;

		actor.sendPacket(ActionStart.getInstance(actionType), true);

		actor.getAI().startAction(actionType.newInstance(actor, name));
	}
}
