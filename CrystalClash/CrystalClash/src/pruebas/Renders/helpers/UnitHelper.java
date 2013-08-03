package pruebas.Renders.helpers;

import java.util.Hashtable;

import pruebas.Renders.UnitRender;
import pruebas.Util.SuperAnimation;
import pruebas.Util.UnitPrefReader;
import pruebas.Util.UnitPrefReader.UnitPrefReaderData;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class UnitHelper {
	public static final float WIDTH = 135;
	public static final float HEIGHT = 150;
	public static Hashtable<String, UnitRender> renderMap;

	public static void init() {
		renderMap = new Hashtable<String, UnitRender>();
	}

	public static UnitRender getUnitRender(String unitName) {
		if (renderMap.contains(unitName)) {
			return renderMap.get(unitName).clone();
		}

		UnitRender render = loadRender(unitName);
		render.setAnimation(UnitRender.ANIM.idle);
		renderMap.put(unitName, render);
		return render;
	}

	private static UnitRender loadRender(String unitName) {
		UnitRender render = new UnitRender();
		render.idleAnim = getUnitSuperAnimation(unitName, "idle");
		// render.idleAnim = getUnitSuperAnimation(unitName, "attack");
		// render.idleAnim = getUnitSuperAnimation(unitName, "run");

		return render;
	}

	public static SuperAnimation getUnitSuperAnimation(String unitName,
			String action) {
		String base_file_name = String.format("data/Units/%s/%s", unitName,
				action);

		UnitPrefReaderData data = UnitPrefReader.load(base_file_name + ".pref");

		Texture sheet = new Texture(Gdx.files.internal(base_file_name + ".png"));
		TextureRegion[][] tmp = TextureRegion.split(sheet, sheet.getWidth()
				/ data.cols, sheet.getHeight() / data.rows);
		TextureRegion[] frames = new TextureRegion[data.cols * data.rows];

		int index = 0;
		for (int i = 0; i < data.rows; i++) {
			for (int j = 0; j < data.cols; j++) {
				frames[index++] = tmp[i][j];
			}
		}

		SuperAnimation anim = new SuperAnimation(data.image_times, frames);
		anim.handle_x = data.handle_x;
		anim.handle_y = data.handle_x;
		return anim;
	}
}
