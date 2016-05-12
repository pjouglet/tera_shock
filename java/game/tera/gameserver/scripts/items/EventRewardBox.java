package tera.gameserver.scripts.items;

import rlib.util.random.Random;
import rlib.util.random.Randoms;
import tera.gameserver.manager.ObjectEventManager;
import tera.gameserver.model.MessageType;
import tera.gameserver.model.inventory.Inventory;
import tera.gameserver.model.items.ItemInstance;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.serverpackets.MessageAddedItem;
import tera.gameserver.network.serverpackets.SystemMessage;

/**
 * Модель призовой бонусной коробки.
 *
 * @author Ronn
 */
public class EventRewardBox extends AbstractItemExecutor
{
	/**
	 * @author Ronn
	 */
	private static class RewardInfo
	{
		/** ид итема */
		private int itemId;
		/** минимальное кол-во */
		private int minCount;
		/** максимальное кол-во */
		private int maxCount;
		/** шанс выпадения */
		private int chance;

		public RewardInfo(int itemId, int minCount, int maxCount, int chance)
		{
			this.itemId = itemId;
			this.minCount = minCount;
			this.maxCount = maxCount;
			this.chance = chance;
		}

		/**
		 * @return chance
		 */
		public final int getChance()
		{
			return chance;
		}

		/**
		 * @return itemId
		 */
		public final int getItemId()
		{
			return itemId;
		}

		/**
		 * @return maxCount
		 */
		public final int getMaxCount()
		{
			return maxCount;
		}

		/**
		 * @return minCount
		 */
		public final int getMinCount()
		{
			return minCount;
		}
	}

	private static RewardInfo[][] REWARD =
	{
		// 0: 0 - 34
		{
			// Sylva Medicated Bandage
			new RewardInfo(174, 1, 10, 50000),
			// Speed Potion
			new RewardInfo(8007, 1, 10, 50000),

			// Major Mana Potion IV
			new RewardInfo(6023, 1, 5, 20000),
			// Major Healing Potion IV
			new RewardInfo(6007, 1, 5, 20000),

			// Onslaught Charm I
			new RewardInfo(7100, 1, 10, 30000),
			// Etheral Charm I
			new RewardInfo(7104, 1, 10, 30000),
			// Sanguine Charm I
			new RewardInfo(7108, 1, 10, 30000),

			// Major Healing Elixir IV
			new RewardInfo(6055, 1, 5, 10000),
			// Major Mana Elixir IV
			new RewardInfo(6071, 1, 5, 10000),

			new RewardInfo(50017, 1, 1, 10),
			new RewardInfo(50018, 1, 1, 10),
			new RewardInfo(50021, 1, 1, 10),
			new RewardInfo(50022, 1, 1, 10),
			new RewardInfo(50023, 1, 1, 10),
			new RewardInfo(50024, 1, 1, 10),
			new RewardInfo(50025, 1, 1, 10),
			new RewardInfo(50026, 1, 1, 10),
			new RewardInfo(50027, 1, 1, 10),
			new RewardInfo(50028, 1, 1, 10),
			new RewardInfo(50029, 1, 1, 10),
			new RewardInfo(50030, 1, 1, 10),
			new RewardInfo(50031, 1, 1, 10),
			new RewardInfo(50032, 1, 1, 10),
			new RewardInfo(50033, 1, 1, 10),
			new RewardInfo(50034, 1, 1, 10),
			new RewardInfo(50035, 1, 1, 10),
			new RewardInfo(50036, 1, 1, 10),
			new RewardInfo(50037, 1, 1, 10),
			new RewardInfo(50038, 1, 1, 10),
			new RewardInfo(50039, 1, 1, 10),
			new RewardInfo(50040, 1, 1, 10),
		},
		// 1: 35 - 47
		{
			// Shetla Medicated Bandage
			new RewardInfo(175, 1, 10, 50000),
			// Speed Potion
			new RewardInfo(8007, 1, 10, 50000),

			// Major Mana Potion V
			new RewardInfo(6025, 1, 5, 20000),
			// Major Healing Potion V
			new RewardInfo(6009, 1, 5, 20000),

			// Onslaught Charm II
			new RewardInfo(7101, 1, 10, 30000),
			// Etheral Charm II
			new RewardInfo(7105, 1, 10, 30000),
			// Sanguine Charm II
			new RewardInfo(7109, 1, 10, 30000),

			// Major Healing Elixir V
			new RewardInfo(6057, 1, 5, 10000),
			// Major Mana Elixir V
			new RewardInfo(6073, 1, 5, 10000),

			new RewardInfo(50017, 1, 1, 20),
			new RewardInfo(50018, 1, 1, 20),
			new RewardInfo(50021, 1, 1, 20),
			new RewardInfo(50022, 1, 1, 20),
			new RewardInfo(50023, 1, 1, 20),
			new RewardInfo(50024, 1, 1, 20),
			new RewardInfo(50025, 1, 1, 20),
			new RewardInfo(50026, 1, 1, 20),
			new RewardInfo(50027, 1, 1, 20),
			new RewardInfo(50028, 1, 1, 20),
			new RewardInfo(50029, 1, 1, 20),
			new RewardInfo(50030, 1, 1, 20),
			new RewardInfo(50031, 1, 1, 20),
			new RewardInfo(50032, 1, 1, 20),
			new RewardInfo(50033, 1, 1, 20),
			new RewardInfo(50034, 1, 1, 20),
			new RewardInfo(50035, 1, 1, 20),
			new RewardInfo(50036, 1, 1, 20),
			new RewardInfo(50037, 1, 1, 20),
			new RewardInfo(50038, 1, 1, 20),
			new RewardInfo(50039, 1, 1, 20),
			new RewardInfo(50040, 1, 1, 20),
		},
		// 2: 48 - 57
		{
			// Toira Medicated Bandage
			new RewardInfo(176, 1, 10, 50000),
			// Speed Potion
			new RewardInfo(8007, 1, 10, 50000),

			// Major Mana Potion VI
			new RewardInfo(6027, 1, 5, 20000),
			// Major Healing Potion VI
			new RewardInfo(6011, 1, 5, 20000),

			// Onslaught Charm III
			new RewardInfo(7102, 1, 10, 30000),
			// Etheral Charm III
			new RewardInfo(7106, 1, 10, 30000),
			// Sanguine Charm III
			new RewardInfo(7110, 1, 10, 30000),

			// Major Healing Elixir VI
			new RewardInfo(6059, 1, 5, 10000),
			// Major Mana Elixir VI
			new RewardInfo(6075, 1, 5, 10000),

			new RewardInfo(50017, 1, 1, 30),
			new RewardInfo(50018, 1, 1, 30),
			new RewardInfo(50021, 1, 1, 30),
			new RewardInfo(50022, 1, 1, 30),
			new RewardInfo(50023, 1, 1, 30),
			new RewardInfo(50024, 1, 1, 30),
			new RewardInfo(50025, 1, 1, 30),
			new RewardInfo(50026, 1, 1, 30),
			new RewardInfo(50027, 1, 1, 30),
			new RewardInfo(50028, 1, 1, 30),
			new RewardInfo(50029, 1, 1, 30),
			new RewardInfo(50030, 1, 1, 30),
			new RewardInfo(50031, 1, 1, 30),
			new RewardInfo(50032, 1, 1, 30),
			new RewardInfo(50033, 1, 1, 30),
			new RewardInfo(50034, 1, 1, 30),
			new RewardInfo(50035, 1, 1, 30),
			new RewardInfo(50036, 1, 1, 30),
			new RewardInfo(50037, 1, 1, 30),
			new RewardInfo(50038, 1, 1, 30),
			new RewardInfo(50039, 1, 1, 30),
			new RewardInfo(50040, 1, 1, 30),
		},
		// 3: 58 - 60
		{
			// Luria Medicated Bandage
			new RewardInfo(177, 1, 10, 50000),
			// Speed Potion
			new RewardInfo(8007, 1, 10, 50000),

			// Major Mana Potion VIII
			new RewardInfo(6031, 1, 5, 20000),
			// Major Healing Potion VIII
			new RewardInfo(6015, 1, 5, 20000),

			// Onslaught Charm IV
			new RewardInfo(7103, 1, 10, 30000),
			// Etheral Charm IV
			new RewardInfo(7107, 1, 10, 30000),
			// Sanguine Charm IV
			new RewardInfo(7111, 1, 10, 30000),

			// Major Healing Elixir VIII
			new RewardInfo(6063, 1, 5, 10000),
			// Major Mana Elixir VIII
			new RewardInfo(6079, 1, 5, 10000),

			new RewardInfo(50017, 1, 1, 40),
			new RewardInfo(50018, 1, 1, 40),
			new RewardInfo(50021, 1, 1, 40),
			new RewardInfo(50022, 1, 1, 40),
			new RewardInfo(50023, 1, 1, 40),
			new RewardInfo(50024, 1, 1, 40),
			new RewardInfo(50025, 1, 1, 40),
			new RewardInfo(50026, 1, 1, 40),
			new RewardInfo(50027, 1, 1, 40),
			new RewardInfo(50028, 1, 1, 40),
			new RewardInfo(50029, 1, 1, 40),
			new RewardInfo(50030, 1, 1, 40),
			new RewardInfo(50031, 1, 1, 40),
			new RewardInfo(50032, 1, 1, 40),
			new RewardInfo(50033, 1, 1, 40),
			new RewardInfo(50034, 1, 1, 40),
			new RewardInfo(50035, 1, 1, 40),
			new RewardInfo(50036, 1, 1, 40),
			new RewardInfo(50037, 1, 1, 40),
			new RewardInfo(50038, 1, 1, 40),
			new RewardInfo(50039, 1, 1, 40),
			new RewardInfo(50040, 1, 1, 40),
		},
	};

	/** рандоминайзер */
	private Random random;

	public EventRewardBox(int[] itemIds, int access)
	{
		super(itemIds, access);

		// создаем реальный рандоминайзер
		random = Randoms.newRealRandom();
	}

	@Override
	public void execution(ItemInstance item, Player player)
	{
		// получаем индекс награды
		int index = item.getItemId() - 408;

		// если не подходящий, выходим
		if(index < 0 || index >= REWARD.length)
			return;

		RewardInfo[] rewards = REWARD[index];

		if(rewards.length < 1)
		{
			player.sendMessage("для вашего уровня сундука награды еще нет, попробуйте позже.");
			return;
		}

		// отправляем пакет о использовании рецепта
		player.sendPacket(SystemMessage.getInstance(MessageType.ITEM_USE).addItem(item.getItemId(), 1), true);

		// получаем инвентарь
		Inventory inventory = player.getInventory();

		// если удалить итем не вышло, выходим
		if(!inventory.removeItem(item.getItemId(), 1L))
			return;

		// перебираем награду
		for(int i = 0, length = rewards.length; i < length; i++)
		{
			RewardInfo reward = rewards[i];

			if(random.nextInt(0, 100000) > reward.getChance())
				continue;

			// рассчитываем кол-во
			int count = random.nextInt(reward.getMinCount(), reward.getMaxCount());

			// добавляем итем в инвентарь
			if(inventory.addItem(reward.getItemId(), count, "EventRewardBox"))
				// отпправляем пакет о выдачи
				player.sendPacket(MessageAddedItem.getInstance(player.getName(), reward.getItemId(), count), true);
		}

		// получаем менеджера событий
		ObjectEventManager eventManager = ObjectEventManager.getInstance();

		// обновляем инвентарь
		eventManager.notifyInventoryChanged(player);
	}
}
