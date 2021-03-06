package pruebas.Renders.helpers;

import java.util.Hashtable;

import pruebas.Util.FileUtil;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;

public class ResourceHelper {
	private static Hashtable<String, Texture> textureMap;

	private static TextureAtlas atlas;
	private static Skin skin;
	private static BitmapFont font;

	private static TextButtonStyle buttonStyle;
	private static TextButtonStyle outerButtonStyle;

	public static void fastLoad() {
		textureMap = new Hashtable<String, Texture>();
	}

	public static void slowLoad() {
		atlas = new TextureAtlas("data/Images/Buttons/buttons.pack");
		skin = new Skin(atlas);
		font = new BitmapFont(Gdx.files.internal("data/Fonts/font.fnt"), false);

		buttonStyle = new TextButtonStyle(
				skin.getDrawable("button_orange"),
				skin.getDrawable("button_orange_pressed"), null, font);

		outerButtonStyle = new TextButtonStyle(
				skin.getDrawable("outer_button_orange"),
				skin.getDrawable("outer_button_orange_pressed"), null, font);
	}

	public static Texture getTexture(String path) {
		if (textureMap.contains(path)) {
			return textureMap.get(path);
		}
		return FileUtil.getTexture(path);
	}

	public static Skin getCommonButtonsSkin() {
		return skin;
	}

	public static BitmapFont getFont() {
		return font;
	}

	public static TextButtonStyle getButtonStyle() {
		return buttonStyle;
	}

	public static TextButtonStyle getOuterButtonStyle() {
		return outerButtonStyle;
	}
}
