package tera.remotecontrol.handlers;

import java.util.ArrayList;

import tera.gameserver.model.World;
import tera.gameserver.model.inventory.Cell;
import tera.gameserver.model.inventory.Inventory;
import tera.gameserver.model.items.ItemInstance;
import tera.gameserver.model.playable.Player;
import tera.remotecontrol.Packet;
import tera.remotecontrol.PacketHandler;
import tera.remotecontrol.PacketType;


/**
 * Загрузка списка игроков
 *
 * @author Ronn
 * @created 09.04.2012
 */
public class UpdateInventoryItemsHandler implements PacketHandler
{
	public static final UpdateInventoryItemsHandler instance = new UpdateInventoryItemsHandler();
	
	@Override
	public Packet processing(Packet packet)
	{
		Player player = World.getPlayer(packet.nextString());
		
		if(player == null)
			return null;
		
		Inventory inventory = player.getInventory();
		
		if(inventory == null)
			return null;
		
		ArrayList<Object[]> items = new ArrayList<Object[]>();
		
		inventory.lock();
		try
		{
			Cell[] cells = inventory.getCells();
			
			for(int i = 0, length = cells.length; i < length; i++)
			{
				Cell cell = cells[i];
				
				if(cell.isEmpty())
					continue;
				
				ItemInstance item = cell.getItem();
				
				items.add(new Object[]
				{
					item.getName(),
					cell.getIndex(),
					item.getItemCount(),
					item.getObjectId(),
					item.isStackable(),
				});
			}
			
			Cell gold = inventory.getGold();
			
			if(!gold.isEmpty())
			{
				ItemInstance item = gold.getItem();
				
				items.add(new Object[]
				{
					item.getName(),
					-1,
					item.getItemCount(),
					item.getObjectId(),
					item.isStackable(),
				});
			}
		}
		finally
		{
			inventory.unlock();
		}
		
		return new Packet(PacketType.RESPONSE, items);
	}
}
