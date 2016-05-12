package tera.gameserver.model.equipment;

/**
 * Перечисление типов одеваемых слотов
 *
 * @author Ronn
 */
public enum SlotType
{
	/** не одеваемый итем */
	NONE,
	/** шапка */
	SLOT_HAT,
	/** маска */
	SLOT_MASK,
	/** майка */
	SLOT_SHIRT,
	/** перчатки */
	SLOT_GLOVES,
	/** боты */
	SLOT_BOOTS,
	/** сам доспех */
	SLOT_ARMOR,
	/** оружие */
	SLOT_WEAPON,
	/** серьга */
	SLOT_EARRING,
	/** кольцо */
	SLOT_RING,
	/** ожерелье */
	SLOT_NECKLACE,
	/** слот для ремодел пушки */
	SLOT_REMODELABLE_WEAPON,
	/** слот для ремодел брони */
	SLOT_ARMOR_REMODEL;

	public static final int SIZE = values().length;
}
