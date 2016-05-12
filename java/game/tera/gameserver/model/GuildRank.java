package tera.gameserver.model;

import rlib.util.pools.Foldable;
import rlib.util.pools.FoldablePool;
import rlib.util.pools.Pools;

/**
 * Модель ранга гильдии.
 *
 * @author Ronn
 */
public class GuildRank implements Foldable
{
	private static final FoldablePool<GuildRank> pool = Pools.newConcurrentFoldablePool(GuildRank.class);

	/** константный ид ранга мастера */
	public static final int GUILD_MASTER = 1;
	/** константный ид ранга рядового мембера */
	public static final int GUILD_MEMBER = 3;

	/**
	 * Создание нового ранга.
	 */
	public static GuildRank newInstance(String name, GuildRankLaw law, int index)
	{
		GuildRank rank = pool.take();

		if(rank == null)
			rank = new GuildRank();

		rank.name = name;
		rank.law = law;
		rank.index = index;

		rank.prepare();

		return rank;
	}

	/** название ранга */
	private String name;

	/** набор прав */
	private GuildRankLaw law;

	/** индекс ранга */
	private int index;

	/** можно ли приглашать/исключать в гильдию */
	private boolean changeLineUp;
	/** можно ли работать с банком */
	private boolean accessBank;
	/** можно ли изменять титул */
	private boolean changeTitle;
	/** можно ли обьявляеть войну */
	private boolean guildWars;

	@Override
	public void finalyze()
	{
		name = null;
		law = GuildRankLaw.MEMBER;
	}

	/**
	 * Складировать.
	 */
	public void fold()
	{
		pool.put(this);
	}

	/**
	 * @return индекс ранга.
	 */
	public int getIndex()
	{
		return index;
	}

	/**
	 * @return ид набора прав.
	 */
	public int getLawId()
	{
		return law.ordinal();
	}

	/**
	 * @return название ранга.
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @return может ли лазить в банк гильдии.
	 */
	public boolean isAccessBank()
	{
		return accessBank;
	}

	/**
	 * @return может ли приглашать/кикать.
	 */
	public boolean isChangeLineUp()
	{
		return changeLineUp;
	}

	/**
	 * @return может ли изменять титулы.
	 */
	public boolean isChangeTitle()
	{
		return changeTitle;
	}

	/**
	 * @return является ли мастером гильдии.
	 */
	public boolean isGuildMaster()
	{
		return law == GuildRankLaw.GUILD_MASTER;
	}

	/**
	 * @return может ли обьявлять войну.
	 */
	public boolean isGuildWars()
	{
		return guildWars;
	}

	/**
	 * Формирование набора флагов в соответствии с типом прав.
	 */
	public void prepare()
	{
		changeLineUp = false;
		accessBank = false;
		changeTitle = false;
		guildWars = false;

		switch(law)
		{
			case BANK: accessBank = true; break;
			case BANK_TITLE:
			{
				accessBank = true;
				changeTitle = true;

				break;
			}
			case BANK_TITLE_GVG:
			{
				accessBank = true;
				changeTitle = true;
				guildWars = true;

				break;
			}
			case GUILD_MASTER:
			{
				accessBank = true;
				changeTitle = true;
				changeLineUp = true;
				guildWars = true;

				break;
			}
			case GVG: guildWars = true; break;
			case LINE_UP: changeLineUp = true; break;
			case LINE_UP_BANK:
			{
				changeLineUp = true;
				accessBank = true;

				break;
			}
			case LINE_UP_BANK_GVG:
			{
				changeLineUp = true;
				accessBank = true;
				guildWars = true;

				break;
			}
			case LINE_UP_BANK_TITLE:
			{
				changeLineUp = true;
				accessBank = true;
				changeTitle = true;

				break;
			}
			case LINE_UP_BANK_TITLE_GVG:
			{
				changeLineUp = true;
				accessBank = true;
				changeTitle = true;
				guildWars = true;

				break;
			}
			case LINE_UP_GVG:
			{
				changeLineUp = true;
				guildWars = true;

				break;
			}
			case LINE_UP_TITLE:
			{
				changeLineUp = true;
				changeTitle = true;

				break;
			}
			case LINE_UP_TITLE_GVG:
			{
				changeLineUp = true;
				changeTitle = true;
				guildWars = true;

				break;
			}
			case TITLE: changeTitle = true; break;
			case TITLE_GVG:
			{
				guildWars = true;
				changeTitle = true;
			}
			default:
				break;
		}
	}

	@Override
	public void reinit(){}

	/**
	 * @param law набор прав.
	 */
	public void setLaw(GuildRankLaw law)
	{
		this.law = law;
	}

	/**
	 * @param name название ранга.
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	@Override
	public String toString()
	{
		return "GuildRank  name = " + name + ", law = " + law + ", index = " + index + ", changeLineUp = " + changeLineUp + ", accessBank = " + accessBank + ", changeTitle = " + changeTitle + ", guildWars = " + guildWars;
	}
}
