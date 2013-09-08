package pruebas.Renders;

import pruebas.Accessors.ActorAccessor;
import pruebas.Controllers.GameController;
import pruebas.Controllers.WorldController;
import pruebas.CrystalClash.CrystalClash;
import pruebas.Entities.Cell;
import pruebas.Entities.GridPos;
import pruebas.Entities.Path;
import pruebas.Entities.Unit;
import pruebas.Entities.helpers.AttackUnitAction;
import pruebas.Entities.helpers.DefendUnitAction;
import pruebas.Entities.helpers.MoveUnitAction;
import pruebas.Entities.helpers.NoneUnitAction;
import pruebas.Entities.helpers.UnitAction;
import pruebas.Entities.helpers.UnitAction.UnitActionType;
import pruebas.Renders.helpers.PathManager;
import pruebas.Renders.helpers.ResourceHelper;
import pruebas.Util.Tuple;
import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.TweenManager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;

public class NormalGame extends GameRender {
	private TweenManager tweenManager;

	private Unit selectedUnit;
	private Cell selectedCell;

	private Image selectorArrow;
	private float arrowX;
	private float arrowY;

	private Image actionsBar;
	private TextButton btnAttack;
	private TextButton btnMove;
	private TextButton btnDefense;
	private TextButton btnUndo;
	private Group grpActionBar;
	private Label lblMoves;
	private Label lblAttack;
	private boolean actionsBarVisible;
	private boolean undoVisible;

	private UnitActionType actionType;
	private int maxMoves;

	private UnitAction unitAction;
	private Array<Cell> alreadyAssigned;
	private Array<MoveUnitAction> mActions;
	private Array<Tuple<Unit, MoveUnitAction>> ghostlyUnits;

	private Array<Unit> defensiveUnits;

	private Array<AttackUnitAction> aActions;

	private PathManager paths;

	public NormalGame(WorldController world) {
		super(world);

		tweenManager = new TweenManager();

		selectedUnit = null;
		selectedCell = null;

		arrowX = 0;
		arrowY = CrystalClash.HEIGHT + 20;
		undoVisible = false;
		actionsBarVisible = false;

		actionType = UnitActionType.NONE;
		maxMoves = 0;

		alreadyAssigned = new Array<Cell>();
		mActions = new Array<MoveUnitAction>();
		ghostlyUnits = new Array<Tuple<Unit, MoveUnitAction>>();

		defensiveUnits = new Array<Unit>();

		aActions = new Array<AttackUnitAction>();

		paths = new PathManager();

		load();
		clearAllChanges();
		GameEngine.hideLoading();
	}

	public void load() {
		GameController.getInstance().loadUnitsStats();

		PathManager.load();

		Texture arrow = new Texture(
				Gdx.files.internal("data/Images/InGame/selector_arrow.png"));
		selectorArrow = new Image(arrow);
		selectorArrow.setPosition(arrowX, arrowY);

		TextureAtlas atlas = new TextureAtlas(
				"data/Images/InGame/options_bar.pack");
		Skin skin = new Skin(atlas);

		TextureRegion aux = skin.getRegion("actions_bar");
		actionsBar = new Image(aux);

		TextButtonStyle attackStyle = new TextButtonStyle(
				skin.getDrawable("action_attack_button"),
				skin.getDrawable("action_attack_button_pressed"), null, ResourceHelper.getFont());
		btnAttack = new TextButton("", attackStyle);
		btnAttack.setPosition(actionsBar.getX() + 15, actionsBar.getY() - 20);
		btnAttack.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				btnUndo.setPosition(btnAttack.getX(), btnDefense.getY());
				undoVisible = true;
				updateActionsBar();
				unitAction = new AttackUnitAction(selectedUnit.isMelee());
				unitAction.origin = selectedCell;

				showAbleToAttackCells();
			}
		});

		TextButtonStyle defenseStyle = new TextButtonStyle(
				skin.getDrawable("action_defensive_button"),
				skin.getDrawable("action_defensive_button_pressed"), null, ResourceHelper.getFont());
		btnDefense = new TextButton("", defenseStyle);
		btnDefense.setPosition(btnAttack.getX() + btnAttack.getWidth() + 15,
				actionsBar.getY());
		btnDefense.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				btnUndo.setPosition(btnDefense.getX(), btnDefense.getY());
				undoVisible = true;
				updateActionsBar();
				unitAction = new DefendUnitAction();
				unitAction.origin = selectedCell;
				defensiveUnits.add(selectedUnit);
				selectedUnit.setDefendingPosition(true);

				showAction(unitAction, false);
				actionType = UnitActionType.DEFENSE;
			}
		});

		TextButtonStyle moveStyle = new TextButtonStyle(
				skin.getDrawable("action_run_button"),
				skin.getDrawable("action_run_button_pressed"), null, ResourceHelper.getFont());
		btnMove = new TextButton("", moveStyle);
		btnMove.setPosition(btnDefense.getX() + btnDefense.getWidth() + 15,
				actionsBar.getY() - 20);
		btnMove.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				btnUndo.setPosition(btnMove.getX(), btnDefense.getY());
				undoVisible = true;
				updateActionsBar();

				unitAction = new MoveUnitAction();
				unitAction.origin = selectedCell;
				((MoveUnitAction) unitAction).moves.add(selectedCell);

				showAbleToMoveCells();
				showAction(unitAction, true);
			}
		});

		TextButtonStyle undoStyle = new TextButtonStyle(
				skin.getDrawable("action_cancel_button"),
				skin.getDrawable("action_cancel_button_pressed"), null, ResourceHelper.getFont());
		btnUndo = new TextButton("", undoStyle);
		btnUndo.setPosition(0, 300); // Afuera de la ventana
		btnUndo.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				undoAction();
				undoVisible = false;
				updateActionsBar();
			}
		});

		lblAttack = new Label("150", new LabelStyle(ResourceHelper.getFont(), Color.WHITE));
		lblAttack.setPosition(btnAttack.getX()
				+ (btnAttack.getWidth() / 2 - lblAttack.getWidth() / 2),
				btnAttack.getY() + 3);

		lblMoves = new Label("5", new LabelStyle(ResourceHelper.getFont(), Color.WHITE));
		lblMoves.setPosition(btnMove.getX()
				+ (btnMove.getWidth() / 2 - lblMoves.getWidth() / 2),
				btnMove.getY() + 3);

		grpActionBar = new Group();
		grpActionBar.addActor(actionsBar);
		grpActionBar.addActor(btnAttack);
		grpActionBar.addActor(lblAttack);
		grpActionBar.addActor(btnMove);
		grpActionBar.addActor(lblMoves);
		grpActionBar.addActor(btnDefense);

		grpActionBar.setSize(actionsBar.getWidth(), actionsBar.getHeight());
		grpActionBar.setPosition(
				CrystalClash.WIDTH / 2 - grpActionBar.getWidth() / 2,
				CrystalClash.HEIGHT + 50);

		addActor(grpActionBar);
	}

	private void moveArrow(Unit u) {
		if (u != null) {
			System.out.println(arrowY);
			if (selectorArrow.getY() >= CrystalClash.HEIGHT) {
				selectorArrow.setPosition(u.getX(), CrystalClash.HEIGHT + 20);
			}
			arrowX = u.getX();
			arrowY = u.getY() + 120;
		} else {
			arrowY = CrystalClash.HEIGHT + 20;
		}

		tweenManager.killAll();
		Timeline.createParallel()
				.push(Tween.to(selectorArrow, ActorAccessor.X, CrystalClash.ANIMATION_SPEED)
						.target(arrowX))
				.push(Tween.to(selectorArrow, ActorAccessor.Y, CrystalClash.ANIMATION_SPEED)
						.target(arrowY))
				.setCallback(new TweenCallback() {
					@Override
					public void onEvent(int type, BaseTween<?> source) {
						selectorArrow.setPosition(arrowX, arrowY);
						arrowAnimation();
					}
				}).start(tweenManager);
	}

	private void arrowAnimation() {
		Timeline.createSequence()
				.push(Tween.set(selectorArrow, ActorAccessor.Y).target(arrowY))
				.push(Tween.to(selectorArrow, ActorAccessor.Y, CrystalClash.ANIMATION_SPEED).target(
						arrowY - 10))
				.push(Tween.to(selectorArrow, ActorAccessor.Y, CrystalClash.ANIMATION_SPEED).target(
						arrowY)).repeat(Tween.INFINITY, 0).start(tweenManager);
	}

	private void showActionsBar() {
		Timeline t = Timeline.createSequence();
		if (450 < selectedUnit.getX() && selectedUnit.getX() < 825) {
			if (actionsBarVisible) {
				t.push(Tween.to(grpActionBar, ActorAccessor.X, CrystalClash.ANIMATION_SPEED).target(
						CrystalClash.WIDTH / 4 - grpActionBar.getWidth() / 2));
			} else {
				grpActionBar.setX(CrystalClash.WIDTH / 4
						- grpActionBar.getWidth() / 2);
				t.push(Tween.to(grpActionBar, ActorAccessor.Y, CrystalClash.ANIMATION_SPEED).target(
						CrystalClash.HEIGHT - grpActionBar.getHeight()));
			}
		} else {
			if (actionsBarVisible) {
				t.push(Tween.to(grpActionBar, ActorAccessor.X, CrystalClash.ANIMATION_SPEED).target(
						CrystalClash.WIDTH / 2 - grpActionBar.getWidth() / 2));
			} else {
				grpActionBar.setX(CrystalClash.WIDTH / 2
						- grpActionBar.getWidth() / 2);
				t.push(Tween.to(grpActionBar, ActorAccessor.Y, CrystalClash.ANIMATION_SPEED).target(
						CrystalClash.HEIGHT - grpActionBar.getHeight()));
			}
		}
		t.start(tweenManager);
		actionsBarVisible = true;
	}

	private void hideActionsBar() {
		actionsBarVisible = false;
		pushHideActionBar(Timeline.createSequence()).start(tweenManager);
	}

	private Timeline pushHideActionBar(Timeline t) {
		return t.push(Tween.to(grpActionBar, ActorAccessor.Y, CrystalClash.ANIMATION_SPEED).target(
				CrystalClash.HEIGHT + 50));
	}

	private void showAbleToMoveCells() {
		clearCells();
		actionType = UnitActionType.MOVE;

		if (((MoveUnitAction) unitAction).moves.size <= maxMoves) {
			Cell top = ((MoveUnitAction) unitAction).moves.peek();
			boolean continueMoving = top.getUnit() == null;

			if (top.Equals(unitAction.origin))
				continueMoving = true;

			if (continueMoving) {
				int[][] cells = top.neigbours;
				Cell aux = null;
				for (int i = 0; i < top.neigbours.length; i++) {
					aux = world.cellAtByGrid(cells[i][0], cells[i][1]);
					if (aux.getUnit() == null
							&& !alreadyAssigned.contains(aux, true))
						aux.setState(Cell.State.ABLE_TO_MOVE);
				}
			}
		}
	}

	private void showAbleToAttackCells() {
		clearCells();
		actionType = UnitActionType.ATTACK;

		showAbleToAttackCellRecursive(selectedCell, selectedUnit.isMelee(), selectedUnit.getRange(), false);
	}

	// Method that actually "shows" (change state) the cell where units can
	// attack
	private void showAbleToAttackCellRecursive(Cell cell, boolean onlyCellsWithUnit, int range, boolean hide) {
		int[][] cells = cell.neigbours;

		Unit unit;
		Cell neigbourCell = null;
		for (int i = 0; i < cells.length; i++) {
			neigbourCell = world.cellAtByGrid(cells[i][0], cells[i][1]);
			unit = neigbourCell.getUnit();
			if ((onlyCellsWithUnit && (unit == null || !unit.isEnemy())) || hide)
				neigbourCell.setState(Cell.State.NONE);
			else
				neigbourCell.setState(Cell.State.ABLE_TO_ATTACK);

			if (range > 1)
				showAbleToAttackCellRecursive(neigbourCell, onlyCellsWithUnit, range - 1, hide);
		}
	}

	private void clearCells() {
		switch (actionType) {
		case PLACE:
			break;
		case ATTACK:
			showAbleToAttackCellRecursive(selectedCell, false, selectedUnit.getRange(), true);
			break;
		case DEFENSE:
			break;
		case MOVE:
			Cell top = null;
			for (int i = 0; i < ((MoveUnitAction) unitAction).moves.size; i++) {
				top = ((MoveUnitAction) unitAction).moves.get(i);
				top.setState(Cell.State.NONE);
				int[][] cells = top.neigbours;
				for (int j = 0; j < top.neigbours.length; j++) {
					world.setCellStateByGridPos(cells[j][0], cells[j][1],
							Cell.State.NONE);
				}
			}
			break;
		case NONE:
			break;
		default:
			break;
		}
		actionType = UnitActionType.NONE;
	}

	private void clearSelection() {
		selectedUnit = null;
		selectedCell = null;
		moveArrow(selectedUnit);
		hideActionsBar();
	}

	private void showAction(UnitAction action, boolean stillAssigning) {
		unitAction = action;
		GridPos g = null;
		switch (unitAction.getActionType()) {
		case ATTACK:
			btnUndo.setPosition(btnAttack.getX(), btnDefense.getY());
			if (stillAssigning) {
				showAbleToAttackCells();
			}

			if (((AttackUnitAction) action).target != null) {
				g = ((AttackUnitAction) action).target.getGridPosition();
				world.setCellStateByGridPos(g.getX(), g.getY(), Cell.State.ATTACK_TARGET_CENTER);
			}
			break;
		case DEFENSE:
			actionType = UnitActionType.DEFENSE;
			btnUndo.setPosition(btnDefense.getX(), btnDefense.getY());
			break;
		case MOVE:
			actionType = UnitActionType.MOVE;
			btnUndo.setPosition(btnMove.getX(), btnDefense.getY());
			if (stillAssigning) {
				showAbleToMoveCells();
			}
			for (int i = 0; i < ((MoveUnitAction) action).moves.size; i++) {
				g = ((MoveUnitAction) action).moves.get(i).getGridPosition();
				world.setCellStateByGridPos(g.getX(), g.getY(), Cell.State.MOVE_TARGET);
			}
			lblMoves.setText(maxMoves + 1 - ((MoveUnitAction) unitAction).moves.size + "");
			undoVisible = true;
			break;
		case NONE:
			undoVisible = false;
			actionType = UnitActionType.NONE;
			break;
		case PLACE:
			undoVisible = false;
			actionType = UnitActionType.NONE;
			break;
		default:
			break;
		}
	}

	private void showAssignedActions() {
		for (int i = 0; i < mActions.size; i++) {
			showAction(mActions.get(i), false);
		}

		for (int i = 0; i < aActions.size; i++) {
			showAction(aActions.get(i), false);
		}

		actionType = UnitActionType.NONE;
	}

	private void hideAction(UnitAction action) {
		GridPos g = null;
		switch (action.getActionType()) {
		case ATTACK:
			if (((AttackUnitAction) action).target != null) {
				g = ((AttackUnitAction) action).target.getGridPosition();
				world.setCellStateByGridPos(g.getX(), g.getY(), Cell.State.NONE);
			}
			break;
		case DEFENSE:
			break;
		case MOVE:
			for (int i = 0; i < ((MoveUnitAction) action).moves.size; i++) {
				g = ((MoveUnitAction) action).moves.get(i).getGridPosition();
				world.setCellStateByGridPos(g.getX(), g.getY(), Cell.State.NONE);
			}
			break;
		case NONE:
			break;
		case PLACE:
			break;
		default:
			break;
		}
	}

	private void hideAssignedActions() {
		for (int i = 0; i < mActions.size; i++) {
			hideAction(mActions.get(i));
		}

		for (int i = 0; i < aActions.size; i++) {
			hideAction(aActions.get(i));
		}
		actionType = UnitActionType.NONE;
	}

	private void undoAction() {
		switch (actionType) {
		case ATTACK:
			clearCells();
			hideAction(unitAction);
			aActions.removeValue((AttackUnitAction) unitAction, false);

			unitAction = new NoneUnitAction();
			selectedCell.setAction(unitAction);

			actionType = UnitActionType.NONE;
			break;
		case DEFENSE:
			selectedUnit.setDefendingPosition(false);

			unitAction = new NoneUnitAction();
			selectedCell.setAction(unitAction);

			actionType = UnitActionType.NONE;
			break;
		case MOVE:
			clearCells();
			lblMoves.setText(maxMoves + "");

			clearMoveAction();

			unitAction = new NoneUnitAction();
			selectedCell.setAction(unitAction);
			paths.removePath(selectedUnit);

			actionType = UnitActionType.NONE;
			break;
		case NONE:
			break;
		case PLACE:
			break;
		default:
			break;
		}
	}

	private void updateActionsBar() {
		if (undoVisible) {
			grpActionBar.removeActor(btnAttack);
			grpActionBar.removeActor(lblAttack);
			grpActionBar.removeActor(btnMove);
			grpActionBar.removeActor(lblMoves);
			grpActionBar.removeActor(btnDefense);
			grpActionBar.addActor(btnUndo);
		} else {
			grpActionBar.addActor(btnAttack);
			grpActionBar.addActor(lblAttack);
			grpActionBar.addActor(btnMove);
			grpActionBar.addActor(lblMoves);
			grpActionBar.addActor(btnDefense);
			grpActionBar.removeActor(btnUndo);
		}
	}

	// Borra la action de la lista, saca el fantasma y saca la cell de las
	// asignadas
	private void clearMoveAction() {
		MoveUnitAction action = (MoveUnitAction) unitAction;
		MoveUnitAction aux = null;
		for (int i = 0; i < ghostlyUnits.size; i++) {
			aux = ghostlyUnits.get(i).getSecond();
			if (action.equals(aux)) {
				ghostlyUnits.removeIndex(i);
				alreadyAssigned.removeValue(aux.moves.get(aux.moves.size - 1),
						true);
			}
		}

		mActions.removeValue(action, false);
	}

	@Override
	public void clearAllChanges() {
		clearSelection();

		mActions.clear();
		alreadyAssigned.clear();
		ghostlyUnits.clear();
		aActions.clear();

		for (int i = 0; i < defensiveUnits.size; i++) {
			defensiveUnits.get(i).setDefendingPosition(false);
		}
		defensiveUnits.clear();

		for (int i = 0; i < world.cellGrid.length; i++) {
			for (int j = 0; j < world.cellGrid[0].length; j++) {
				unitAction = new NoneUnitAction();
				world.cellGrid[i][j].setAction(unitAction);
				world.cellGrid[i][j].setState(Cell.State.NONE);
			}
		}

		actionType = UnitActionType.NONE;
	}

	@Override
	public void render(float dt, SpriteBatch batch) {
		selectorArrow.draw(batch, 1);
		paths.render(batch, dt);
		tweenManager.update(dt);
	}

	@Override
	public boolean touchDown(float x, float y, int pointer, int button) {
		Cell cell = world.cellAt(x, y);
		if (cell != null) {
			switch (actionType) {
			case PLACE:
				break;
			case ATTACK:
				if (cell.getState() == Cell.State.ABLE_TO_ATTACK) {
					showAbleToAttackCells();
					cell.setState(Cell.State.ATTACK_TARGET_CENTER);
					((AttackUnitAction) unitAction).target = cell;
				} else {
					saveAttack();
				}
				break;
			case DEFENSE:
				saveDefense();
				break;
			case MOVE:
				if (cell.getState() == Cell.State.ABLE_TO_MOVE) {
					Array<Cell> moves = ((MoveUnitAction) unitAction).moves;

					Path p = paths.getOrCreatePath(selectedUnit);

					if (moves.size == 0) {
						paths.addRect(p, selectedCell.getCenterX(), selectedCell.getCenterY(),
								cell.getCenterX(), cell.getCenterY());
					} else {
						paths.addRect(p, moves.get(moves.size - 1).getCenterX(), moves.get(moves.size - 1).getCenterY(),
								cell.getCenterX(), cell.getCenterY());
					}

					lblMoves.setText(maxMoves - moves.size + "");
					moves.add(cell);

					clearCells();
					showAction(unitAction, true);
				} else if (cell.getState() == Cell.State.MOVE_TARGET) {
					clearMoveAction();
					clearCells();
					int index = ((MoveUnitAction) unitAction).moves.indexOf(cell, true);
					if (index != -1) {
						((MoveUnitAction) unitAction).moves.truncate(index + 1);
						paths.getPath(selectedUnit).truncate(index + 1);
					}

					lblMoves.setText(maxMoves + 1 - ((MoveUnitAction) unitAction).moves.size + "");

					clearCells();
					showAction(unitAction, true);
				} else {
					saveMove();
				}
				break;
			case NONE:
				showAssignedActions();
				clearSelection();
				Unit u = cell.getUnit();
				if (u != null) {
					if (selectedUnit != u) {
						selectedUnit = u;
						selectedCell = cell;

						lblAttack.setText(GameController.getInstance()
								.getUnitAttack(selectedUnit.getName()) + "");
						maxMoves = GameController.getInstance().getUnitSpeed(
								selectedUnit.getName());
						lblMoves.setText(maxMoves + "");

						moveArrow(selectedUnit);
						hideAssignedActions();

						if (u.isEnemy()) {
							hideActionsBar();
						} else {
							if (cell.getAction() != null) {
								showAction(cell.getAction(), true);
							}

							updateActionsBar();
							showActionsBar();
						}
					}
				}
				break;
			default:
				break;
			}
		}
		return true;
	}

	@Override
	public boolean touchUp(float screenX, float screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(float screenX, float screenY, int pointer) {
		return false;
	}

	@Override
	public boolean pan(float x, float y, float deltaX, float deltaY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Timeline pushEnterAnimation(Timeline t) {
		return t;
	}

	@Override
	public Timeline pushExitAnimation(Timeline t) {
		arrowY = CrystalClash.HEIGHT + 20;
		tweenManager.killAll();
		return pushHideActionBar(t)
				.push(Tween.to(grpActionBar, ActorAccessor.X, CrystalClash.ANIMATION_SPEED)
						.target(-grpActionBar.getHeight()))
				.push(Tween.to(selectorArrow, ActorAccessor.Y, CrystalClash.ANIMATION_SPEED)
						.target(arrowY));
	}

	@Override
	public boolean canSend() {
		return selectedUnit == null;
	}

	private void saveAttack() {
		if (((AttackUnitAction) unitAction).target != null) {
			aActions.add((AttackUnitAction) unitAction);
		} else {
			unitAction = new NoneUnitAction();
		}
		selectedCell.setAction(unitAction);
		clearCells();
		clearSelection();
		showAssignedActions();
	}

	private void saveDefense() {
		selectedCell.setAction(unitAction);
		clearSelection();
		actionType = UnitActionType.NONE;
	}

	private void saveMove() {
		clearMoveAction();
		clearCells();

		MoveUnitAction action = (MoveUnitAction) unitAction;
		if (action.moves.size > 1) {
			mActions.add(action);
			// Unit ghost = new Unit(selectedUnit.getName());
			// if (world.player == 2)
			// ghost.getRender().setFacing(FACING.left);

			// Cell ghostCell = action.moves
			// .get(action.moves.size - 1);

			// float offSetX = CellHelper.UNIT_PLAYER_1_X;
			// float offSetY = CellHelper.UNIT_PLAYER_1_Y;

			// ghost.setPosition(ghostCell.getX() + offSetX,
			// ghostCell.getY() + offSetY);
			//
			// ghostlyUnits.add(new Tuple<Unit, MoveUnitAction>(ghost,
			// action));
			// alreadyAssigned.add(ghostCell);
		} else {
			unitAction = new NoneUnitAction();
		}
		selectedCell.setAction(unitAction);

		clearSelection();
		showAssignedActions();
	}

	@Override
	public void onSend() {
		switch (actionType) {
		case ATTACK:
			saveAttack();
			break;
		case DEFENSE:
			saveDefense();
			break;
		case MOVE:
			saveMove();
			break;
		}
	}

	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}
}
