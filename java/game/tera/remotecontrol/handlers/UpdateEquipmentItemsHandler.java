package tera.remotecontrol.handlers;

import java.util.ArrayList;

import tera.gameserver.model.World;
import tera.gameserver.model.equipment.Equipment;
import tera.gameserver.model.equipment.Slot;
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
public class UpdateEquipmentItemsHandler implements PacketHandler
{
	public static final UpdateEquipmentItemsHandler instance = new UpdateEquipmentItemsHandler();
	
	@Override
	public Packet processing(Packet packet)
	{
		Player player = World.getPlayer(packet.nextString());
		
		if(player == null)
			return null;
		
		Equipment equipment = player.getEquipment();
		
		if(equipment == null)
			return null;
		
		ArrayList<Object[]> items = new ArrayList<Object[]>();
		
		equipment.lock();
		try
		{
			Slot[] slots = equipment.getSlots();
			
			for(int i = 0, length = slots.length; i < length; i++)
			{
				Slot slot = slots[i];
				
				if(slot.isEmpty())
					continue;
				
				ItemInstance item = slot.getItem();
				
				items.add(new Object[]
				{
					item.getName(),
					slot.getIndex(),
					item.getObjectId(),
				});
			}
		}
		finally
		{
			equipment.unlock();
		}
		
		return new Packet(PacketType.RESPONSE, items);
	}
}
