package dev.kabin.util.eventhandlers;

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
			case Input.Keys.Q -> KeyEventUtil.getInstance().registerEvent(KeyCode.Q, pressed);
			case Input.Keys.W -> KeyEventUtil.getInstance().registerEvent(KeyCode.W, pressed);
			case Input.Keys.E -> KeyEventUtil.getInstance().registerEvent(KeyCode.E, pressed);
			case Input.Keys.R -> KeyEventUtil.getInstance().registerEvent(KeyCode.R, pressed);
			case Input.Keys.T -> KeyEventUtil.getInstance().registerEvent(KeyCode.T, pressed);
			case Input.Keys.Y -> KeyEventUtil.getInstance().registerEvent(KeyCode.Y, pressed);
			case Input.Keys.U -> KeyEventUtil.getInstance().registerEvent(KeyCode.U, pressed);
			case Input.Keys.I -> KeyEventUtil.getInstance().registerEvent(KeyCode.I, pressed);
			case Input.Keys.O -> KeyEventUtil.getInstance().registerEvent(KeyCode.O, pressed);
			case Input.Keys.P -> KeyEventUtil.getInstance().registerEvent(KeyCode.P, pressed);
			case Input.Keys.A -> KeyEventUtil.getInstance().registerEvent(KeyCode.A, pressed);
			case Input.Keys.S -> KeyEventUtil.getInstance().registerEvent(KeyCode.S, pressed);
			case Input.Keys.D -> KeyEventUtil.getInstance().registerEvent(KeyCode.D, pressed);
			case Input.Keys.F -> KeyEventUtil.getInstance().registerEvent(KeyCode.F, pressed);
			case Input.Keys.G -> KeyEventUtil.getInstance().registerEvent(KeyCode.G, pressed);
			case Input.Keys.H -> KeyEventUtil.getInstance().registerEvent(KeyCode.H, pressed);
			case Input.Keys.J -> KeyEventUtil.getInstance().registerEvent(KeyCode.J, pressed);
			case Input.Keys.K -> KeyEventUtil.getInstance().registerEvent(KeyCode.K, pressed);
			case Input.Keys.L -> KeyEventUtil.getInstance().registerEvent(KeyCode.L, pressed);
			case Input.Keys.Z -> KeyEventUtil.getInstance().registerEvent(KeyCode.Z, pressed);
			case Input.Keys.X -> KeyEventUtil.getInstance().registerEvent(KeyCode.X, pressed);
			case Input.Keys.C -> KeyEventUtil.getInstance().registerEvent(KeyCode.C, pressed);
			case Input.Keys.V -> KeyEventUtil.getInstance().registerEvent(KeyCode.V, pressed);
			case Input.Keys.B -> KeyEventUtil.getInstance().registerEvent(KeyCode.B, pressed);
			case Input.Keys.N -> KeyEventUtil.getInstance().registerEvent(KeyCode.N, pressed);
			case Input.Keys.M -> KeyEventUtil.getInstance().registerEvent(KeyCode.M, pressed);
			case Input.Keys.F12 -> KeyEventUtil.getInstance().registerEvent(KeyCode.F12, pressed);
			case Input.Keys.CONTROL_LEFT -> KeyEventUtil.getInstance().registerEvent(KeyCode.CONTROL_LEFT, pressed);
			case Input.Keys.SHIFT_LEFT -> KeyEventUtil.getInstance().registerEvent(KeyCode.SHIFT_LEFT, pressed);
			case Input.Keys.ALT_LEFT -> KeyEventUtil.getInstance().registerEvent(KeyCode.ALT_LEFT, pressed);
			case Input.Keys.ESCAPE -> KeyEventUtil.getInstance().registerEvent(KeyCode.ESCAPE, pressed);
			case Input.Keys.SPACE -> KeyEventUtil.getInstance().registerEvent(KeyCode.SPACE, pressed);
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
		if (MouseEventUtil.getInstance().isPressed(MouseEventUtil.MouseButton.LEFT)) {
			MouseEventUtil.getInstance().registerMouseDragged(MouseEventUtil.MouseButton.LEFT, x, y);
		}
		if (MouseEventUtil.getInstance().isPressed(MouseEventUtil.MouseButton.RIGHT)) {
			MouseEventUtil.getInstance().registerMouseDragged(MouseEventUtil.MouseButton.RIGHT, x, y);
		}
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
