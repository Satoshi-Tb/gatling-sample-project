package example;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;


public class RestApiSimulation extends Simulation {
	
	private static final String baseUrl = System.getenv().getOrDefault("BASE_URL", "http://localhost:8080");
	
	// ユーザー情報更新用のパラメータ
	private static final String UPDATE_USER_BODY = """
		{
			"id": "system1@co.jp",
			"userId": "system1@co.jp",
			"userName": "システムアカウント　太郎",
			"password": "password",
			"birthday": "2001-01-01",
			"age": 21,
			"gender": 1,
			"profile": "プロファイルです",
			"departmentId": 1
		}
		""";
	
    // フィーダー：複数の userId を使ってテスト
	private FeederBuilder<String> userIdFeeder = csv("user_ids.csv").circular();  // データが末端に達した場合、最初から読込直す
	
	
    // HTTP設定
    private HttpProtocolBuilder httpProtocol = http
		.baseUrl(baseUrl)  // REST APIのベースURL
		.acceptHeader("application/json")
		.contentTypeHeader("application/json")
		.userAgentHeader("Gatling/Java Test");

    // シナリオ定義
    private ScenarioBuilder scn = scenario("REST API Test Scenario")
    	// 1回のシナリオ実行ごとに user_ids.csv から1行が供給され、その行の内容（この場合は userId）が全てのリクエストで有効なコンテキストとなります
    	.feed(userIdFeeder)
		// リクエスト例
		.exec(http("ユーザー詳細取得")
			.get("/api/user/detail/#{userId}")
			.check(status().is(200))
			.check(jsonPath("$.code").is("0000")) // 基本的な検証: codeが"0000"
		)
		.pause(1) // 1秒のポーズ
		.exec(http("一覧取得（ページャー）")
			.get("/api/user/get/list-pager")
			.queryParam("page", 0)
			.queryParam("size", 5)
			.check(status().is(200))
			.check(jsonPath("$.code").is("0000"))
		)
		.pause(1)
		.exec(http("ユーザー情報更新")
			.put("/api/user/update")
			.body(StringBody(UPDATE_USER_BODY))
			.check(status().is(200))
			.check(jsonPath("$.code").is("0000"))
		);
    // シミュレーション設定
    {
		setUp(
		    scn.injectOpen( // 到着レートベース
				atOnceUsers(5) // 一度に5ユーザーを同時投入、1回だけシナリオ実施。各仮想ユーザーごとに feed から1行ずつデータが消費（consume）されます。
		    )
//			scn.injectClosed( // 同時実行ユーザー数ベース
//				constantConcurrentUsers(5).during(Duration.ofSeconds(20)) // 20秒間、常に5人の仮想ユーザーを同時に実行し続ける
//			)
        ).protocols(httpProtocol);
    }
}
