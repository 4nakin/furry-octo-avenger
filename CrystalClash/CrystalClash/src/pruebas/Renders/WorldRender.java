package pruebas.Renders;

import pruebas.Accessors.ActorAccessor;
import pruebas.Controllers.WorldController;
import pruebas.CrystalClash.CrystalClash;
import pruebas.Renders.helpers.CellHelper;
import pruebas.Renders.helpers.ResourceHelper;
import pruebas.Renders.helpers.UnitHelper;
import pruebas.Renders.helpers.ui.MessageBox;
import pruebas.Renders.helpers.ui.MessageBoxCallback;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class WorldRender implements InputProcessor {

	private TweenManager tweenManager;
	public static CellHelper cellHelper;

	private Texture backgroundTexture;
	private Image background;
	private Image sendBar;
	private Image moreOptions;
	private Image optionsBar;

	private TextButton btnSend;
	private TextButton btnMoreOptions;
	private TextButton btnSurrender;
	private TextButton btnBack;
	private TextButton btnClear;
	private Group grpMoreOptions;
	private boolean hideMoreOptions;
	public MessageBox msgBoxSurrender;

	private boolean showingAnimations;

	private WorldController world;
	GameRender gameRender;

	public WorldRender(WorldController world) {
		this.world = world;

		tweenManager = new TweenManager();

		cellHelper = new CellHelper();
		cellHelper.load();

		hideMoreOptions = false;
		showingAnimations = false;

		UnitHelper.init();
		load();
	}

	public void initFirstTurn() {
		gameRender = new SelectUnitsRender(world);
		showingAnimations = false;
		showHuds();
	}

	public void initNormalTurn() {
		gameRender = new NormalGame(world);
		showingAnimations = false;

		msgBoxSurrender = MessageBox.create()
				.setMessage("Surrender?? srsly??")
				.setTweenManager(tweenManager)
				.setCallback(new MessageBoxCallback() {
					@Override
					public void onEvent(int type, Object data) {
						if (type == MessageBoxCallback.YES) {
							System.out.println("surrender: "
									+ data);

						}
					}
				});
		showHuds();
	}

	public void initTurnAnimations() {
		gameRender = new TurnAnimations(world);
		showingAnimations = true;
	}

	public void render(float dt, SpriteBatch batch, Stage stage) {
		background.draw(batch, 1);
		for (int i = 0; i < world.cellGrid.length; i++) {
			for (int j = world.cellGrid[i].length - 1; j >= 0; j--) {
				world.cellGrid[i][j].getRender().draw(dt, batch);
			}
		}

		for (int j = 5; j >= 0; j--) {
			for (int i = 0; i < 9; i += 2) {
				world.cellGrid[i][j].getRender().drawUnits(dt, batch);
			}
			for (int i = 1; i < 9; i += 2) {
				world.cellGrid[i][j].getRender().drawUnits(dt, batch);
			}
		}

		gameRender.render(dt, batch, stage);

		if (!showingAnimations) {
			moreOptions.draw(batch, 1);
			optionsBar.draw(batch, 1);

			stage.addActor(btnMoreOptions);
			stage.addActor(grpMoreOptions);
			stage.addActor(sendBar);
			stage.addActor(btnSend);
			grpMoreOptions.act(dt);
			stage.addActor(msgBoxSurrender);
			tweenManager.update(dt);
		}
	}

	private void load() {
		TextureAtlas atlas = new TextureAtlas(
				"data/Images/InGame/options_bar.pack");
		Skin skin = new Skin(atlas);

		backgroundTexture = new Texture(
				Gdx.files.internal("data/Images/InGame/terrain.jpg"));
		background = new Image(backgroundTexture);
		background.setSize(CrystalClash.WIDTH, CrystalClash.HEIGHT);

		TextureRegion aux = skin.getRegion("option_send_bar");
		sendBar = new Image(aux);
		sendBar.setPosition(-sendBar.getWidth(), 0);
		aux = skin.getRegion("option_more_bar");
		moreOptions = new Image(aux);
		moreOptions.setPosition(-moreOptions.getWidth(), 0);
		aux = skin.getRegion("options_bar");
		optionsBar = new Image(aux);
		optionsBar.setPosition(0 - optionsBar.getWidth() - 75, 0);

		TextButtonStyle sendStyle = new TextButtonStyle(
				skin.getDrawable("option_send_button"),
				skin.getDrawable("option_send_button_pressed"), null, ResourceHelper.getFont());
		btnSend = new TextButton("", sendStyle);
		btnSend.setPosition(-sendBar.getWidth(), 0);
		btnSend.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				world.sendTurn();
			}
		});

		TextButtonStyle moreStyle = new TextButtonStyle(
				skin.getDrawable("option_more_button"),
				skin.getDrawable("option_more_button_pressed"), null, ResourceHelper.getFont());
		btnMoreOptions = new TextButton("", moreStyle);
		btnMoreOptions.setPosition(-moreOptions.getWidth(), 0);
		btnMoreOptions.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				hideMoreOptions = true;
				showMoreOptions();
			}
		});

		TextButtonStyle optionsStyle = new TextButtonStyle(
				skin.getDrawable("option_button"),
				skin.getDrawable("option_button_pressed"), null, ResourceHelper.getFont());

		btnSurrender = new TextButton("Surrender", optionsStyle);
		btnSurrender.setPosition(0, 0);
		btnSurrender.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				msgBoxSurrender.show("");
			}
		});

		btnBack = new TextButton("Back to Menu", optionsStyle);
		btnBack.setPosition(btnSurrender.getX() + btnSurrender.getWidth() + 2,
				0);
		btnBack.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				world.leaveGame();
			}
		});

		btnClear = new TextButton("Clear Moves", optionsStyle);
		btnClear.setPosition(btnBack.getX() + btnBack.getWidth() + 2, 0);
		btnClear.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				gameRender.clearAllChanges();
			}
		});

		grpMoreOptions = new Group();
		grpMoreOptions.addActor(btnSurrender);
		grpMoreOptions.addActor(btnBack);
		grpMoreOptions.addActor(btnClear);

		grpMoreOptions.setSize(optionsBar.getWidth(), optionsBar.getHeight());
		grpMoreOptions.setPosition(0 - grpMoreOptions.getWidth(), optionsBar.getY() + 5);
	}

	private void showMoreOptions() {
		float speed = 0.2f; // CrystalClash.ANIMATION_SPEED;
		Timeline.createParallel()
				.beginParallel()
				.push(Tween.to(moreOptions, ActorAccessor.X, speed).target(
						0 - moreOptions.getWidth()))
				.push(Tween.to(btnMoreOptions, ActorAccessor.X, speed).target(
						0 - btnMoreOptions.getWidth()))
				.end()
				.beginParallel()
				.beginSequence()
				.push(Tween.to(optionsBar, ActorAccessor.X, speed).target(
						sendBar.getWidth() - 70 + 15))
				.push(Tween.to(optionsBar, ActorAccessor.X, 0.05f).target(
						sendBar.getWidth() - 70))
				.end()
				.beginSequence()
				.push(Tween.to(grpMoreOptions, ActorAccessor.X, speed).target(
						sendBar.getWidth() - 70 + 75 + 15))
				.push(Tween.to(grpMoreOptions, ActorAccessor.X, 0.05f).target(
						sendBar.getWidth() - 70 + 75)).end().end()
				.start(tweenManager);
	}

	private void hideMoreOptions() {
		float speed = 0.2f; // CrystalClash.ANIMATION_SPEED;
		Timeline.createParallel()
				.beginParallel()
				.push(Tween.to(moreOptions, ActorAccessor.X, speed).target(
						sendBar.getWidth() - 35))
				.push(Tween.to(btnMoreOptions, ActorAccessor.X, speed).target(
						sendBar.getWidth() - 35 + moreOptions.getWidth()
								- btnMoreOptions.getWidth()))
				.end()
				.beginParallel()
				.push(Tween.to(optionsBar, ActorAccessor.X, speed).target(
						0 - optionsBar.getWidth()))
				.push(Tween.to(grpMoreOptions, ActorAccessor.X, speed).target(
						0 - grpMoreOptions.getWidth())).end()
				.start(tweenManager);
	}

	public void showHuds() {
		Timeline.createSequence()
				.beginParallel()
				.push(Tween.to(sendBar, ActorAccessor.X, CrystalClash.FAST_ANIMATION_SPEED).target(0))
				.push(Tween.to(btnSend, ActorAccessor.X, CrystalClash.FAST_ANIMATION_SPEED).target(0))
				.end()
				.beginParallel()
				.push(Tween.to(moreOptions, ActorAccessor.X, CrystalClash.FAST_ANIMATION_SPEED)
						.target(sendBar.getWidth() - 35))
				.push(Tween.to(btnMoreOptions, ActorAccessor.X, CrystalClash.FAST_ANIMATION_SPEED)
						.target(sendBar.getWidth() - 35 + moreOptions.getWidth() - btnMoreOptions.getWidth()))
				.end()
				.start(tweenManager);
	}

	public void dispose() {
		backgroundTexture.dispose();
	}

	@Override
	public boolean keyDown(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		Vector2 vec = GameEngine.getRealPosition(screenX, screenY);

		if (hideMoreOptions
				&& (vec.x > optionsBar.getX() + optionsBar.getWidth() || vec.y > btnSurrender
						.getTop() + 25)) {
			hideMoreOptions();
		}
		gameRender.touchDown(vec.x, vec.y, pointer, button);

		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		Vector2 vec = GameEngine.getRealPosition(screenX, screenY);
		gameRender.touchUp(vec.x, vec.y, pointer, button);
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		Vector2 vec = GameEngine.getRealPosition(screenX, screenY);
		gameRender.touchDragged(vec.x, vec.y, pointer);
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		return false;
	}

	public Timeline pushEnterAnimation(Timeline t) {
		return t;
	}

	public Timeline pushExitAnimation(Timeline t) {
		t.beginSequence();
		t.beginParallel();
		gameRender.pushExitAnimation(t);
		t.push(Tween.to(optionsBar, ActorAccessor.X, CrystalClash.FAST_ANIMATION_SPEED)
				.target(-optionsBar.getWidth()))
				.push(Tween.to(btnMoreOptions, ActorAccessor.X, CrystalClash.FAST_ANIMATION_SPEED)
						.target(-optionsBar.getWidth()))
				.push(Tween.to(grpMoreOptions, ActorAccessor.X, CrystalClash.FAST_ANIMATION_SPEED)
						.target(-grpMoreOptions.getWidth()))
				.push(Tween.to(moreOptions, ActorAccessor.X, CrystalClash.FAST_ANIMATION_SPEED)
						.target(-grpMoreOptions.getWidth()))
				.end()
				.beginParallel()
				.push(Tween.to(btnSend, ActorAccessor.X, CrystalClash.FAST_ANIMATION_SPEED)
						.target(-sendBar.getWidth()))
				.push(Tween.to(sendBar, ActorAccessor.X, CrystalClash.FAST_ANIMATION_SPEED)
						.target(-sendBar.getWidth()))
				.end()
				.end();
		return t;
	}
}
