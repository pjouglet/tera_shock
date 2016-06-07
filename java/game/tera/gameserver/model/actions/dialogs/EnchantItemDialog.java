package tera.gameserver.model.actions.dialogs;

import rlib.util.array.Arrays;
import rlib.util.random.Random;
import rlib.util.random.Randoms;
import rlib.util.table.IntKey;
import rlib.util.table.Table;
import rlib.util.table.Tables;
import tera.gameserver.manager.DataBaseManager;
import tera.gameserver.manager.ObjectEventManager;
import tera.gameserver.manager.PacketManager;
import tera.gameserver.model.inventory.Inventory;
import tera.gameserver.model.items.ItemInstance;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.serverpackets.DialogPanel;
import tera.gameserver.network.serverpackets.DialogPanel.PanelType;
import tera.gameserver.network.serverpackets.EnchantResult;
import tera.gameserver.network.serverpackets.EnchatItemInfo;

/**
 * Модель диалога по зачарованию вещей.
 * 
 * @author Ronn
 */
public class EnchantItemDialog extends AbstractActionDialog {

	private static final int MAX_ENCHANT_LEVEL = 12;

	public static final int ITEM_COUNTER = 2;

	private static final int ALKAHEST_ITEM_INDEX = 2;
	private static final int CONSUME_ITEM_INDEX = 1;
	private static final int SOURCE_ITEM_INDEX = 0;

	/**
	 * [20:25:19] BrabusX: 1-6 если белое ложишь = 5% если грин = 15% если
	 * синька = 30% если голд = 50%
	 * 
	 * 7-9 если белое = 3% если грин = 10% если синька = 20% если голд = 35%
	 * 
	 * 10-12 если белое = 1% если грин = 3% если синька = 10% если голд = 25%
	 */
	private static final int[][] CHANE_TABLE = {
		// 1 - 3
		// common/uncommon/rare/epic
		{
			70,
			80,
			90,
			99
		},
		{
			70,
			80,
			90,
			99
		},
		{
			70,
			80,
			90,
			99
		},
		// 4 - 6
		{
			20,
			30,
			50,
			66
		},
		{
			20,
			30,
			50,
			66
		},
		{
			20,
			30,
			50,
			66
		},
		// 7 - 9
		{
			5,
			15,
			25,
			40
		},
		{
			5,
			15,
			25,
			40
		},
		{
			5,
			15,
			25,
			40
		},
		// 10 - 12
		{
			1,
			5,
			15,
			20
		},
		{
			1,
			5,
			15,
			20
		},
		{
			1,
			5,
			15,
			20
		},
	};

	private static final Table<IntKey, int[]> ALKAHEST_TABLE;

	static {

		ALKAHEST_TABLE = Tables.newIntegerTable();

		ALKAHEST_TABLE.put(15, Arrays.toIntegerArray(0, 6));
		ALKAHEST_TABLE.put(446, Arrays.toIntegerArray(6, 9));
		ALKAHEST_TABLE.put(448, Arrays.toIntegerArray(6, 9));
		ALKAHEST_TABLE.put(447, Arrays.toIntegerArray(9, 12));
	}

	public static EnchantItemDialog newInstance(Player player) {

		EnchantItemDialog dialog = (EnchantItemDialog) ActionDialogType.ENCHANT_ITEM_DIALOG.newInstance();

		if(dialog == null) {
			dialog = new EnchantItemDialog();
		}

		dialog.actor = player;
		dialog.enemy = player;

		return dialog;
	}

	/** рандоминайзер диалога */
	private final Random random;

	/** целевой затачиваемый итем */
	private ItemInstance consume;
	/** средство для заточки */
	private ItemInstance alkahest;
	/** ресурс для заточки */
	private ItemInstance source;

	public EnchantItemDialog() {
		this.random = Randoms.newRealRandom();
	}

	@Override
	public boolean apply() {
		try {

			Player actor = getActor();

			if(actor == null) {
				return false;
			}

			Inventory inventory = actor.getInventory();

			if(inventory == null) {
				return false;
			}

			inventory.lock();
			try {

				ItemInstance target = getSource();
				ItemInstance source = inventory.getItemForObjectId(target.getObjectId());

				if(source == null) {
					return false;
				}

				target = getConsume();

				ItemInstance consume = inventory.getItemForObjectId(target.getObjectId());

				if(consume == null || consume == source || !source.isEnchantable() || consume.getExtractable() < source.getExtractable()) {
					return false;
				}

				if(!source.getClass().isInstance(consume)) {
					return false;
				}

				target = getAlkahest();

				ItemInstance alkahest = inventory.getItemForItemId(target.getItemId());

				if(alkahest == null || alkahest.getItemCount() < source.getExtractable()) {
					return false;
				}

				int[] range = ALKAHEST_TABLE.get(alkahest.getItemId());

				if(source.getEnchantLevel() < range[0] || source.getEnchantLevel() >= range[1]) {
					return false;
				}

				int chance = CHANE_TABLE[source.getEnchantLevel()][consume.getRank().ordinal()];

				// actor.sendMessage("chance " + chance);

				boolean fail = !random.chance(chance);

				consume.setOwnerId(0);

				PacketManager.showDeleteItem(actor, consume);

				inventory.removeItem(consume);

				DataBaseManager manager = DataBaseManager.getInstance();
				manager.updateLocationItem(consume);

				inventory.removeItem(alkahest.getItemId(), source.getExtractable());

				if(fail) {
					actor.sendPacket(EnchantResult.getFail(), false);
				} else {
					source.setEnchantLevel(source.getEnchantLevel() + 1);
					manager.updateItem(source);
					actor.sendPacket(EnchantResult.getSuccessful(), false);
				}

				ObjectEventManager eventManager = ObjectEventManager.getInstance();
				eventManager.notifyInventoryChanged(actor);

				return true;
			} finally {
				inventory.unlock();
			}
		} finally {
			setConsume(null);
		}
	}

	@Override
	public ActionDialogType getType() {
		return ActionDialogType.ENCHANT_ITEM_DIALOG;
	}

	@Override
	public synchronized boolean init() {
		if(super.init()) {

			Player actor = getActor();

			actor.sendPacket(DialogPanel.getInstance(actor, PanelType.ENCHANT_ITEM), true);
			updateDialog();

			return true;
		}

		return false;
	}

	/**
	 * @return разбиваемый предмет.
	 */
	public ItemInstance getConsume() {
		return consume;
	}

	/**
	 * @param consume разбиваемый предмет.
	 */
	public void setConsume(ItemInstance consume) {
		this.consume = consume;
	}

	/**
	 * @return используемый алкахест.
	 */
	public ItemInstance getAlkahest() {
		return alkahest;
	}

	/**
	 * @param alkahest затачиваемый предмет.
	 */
	public void setAlkahest(ItemInstance alkahest) {
		this.alkahest = alkahest;
	}

	/**
	 * @return затачиваемый предмет.
	 */
	public ItemInstance getSource() {
		return source;
	}

	/**
	 * @param source затачиваемый предмет.
	 */
	public void setSource(ItemInstance source) {
		this.source = source;
	}

	/**
	 * Обновление диалога.
	 */
	private void updateDialog() {

		Player actor = getActor();

		if(actor != null) {
			actor.sendPacket(EnchatItemInfo.getInstance(this), true);
		}
	}

	/**
	 * Ид шаблона предмета по указанному индексу ячейки.
	 * 
	 * @param index индекс ячейки.
	 * @return ид шаблона предмета.
	 */
	public int getItemId(int index) {
		switch(index) {
			case SOURCE_ITEM_INDEX: {

				ItemInstance source = getSource();

				if(source != null) {
					return source.getItemId();
				}

				break;
			}
			case CONSUME_ITEM_INDEX: {

				ItemInstance consume = getConsume();

				if(consume != null) {
					return consume.getItemId();
				}

				break;
			}
			case ALKAHEST_ITEM_INDEX: {

				ItemInstance alkahest = getAlkahest();

				if(alkahest != null) {
					return alkahest.getItemId();
				}

				break;
			}
		}

		return 0;
	}

	/**
	 * Уникальный ид предмета по указанному индексу ячейки.
	 * 
	 * @param index индекс ячейки.
	 * @return уникальный ид предмета.
	 */
	public int getObjectId(int index) {
		switch(index) {
			case SOURCE_ITEM_INDEX: {

				ItemInstance source = getSource();

				if(source != null) {
					return source.getObjectId();
				}

				break;
			}
			case CONSUME_ITEM_INDEX: {

				ItemInstance consume = getConsume();

				if(consume != null) {
					return consume.getObjectId();
				}

				break;
			}
			case ALKAHEST_ITEM_INDEX: {

				ItemInstance alkahest = getAlkahest();

				if(alkahest != null) {
					return alkahest.getObjectId();
				}

				break;
			}
		}

		return 0;
	}

	/**
	 * Кол-во необходимых предметов для заточки в ячейку по указанному индексу.
	 * 
	 * @param index индекс ячейки.
	 * @return кол-во вставляемых предметов.
	 */
	public int getNeedItemCount(int index) {
		switch(index) {
			case SOURCE_ITEM_INDEX: {
				return 1;
			}
			case CONSUME_ITEM_INDEX: {
				return 1;
			}
			case ALKAHEST_ITEM_INDEX: {

				ItemInstance source = getSource();

				if(source != null) {
					return source.getExtractable();
				}

				break;
			}
		}

		return 0;
	}

	/**
	 * Является ли ячейка с указанным индексом ,ячейкой для затачиваемого
	 * предмета.
	 * 
	 * @param index индекс ячейки.
	 * @return является ли ячейка с указанным индексом, ячейкой для
	 * затачиваемого предмета.
	 */
	public boolean isEnchantItem(int index) {
		switch(index) {
			case SOURCE_ITEM_INDEX: {
				return true;
			}
			default: {
				return false;
			}
		}
	}

	/**
	 * Добавление предмета в диалог.
	 * 
	 * @param index индекс ячейки.
	 * @param objectId уникальный ид предмета.
	 * @param itemId ид шаблона предмета.
	 */
	public void addItem(int index, int objectId, int itemId) {

		Player actor = getActor();

		switch(index) {
			case SOURCE_ITEM_INDEX: {

				if(getConsume() != null || getAlkahest() != null) {
					actor.sendMessage("Нужно очистить используемый предмет и alkshest.");
					return;
				}

				ItemInstance source = findItem(objectId, itemId);

				if(source == null) {
					actor.sendMessage("Unable to find item.");
					return;
				}

				if(source.getEnchantLevel() == 9 && source.getMasterworked() == 0){
					actor.sendMessage("Item need to be masterworked.");
					return;
				}

				if(source.getEnchantLevel() >= MAX_ENCHANT_LEVEL) {
					actor.sendMessage("Item cannot be enchanted further.");
					return;
				}

				if(!source.isEnchantable()) {
					actor.sayMessage("Item cannot be enchanted.");
					return;
				}

				setSource(source);
				updateDialog();
				break;
			}
			case CONSUME_ITEM_INDEX: {

				ItemInstance source = getSource();

				if(source == null) {
					actor.sendMessage("Unknonw item.");
					return;
				}

				ItemInstance consume = findItem(objectId, itemId);

				if(consume == null || consume == getSource()) {
					actor.sendMessage("Unable to find item.");
					return;
				}

				if(source.getExtractable() != consume.getExtractable()) {
					actor.sendMessage("Item cannot be extracted");
					return;
				}

				if(!source.getClass().isInstance(consume)) {
					actor.sendMessage("The item is not suitable for the type.");
					return;
				}

				setConsume(consume);
				updateDialog();
				break;
			}
			case ALKAHEST_ITEM_INDEX: {

				ItemInstance source = getSource();

				if(source == null || getConsume() == null) {
					actor.sendMessage("Fill all cells");
					return;
				}

				int[] range = ALKAHEST_TABLE.get(itemId);

				if(range == null) {
					actor.sendMessage("Item isn't Alkahest");
					return;
				}

				if(source.getEnchantLevel() >= range[1] || source.getEnchantLevel() < range[0]) {
					actor.sendMessage("This kind of  Alkahest is not suitable.");
					return;
				}

				ItemInstance alkahest = findItem(objectId, itemId);

				if(alkahest == null) {
					actor.sendMessage("There are no Alkahest.");
					return;
				}

				if(alkahest.getItemCount() < source.getExtractable()) {
					actor.sendMessage("Insufficient number.");
					return;
				}

				setAlkahest(alkahest);
				updateDialog();
			}
		}
	}

	/**
	 * Поиск предмета.
	 * 
	 * @param objectId уникальный ид предмета.
	 * @param itemId ид шаблона прдемета.
	 * @return предмет в инвенторе.
	 */
	public ItemInstance findItem(int objectId, int itemId) {

		Player actor = getActor();

		if(actor == null) {
			return null;
		}

		Inventory inventory = actor.getInventory();

		if(objectId != 0) {
			return inventory.getItemForObjectId(objectId);
		}

		return inventory.getItemForItemId(itemId);
	}

	/**
	 * Уровень заточки затачиваемого предмета.
	 * 
	 * @param index индекс ячейки.
	 * @return уровень заточки.
	 */
	public int getEnchantLevel(int index) {

		if(index == SOURCE_ITEM_INDEX) {

			ItemInstance source = getSource();

			if(source != null) {
				return source.getEnchantLevel();
			}
		}

		return 0;
	}

	@Override
	public void finalyze() {
		setAlkahest(null);
		setConsume(null);
		setSource(null);
	}
}
