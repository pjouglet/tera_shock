package tera.gameserver.model.actions.classes;

import tera.gameserver.model.actions.ActionType;
import tera.gameserver.model.actions.dialogs.EnchantItemDialog;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.serverpackets.AppledAction;

/**
 * Модель действия по открытию диалога зачоравания вещей.
 * 
 * @author Ronn
 */
public final class EnchantItemAction extends AbstractAction<Void>
{
	@Override
	public void assent(Player player)
	{
		Player actor = getActor();

		super.assent(player);

		if (!test(actor, target))
			return;

		EnchantItemDialog dialog = EnchantItemDialog.newInstance(actor);

		if (!dialog.init())
			dialog.cancel(actor);
	}

	@Override
	public synchronized void cancel(Player player)
	{
		Player actor = getActor();

		if (actor == null)
		{
			log.warning(this, new Exception("not found actor"));
			return;
		}

		actor.setLastAction(null);

		super.cancel(player);
	}

	@Override
	public ActionType getType()
	{
		return ActionType.ENCHANT_ITEM;
	}

	@Override
	public void init(Player actor, String name)
	{
		this.actor = actor;
	}

	@Override
	public synchronized void invite()
	{
		Player actor = getActor();

		if (actor == null || actor.isOnMount() || actor.isFlyingPegas() || actor.hasLastActionDialog())
			return;

		actor.setLastAction(this);

		ActionType type = getType();

		actor.sendPacket(AppledAction.newInstance(actor, null, type.ordinal(), objectId), true);

		assent(actor);
	}

	@Override
	public boolean test(Player actor, Void target)
	{
		if (actor == null || actor.isOnMount() || actor.isFlyingPegas() || actor.hasLastActionDialog())
			return false;

		return true;
	}
}
