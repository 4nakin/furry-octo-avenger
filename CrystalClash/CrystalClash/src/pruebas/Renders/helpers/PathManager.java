package pruebas.Renders.helpers;

import java.util.Enumeration;
import java.util.Hashtable;

import pruebas.Entities.Path;
import pruebas.Entities.Unit;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class PathManager {

	private Hashtable<Unit, Path> paths;

	public static float STEP_LENGHT;
	public static int STEP_COUNT_PER_SEGMENT;
	public static float BIG_DOT_TIME;
	public static Texture SMALL_DOT_TEXTURE;
	public static Texture BIG_DOT_TEXTURE;

	public PathManager() {
		paths = new Hashtable<Unit, Path>();
	}

	public static void load() {
		STEP_COUNT_PER_SEGMENT = 13;
		BIG_DOT_TIME = 0.1f;
		STEP_LENGHT = 10;

		SMALL_DOT_TEXTURE = ResourceHelper.getTexture("data/Images/InGame/ActionDots/walk_dot_small.png");
		BIG_DOT_TEXTURE = ResourceHelper.getTexture("data/Images/InGame/ActionDots/walk_dot_big.png");
	}

	public void render(SpriteBatch batch, float dt) {
		Enumeration<Path> en = paths.elements();
		while (en.hasMoreElements())
			en.nextElement().draw(batch, dt);
	}

	public Path getOrCreatePath(Unit u) {
		Path p = getPath(u);
		if (p == null) {
			p = new Path();
			paths.put(u, p);
		}
		return p;
	}

	public Path getPath(Unit u) {
		return paths.get(u);
	}

	public void removePath(Unit u) {
		paths.remove(u);
	}

	public void addRect(Path p, float iniX, float iniY, float endX, float endY) {
		double angle = Math.atan2(endY - iniY, endX - iniX);
		float dx = (float) (STEP_LENGHT * Math.cos(angle));
		float dy = (float) (STEP_LENGHT * Math.sin(angle));
		for (int i = 0; i < STEP_COUNT_PER_SEGMENT; i++) {
			p.add(iniX, iniY);
			iniX += dx;
			iniY += dy;
		}
	}
}
