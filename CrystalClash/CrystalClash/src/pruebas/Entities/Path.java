package pruebas.Entities;

import pruebas.Renders.helpers.PathManager;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class Path {
	public float time;
	public Array<Vector2> dots;
	public int bigDotIndex;

	public Path() {
		dots = new Array<Vector2>();
		bigDotIndex = 0;
	}

	public void add(float x, float y) {
		dots.add(new Vector2(x, y));
	}

	public void truncate(int size) {
		for (int i = dots.size - 1; i >= (size - 1) * PathManager.STEP_COUNT_PER_SEGMENT; i--)
			dots.removeIndex(i);
	}

	public void draw(SpriteBatch batch, float dt) {
		if (dots.size > 0) {
			time += dt;
			if (time >= PathManager.BIG_DOT_TIME) {
				time = 0;
				bigDotIndex = (bigDotIndex + 1) % dots.size;
			}
			Vector2 v;
			for (int i = 0; i < dots.size; i++) {
				v = dots.get(i);
				if (i == bigDotIndex)
					batch.draw(PathManager.BIG_DOT_TEXTURE, v.x, v.y);
				else
					batch.draw(PathManager.SMALL_DOT_TEXTURE, v.x, v.y);
			}
		}
	}
}
