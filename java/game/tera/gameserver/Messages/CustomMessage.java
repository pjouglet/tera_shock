package tera.gameserver.Messages;

import rlib.util.VarTable;
import tera.gameserver.document.DocumentConfig;

import java.io.File;

/**
 * Created by Luciole on 05/12/2015.
 */
public final class CustomMessage {
    //events/auto/AbstractAutoEvent.java
    public static String MINUTES;
    public static String EVENT_NO_RUNNING;
    public static String EVENT_REGISTRATION_LEFT;
    public static String EVENT_NO_LVL_REQUIRED;
    public static String EVENT_ALREADY_REGISTERED;
    public static String EVENT_PLAYER_DEAD;
    public static String EVENT_PLAYER_IN_DUEL;
    public static String EVENT_REGISTRATION_OK;
    public static String EVENT_TIME_BEFORE_EVENT;
    public static String REGISTERED_PARTICIPANTS;
    public static String EVENT_HOW_TO_REGISTER;
    public static String EVENT_START_MESSAGE;
    public static String EVENT_END_MESSAGE;
    public static String EVENT_CAN_REGISTER;
    public static String EVENT_NO_REGISTER;
    public  static String EVENT_USER_REGISTERED;

    //events/auto/EpicBattle.java
    public static String EVENT_PLAYER_LEFT_EVENT;
    public static String EVENT_FIGHT_OVER;
    public static void init(){
        VarTable vars = VarTable.newInstance();
        vars.set(new DocumentConfig(new File("./config/Messages.xml")).parse());
        //events/auto/AbstractAutoEvent.java
        MINUTES = vars.getString("MINUTES");
        EVENT_NO_RUNNING = vars.getString("EVENT_NO_RUNNING");
        EVENT_REGISTRATION_LEFT = vars.getString("EVENT_REGISTRATION_LEFT");
        EVENT_NO_LVL_REQUIRED = vars.getString("EVENT_NO_LVL_REQUIRED");
        EVENT_ALREADY_REGISTERED = vars.getString("EVENT_ALREADY_REGISTERED");
        EVENT_PLAYER_DEAD = vars.getString("EVENT_PLAYER_DEAD");
        EVENT_PLAYER_IN_DUEL = vars.getString("EVENT_PLAYER_IN_DUEL");
        EVENT_REGISTRATION_OK = vars.getString("EVENT_REGISTRATION_OK");
        EVENT_TIME_BEFORE_EVENT = vars.getString("EVENT_TIME_BEFORE_EVENT");
        REGISTERED_PARTICIPANTS = vars.getString("REGISTERED_PARTICIPANTS");
        EVENT_HOW_TO_REGISTER = vars.getString("EVENT_HOW_TO_REGISTER");
        EVENT_START_MESSAGE = vars.getString("EVENT_START_MESSAGE");
        EVENT_END_MESSAGE = vars.getString("EVENT_END_MESSAGE");
        EVENT_CAN_REGISTER = vars.getString("EVENT_CAN_REGISTER");
        EVENT_NO_REGISTER = vars.getString("EVENT_NO_REGISTER");
        EVENT_USER_REGISTERED = vars.getString("EVENT_USER_REGISTERED");

        //events/auto/EpicBattle.java
        EVENT_PLAYER_LEFT_EVENT = vars.getString("EVENT_PLAYER_LEFT_EVENT");
        EVENT_FIGHT_OVER = vars.getString("EVENT_FIGHT_OVER");
    }
}
