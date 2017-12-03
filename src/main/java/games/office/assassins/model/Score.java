package games.office.assassins.model;

import java.util.ArrayList;

import lombok.Getter;

/**
 * Keeps track of a player's weekly score, targets, assassins, and bonuses.
 * Primarily a helper to the Player class
 */
@Getter
public class Score {
	public Score() {
		targetPlayerIds = new ArrayList();
		assassinPlayerIds = new ArrayList();
		bonuses = new ArrayList();
	}

	/** Always set to the value of killCount - deathCount */
	private int score;

	/** Number of kills made */
	private int kills;

	/** Number of times killed */
	private int deaths;

	/** Ids of players that this player has killed */
	private ArrayList<Integer> targetPlayerIds;

	/** Ids of players who assassinated this player */
	private ArrayList<Integer> assassinPlayerIds;

	/** Holds any bonuses earned by the player for the week */
	private ArrayList<Bonus> bonuses;

	/** Adds a new assassin that killed this player */
	public void addAssassinKiller(int playerId) {
		assassinPlayerIds.add(playerId);
		recomputeTotals();
	}

	/** Adds a new target that this player killed */
	public void addTargetKilled(int playerId) {
		targetPlayerIds.add(playerId);
		recomputeTotals();
	}

	public void addBonus(Bonus bonus) {
		if (bonus == null) {
			return;
		}

		bonuses.add(bonus);
		recomputeTotals();
	}

	/**
	 * Recalculates kills, deaths, and total score using existing class data. Called whenever new data is added that
	 * may or may not affect the scores.
	 */
	private void recomputeTotals() {
		kills = targetPlayerIds.size();
		deaths = assassinPlayerIds.size();
		int totalBonusPoints = 0;
		for (Bonus i : bonuses) {
			totalBonusPoints += i.getBonusPoints();
		}
		score = kills - deaths + totalBonusPoints;
	}
}
