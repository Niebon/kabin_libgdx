package dev.kabin.entities;

import java.util.function.Consumer;


/**
 * A helper class that renders physics frames at a target {@code dt}.
 *
 * @param <E> the type of entities that are being rendered.
 */
public class PhysicsRenderer<E extends Entity<?, ?, ?>> {

	// Private data
	private float accumulatedPeriodToRenderFramesFor = 0f;

	/**
	 * Renders all outstanding physics frames (if there are any), given the accumulated render period for this instance.
	 *
	 * @param timeElapsedSinceLastGraphicsFrame a value that is accumulated for each call to this method.
	 * @param params                            physics parameters.
	 * @param forEachEntity                     a <i>for each</i> procedure on a collection of entities, that can perform an given action {@code Consumer<E>}
	 *                                          for each entity in the collection. This is used to do this:
	 *                                          <pre>forEachEntity.accept(e -> e.updatePhysics(params));</pre>
	 */
	public void renderOutstandingPhysicsFrames(float timeElapsedSinceLastGraphicsFrame, PhysicsParameters params, Consumer<Consumer<E>> forEachEntity) {
		accumulatedPeriodToRenderFramesFor += timeElapsedSinceLastGraphicsFrame;
		if (accumulatedPeriodToRenderFramesFor > params.dt()) {
			int numberOfFramesRendered = renderFramesFor(accumulatedPeriodToRenderFramesFor, params, forEachEntity);
			accumulatedPeriodToRenderFramesFor = accumulatedPeriodToRenderFramesFor - numberOfFramesRendered * params.dt();
		}
	}

	/**
	 * Renders all outstanding frames for the given period.
	 *
	 * @param period        a period. If the period is less than {@code params.dt()}, then no frames will be rendered.
	 *                      The number of frames that will be rendered equals the number of times {@code params.dt()} fits
	 *                      into the given period.
	 * @param params        physics parameters.
	 * @param forEachEntity a <i>for each</i> procedure on a collection of entities, that can perform an given action {@code Consumer<E>}
	 *                      for each entity in the collection. This is used to do this:
	 *                      <pre>forEachEntity.accept(e -> e.updatePhysics(params));</pre>
	 * @return the number of frames that were rendered.
	 */
	private int renderFramesFor(float period, PhysicsParameters params, Consumer<Consumer<E>> forEachEntity) {
		if (period > params.dt()) {
			forEachEntity.accept(e -> e.updatePhysics(params));
			return 1 + renderFramesFor(period - params.dt(), params, forEachEntity);
		} else return 0;
	}

}
