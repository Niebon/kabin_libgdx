package dev.kabin.ui.developer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import dev.kabin.GlobalData;
import dev.kabin.MainGame;
import dev.kabin.Serializer;
import dev.kabin.components.WorldRepresentation;
import dev.kabin.entities.GraphicsParameters;
import dev.kabin.entities.impl.Entity;
import dev.kabin.ui.developer.widgets.DraggedEntity;
import dev.kabin.ui.developer.widgets.EntityLoadingWidget;
import dev.kabin.ui.developer.widgets.TileSelectionWidget;
import dev.kabin.util.Functions;
import dev.kabin.util.eventhandlers.KeyEventUtil;
import dev.kabin.util.eventhandlers.MouseEventUtil;
import org.json.JSONObject;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static dev.kabin.GlobalData.WORLDS_PATH;
import static dev.kabin.GlobalData.atlas;

public class DeveloperUI {

	public static final String OPEN = "open";
	public static final String SAVE = "save";
	public static final String SAVE_AS = "save as";
	private static final Set<DraggedEntity> CURRENTLY_DRAGGED_ENTITIES = new HashSet<>();
	private static final Executor EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();

	private static final SelectBox<Button> FILE_DROP_DOWN_MENU = new SelectBox<>(new Skin(Gdx.files.internal("default/skin/uiskin.json")), "default");
	private static final DragListener SELECTION_END = new DragListener() {
		@Override
		public void dragStop(InputEvent event, float x, float y, int pointer) {
			ENTITY_SELECTION.end();
		}
	};
	private static Supplier<WorldRepresentation> worldRepresentationSupplier = Functions.nullSupplier();
	private static Supplier<MouseEventUtil> mouseEventUtilSupplier = Functions.nullSupplier();
	private static final EntitySelection ENTITY_SELECTION = new EntitySelection(mouseEventUtilSupplier);
	private static Supplier<KeyEventUtil> keyEventUtilSupplier = Functions.nullSupplier();
	private static Consumer<Runnable> synchronizer;
	private static EntityLoadingWidget ENTITY_LOADING_WIDGET;
	private static TileSelectionWidget TILE_SELECTION_WIDGET;
	private static final DragListener SELECTION_BEGIN = new DragListener() {
		@Override
		public void dragStart(InputEvent event, float x, float y, int pointer) {
			if (
					!keyEventUtilSupplier.get().isAltDown() &&
							CURRENTLY_DRAGGED_ENTITIES.isEmpty() &&
							!ENTITY_LOADING_WIDGET.getWidget().isDragging() &&
							!TILE_SELECTION_WIDGET.getWidget().isDragging()
			) {
				ENTITY_SELECTION.begin();
			}
		}
	};

	public static EntitySelection getEntitySelection() {
		return ENTITY_SELECTION;
	}

	public static void init(Stage stage,
							Supplier<WorldRepresentation> worldRepresentationSupplier,
							Supplier<MouseEventUtil> mouseEventUtilSupplier,
							Supplier<KeyEventUtil> keyEventUtilSupplier,
							Supplier<TextureAtlas> textureAtlasSupplier,
							Consumer<Runnable> synchronizer) {
		DeveloperUI.synchronizer = synchronizer;

		ENTITY_LOADING_WIDGET = new EntityLoadingWidget(
				EXECUTOR_SERVICE,
				() -> mouseEventUtilSupplier.get().getMouseXRelativeToWorld(),
				() -> mouseEventUtilSupplier.get().getMouseYRelativeToWorld(),
				e -> worldRepresentationSupplier.get().registerEntity(e),
				textureAtlasSupplier);
		TILE_SELECTION_WIDGET = new TileSelectionWidget(
				textureAtlasSupplier,
				EXECUTOR_SERVICE,
				() -> mouseEventUtilSupplier.get().getMouseXRelativeToWorld(),
				() -> mouseEventUtilSupplier.get().getMouseYRelativeToWorld(),
				worldRepresentationSupplier,
				DeveloperUI.synchronizer
		);


		stage.addListener(new DragListener() {
			@Override
			public void dragStop(InputEvent event, float x, float y, int pointer) {
				DeveloperUI.clearDraggedEntities();
			}
		});
		DeveloperUI.setVisible(GlobalData.developerMode);
		DeveloperUI.worldRepresentationSupplier = worldRepresentationSupplier;
		DeveloperUI.mouseEventUtilSupplier = mouseEventUtilSupplier;
		DeveloperUI.keyEventUtilSupplier = keyEventUtilSupplier;


		// Add drop down menu:
		var buttonOpen = new Button();
		buttonOpen.setName(OPEN);
		var buttonSave = new Button();
		buttonSave.setName(SAVE);
		var buttonSaveAs = new Button();
		buttonSaveAs.setName(SAVE_AS);
		FILE_DROP_DOWN_MENU.setItems(buttonOpen, buttonSave, buttonSaveAs);
		FILE_DROP_DOWN_MENU.setSelectedIndex(0);
		FILE_DROP_DOWN_MENU.setPosition(0f, MainGame.screenHeight - FILE_DROP_DOWN_MENU.getHeight());
		FILE_DROP_DOWN_MENU.setName("File");
		FILE_DROP_DOWN_MENU.addListener(new ChangeListener() {
			public void changed(ChangeEvent event, Actor actor) {
				switch (FILE_DROP_DOWN_MENU.getSelected().getName()) {
					case OPEN:
						DeveloperUI.loadWorld();
						break;
					case SAVE:
						DeveloperUI.saveWorld();
						break;
					case SAVE_AS:
						DeveloperUI.saveWorldAs();
						break;
					default:
						break;
				}
			}
		});
		stage.addActor(FILE_DROP_DOWN_MENU);
		worldRepresentationSupplier.get().actionForEachEntityOrderedByType(e ->
				e.getActor().ifPresent(a -> a.addListener(new ClickListener() {
							@Override
							public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
								if (button != Input.Buttons.RIGHT) return false;
								if (GlobalData.developerMode) {
									final Skin skin = new Skin(Gdx.files.internal("default/skin/uiskin.json"));
									final var dialog = new Dialog("Actions", skin);
									final float width = 200;
									final float height = 200;
									dialog.setBounds(
											mouseEventUtilSupplier.get().getXRelativeToUI() + width * 0.1f,
											mouseEventUtilSupplier.get().getYRelativeToUI() + height * 0.1f,
											width, height
									);
									dialog.getContentTable().defaults().pad(10);

									// Remove button.
									final var removeButton = new TextButton("Remove", skin, "default");
									removeButton.addListener(
											new ClickListener() {
												@Override
												public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {

													// Find all dev.kabin.entities scheduled for removal.
													final Set<Entity> entitiesScheduledForRemoval = new HashSet<>();
													final Set<Entity> currentlySelectedEntities = DeveloperUI.getEntitySelection()
															.getCurrentlySelectedEntities();
													if (currentlySelectedEntities.contains(e)) {
														entitiesScheduledForRemoval.addAll(currentlySelectedEntities);
													} else {
														entitiesScheduledForRemoval.add(e);
													}

													entitiesScheduledForRemoval.forEach(e -> {
														worldRepresentationSupplier.get().unregisterEntity(e);
														e.getActor().ifPresent(Actor::remove);
													});

													dialog.remove();
													return true;
												}
											}
									);
									dialog.getContentTable().add(removeButton).size(100, 30);

									// Exit button.
									var exitButton = new TextButton("x", skin, "default");
									exitButton.addListener(
											new ClickListener() {
												@Override
												public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
													return dialog.remove();
												}
											}
									);
									dialog.getTitleTable().add(exitButton)
											.size(20, 20)
											.padRight(0).padTop(0);
									dialog.setModal(true);
									GlobalData.stage.addActor(dialog);
								}
								return true;
							}
						})
				)
		);
	}

	private static void saveWorldAs() {
		EXECUTOR_SERVICE.execute(() -> {
			final String relativePath = Gdx.files.getLocalStoragePath().replace("\\", "/")
					+ "core/assets/worlds/";
			JFileChooser chooser = new JFileChooser(relativePath);
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			JFrame f = new JFrame();
			f.setVisible(true);
			f.toFront();
			f.setVisible(false);
			int res = chooser.showOpenDialog(f);
			f.dispose();
			if (res == JFileChooser.APPROVE_OPTION) {
				File selectedFile = chooser.getSelectedFile();
				GlobalData.currentWorld = selectedFile.getName();
				saveWorld(Path.of(WORLDS_PATH + GlobalData.currentWorld));
			}
		});
	}

	public static void addEntityToDraggedEntities(Entity e) {
		CURRENTLY_DRAGGED_ENTITIES.add(new DraggedEntity(
				e.getX(),
				e.getY(),
				mouseEventUtilSupplier.get().getMouseXRelativeToWorld(),
				mouseEventUtilSupplier.get().getMouseYRelativeToWorld(),
				e));
	}

	public static void render(GraphicsParameters params) {
		ENTITY_SELECTION.render(params.forEachEntityInCameraNeighborhood());
		ENTITY_LOADING_WIDGET.render(params);
		TILE_SELECTION_WIDGET.render(params);
	}

	public static void clearDraggedEntities() {
		CURRENTLY_DRAGGED_ENTITIES.clear();
	}

	public static void updatePositionsOfDraggedEntities() {
		for (DraggedEntity de : CURRENTLY_DRAGGED_ENTITIES) {
			final Entity e = de.getEntity();

			// The update scheme is r -> r + delta mouse. Also, snap to pixels (respecting pixel art).
			e.setX(Functions.snapToPixel(de.getEntityOriginalX() + mouseEventUtilSupplier.get().getMouseXRelativeToWorld() - de.getInitialMouseX(), MainGame.scaleFactor));
			e.setY(Functions.snapToPixel(de.getEntityOriginalY() + mouseEventUtilSupplier.get().getMouseYRelativeToWorld() - de.getInitialMouseY(), MainGame.scaleFactor));
		}
	}

	public static void setVisible(boolean b) {
		ENTITY_LOADING_WIDGET.getWidget().setVisible(b);
		TILE_SELECTION_WIDGET.getWidget().setVisible(b);
		if (b) {
			GlobalData.stage.addListener(SELECTION_BEGIN);
			GlobalData.stage.addListener(SELECTION_END);
		} else {
			GlobalData.stage.removeListener(SELECTION_BEGIN);
			GlobalData.stage.removeListener(SELECTION_END);
		}
	}

	public static void addDevCue() {
	}

	public static void saveWorld() {
		saveWorld(Path.of(WORLDS_PATH + GlobalData.currentWorld));
	}

	public static void saveWorld(Path path) {
		JSONObject worldState = Serializer.recordWorldState(worldRepresentationSupplier.get());
		try {
			Files.write(path, worldState.toString().getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void undoChange() {
	}

	public static void redoChange() {
	}

	public static void loadWorld() {
		EXECUTOR_SERVICE.execute(() -> {
			final String relativePath = Gdx.files.getLocalStoragePath().replace("\\", "/")
					+ "core/assets/worlds/";
			JFileChooser chooser = new JFileChooser(relativePath);
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			JFrame f = new JFrame();
			f.setVisible(true);
			f.toFront();
			f.setVisible(false);
			int res = chooser.showOpenDialog(f);
			f.dispose();
			if (res == JFileChooser.APPROVE_OPTION) {
				File selectedFile = chooser.getSelectedFile();
				GlobalData.currentWorld = selectedFile.getName();
				try {

					// TODO: deal with.
					Serializer.loadWorldState(atlas, new JSONObject(Files.readString(selectedFile.toPath())));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	public static EntityLoadingWidget getEntityLoadingWidget() {
		return ENTITY_LOADING_WIDGET;
	}

	public static TileSelectionWidget getTileSelectionWidget() {
		return TILE_SELECTION_WIDGET;
	}

}
