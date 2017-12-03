package games.office.assassins.model;

import java.util.ArrayList;
import java.util.HashMap;

import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.Random;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Primary data model structure that represents a single player's information and all of their
 * score history throughout the game.
 */
@Getter
public class Player implements Comparable<Player> {
	public Player() {
		scoreData = new HashMap();
	}

	/** ID serves both as the row ID in the database and the player's unique ID number */
	@Setter
	private Integer id;

	@Setter
	private String firstName;

	@Setter
	private String lastName;

	@Setter
	private String email;

	/** Set to non-zero when a player has been eliminated from the game */
	@Setter
	private Integer weekEliminated;

	/** The player's assigned rank number (1-n) relative to other non-eliminated players */
	@Setter
	private Integer rank = 0;

	/** Tracks the players entire score history for each week. The Integer key is the week number */
	private HashMap<Integer, Score> scoreData;

	/** Player's total score summed across all weeks */
	private Integer totalScore = 0;

	/** Player's total number of kills summed across all weeks */
	private Integer totalKills = 0;

	/** Player's total number of deaths summed across all weeks */
	private Integer totalDeaths = 0;

	/** Returns true if this player has been eliminated from the game */
	public boolean isPlayerEliminated() {
		return (weekEliminated != 0);
	}

	/**
	 * Adds a new set of score data to the player
	 * @param weekNumber The week number that the score applies to
	 * @param score The score data to add
	 *
	 * If score data already exists at the specified week number, it will be overwritten
	 */
	public void addScoreData(int weekNumber, Score score) {
		scoreData.put(weekNumber, score);
		recalculateTotalScores();
	}


	/**
	 * Updates the total score, kills, and deaths for the player
	 */
	private void recalculateTotalScores() {
		totalScore = 0;
		totalKills = 0;
		totalDeaths = 0;

		for (Score s : scoreData.values()) {
			totalScore += s.getScore();
			totalKills += s.getKills();
			totalDeaths += s.getDeaths();
		}
	}

	/**
	 * Used for comparison in collections. Players with higher scores are ranked higher. If scores are tied, the player
	 * with the most kills is ranked higher. If kills are also tied, the player with the fewer deaths is ranked higher.
	 * @param otherPlayer Other Player object to compare
	 * @return The difference in rank score
	 */
	public int compareTo(Player otherPlayer) {
		int difference = otherPlayer.totalScore - this.totalScore;
		if (difference == 0) {
			difference = otherPlayer.totalKills - this.totalKills;
		}
		if (difference == 0) {
			difference = this.totalDeaths - otherPlayer.totalDeaths;
		}

		return difference;
	}

	/**
	 * Used for debugging. Prints all data about the player to standard output
	 */
	public void printPlayerData() {
		if (id == 0) {
			System.out.println("No player data available");
			return;
		}

		System.out.println("Player: " +
			id + "|" +
			firstName + "|" +
			lastName + "|" +
			email + "|" +
			weekEliminated
		);
		System.out.println("\tKills/Deaths/Score: " +
			totalKills + "/" +
			totalDeaths + "/" +
			totalScore
		);
		for (int i : scoreData.keySet()) {
			System.out.println(
				"\t\tWeek " + i + ": " +
					scoreData.get(i).getKills() + "/" +
					scoreData.get(i).getDeaths() + "/" +
					scoreData.get(i).getScore()
			);
		}
		System.out.println("\tBonuses:");
		for (int i : scoreData.keySet()) {
			ArrayList<Bonus> bonuses = scoreData.get(i).getBonuses();
			if (bonuses.isEmpty()) {
				continue;
			}

			for (Bonus j : bonuses) {
				System.out.println(
					"\t\tWeek " + i + ": " +
						j.getBonusName() + " - " +
						j.getBonusDescription() + ": " +
						j.getBonusPoints() + ", " +
						j.getTargetId()
				);
			}
		}
	}
}
