package tera.gameserver.network.clientpackets;

import tera.gameserver.model.actions.dialogs.ActionDialog;
import tera.gameserver.model.actions.dialogs.ActionDialogType;
import tera.gameserver.model.actions.dialogs.EnchantItemDialog;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.model.UserClient;

/**
 * Клиентский пакет с запросом добавления итема в диалог заточки.
 * 
 * @author Ronn
 */
public class RequestAddEnchantItem extends ClientPacket
{
	/** индекс ячейки */
	private int index;
	/** уникальный ид предмета */
	private int objectId;
	/** ид шаблона предмета */
	private int itemId;

	@Override
	protected void readImpl()
	{
		index = readInt();// 01 00 00 00 индекс куда класть в окне точки
		objectId = readInt();// AB 96 94 03 обжект ид итема
		readInt();// 00 00 00 00
		itemId = readInt();// DB 27 00 00 итем ид итема
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
		enchantDialog.addItem(index, objectId, itemId);
	}
}
