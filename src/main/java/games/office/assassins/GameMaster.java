package games.office.assassins;

import games.office.assassins.config.DatabaseConnection;
import games.office.assassins.model.Bonus;
import games.office.assassins.model.Player;
import games.office.assassins.model.Score;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * Main class for the application. Loads all the game data, prints player rankings, and
 * generates weekly e-mails for all players with their target assignements or elimination notification.
 */
public class GameMaster {
	/** A map containing all player and game data loaded from the database */
	private static HashMap<Integer, Player> playerData;

	static public void main(String args[]) {
		// Open a connection to the database
		DatabaseConnection database = new DatabaseConnection();
		if (database.openConnection(args[0]) == false) {
			System.out.println("Failed to open DB connection to file: " + args[0]);
			System.exit(1);
		}

		// Load all data from the database tables
		loadGameData(database.getConnection());

		// Print out player rankings and score info to determine weekly eliminations
		PlayerRank rankings = new PlayerRank(playerData, true);
		rankings.printPlayerRanks();


		WeeklySummaryAssignment assignments = new WeeklySummaryAssignment(playerData, 3);
		assignments.setOutputFilename("emails/sample_week1.txt");
		assignments.assignRandomTargets();
		assignments.writePlayerEmails(1);

		// Week 1: assign each player to 3 random targets
//		WeeklySummaryAssignment assignments = new WeeklySummaryAssignment(playerData, 3);
//		assignments.setOutputFilename("emails/targets_week_1.txt");
//		assignments.assignRandomTargets();
//		assignments.writePlayerEmails(1);

		// Week 2: create groups of four players, and assign each player in the group as each other's assassins
//		WeeklySummaryAssignment assignments = new WeeklySummaryAssignment(playerData, 3);
//		assignments.setOutputFilename("emails/targets_week_2.txt");
//		assignments.assignMutualTargets();
//		assignments.writePlayerEmails(2);

		// Week 3: assign each active player 2 random targets
//		WeeklySummaryAssignment assignments = new WeeklySummaryAssignment(playerData, 2);
//		assignments.setOutputFilename("emails/targets_week_3.txt");
//		assignments.assignRandomTargets();
//		assignments.writePlayerEmails(3);

		// Week 4: assign each active player 2 random targets
//		WeeklySummaryAssignment assignments = new WeeklySummaryAssignment(playerData, 2);
//		assignments.setOutputFilename("emails/targets_week_4.txt");
//		assignments.assignRandomTargets();
//		assignments.writePlayerEmails(4);

		// Week 5: assign the rankings to remaining players, divide players into two tiers based on rankings,
		// and assign 4 random targets within the tiers
//		WeeklySummaryAssignment assignments = new WeeklySummaryAssignment(playerData, 4);
//		assignments.setOutputFilename("emails/targets_week_5.txt");
//		assignments.assignTieredRandomTargets();
//		assignments.writePlayerEmails(5);

		// Week 6: assign all remaining (5) players to be targets of each other
//		WeeklySummaryAssignment assignments = new WeeklySummaryAssignment(playerData, 4);
//		assignments.setOutputFilename("emails/targets_week_6.txt");
//		assignments.assignRandomTargets();
//		assignments.writePlayerEmails(6);

		// Close the database connection
		database.closeConnection();
		System.exit(0);
	}

	/**
	 * Loads all game data from the database and stores it in the static playerData structure.
	 * This includes all data from the following tables:
	 * - players
	 * - kills
	 * - bonuses
	 *
	 * @param dbConnection An open connection to the database
	 */
	static void loadGameData(Connection dbConnection) {
		playerData = new HashMap();

		// Load data from table: players
		try {
			String sql = "SELECT ID, FirstName, LastName, Email, WeekEliminated FROM players";
			Statement dbStatement = dbConnection.createStatement();
			ResultSet results = dbStatement.executeQuery(sql);

			while (results.next()) {
				Player nextPlayer = new Player();
				nextPlayer.setId(results.getInt("id"));
				nextPlayer.setFirstName(results.getString("FirstName"));
				nextPlayer.setLastName(results.getString("LastName"));
				nextPlayer.setEmail(results.getString("Email"));
				nextPlayer.setWeekEliminated(results.getInt("WeekEliminated"));
				playerData.put(nextPlayer.getId(), nextPlayer);
			}
		}
		catch (SQLException e) {
			System.out.println("Error reading 'players' table: " + e.getMessage());
			return;
		}
		System.out.println("Loaded data for " + playerData.size() + " players");

		// Lists the unique week numbers where we have kill data, sorted from oldest to newest
		ArrayList<Integer> weekNumbers = new ArrayList();

		// Holds score data for all players for all weeks of play
		ArrayList<Score> playerScoreData = new ArrayList();

		// Load data from table: kills
		try {
			String sql;
			Statement dbStatement;
			ResultSet results;

			// First figure out how many distinct weeks of kill data exist so we can
			// prepare enough objects to hold player scores
			sql = "SELECT distinct WeekNumber FROM kills ORDER BY WeekNumber";
			dbStatement = dbConnection.createStatement();
			results = dbStatement.executeQuery(sql);

			while (results.next()) {
				weekNumbers.add(results.getInt("WeekNumber"));
			}

			// Create a score record for each week for each player
			for (int i = 0; i < playerData.size() * weekNumbers.size(); ++i) {
				playerScoreData.add(new Score());
			}

			// Now grab all the kill data and put it into the appropriate score container
			sql = "SELECT AssassinId, TargetId, WeekNumber FROM kills";
			dbStatement = dbConnection.createStatement();
			results = dbStatement.executeQuery(sql);

			while (results.next()) {
				int assassinId = results.getInt("AssassinId");
				int targetId = results.getInt("TargetId");
				int weekNumber = results.getInt("WeekNumber");

				// The kill needs to be logged in the scores for both the assassin and target.
				// Player Ids are presumed to range from 1 to MAX. We can use this information
				// and the week number to figure out where the two relevant scores are in the list.
				int index;
				// Eliminated players can still make kills in the game, but those kills do not get
				// added to their scores. Deaths will always be added to a player's score.
				if (playerData.get(assassinId).isPlayerEliminated() == false || playerData.get(assassinId).getWeekEliminated() >= weekNumber) {
					index = (assassinId - 1) + (playerData.size() * (weekNumber - 1));
					playerScoreData.get(index).addTargetKilled(targetId);
				}
				index = (targetId - 1) + (playerData.size() * (weekNumber - 1));
				playerScoreData.get(index).addAssassinKiller(assassinId);
			}
		}
		catch (SQLException e) {
			System.out.println("Error reading 'kills' table: " + e.getMessage());
			return;
		}

		// Load data from table: bonuses
		try {
			String sql = "SELECT AssassinId, TargetId, WeekNumber, BonusPoints, BonusName, BonusDescription FROM bonuses";
			Statement dbStatement = dbConnection.createStatement();
			ResultSet results = dbStatement.executeQuery(sql);

			// AssassinID int, TargetId int, WeekNumber int, BonusPoints int, BonusName varchar(255), BonusDescription
			while (results.next()) {
				int assassinId = results.getInt("AssassinId");
				int weekNumber = results.getInt("WeekNumber");

				Bonus bonus = new Bonus();
				bonus.setTargetId(results.getInt("TargetId"));
				bonus.setBonusPoints(results.getInt("BonusPoints"));
				bonus.setBonusName(results.getString("BonusName"));
				bonus.setBonusDescription(results.getString("BonusDescription"));

				// Determine which Score object the bonus applies to using the player ID and week number
				int index = (assassinId - 1) + (playerData.size() * (weekNumber - 1));
				playerScoreData.get(index).addBonus(bonus);
			}
		}
		catch (SQLException e) {
			System.out.println("Error reading 'bonuses' table: " + e.getMessage());
			return;
		}

		// With the score data complete, add it to the existing player data
		for (Integer i : playerData.keySet()) {
			for (int j : weekNumbers) {
				int index = (i - 1) + (playerData.size() * (j - 1));
				playerData.get(i).addScoreData(j, playerScoreData.get(index));
			}
		}
	}
}
