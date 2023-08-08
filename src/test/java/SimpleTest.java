import static io.restassured.RestAssured.given;
import static java.util.Objects.isNull;
import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import qa.TikerData;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SimpleTest {
    @Test
    void simpleTest() {
        List<TikerData> tikerDataList= given()
                .contentType(ContentType.JSON)
                .when()
                .get("https://api.kucoin.com/api/v1/market/allTickers")
                .then().log().body()
                .extract().jsonPath().getList("data.ticker", TikerData.class);
        assertThat(tikerDataList).isNotNull();

        List<TikerData> tikerDataList2 = tikerDataList.stream()
                .filter(tikerData -> Float.parseFloat(tikerData.getSell())>0.1).collect(Collectors.toList());
        assertThat(tikerDataList2).isNotNull();
        List<Float> floatComparePriceList = tikerDataList.stream()
                .filter(tikerData -> tikerData.getSymbol().endsWith("USDT") && !isNull(tikerData.getChangePrice()))
                .map(tikerData -> Float.parseFloat(tikerData.getChangePrice()))
                .sorted(new Comparator<Float>() {
                    @Override
                    public int compare(Float o1, Float o2) {
                        return o2.compareTo(o1);
                    }
                })
                .collect(Collectors.toList());
        List<Float> stringComparePriceList = tikerDataList.stream()
                .filter(tikerData -> tikerData.getSymbol().endsWith("USDT") && !isNull(tikerData.getChangePrice()))
                .sorted(new Comparator<TikerData>() {
                    @Override
                    public int compare(TikerData o1, TikerData o2) {
                        return o1.getChangePrice().compareTo(o2.getChangePrice());
                    }
                })
                .map(tikerData -> Float.parseFloat(tikerData.getChangePrice()))
                .collect(Collectors.toList());
        assertThat(floatComparePriceList).isNotNull();
        assertThat(stringComparePriceList).isNotNull();
        assertThat(floatComparePriceList).isNotEqualTo(stringComparePriceList);
        int a=1;
    }

    @Test
    void testWithResponse () {
        Response response = given()
                .contentType(ContentType.JSON)
                .when()
                .get("https://api.kucoin.com/api/v1/markets");
        response.then().log().all();
        List<Object> responseList = response.jsonPath().getList("data");
        assertThat(responseList)
                .isNotNull()
                .hasSize(11)
                .contains("USDS", "ETF");
        int a=1;
    }

    @Test
    void testWithQueryParam () {
        Response response = given()
                .contentType(ContentType.JSON)
                .queryParam("symbol", "BTC-USDT")
                .when()
                .get("https://api.kucoin.com/api/v1/market/orderbook/level2_20");
        response.then().log().all();
        List<List<Object>> responseList = response.jsonPath().getList("data.bids");
        assertThat(responseList)
                .isNotNull()
                .hasSize(20);
        int a=1;
    }


    @Test
    void testWithPathParam () {
        Response response = given()
                .contentType(ContentType.JSON)
                .when()
                .pathParam("currency", "USDT")
                .get("https://api.kucoin.com/api/v2/currencies/{currency}");
        response.then().log().all();
        Map<String,String> responseMap = response.jsonPath().getMap("data.chains.find {it.chainName=='SOL' && it.confirms==200}");
        assertThat(responseMap)
                .isNotNull()
                .hasSizeGreaterThan(1);
//                .contains(Arrays.asList("18954.8", "0.00072642"));

        int a=1;
    }
}
