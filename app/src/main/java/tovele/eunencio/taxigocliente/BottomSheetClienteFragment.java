package tovele.eunencio.taxigocliente;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tovele.eunencio.taxigocliente.Common.Common;
import tovele.eunencio.taxigocliente.Remote.IGoogleAPI;

/**
 * Created by Eunencio Tovele on 8/31/2018.
 */

public class BottomSheetClienteFragment extends BottomSheetDialogFragment {

    String mLocation, mDestination;

    boolean isTapOnMap;

    IGoogleAPI mService;

    TextView txtCalculate, txtLocation, txtDestinition;

    public static BottomSheetClienteFragment newInstance (String location, String destination, boolean isTapOnMap){

        BottomSheetClienteFragment f = new BottomSheetClienteFragment();
        Bundle args = new Bundle();
        args.putString("location",location);
        args.putString("destination",destination);
        args.putBoolean("isTapOnMap",isTapOnMap);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLocation = getArguments().getString("location");
        mDestination = getArguments().getString("destination");
        isTapOnMap = getArguments().getBoolean("isTapOnMap");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.botton_sheet_cliente, container,false);

        txtLocation = (TextView) view.findViewById(R.id.txtLocation);
        txtDestinition = (TextView) view.findViewById(R.id.txtDestinition);
        txtCalculate = (TextView) view.findViewById(R.id.txtCalculate);

        mService = Common.getGoogleService();
        getPrice(mLocation, mDestination);

        //set Data
        if(!isTapOnMap)
        {
            txtLocation.setText(mLocation);
            txtDestinition.setText(mDestination);
        }

        return view;
    }

    private void getPrice(String mLocation, String mDestination) {
        String requestUrl = null;
        try{
            requestUrl = "https://maps.googleapis.com/maps/api/directions/json?"+
                    "mode=driving&"
                    +"transit_routing_preference=less_driving&"
                    +"oringin="+mLocation+"&"
                    +"destination="+mDestination+"&"
                    +"key="+getResources().getString(R.string.google_browser_key);
            Log.e("LINK", requestUrl);
            mService.getPath(requestUrl).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().toString());
                        JSONArray routes = jsonObject.getJSONArray("routes");

                        JSONObject object = routes.getJSONObject(0);
                        JSONArray legs = object.getJSONArray("legs");

                        JSONObject legsObject = legs.getJSONObject(0);

                        //set distance
                        JSONObject distance = legsObject.getJSONObject("distance");
                        String distance_text = distance.getString("text");

                        //use regex to extract double from string
                        Double distance_value = Double.parseDouble(distance_text.replaceAll("[^0=9\\\\.]", ""));

                        //Get Time
                        JSONObject time = legsObject.getJSONObject("duration");
                        String time_text = time.getString("text");
                        Integer time_value = Integer.parseInt(time_text.replaceAll("\\D+", ""));

                        String final_calculate = String.format("%s + %s = $%.2f", distance_text, time_text,
                                                Common.getPrice(distance_value, time_value));

                        txtCalculate.setText(final_calculate);

                        if(!isTapOnMap)
                        {
                            String start_address = legsObject.getString("strat_address");
                            String end_address = legsObject.getString("end_address");

                            txtLocation.setText(start_address);
                            txtDestinition.setText(end_address);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Log.e("ERROR", t.getMessage());

                }
            });

        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
