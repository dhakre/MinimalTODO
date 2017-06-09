package dhakre.fr.uha.minimaltodo.payment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.sumup.merchant.Models.TransactionInfo;
import com.sumup.merchant.api.SumUpAPI;
import com.sumup.merchant.api.SumUpLogin;
import com.sumup.merchant.api.SumUpPayment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.UUID;

import dhakre.fr.uha.minimaltodo.R;

public class TransactionActivity extends Activity {
    //calculate time app is used
    long start = Calendar.getInstance().getTimeInMillis();

    private static final int REQUEST_CODE_LOGIN = 1;
    private static final int REQUEST_CODE_PAYMENT = 2;
    private static final int REQUEST_CODE_PAYMENT_SETTINGS = 3;

    private TextView mResultCode;
    private TextView mResultMessage;
    private TextView mTxCode;
    private TextView mReceiptSent;
    private TextView mTxInfo;
    private Button btn_receiptData;


    String txCode;//transaction code
    String Mcode="ME9NH3ET";
    String respond;

    private OkHttpClient okHttpClient;
    private Request request;
    private String url;//rest api url
    public JSONArray jsonArray;
    public String josnstr;
    JSONObject json;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);


        findViews();

        Button login = (Button) findViewById(R.id.button_login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SumUpLogin sumUplogin = SumUpLogin.builder("a5a755cd-aec9-4609-ae71-cca9575cce3f").build();
                SumUpAPI.openLoginActivity(TransactionActivity.this, sumUplogin, REQUEST_CODE_LOGIN);
            }
        });

        Button btnCharge = (Button) findViewById(R.id.button_charge);
        btnCharge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SumUpPayment payment = SumUpPayment.builder()
                        // mandatory parameters
                        // Please go to https://me.sumup.com/developers to get your Affiliate Key by entering the application ID of your app. (e.g. com.sumup.sdksampleapp)
                        .affiliateKey("a5a755cd-aec9-4609-ae71-cca9575cce3f")
                        .productAmount(1.12)
                        .currency(SumUpPayment.Currency.EUR)
                        // optional: add details
                        .productTitle("Taxi Ride")
                        .receiptEmail("customer@mail.com")
                        .receiptSMS("+33769009780")
                        // optional: Add metadata
                        .addAdditionalInfo("AccountId", "taxi0334")
                        .addAdditionalInfo("From", "Paris")
                        .addAdditionalInfo("To", "Berlin")
                        // optional: foreign transaction ID, must be unique!
                        .foreignTransactionId(UUID.randomUUID().toString()) // can not exceed 128 chars
                        .build();

                SumUpAPI.openPaymentActivity(TransactionActivity.this, payment, REQUEST_CODE_PAYMENT);
            }
        });

      // Toast.makeText(getApplicationContext(), josnstr, Toast.LENGTH_LONG).show();
        Button paymentSettings = (Button) findViewById(R.id.button_payment_settings);
        paymentSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SumUpAPI.openPaymentSettingsActivity(TransactionActivity.this, REQUEST_CODE_PAYMENT_SETTINGS);
            }
        });

        Button prepareCardTerminal = (Button) findViewById(R.id.button_prepare_card_terminal);
        prepareCardTerminal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SumUpAPI.prepareForCheckout();
            }
        });


        Button btnLogout = (Button) findViewById(R.id.button_logout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SumUpAPI.logout();

            }
        });
        btn_receiptData= (Button) findViewById(R.id.btn_receiptData);
        btn_receiptData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(TransactionActivity.this,ReceiptData.class);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        resetViews();

        switch (requestCode) {
            case REQUEST_CODE_LOGIN:
                if (data != null) {
                    Bundle extra = data.getExtras();
                    mResultCode.setText("Result code: " + extra.getInt(SumUpAPI.Response.RESULT_CODE));
                    mResultMessage.setText("Message: " + extra.getString(SumUpAPI.Response.MESSAGE));
                }
                break;

            case REQUEST_CODE_PAYMENT:
                if (data != null) {
                    Bundle extra = data.getExtras();

                    mResultCode.setText("Result code: " + extra.getInt(SumUpAPI.Response.RESULT_CODE));
                    mResultMessage.setText("Message: " + extra.getString(SumUpAPI.Response.MESSAGE));

                    String txCode = extra.getString(SumUpAPI.Response.TX_CODE);
                    mTxCode.setText(txCode == null ? "" : "Transaction Code: " + txCode);
                    //get http url
                    url="https://receipts-ng.sumup.com/v0.1/receipts/"+txCode+"?mid=ME9NH3ET";

                    boolean receiptSent = extra.getBoolean(SumUpAPI.Response.RECEIPT_SENT);
                    mReceiptSent.setText("Receipt sent: " + receiptSent);

                    TransactionInfo transactionInfo = extra.getParcelable(SumUpAPI.Response.TX_INFO);
                    mTxInfo.setText(transactionInfo == null ? "" : "Transaction Info : " + transactionInfo);
                   // String Mcode=transactionInfo.getPaymentType();

                    //Toast.makeText(getApplicationContext(), "payment type code not found"+transactionInfo.getMerchantCode(), Toast.LENGTH_LONG).show();
                    //when the recipt is send we will  call the sumup rest api and request the  receipt data to be shon on the android device
                    if(receiptSent==true)
                    {
                        //using okkHttp
                        okHttpClient=new OkHttpClient();
                        //initionalise request
                        request= new Request.Builder().url(url).build();
                        //execute the request
                        okHttpClient.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(Request request, IOException e) {
                                Toast.makeText(getApplicationContext(), "On Request failure", Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onResponse(Response response) throws IOException {
                                 respond=response.body().string();
                                Handler h = new Handler(Looper.getMainLooper());
                                h.post(new Runnable() {
                                    public void run() {
                                        //Toast.makeText(getApplicationContext(), respond, Toast.LENGTH_LONG).show();
                                        josnstr=respond;
                                        Intent intent=new Intent(TransactionActivity.this,ReceiptData.class);
                                        intent.putExtra("json",josnstr);
                                        startActivity(intent);

                                    }
                                });


                            }
                        });


                    }
                }
                break;

            case REQUEST_CODE_PAYMENT_SETTINGS:
                if (data != null) {
                    Bundle extra = data.getExtras();
                    mResultCode.setText("Result code: " + extra.getInt(SumUpAPI.Response.RESULT_CODE));
                    mResultMessage.setText("Message: " + extra.getString(SumUpAPI.Response.MESSAGE));
                }
                break;

            default:
                break;
        }
    }

    private void resetViews() {
        mResultCode.setText("");
        mResultMessage.setText("");
        mTxCode.setText("");
        mReceiptSent.setText("");
        mTxInfo.setText("");
    }

    private void findViews() {
        mResultCode = (TextView) findViewById(R.id.result);
        mResultMessage = (TextView) findViewById(R.id.result_msg);
        mTxCode = (TextView) findViewById(R.id.tx_code);
        mReceiptSent = (TextView) findViewById(R.id.receipt_sent);
        mTxInfo = (TextView) findViewById(R.id.tx_info);
    }

}
