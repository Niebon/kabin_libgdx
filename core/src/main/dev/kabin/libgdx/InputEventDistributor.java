package dev.kabin.libgdx;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import dev.kabin.util.eventhandlers.EnumEventHandler;
import dev.kabin.util.eventhandlers.KeyCode;
import dev.kabin.util.eventhandlers.KeyEventUtil;
import dev.kabin.util.eventhandlers.MouseEventUtil;

/**
 * An input event distributor that distributes events accordingly to event handlers of this game project.
 * An instance of this class effectively acts as a translator for the input processor to the event-based implementation
 * of the {@link EnumEventHandler}:
 * <ul>
 *     <li>{@link KeyEventUtil}</li>
 *     <li>{@link MouseEventUtil}</li>
 * </ul>
 */
public record InputEventDistributor(MouseEventUtil mouseEventUtil,
									KeyEventUtil keyEventUtil) implements InputProcessor {

	@Override
	public boolean keyDown(int keycode) {
		final boolean pressed = true;
		setKeyCodePressedStatus(keycode, pressed);
		return false;
	}

	private void setKeyCodePressedStatus(int keycode, boolean pressed) {
		switch (keycode) {
			case Input.Keys.Q -> keyEventUtil.registerEvent(KeyCode.Q, pressed);
			case Input.Keys.W -> keyEventUtil.registerEvent(KeyCode.W, pressed);
			case Input.Keys.E -> keyEventUtil.registerEvent(KeyCode.E, pressed);
			case Input.Keys.R -> keyEventUtil.registerEvent(KeyCode.R, pressed);
			case Input.Keys.T -> keyEventUtil.registerEvent(KeyCode.T, pressed);
			case Input.Keys.Y -> keyEventUtil.registerEvent(KeyCode.Y, pressed);
			case Input.Keys.U -> keyEventUtil.registerEvent(KeyCode.U, pressed);
			case Input.Keys.I -> keyEventUtil.registerEvent(KeyCode.I, pressed);
			case Input.Keys.O -> keyEventUtil.registerEvent(KeyCode.O, pressed);
			case Input.Keys.P -> keyEventUtil.registerEvent(KeyCode.P, pressed);
			case Input.Keys.A -> keyEventUtil.registerEvent(KeyCode.A, pressed);
			case Input.Keys.S -> keyEventUtil.registerEvent(KeyCode.S, pressed);
			case Input.Keys.D -> keyEventUtil.registerEvent(KeyCode.D, pressed);
			case Input.Keys.F -> keyEventUtil.registerEvent(KeyCode.F, pressed);
			case Input.Keys.G -> keyEventUtil.registerEvent(KeyCode.G, pressed);
			case Input.Keys.H -> keyEventUtil.registerEvent(KeyCode.H, pressed);
			case Input.Keys.J -> keyEventUtil.registerEvent(KeyCode.J, pressed);
			case Input.Keys.K -> keyEventUtil.registerEvent(KeyCode.K, pressed);
			case Input.Keys.L -> keyEventUtil.registerEvent(KeyCode.L, pressed);
			case Input.Keys.Z -> keyEventUtil.registerEvent(KeyCode.Z, pressed);
			case Input.Keys.X -> keyEventUtil.registerEvent(KeyCode.X, pressed);
			case Input.Keys.C -> keyEventUtil.registerEvent(KeyCode.C, pressed);
			case Input.Keys.V -> keyEventUtil.registerEvent(KeyCode.V, pressed);
			case Input.Keys.B -> keyEventUtil.registerEvent(KeyCode.B, pressed);
			case Input.Keys.N -> keyEventUtil.registerEvent(KeyCode.N, pressed);
			case Input.Keys.M -> keyEventUtil.registerEvent(KeyCode.M, pressed);
			case Input.Keys.F12 -> keyEventUtil.registerEvent(KeyCode.F12, pressed);
			case Input.Keys.CONTROL_LEFT -> keyEventUtil.registerEvent(KeyCode.CONTROL_LEFT, pressed);
			case Input.Keys.SHIFT_LEFT -> keyEventUtil.registerEvent(KeyCode.SHIFT_LEFT, pressed);
			case Input.Keys.ALT_LEFT -> keyEventUtil.registerEvent(KeyCode.ALT_LEFT, pressed);
			case Input.Keys.ESCAPE -> keyEventUtil.registerEvent(KeyCode.ESCAPE, pressed);
			case Input.Keys.SPACE -> keyEventUtil.registerEvent(KeyCode.SPACE, pressed);
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
			case Input.Buttons.RIGHT -> mouseEventUtil.registerEvent(MouseEventUtil.MouseButton.RIGHT, pressed);
			case Input.Buttons.LEFT -> mouseEventUtil.registerEvent(MouseEventUtil.MouseButton.LEFT, pressed);
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
		if (mouseEventUtil.isPressed(MouseEventUtil.MouseButton.LEFT)) {
			mouseEventUtil.registerMouseDragged(MouseEventUtil.MouseButton.LEFT, x, y);
		}
		if (mouseEventUtil.isPressed(MouseEventUtil.MouseButton.RIGHT)) {
			mouseEventUtil.registerMouseDragged(MouseEventUtil.MouseButton.RIGHT, x, y);
		}
		return false;
	}

	@Override
	public boolean mouseMoved(int x, int y) {
		mouseEventUtil.registerMouseMoved(x, y);
		return false;
	}

	@Override
    public boolean scrolled(float amountX, float amountY) {
        mouseEventUtil.registerMouseScroll(amountY);
        return false;
    }


}
