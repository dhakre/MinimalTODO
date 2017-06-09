package dhakre.fr.uha.minimaltodo.payment;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

//import dhakre.fr.uha.minimaltodo.R;

import static android.R.attr.country;
import static com.adyen.adyenpos.DAO.util.ColumnNames.R;

public class ReceiptData extends AppCompatActivity {
    String jsonString;
    long date;
    String transaction_code,amount,last_4_digits,type,cardholder_name,entry_mode,status,timestamp,business_name,currency,lat,lon,city,country;
    TextView tx_amount,tx_business_name,tx_timestamp,tx_status,tx_trans_code,tx_entry_mode,tx_city,tx_country;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt_data);

        tx_amount= (TextView) findViewById(R.id.tx_amount);
        tx_business_name= (TextView) findViewById(R.id.tx_business_name);
        tx_trans_code= (TextView) findViewById(R.id.tx_trans_code);
        tx_timestamp= (TextView) findViewById(R.id.tx_timestamp);
        tx_entry_mode= (TextView) findViewById(R.id.tx_entry_mode);
        tx_status= (TextView) findViewById(R.id.tx_status);


        Intent intent = this.getIntent();
        if (intent != null)
            jsonString = intent.getStringExtra("json");
        //Toast.makeText(getApplicationContext(),jsonString,Toast.LENGTH_LONG).show();}

        if (jsonString == null)
            Toast.makeText(getApplicationContext(), "json has null value", Toast.LENGTH_LONG).show();
        else {
            JSONObject json = null;
            try {
                json = new JSONObject(jsonString);
                JSONObject json2 = json.getJSONObject("transaction_data");
                transaction_code = json2.getString("transaction_code");
                tx_trans_code.setText("Transction Code : "+transaction_code);
                amount=json2.getString("amount");
                status=json2.getString("status");
                tx_status.setText("Status : "+status);
                entry_mode=json2.getString("entry_mode");
               tx_entry_mode.setText("Entry Mode :"+entry_mode);

                timestamp=json2.getString("timestamp");
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                Date date = format.parse(timestamp);
                //timestamp=timestamp.substring(0,14);
                tx_timestamp.setText("Timestamp : "+timestamp);

                currency=json2.getString("currency");
                tx_amount.setText("You Just Paid : "+amount+" "+currency);

                JSONObject json3=json2.getJSONObject("card");
                type=json3.getString("type");
                cardholder_name=json3.getString("cardholder_name");
               // last_4_digits=json3.getString("last_4_digits");

                JSONObject json4=json.getJSONObject("merchant_data");
                JSONObject json5=json4.getJSONObject("merchant_profile");
                business_name=json5.getString("business_name");
                tx_business_name.setText(business_name);

                JSONObject json6=json.getJSONObject("location");
                lat=json6.getString("lat");
                lon=json6.getString("lon");
               // Toast.makeText(getApplicationContext(),"lat="+lat+"lon"+lon, Toast.LENGTH_LONG).show();
                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

                double latitude=Double.parseDouble(lat);
                double longitude=Double.parseDouble(lon);
                List<Address> addresses  = null;
                try {
                    addresses = geocoder.getFromLocation(latitude,longitude, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }


                String state = addresses.get(0).getAdminArea();
                String zip = addresses.get(0).getPostalCode();
                country = addresses.get(0).getCountryName();
                tx_city.setText("City : "+city+","+zip);
                tx_country.setText("Country : "+country);

               // Toast.makeText(getApplicationContext(),business_name,Toast.LENGTH_LONG).show();
            } catch (JSONException e1) {
                e1.printStackTrace();
            } catch (ParseException e1) {
                e1.printStackTrace();
            }


        }
    }
    private String stringToDate(String aDate, String aFormat) {

        if(aDate==null) return null;
        SimpleDateFormat dateformat = new SimpleDateFormat(aFormat);
        Date date = new Date();
        String datetime = dateformat.format(date);
        return datetime;

    }
}
