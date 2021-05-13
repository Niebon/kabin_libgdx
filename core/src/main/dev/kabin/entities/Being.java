package dev.kabin.entities;

import java.util.List;
import java.util.Optional;

/**
 * A being is a living entity that can interact with other beings.
 */
public interface Being {

	float health();

	float stamina();

	void doToOther(Being being, Action action);

	Optional<Being> getTargetOf(Action action);

	boolean is(Action action);

	float x();

	float y();

	float vx();

	float vy();

	float jumpVel();

	float movementSpeed();

	List<Being> findOthers();

}
