package tera.gameserver.model.npc.interaction.replyes;

import org.w3c.dom.Node;
import rlib.util.VarTable;
import tera.gameserver.manager.ObjectEventManager;
import tera.gameserver.manager.PacketManager;
import tera.gameserver.model.MessageType;
import tera.gameserver.model.inventory.Inventory;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.npc.interaction.Link;
import tera.gameserver.model.playable.Player;

/**
 * Created by Luciole on 08/04/2016.
 */
public class ReplyRestoreHPandMP extends AbstractReply {

    private int price;

    public ReplyRestoreHPandMP(Node node){
        super(node);
        this.price = VarTable.newInstance(node).getInteger("price");
    }
    @Override
    public void reply(Npc npc, Player player, Link link) {
        if(player.getCurrentHp() >= player.getMaxHp() && player.getCurrentMp() >= player.getMaxMp())
            return;

        Inventory inventory = player.getInventory();

        if(inventory.getMoney() < price){
            player.sendMessage(MessageType.YOU_DONT_HAVE_ENOUGH_GOLD);
            return;
        }

        inventory.subMoney(price);
        PacketManager.showPaidGold(player, price);

        ObjectEventManager eventManager = ObjectEventManager.getInstance();
        eventManager.notifyInventoryChanged(player);

        player.effectHealHp(player.getMaxHp(), player);
        player.effectHealMp(player.getMaxMp(), player);
    }
}
