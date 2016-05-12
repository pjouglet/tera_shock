package tera.gameserver.model.npc.interaction.dialogs;

import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.serverpackets.MultiShop;
import tera.gameserver.templates.ItemTemplate;

/**
 * Модель окна мульти магазина.
 *
 * @author Ronn
 */
public final class MultiShopDialog extends AbstractDialog
{
	public static final MultiShopDialog newInstance(Npc npc, Player player, ItemTemplate[] items, int[] price, int priceId)
	{
		MultiShopDialog dialog = (MultiShopDialog) DialogType.MULTI_SHOP.newInstance();

		dialog.items = items;
		dialog.npc = npc;
		dialog.player = player;
		dialog.price = price;
		dialog.priceId = priceId;

		return dialog;
	}

	/** список продаваемых итемов */
	private ItemTemplate[] items;

	/** список цен на продаваемые итемы */
	private int[] price;

	/** ид итема для цены */
	private int priceId;

	/**
	 * @param npc нпс.
	 * @param sections секции с итемами.
	 * @param availableItems доступные итемы.
	 * @param player игрок.
	 * @param sectionId ид первой секции.
	 */
	protected MultiShopDialog()
	{
		super();
	}

	@Override
	public DialogType getType()
	{
		return DialogType.MULTI_SHOP;
	}

	@Override
	public synchronized boolean init()
	{
		if(!super.init())
			return false;

		Player player = getPlayer();

		if(player == null)
		{
			log.warning(this, new Exception("not found player"));
			return false;
		}

		//player.sendPacket(ShopReplyPacket.getInstance(sections, player, sectionId), true);
		player.sendPacket(MultiShop.getInstance(player, items, price, priceId), true);

		return true;
	}
}
