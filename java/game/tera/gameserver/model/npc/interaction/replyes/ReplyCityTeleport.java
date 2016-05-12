package tera.gameserver.model.npc.interaction.replyes;

import org.w3c.dom.Node;
import rlib.util.VarTable;
import tera.gameserver.model.inventory.Cell;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.npc.interaction.Link;
import tera.gameserver.model.playable.Player;
import tera.util.Location;

import java.io.*;

/**
 * Created by Luciole on 08/04/2016.
 */
public class ReplyCityTeleport extends AbstractReply {

    private float x;
    private float y;
    private float z;
    private int id;
    private int heading;

    public ReplyCityTeleport(Node node){
        super(node);
        x = VarTable.newInstance(node).getFloat("x");
        y = VarTable.newInstance(node).getFloat("y");
        z = VarTable.newInstance(node).getFloat("z");
        id = VarTable.newInstance(node).getInteger("id");
        heading = VarTable.newInstance(node).getInteger("heading");
    }

    @Override
    public void reply(Npc npc, Player player, Link link) {
        player.teleToLocation(id, x, y, z, heading);
        //player.teleToLocation(0, (float)-583.9613, (float)6423.1274, (float)1955.9696, 22532);
    }
}
