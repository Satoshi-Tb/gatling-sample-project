package example;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import java.time.Duration;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;


public class RestApiSimulation extends Simulation {
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
	
    // HTTP設定
    private HttpProtocolBuilder httpProtocol = http
		.baseUrl("http://localhost:8080")  // REST APIのベースURL
		.acceptHeader("application/json")
		.contentTypeHeader("application/json")
		.userAgentHeader("Gatling/Java Test");

    // シナリオ定義
    private ScenarioBuilder scn = scenario("REST API Test Scenario")
		// リクエスト例
		.exec(http("ユーザー詳細取得")
			.get("/api/user/detail/system1@co.jp")
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
//            scn.injectOpen( // 到着レートベース
//                constantUsersPerSec(5).during(Duration.ofMinutes(1))  // 1秒あたり5ユーザーを2分間維持
//            ),
			scn.injectClosed( // 同時実行ユーザー数ベース
				constantConcurrentUsers(2).during(Duration.ofSeconds(30)) // 常に2人のユーザーを30秒維持
			)
        ).protocols(httpProtocol);
    }
}
