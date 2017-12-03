package games.office.assassins;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import lombok.Setter;

import games.office.assassins.model.Player;
import games.office.assassins.model.Score;

/**
 * Generates the weekly e-mails to be sent to individual players. This involves using one of several
 * available algorithms to generate and assign targets to active players. E-mails also include other data such
 * as a player summary and score data. The e-mail address, subject, and text for all players is printed to
 * a single text file, and must be copied over manually to generate the individual e-mails.
 */
public class WeeklySummaryAssignment {
	/** A map containing all player and game data loaded from the database. The player ID is the key */
	private final HashMap<Integer, Player> playerData;
	
	/** The list of target names for placers */
	private HashMap<Integer, String> targetAssignments;

	/** The number of targets to be assigned to each active player */
	private final int numberTargets;

	/** The number of players in playerData that have not been eliminated */
	private final int activePlayerCount;

	/** Holds the name of the output file to write to */
	@Setter
	private String outputFilename = "emails/unnamed_output_list.txt";

	/**
	 *
	 * @param data
	 * @param targets
	 */
	public WeeklySummaryAssignment(HashMap<Integer, Player> data, int targets) {
		targetAssignments = new HashMap();

		if (data == null || data.isEmpty()) {
			throw new RuntimeException("Null or empty player data passed to WeeklySummaryAssignment constructor");
		}
		playerData = data;

		int activePlayers = 0;
		for (Player p : playerData.values()) {
			if (p.isPlayerEliminated() == false) {
				activePlayers++;
			}
		}
		activePlayerCount = activePlayers;

		if (targets <= 0 || targets >= activePlayerCount) {
			throw new RuntimeException("Invalid number of targets in WeeklySummaryAssignment constructor: " + targets);
		}
		numberTargets = targets;
	}
	
	/**
	 * Assigns random targets to all active players. See assignListRandomTargets for detailed information on
	 * how the assignments are made.
	 */
	public void assignRandomTargets() {
		// Build a list of all active player IDs
		ArrayList<Integer> activePlayerIds = new ArrayList();
		for (Player p : playerData.values()) {
			if (p.isPlayerEliminated() == false) {
				activePlayerIds.add(p.getId());
			}
		}

		assignListRandomTargets(activePlayerIds);
	}

	/**
	 * Active players are split into two tiers. Players are assigned to random targets within their tier in the same
	 * manner as assignRandomTargets() does.
	 */
	public void assignTieredRandomTargets() {
		// First assign the rankings for all players
		PlayerRank.assignPlayerRanks(playerData);

		// Determine how many players in the game are still active and divide that by two to get the tier split
		int splitRankNumber = 0;
		for (Player p : playerData.values()) {
			if (p.isPlayerEliminated() == false) {
				splitRankNumber++;
			}
		}
		splitRankNumber = splitRankNumber / 2;

		// Now build two player lists: one for the top tier players and another for the bottom tier players. Populate
		// each, and send each container off to be randomized and target assignments made
		ArrayList<Integer> topTierPlayerIds = new ArrayList();
		ArrayList<Integer> bottomTierPlayerIds = new ArrayList();
		for (Player p : playerData.values()) {
			if (p.isPlayerEliminated() == true) {
				continue;
			}

			if (p.getRank() <= splitRankNumber) {
				topTierPlayerIds.add(p.getId());
			}
			else {
				bottomTierPlayerIds.add(p.getId());
			}
		}

		assignListRandomTargets(topTierPlayerIds);
		assignListRandomTargets(bottomTierPlayerIds);
	}

	/**
	 * Places all active players into groups of size (1 + number of targets) and then assigns all players within those
	 * groups to each other as targets. This algorithm will fail if the number of active players is not evenly divisible
	 * by (1 + numberTargets).
	 */
	public void assignMutualTargets() {
		// Build a list of all active player IDs
		int activePlayerCount = 0;
		ArrayList<Integer> randomizedTargetList = new ArrayList();
		for (Player p : playerData.values()) {
			if (p.isPlayerEliminated() == false) {
				activePlayerCount++;
				randomizedTargetList.add(p.getId());
			}
		}

		// Ascertain that the number of active players is evenly divisible by the number of targets + 1
		// This ensures that we will have even sized groups.
		if (activePlayerCount % (numberTargets + 1) != 0) {
			System.out.println("Active player count not evenly divisible by target number + 1");
			return;
		}

		// Shuffle the list of IDs. We can then iterate through here and assign targets
		Collections.shuffle(randomizedTargetList, new Random());
		String targetOrder = "";
		for (Integer i : randomizedTargetList) {
			targetOrder += i + ",";
		}
		System.out.println(targetOrder);

		for (int i = 0; i < randomizedTargetList.size(); i = i + (1 + numberTargets)) {
			List<Integer> targetGroup = randomizedTargetList.subList(i, i + (1+ numberTargets));
			for (int j = 0; j < targetGroup.size(); ++j) {
				int playerId = targetGroup.get(j);
				String targets = "";
				for (int k = 0; k < targetGroup.size(); ++k) {
					if (playerId == targetGroup.get(k)) {
						continue;
					}

					Player target = playerData.get(targetGroup.get(k));
					targets += "- " + target.getFirstName() + " " + target.getLastName() + "\n";
				}

				targetAssignments.put(playerId, targets);
			}
		}
	}

	/**
	 * A helper function that takes a list of player IDs, randomly sorts them, and assigns targets to each
	 * player in the list based on the numberTargets. Each player's target list is set to the players at
	 * incremental locations from them in the list. This has the effect of having a high probability that a player
	 * and a particular target will share a mutual second target player in common, leading to some interesting
	 * effects in the game.
	 *
	 * @param playerIds The list of playerIds to assign targets to (all players in this list are assumed to be active)
	 * @return False if target assignment was unsuccessful
	 */
	private boolean assignListRandomTargets(ArrayList<Integer> playerIds) {
		// Build a list of all active player IDs
		if (numberTargets >= playerIds.size() - 1) {
			System.out.println("Number of targets (" + numberTargets + ") exceeds active player size: " + playerIds.size());
			return false;
		}

		// Shuffle the list of IDs. We can then iterate through here and assign targets
		Collections.shuffle(playerIds, new Random());

		int assassinIndex = 0;
		for (int assassinId : playerIds) {
			String targetText = "";

			for (int j = 0; j < numberTargets; ++j) {
				int nextTargetIndex = assassinIndex + 1 + j;
				if (nextTargetIndex >= playerIds.size()) {
					nextTargetIndex -= playerIds.size();
				}

				Player target = playerData.get(playerIds.get(nextTargetIndex));
				targetText += "- " + target.getFirstName() + " " + target.getLastName() + "\n";
			}

			targetAssignments.put(assassinId, targetText);
			assassinIndex++;
		}

		return true;
	}

	/**
	 * Writes a player's target assignments for a given week, as well as their score summary
	 * @param gameWeek The week of play that the e-mails are being written for
	 */
	public void writePlayerEmails(int gameWeek) {
		try {
			File outputFile = new File(outputFilename);
			FileOutputStream os = new FileOutputStream(outputFile);
			OutputStreamWriter osw = new OutputStreamWriter(os);
			Writer writer = new BufferedWriter(osw);

			for (int id : playerData.keySet()) {
				Player assassin = playerData.get(id);

				// If a player has not been eliminated, write their assign targets and then their score summaries
				if (assassin.isPlayerEliminated() == false) {
					String outputText = assassin.getEmail() + "\n[Assassins] - Week " + gameWeek + " Targets\n";
					outputText += "Assassin " + assassin.getFirstName() + ", your list of assigned targets for this week follows.\n\n";
					outputText += targetAssignments.get(id) + "\n";
					outputText += "Player Career Summary:\n\n";
					outputText += playerScoreSummary(id);
					outputText += "\n=================================================================\n";
					writer.write(outputText);
				}
				// If a player was eliminated last week, write a notification along with their score summaries
				else if (assassin.getWeekEliminated() == (gameWeek - 1)) {
					String outputText = assassin.getEmail() + "\n[Assassins] - You have been eliminated at the end of week " + (gameWeek - 1) + "\n";
					outputText += "Assassin " + assassin.getFirstName() + ", unfortunately your performance relative to your peers has been insufficient. " +
						"You have been eliminated from the game. You will continue to receive the weekly game update for the remainder of the game. " +
						"New modifiers may come into effect that will allow you to continue participating.\n\n";
					outputText += "Below is your final score and career summary.\n\n";
					outputText += playerScoreSummary(id);
					outputText += "\n=================================================================\n";
					writer.write(outputText);
				}
				// If a player was eliminated in an earlier week, do not write any information to e-mail them
				else {
					continue;
				}
			}
			writer.close();
		}
		catch (IOException e) {
			System.err.println("IOException while writing to output file " + outputFilename + ": " + e.getMessage());
		}
	}

	/**
	 * Prints out a player's entire career score summary. This includes their total score/kills/deaths, bonuses, and
	 * weekly breakdowns.
	 * @param playerId The ID of the player to print
	 * @return Human-friendly text summarizing the player's career activity
	 */
	private String playerScoreSummary(int playerId) {
		Player player = playerData.get(playerId);
		String summaryText = "";
		summaryText += "Total Kills: " + player.getTotalKills() + "\n";
		summaryText += "Total Deaths: " + player.getTotalDeaths() + "\n";
		summaryText += "Total Score: " + player.getTotalScore() + "\n";
		summaryText += "\n";

		summaryText += "=== Weekly Breakdown ===\n\n";
		ArrayList<Integer> weekNumbers = new ArrayList(player.getScoreData().keySet());
		Collections.sort(weekNumbers);
		for (int i : weekNumbers) {
			summaryText += playerWeekSummary(playerId, i) + "\n";
		}
		return summaryText;
	}

	/**
	 * Returns a string of text summarizing a player's activity for a given week
	 * @param playerId The ID of the player to generate the summary for
	 * @param weekNumber The week number to grab score data from
	 * @return Human-friendly text describing the activity
	 *
	 * Note that this function does not summarize a player's total activity, as this is done in playerScoreSummary()
	 */
	private String playerWeekSummary(int playerId, int weekNumber) {
		Player player = playerData.get(playerId);
		HashMap<Integer, Score> scores = player.getScoreData();

		String text = "Week number " + weekNumber + ":\n";
		if (scores.containsKey(weekNumber) == false) {
			return "no data";
		}

		Score weeklyScore = scores.get(weekNumber);
		text += "Kills / Deaths / Score == " + weeklyScore.getKills() + " / " + weeklyScore.getDeaths() + " / " + weeklyScore.getScore() + "\n";
		if (weeklyScore.getKills() > 0) {
			text += "You successfully killed the following targets:\n";
			for (int i : weeklyScore.getTargetPlayerIds()) {
				text += "\t- " + playerData.get(i).getFirstName() + " " + playerData.get(i).getLastName() + "\n";
			}
		}
		if (weeklyScore.getDeaths() > 0) {
			text += "You were killed by the following assassins:\n";
			for (int i : weeklyScore.getAssassinPlayerIds()) {
				text += "\t- " + playerData.get(i).getFirstName() + " " + playerData.get(i).getLastName() + "\n";
			}
		}

		// TODO: print any bonuses the player earned for the week

		return text;
	}
}
