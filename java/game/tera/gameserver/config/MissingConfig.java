package tera.gameserver.config;

import rlib.util.VarTable;
import tera.gameserver.document.DocumentConfig;

import java.io.File;

/**
 * Created by Luciole on 05/12/2015.
 */
public final class MissingConfig {
    public static int WORLD_MIN_TARGET_LEVEl_FOR_PK;
    public static int EVENT_TDM_MAX_LEVEL;
    public static int EVENT_TDM_MIN_LEVEL;
    public static int EVENT_TDM_REGISTER_TIME;
    public static int EVENT_TDM_MAX_PLAYERS;
    public static int EVENT_TDM_MIN_PLAYERS;
    public static int EVENT_TDM_BATTLE_TIME;
    public static boolean SERVER_FALLING_DAMAGE;

    public static void init(){
        VarTable vars = VarTable.newInstance();
        vars.set((VarTable)(new DocumentConfig(new File("./config/Misc.xml")).parse()));
        WORLD_MIN_TARGET_LEVEl_FOR_PK = vars.getInteger("WORLD_MIN_TARGET_LEVEL_FOR_PK");
        EVENT_TDM_MAX_LEVEL = vars.getInteger("EVENT_TDM_MAX_LEVEL");
        EVENT_TDM_MIN_LEVEL = vars.getInteger("EVENT_TDM_MIN_LEVEL");
        EVENT_TDM_REGISTER_TIME = vars.getInteger("EVENT_TDM_REGISTER_TIME");
        EVENT_TDM_MAX_PLAYERS = vars.getInteger("EVENT_TDM_MAX_PLAYERS");
        EVENT_TDM_MIN_PLAYERS = vars.getInteger("EVENT_TDM_MIN_PLAYERS");
        EVENT_TDM_BATTLE_TIME = vars.getInteger("EVENT_TDM_BATTLE_TIME");
        SERVER_FALLING_DAMAGE = vars.getBoolean("SERVER_FALLING_DAMAGE");
    }
}
