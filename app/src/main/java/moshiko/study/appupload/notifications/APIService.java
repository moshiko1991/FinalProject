package moshiko.study.appupload.notifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAA1IaS_oM:APA91bFBquQ1UCIbsX5wfY-cptVDpDXlaQ3lplnx3_SA_gNIE9as2P8aY6TO-UHgiSF-tzQDhwdg3tlW4u0l2lMyKzSUUcLmZgLrcNzEM6N3HZOdpMXj_UH6CObRCVNbsYG_K5GlY8iB"
    })
    @POST("fcm/send")
    Call<Response> sendNotification(@Body Sender body);
}
