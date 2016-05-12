package tera.gameserver.network.clientpackets;

import tera.gameserver.model.actions.dialogs.ActionDialog;
import tera.gameserver.model.actions.dialogs.ActionDialogType;
import tera.gameserver.model.actions.dialogs.EnchantItemDialog;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.model.UserClient;

/**
 * Уведомление о завершении анимации заточки.
 * 
 * @author Ronn
 */
public class EnchantFinish extends ClientPacket
{

	@Override
	protected void readImpl()
	{
	}

	@Override
	protected void runImpl()
	{
		UserClient client = getOwner();

		if (client == null)
			return;

		Player actor = client.getOwner();

		if (actor == null)
			return;

		ActionDialog dialog = actor.getLastActionDialog();

		if (dialog == null || dialog.getType() != ActionDialogType.ENCHANT_ITEM_DIALOG)
			return;

		EnchantItemDialog enchantDialog = (EnchantItemDialog) dialog;
		enchantDialog.apply();
	}
}
