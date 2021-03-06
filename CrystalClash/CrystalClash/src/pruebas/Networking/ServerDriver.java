package pruebas.Networking;

import java.util.HashMap;
import java.util.Map;

import pruebas.Controllers.GameController;
import pruebas.Controllers.MenuGames;
import pruebas.Renders.GameEngine;
import pruebas.Renders.helpers.ui.MessageBox;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.HttpMethods;
import com.badlogic.gdx.Net.HttpRequest;
import com.badlogic.gdx.Net.HttpResponse;
import com.badlogic.gdx.Net.HttpResponseListener;
import com.badlogic.gdx.net.HttpParametersUtils;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

public class ServerDriver {
	private static String SERVER_URL = "http://fuzzy-adventure.herokuapp.com/";

	private final static String ACTION_LOG_IN = "log_in";
	private final static String ACTION_SIGN_IN = "sign_in";
	private final static String ACTION_LIST_GAMES = "list_games";
	private final static String ACTION_ENABLE_RANDOM = "enable_random";
	private final static String ACTION_GAME_TURN = "game_turn";

	public static void sendSignIn(final String email, final String password) {
		Map<String, String> data = new HashMap<String, String>();
		data.put("email", email);
		data.put("password", password);

		Gdx.net.sendHttpRequest(getPost(ACTION_SIGN_IN, data),
				new HttpResponseListener() {
					public void handleHttpResponse(HttpResponse httpResponse) {
						JsonValue values = ServerDriver
								.ProcessResponce(httpResponse);
						if (values.getString("value").equals("ok")) {
							JsonValue data = values.get("data");
							GameController.getInstance().logInSuccess(data.getString("id"), email, password);
						} else {
							GameEngine.getInstance().logInError(values.getString("message"));
						}
					}

					public void failed(Throwable t) {
						exceptionMessage();
					}
				});
	}

	public static void sendLogIn(final String email, final String password) {
		Map<String, String> data = new HashMap<String, String>();
		data.put("email", email);
		data.put("password", password);

		Gdx.net.sendHttpRequest(getPost(ACTION_LOG_IN, data),
				new HttpResponseListener() {
					public void handleHttpResponse(HttpResponse httpResponse) {
						JsonValue values = ServerDriver.ProcessResponce(httpResponse);
						if (values.getString("value").equals("ok")) {
							JsonValue data = values.get("data");
							GameController.getInstance().logInSuccess(data.getString("id"), email, password);
						} else {
							GameEngine.getInstance().logInError(values.getString("message"));
						}
					}

					public void failed(Throwable t) {
						exceptionMessage();
					}
				});
	}

	public static void getListGames(String id) {
		Gdx.net.sendHttpRequest(getGet(ACTION_LIST_GAMES + "/p/" + id),
				new HttpResponseListener() {
					public void handleHttpResponse(HttpResponse httpResponse) {
						JsonValue values = ServerDriver
								.ProcessResponce(httpResponse);
						if (values.getString("value").equals("ok")) {
							JsonValue data = values.get("data");
							String[][] games = new String[data.size][5];
							JsonValue child;
							for (int i = 0; i < games.length; i++) {
								child = data.get(i);
								games[i][0] = child.getString("game_id");
								games[i][1] = child.getString("name");
								games[i][2] = child.getString("victories");
								games[i][3] = child.getString("turn");
								games[i][4] = child.getString("state");
							}
							MenuGames.getInstance().getGamesListSuccess(games);
						} else {
							MenuGames.getInstance().getGamesListError(values.getString("message"));
						}
					}

					public void failed(Throwable t) {
						exceptionMessage();
					}
				});
	}

	public static void enableRandom(String id) {
		Map<String, String> data = new HashMap<String, String>();
		data.put("id", id);

		Gdx.net.sendHttpRequest(getPost(ACTION_ENABLE_RANDOM, data),
				new HttpResponseListener() {
					public void handleHttpResponse(HttpResponse httpResponse) {
						JsonValue values = ServerDriver
								.ProcessResponce(httpResponse);
						if (values.getString("value").equals("ok")) {
							MenuGames.getInstance().enableRandomSuccess();
						} else {
							MenuGames.getInstance().enableRandomError(values.getString("message"));
						}
					}

					public void failed(Throwable t) {
						exceptionMessage();
					}
				});
	}

	public static void sendGameTurn(String playerId, String gameId, String turnData, String result) {
		Map<String, String> data = new HashMap<String, String>();
		data.put("player_id", playerId);
		data.put("game_id", gameId);
		if (turnData == null)
			turnData = "ended";
		data.put("data", turnData);
		if (result != null) {
			data.put("result", result);
		}

		System.out.println("Sending-> " + data);
		Gdx.net.sendHttpRequest(getPost(ACTION_GAME_TURN, data),
				new HttpResponseListener() {
					public void handleHttpResponse(HttpResponse httpResponse) {
						JsonValue values = ServerDriver
								.ProcessResponce(httpResponse);
						if (values.getString("value").equals("ok")) {
							MenuGames.getInstance().sendGameTurnSuccess(values.getString("data"));
						} else {
							MenuGames.getInstance().sendGameTurnError(values.getString("message"));
						}
					}

					public void failed(Throwable t) {
						exceptionMessage();
					}
				});
	}

	public static void getGameTurn(String playerId, String gameId, final int turn) {
		Gdx.net.sendHttpRequest(getGet(ACTION_GAME_TURN + "/p/" + playerId
				+ "/g/" + gameId), new HttpResponseListener() {
			public void handleHttpResponse(HttpResponse httpResponse) {
				JsonValue values =
						ServerDriver.ProcessResponce(httpResponse);
				if (values.getString("value").equals("ok")) {
					MenuGames.getInstance().getGameTurnSuccess(values.get("data"), turn);
				} else {
					MenuGames.getInstance().getGameTurnError(values.getString("message"));
				}
			}

			public void failed(Throwable t) {
				exceptionMessage();
			}
		});
	}

	private static void exceptionMessage() {
		MessageBox.build()
				.setMessage("Something went wrong, but I think we will survive. You should go play outside, get some sun and stuff. Try later.")
				.oneButtonsLayout("LOL ok :p")
				.setCallback(null)
				.show();
	}

	private static HttpRequest getPost(String url, Map<String, String> data) {
		HttpRequest httpPost = new HttpRequest(HttpMethods.POST);
		httpPost.setUrl(SERVER_URL + url);
		httpPost.setContent(HttpParametersUtils.convertHttpParameters(data));
		return httpPost;
	}

	private static HttpRequest getGet(String url) {
		HttpRequest httpGet = new HttpRequest(HttpMethods.GET);
		httpGet.setUrl(SERVER_URL + url);
		return httpGet;
	}

	public static JsonValue parseJson(String response) {
		System.out.println("Parseado-> " + response);
		return new JsonReader().parse(response);
	}

	public static JsonValue ProcessResponce(HttpResponse response) {
		String res = response.getResultAsString();
		return parseJson(res);
	}
}
