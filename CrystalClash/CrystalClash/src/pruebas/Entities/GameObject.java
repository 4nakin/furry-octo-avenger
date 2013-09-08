package pruebas.Entities;

import pruebas.Renders.helpers.CellHelper;

import com.badlogic.gdx.math.Vector2;

public class GameObject {
	private Vector2 position = Vector2.Zero;
	private GridPos gridPos = new GridPos(0, 0);
	private boolean visible;

	public void setPosition(float x, float y) {
		position = new Vector2(x, y);
	}

	public Vector2 getPosition() {
		return position;
	}

	public float getX() {
		return position.x;
	}

	public float getY() {
		return position.y;
	}

	public float getCenterX() {
		return position.x + CellHelper.CELL_WIDTH / 2;
	}

	public float getCenterY() {
		return position.y + CellHelper.CELL_HEIGHT / 2;
	}

	public void setGrisPosition(int x, int y) {
		gridPos.setX(x);
		gridPos.setY(y);
	}

	public GridPos getGridPosition() {
		return gridPos;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean v) {
		visible = v;
	}

	@Override
	public boolean equals(Object obj) {
		return this.gridPos == ((GameObject) obj).getGridPosition();
	}
}
