package games.office.assassins.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tracks bonus points and effects that the player has earned for a given week.
 */
@Getter @Setter @NoArgsConstructor
public class Bonus {
	/** If applicable, the player ID related to the bonus effect */
	Integer targetId;

	/** If applicable, the number of bonus points awarded. This may be a negative number */
	Integer bonusPoints = 0;

	/** The name of the bonus earned */
	String bonusName;

	/** Short description text explaining the bonus effect */
	String bonusDescription;
}
