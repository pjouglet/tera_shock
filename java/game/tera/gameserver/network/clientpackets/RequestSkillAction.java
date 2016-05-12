package tera.gameserver.network.clientpackets;

import tera.gameserver.model.playable.Player;

/**
 * Клиентский пакет старта мили скила.
 *
 * @author Ronn
 */
@SuppressWarnings("unused")
public class RequestSkillAction extends ClientPacket
{
	public static enum ActionType
	{
		NONE,
		NONE1,
		CANCEL;

		public static ActionType valueOf(int index)
		{
			ActionType[] values = values();

			if(index >= values.length)
				return NONE;

			return values[index];
		}
	}

	/** игрок */
	private Player player;

	/** тип действия над скилом */
	private ActionType type;

	/** скил ид */
	private int skillId;

	@Override
	public void finalyze()
	{
		player = null;
		skillId = 0;
	}

	/**
	 * @return игрок.
	 */
	public final Player getPlayer()
	{
		return player;
	}

	@Override
	public void readImpl()
	{
		player = owner.getOwner();

		skillId = readInt();

		type = ActionType.valueOf(readInt());
	}

	@Override
	public void runImpl()
	{

	}
}