package dev.kabin.utilities.eventhandlers;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;

/**
 * An input event distributor that distributes events accordingly to event handlers of this game project.
 * An instance of this class effectively acts as a translator for the input processor to the event-based implementation
 * of the {@link EnumHandler}:
 * <ul>
 *     <li>{@link KeyEventUtil}</li>
 *     <li>{@link MouseEventUtil}</li>
 * </ul>
 */
public class InputEventDistributor implements InputProcessor {

	@Override
	public boolean keyDown(int keycode) {
		final boolean pressed = true;
		setKeyCodePressedStatus(keycode, pressed);
		return false;
	}

	private void setKeyCodePressedStatus(int keycode, boolean pressed) {
		switch (keycode) {
			case Input.Keys.Q -> KeyEventUtil.getInstance().registerEvent(KeyEventUtil.KeyCode.Q, pressed);
			case Input.Keys.W -> KeyEventUtil.getInstance().registerEvent(KeyEventUtil.KeyCode.W, pressed);
			case Input.Keys.E -> KeyEventUtil.getInstance().registerEvent(KeyEventUtil.KeyCode.E, pressed);
			case Input.Keys.R -> KeyEventUtil.getInstance().registerEvent(KeyEventUtil.KeyCode.R, pressed);
			case Input.Keys.T -> KeyEventUtil.getInstance().registerEvent(KeyEventUtil.KeyCode.T, pressed);
			case Input.Keys.Y -> KeyEventUtil.getInstance().registerEvent(KeyEventUtil.KeyCode.Y, pressed);
			case Input.Keys.U -> KeyEventUtil.getInstance().registerEvent(KeyEventUtil.KeyCode.U, pressed);
			case Input.Keys.I -> KeyEventUtil.getInstance().registerEvent(KeyEventUtil.KeyCode.I, pressed);
			case Input.Keys.O -> KeyEventUtil.getInstance().registerEvent(KeyEventUtil.KeyCode.O, pressed);
			case Input.Keys.P -> KeyEventUtil.getInstance().registerEvent(KeyEventUtil.KeyCode.P, pressed);
			case Input.Keys.A -> KeyEventUtil.getInstance().registerEvent(KeyEventUtil.KeyCode.A, pressed);
			case Input.Keys.S -> KeyEventUtil.getInstance().registerEvent(KeyEventUtil.KeyCode.S, pressed);
			case Input.Keys.D -> KeyEventUtil.getInstance().registerEvent(KeyEventUtil.KeyCode.D, pressed);
			case Input.Keys.F -> KeyEventUtil.getInstance().registerEvent(KeyEventUtil.KeyCode.F, pressed);
			case Input.Keys.G -> KeyEventUtil.getInstance().registerEvent(KeyEventUtil.KeyCode.G, pressed);
			case Input.Keys.H -> KeyEventUtil.getInstance().registerEvent(KeyEventUtil.KeyCode.H, pressed);
			case Input.Keys.J -> KeyEventUtil.getInstance().registerEvent(KeyEventUtil.KeyCode.J, pressed);
			case Input.Keys.K -> KeyEventUtil.getInstance().registerEvent(KeyEventUtil.KeyCode.K, pressed);
			case Input.Keys.L -> KeyEventUtil.getInstance().registerEvent(KeyEventUtil.KeyCode.L, pressed);
			case Input.Keys.Z -> KeyEventUtil.getInstance().registerEvent(KeyEventUtil.KeyCode.Z, pressed);
			case Input.Keys.X -> KeyEventUtil.getInstance().registerEvent(KeyEventUtil.KeyCode.X, pressed);
			case Input.Keys.C -> KeyEventUtil.getInstance().registerEvent(KeyEventUtil.KeyCode.C, pressed);
			case Input.Keys.V -> KeyEventUtil.getInstance().registerEvent(KeyEventUtil.KeyCode.V, pressed);
			case Input.Keys.B -> KeyEventUtil.getInstance().registerEvent(KeyEventUtil.KeyCode.B, pressed);
			case Input.Keys.N -> KeyEventUtil.getInstance().registerEvent(KeyEventUtil.KeyCode.N, pressed);
			case Input.Keys.M -> KeyEventUtil.getInstance().registerEvent(KeyEventUtil.KeyCode.M, pressed);
			case Input.Keys.CONTROL_LEFT -> KeyEventUtil.getInstance().registerEvent(KeyEventUtil.KeyCode.CONTROL_LEFT, pressed);
			case Input.Keys.SHIFT_LEFT -> KeyEventUtil.getInstance().registerEvent(KeyEventUtil.KeyCode.SHIFT_LEFT, pressed);
			case Input.Keys.ALT_LEFT -> KeyEventUtil.getInstance().registerEvent(KeyEventUtil.KeyCode.ALT_LEFT, pressed);
			case Input.Keys.ESCAPE -> KeyEventUtil.getInstance().registerEvent(KeyEventUtil.KeyCode.ESCAPE, pressed);
		}
	}

	@Override
	public boolean keyUp(int keycode) {
		final boolean pressed = false;
		setKeyCodePressedStatus(keycode, pressed);
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int x, int y, int pointer, int button) {
		final boolean pressed = true;
		setMouseButtonStatus(button, pressed);
		return false;
	}

	private void setMouseButtonStatus(int button, boolean pressed) {
		switch (button) {
			case Input.Buttons.RIGHT -> MouseEventUtil.getInstance().registerEvent(MouseEventUtil.MouseButton.RIGHT, pressed);
			case Input.Buttons.LEFT -> MouseEventUtil.getInstance().registerEvent(MouseEventUtil.MouseButton.LEFT, pressed);
		}
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button) {
		final boolean pressed = false;
		setMouseButtonStatus(button, pressed);
		return false;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer) {
		MouseEventUtil.getInstance().registerMouseDragged(MouseEventUtil.MouseButton.LEFT, x, y);
		return false;
	}

	@Override
	public boolean mouseMoved(int x, int y) {
		MouseEventUtil.getInstance().registerMouseMoved(x, y);
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		MouseEventUtil.getInstance().registerMouseScroll(amount);
		return false;
	}

}
