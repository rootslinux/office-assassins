# Overview
Office Assassins is a game where players are assigned targets on a weekly basis and have to kill as many of those players as they can. After each week, players are then ranked accordingly and the game master eliminates the lowest performing players from the game. This continues for several weeks until a winner is crowned. The method of how to kill a player is up to the game master's discretion. For example, by throwing and striking a player with a projectile. Players then report their kills to the game master that runs the show.

This code is a simple application for use by the game master to assist with managing the game. It is written in Java and uses a SQlite database for persistence. The application will load all data in the database, then the user will have the option to do things such as print player rankings, score summaries, assign players new targets using a number of different algorithms, and generate e-mails to send players that contain their new target assignments, elimination notice if applicable, and a summary of their kill and death history.

# Usage
Use maven to build/package: 
`$ mvn compile package`

The application requires a single argument specifying the database file to load:

`$ java -jar target/office-assassins-1.0-jar-with-dependencies.jar db/sample_test.db`

If everything worked, you should see output like the following.

`Established DB connection with file db/sample_test.db

Loaded data for 32 players

========== Player Rankings ==========

(Rank) ID: Name ... Score/Kills/Deaths

.....
`

## Running a game
In its initial state this application is rather limited in what it does. If you are using this to run a game, open up GameMaster.java and examine the main() method. There you will see several blocks of code commented out representing the various actions that will generate target assignments and weekly player e-mails in different ways. You'll want to temporarily add in the appropriate lines to do the actions you desire.

Note that the application does not do any write operations to the database. Adding players to the game, registering kills, eliminating players, and so on all need to be done manually by the game master using SQL. Use the `sqlite3` application in your terminal to open your database file and manipulate the data as you desire.


### Future work
While this application helped me tremendously in managing a rather complex game of assassins, there are many features that I would have loved to add if I had the time to do so. In particular there are still many parts of the game management (manual data entry) that are error-prone and tedious. Here's my feature wish list.

- Enable the application to perform database writes
- Add a GUI to simplify common actions like registering kills, eliminating players, etc.
- Add a database commit history that is persistent and can be used to undo operations
- Figure out a means for the application to connect to an e-mail service and directly generate and e-mail players
