package dev.kabin.entities;

import dev.kabin.util.lambdas.FloatSupplier;

import java.util.function.BiConsumer;
import java.util.function.Consumer;


/**
 * A helper class that renders physics frames at a target {@code dt}.
 *
 * @param <E> the type of entities that are being rendered.
 */
public class ConstantFrameRateRenderer<E extends Entity<?, ?, ?>, ParamType> {

	// Private data
	private float accumulatedPeriodToRenderFramesFor = 0f;
	private final FloatSupplier dt;
	private final BiConsumer<E, ParamType> renderAction;

	public ConstantFrameRateRenderer(FloatSupplier dt, BiConsumer<E, ParamType> renderAction) {
		this.dt = dt;
		this.renderAction = renderAction;
	}

	public void accumulateTime(float timeElapsedSinceLastGraphicsFrame) {
		accumulatedPeriodToRenderFramesFor += timeElapsedSinceLastGraphicsFrame;
	}

	/**
	 * Renders all outstanding frames (if there are any), given the accumulated render period for this instance.
	 *
	 * @param params        parameters.
	 * @param forEachEntity a <i>for each</i> procedure on a collection of entities, that can perform an given action {@code Consumer<E>}
	 *                      for each entity in the collection. This is used to do this:
	 *                      <pre>forEachEntity.accept(e -> e.updatePhysics(params));</pre>
	 */
	public void render(ParamType params,
					   Consumer<Consumer<E>> forEachEntity) {
		if (accumulatedPeriodToRenderFramesFor > dt.get()) {
			int numberOfFramesRendered = renderFramesFor(accumulatedPeriodToRenderFramesFor, params, forEachEntity);
			accumulatedPeriodToRenderFramesFor = accumulatedPeriodToRenderFramesFor - numberOfFramesRendered * dt.get();
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
	private int renderFramesFor(float period,
								ParamType params,
								Consumer<Consumer<E>> forEachEntity) {
		if (period > dt.get()) {
			forEachEntity.accept(e -> renderAction.accept(e, params));
			return 1 + renderFramesFor(period - dt.get(), params, forEachEntity);
		} else return 0;
	}

	public boolean isReadyToRender() {
		return accumulatedPeriodToRenderFramesFor > dt.get();
	}
}
