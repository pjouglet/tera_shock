package tera.gameserver.network.clientpackets;

import tera.gameserver.manager.ObjectEventManager;
import tera.gameserver.model.World;
import tera.gameserver.model.npc.summons.Summon;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.serverpackets.DeleteCharacter;
import tera.gameserver.network.serverpackets.Tp1;
import tera.gameserver.network.serverpackets.WorldZone;
import tera.gameserver.tables.WorldZoneTable;
import tera.util.Location;

public class RequestPlayerUnstuck extends ClientPacket
{
    private Player player;

    @Override
    public void finalyze()
    {
        player = null;
    }

    @Override
    public boolean isSynchronized()
    {
        return false;
    }
    @Override
    public void readImpl()
    {
        player = owner.getOwner();
        readInt();
        readInt();
    }

    public void runImpl()
    {
        if(player == null || !player.isResurrected())
            return;

        Summon summon = player.getSummon();

        if(summon != null)
            summon.remove();

        player.decayMe(DeleteCharacter.DISAPPEARS);
        WorldZoneTable zoneTable = WorldZoneTable.getInstance();
        Location point = zoneTable.getRespawn(player);

        if(point == null)
        {
            log.warning(this, "not found respawn for " + player.getLoc());
            return;
        }

        player.setLoc(point);

        int zoneId = World.getRegion(player).getZoneId(player);

        if(zoneId < 1)
            zoneId = player.getContinentId() + 1;

        player.setZoneId(zoneId);
        ObjectEventManager eventManager = ObjectEventManager.getInstance();
        eventManager.notifyChangedZoneId(player);

        if(!player.isInBattleTerritory())
            player.setStamina(120);
        player.setCurrentHp(player.getMaxHp());
        player.setCurrentMp(player.getMaxMp());

        player.sendPacket(Tp1.getInstance(player), true);
        player.sendPacket(WorldZone.getInstance(player), true);
    }
}