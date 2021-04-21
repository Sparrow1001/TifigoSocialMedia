package com.example.tifigosocialmedia.Notifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAACs6DvHk:APA91bGUV_9BAaQ1nWEsd7oSHBF4yVY4ujFW_iZkFUwxfwqrdCNBMgXlFC9yvGNbafQvnf0tP-CSlHjnZgQPT3TqfggVjhPius1i-D-wbnsPC1o42HwhstHtUgFXrlkTigVeZfor2J9c"
            }
    )

    @POST("fcm/send")
    Call<Response> sendNotification(@Body Sender body);
}
