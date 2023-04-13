package my.ilpsdk.mywaktusolattv;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import my.ilpsdk.mywaktusolattv.databinding.FragmentFirstBinding;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FirstFragment extends Fragment {

private FragmentFirstBinding binding;
private OkHttpClient client;
private  MediaType JSON;
private EditText editText;
private SharedPreferences config;
private Context context;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        context = container.getContext();
      binding = FragmentFirstBinding.inflate(inflater, container, false);
      return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        config = getActivity().getSharedPreferences(Constant.KEY_CONFIG, 0);
        binding.buttonFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editText = getActivity().findViewById(R.id.et_masjid_id);
                String masjid_id = editText.getText().toString();
                if(masjid_id.length() >= 5 && masjid_id.length() <= 10){
                    post(masjid_id);
                }
                else {
                    Toast.makeText(context, "Wrong Format", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    public void post(String masjid_id) {
        RequestBody requestBody = new FormBody.Builder()
                .add("masjid_id", masjid_id)
                .build();
        Request request = new Request.Builder().url(Constant.SERVER_URL + "check-id").post(requestBody).build();
        client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String jsonData = response.body().string();
                JSONObject Jobject = null;
                try {
                    Jobject = new JSONObject(jsonData);
                    Log.d("test", "onResponse: " + Jobject.getBoolean("isExists"));
                    if(Jobject.getBoolean("isExists")){
                        config = getActivity().getSharedPreferences(Constant.KEY_CONFIG, 0);

                        SharedPreferences.Editor editor = config.edit();
                        editor.putString(Constant.KEY_MASJId_ID,masjid_id);
                        editor.apply();
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, "ID found", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getActivity(), FullscreenActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                getActivity().finish(); // if the activity running has it's own context
                            }
                        });
                    }
                    else {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, "ID Not Found", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }


            }
        });
    }

@Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}