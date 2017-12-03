package games.office.assassins;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import games.office.assassins.model.Player;

/**
 * Examines the scores of all players relative to one another to determine ranks. Rankings are determined by the following
 * algorithm:
 * - Players with a higher score are ranked higher
 * - Players with a higher kill count are ranked higher if scores are tied
 * - Players with a lower death count are ranked higher if scores and kills are tied
 *
 * This class is a little wonky with how ranks are assigned due to the fact that eliminated players can be either included
 * or ignored, and eliminated players are assigned a negative ranking number. This class can be improved by not ignoring
 * eliminated players, but ranking players in an earlier eliminated week below eliminated or active players
 * in a later week. Getting rid of the negative ranking numbers would be ideal as well.
 */
public class PlayerRank {
	/** A map containing all player and game data loaded from the database. The player ID is the key */
	private HashMap<Integer, Player> playerData;

	/**
	 * Creates a copy of player data that is ready to be ranked.
	 *
	 * @param data The player data to clone a copy of
	 * @param ignoreEliminatedPlayers If true, eliminated players will be discarded and not ranked
	 */
	public PlayerRank(HashMap<Integer, Player> data, boolean ignoreEliminatedPlayers) {
		if (data == null || data.isEmpty()) {
			throw new RuntimeException("Null or empty player data passed to PlayerRank constructor");
		}
		playerData = (HashMap<Integer, Player>) data.clone();

		if (ignoreEliminatedPlayers == true) {
			ArrayList<Integer> playersToRemove = new ArrayList();
			for (int i : playerData.keySet()) {
				if (playerData.get(i).isPlayerEliminated() == true) {
					playersToRemove.add(i);
				}
			}

			for (int i : playersToRemove) {
				playerData.remove(i);
			}
		}
	}

	/**
	 * Assigns the rank member for all Player objects in a hash. Active players are assigned ranks of (1..n), while
	 * eliminated players are ranked on the order of (-1..-n).
	 *
	 * @param allPlayerData A reference to the player data structure to set rankings for
	 */
	public static void assignPlayerRanks(HashMap<Integer, Player> allPlayerData) {
		ArrayList<Player> rankingList = new ArrayList(allPlayerData.values());
		Collections.sort(rankingList);

		int rankNumber = 1;
		int eliminatedRankNumber = -1;
		for (Player p : rankingList) {
			if (p.isPlayerEliminated() == true) {
				p.setRank(eliminatedRankNumber);
				eliminatedRankNumber--;
			}
			else {
				p.setRank(rankNumber);
				rankNumber++;
			}
		}
	}

	/**
	 * Prints rankings for all players to the screen
	 */
	public void printPlayerRanks() {
		ArrayList<Player> rankingList = new ArrayList(playerData.values());
		Collections.sort(rankingList);

		System.out.println("========== Player Rankings ==========");
		System.out.println("(Rank) ID: Name ... Score/Kills/Deaths\n");

		int rankNumber = 1;
		for (Player p : rankingList) {
			System.out.println("(" + rankNumber + ") " + p.getId() + ": " + p.getFirstName() + " " + p.getLastName());
			System.out.println("    " + p.getTotalScore() + "/" + p.getTotalKills() + "/" + p.getTotalDeaths());
			rankNumber++;
		}
	}
}
