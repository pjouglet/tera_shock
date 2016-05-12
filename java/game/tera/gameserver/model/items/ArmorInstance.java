package tera.gameserver.model.items;

import tera.gameserver.templates.ArmorTemplate;
import tera.gameserver.templates.ItemTemplate;

/**
 * Модель брони.
 * 
 * @author Ronn
 */
public final class ArmorInstance extends GearedInstance
{
	/** защита */
	private int defense;
	/** баланс */
	private int balance;

	/**
	 * @param objectId уникальный ид.
	 * @param template темплейт итема.
	 */
	public ArmorInstance(int objectId, ItemTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public boolean checkCrystal(CrystalInstance crystal)
	{
		if (crystals == null || crystal.getType() != CrystalType.ARMOR)
			return false;

		if (crystal.getItemLevel() > template.getItemLevel())
			return false;

		return crystals.hasEmptySlot();
	}

	@Override
	public ArmorInstance getArmor()
	{
		return this;
	}

	/**
	 * @return armorKind материал брони.
	 */
	public ArmorKind getArmorKind()
	{
		return getTemplate().getArmorKind();
	}

	@Override
	public ArmorTemplate getTemplate()
	{
		return (ArmorTemplate) template;
	}

	@Override
	public boolean isArmor()
	{
		return true;
	}

	@Override
	protected void updateEnchantStats()
	{
		int defense = super.getDefence();
		int balance = super.getBalance();

		float mod = 4.5F * getEnchantLevel() / 100F + 1;

		defense *= mod;

		mod = 7F * getEnchantLevel() / 100F + 1;

		balance *= mod;

		setDefense(defense);
		setBalance(balance);
	}

	@Override
	public int getDefence()
	{
		return defense;
	}

	@Override
	public int getBalance()
	{
		return balance;
	}

	private void setDefense(int defense)
	{
		this.defense = defense;
	}

	private void setBalance(int balance)
	{
		this.balance = balance;
	}
}
