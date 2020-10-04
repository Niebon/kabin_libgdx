package dev.kabin.utilities.eventhandlers.codewritehack;

import dev.kabin.utilities.eventhandlers.KeyEventUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * A simple hack to write code lines of the form:
 * <pre>
 * case Input.Keys.Q
 * -> KeyEventUtil.getInstance().registerEvent(KeyEventUtil.KeyCode.Q, pressed);
 * </pre>
 * for all keys in {@link KeyEventUtil.KeyCode}. It is not possible to do this with a for-each-pair loop inside the source code,
 * because Input.Keys.Q is an {@code int} constant, like all other keys in {@link com.badlogic.gdx.Input.Keys Input.Keys}.
 * So it is not enumerable.
 */
public class WriteCodeLineForEach {
	public static void main(String[] args) throws IOException {
		File output = new File("core/src/dev/dev.kabin.kabin/dev.kabin.utilities/eventhandlers/codewritehack/result.txt");
		BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(output));
		for (KeyEventUtil.KeyCode keyCode : KeyEventUtil.KeyCode.values()) {
			bufferedWriter.write(
					"case Input.Keys." + keyCode.name() + "-> KeyEventUtil.getInstance().registerEvent(KeyEventUtil.KeyCode." + keyCode.name() + ", pressed);\n"
			);
		}
		bufferedWriter.close();
	}
}
