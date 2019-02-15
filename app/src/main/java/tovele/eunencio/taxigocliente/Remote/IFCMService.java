package tovele.eunencio.taxigocliente.Remote;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import tovele.eunencio.taxigocliente.Model.DataMessage;
import tovele.eunencio.taxigocliente.Model.FCMResponse;

/**
 * Created by Eunencio Tovele on 9/3/2018.
 */

public interface IFCMService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:Key=AAAAmJR5o7o:APA91bFJEBnQvmxj1lUiEf8WU4zHPJFydtq1VVyGvAvS4johWmn58b0yPIF5WmhW4vsU3lCh7e3kVk2qzdHV_FrPNhepC96dtrItjED89o6L2kPKXP98rRvWHLtngtOWuzsdI7df_hxs9CBxSwd31cR0YPm4_1kZBQ"
    })
    @POST("from/send")
    Call<FCMResponse> sendMessage (@Body DataMessage body);
}
