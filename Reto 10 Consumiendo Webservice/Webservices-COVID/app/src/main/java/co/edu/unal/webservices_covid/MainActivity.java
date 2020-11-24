package co.edu.unal.webservices_covid;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.slider.RangeSlider;
import com.google.android.material.slider.Slider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    private static final int CONNECTION_TIMEOUT = 60000;
    private static final int DATARETRIEVAL_TIMEOUT = 60000;
    private static final String[] meses = {"Enero", "Febrero",
            "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto",
            "Septiembre", "Octubre", "Noviembre", "Diciembre",};

    RangeSlider ageSlider;
    RangeSlider medesSlider;
    TextView totalResult;
    ProgressDialog pd;
    Button buscar;


    int conteo = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        totalResult = (TextView) findViewById(R.id.total);

        buscar = (Button) findViewById(R.id.buscar);
        buscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new JsonTask().execute();
            }
        });

        ageSlider = (RangeSlider) findViewById(R.id.slideredad);
        medesSlider = (RangeSlider) findViewById(R.id.slidermeses);
    }

    public String getEdadesString() {
        List<Float> edades = ageSlider.getValues();
        String edadq = "edad between '" + edades.get(0) + "' and '" + edades.get(1) + "'";
        System.out.println(edadq);
        return edadq;
    }

    public String getFechas() {
        //"1/4/2020 0:00:00"
        List<Float> meses = medesSlider.getValues();
        StringBuilder salida = new StringBuilder("fecha_reporte_web in(");
        for (int i = Math.round(meses.get(0)); i <= meses.get(1); i++) {
            for (int j = 1; j <= 31; j++) {
                salida.append("'").append(j).append("/").append(i).append("/2020 0:00:00'");
                salida.append(j != 31 ? "," : ")");
            }
        }
        return salida.toString();
    }

    public String getMes(int i){
        StringBuilder salida = new StringBuilder("fecha_reporte_web in(");
        for (int j = 1; j <= 31; j++) {
            salida.append("'").append(j).append("/").append(i).append("/2020 0:00:00'");
            salida.append(j != 31 ? "," : ")");
        }
        return salida.toString();
    }

    public String getStringCasos() {
        List<Float> mes = medesSlider.getValues();
        List<Float> edades = ageSlider.getValues();
        String salida = "Casos reportados ";
        if(Math.round( mes.get(0)) == Math.round(mes.get(1))){
            salida += "en " + meses[Math.round(mes.get(0)) - 1];
        } else {
            salida += " entre " + meses[Math.round(mes.get(0)) - 1] + " y " +
                    meses[Math.round(mes.get(1)) - 1];
        }
        salida += "\nde personas ";
        if(edades.get(0) == edades.get(1) ){
            salida += "de " + edades.get(0) + " años";
        } else {
            salida += "de " + edades.get(0) + " a " + edades.get(1) + " años";
        }
        salida += "\n" + conteo;
//        String salida = "Casos: " + conteo;
        System.out.println(salida);
        return salida;
    }

    public void getCountCases() {
        //https://www.datos.gov.co/resource/gt2j-8ykr.json?&$select=count(edad)&$where=fecha_reporte_web in('9/5/2020 0:00:00', '29/5/2020 0:00:00')
        String direccionBase = "https://www.datos.gov.co/resource/gt2j-8ykr.json?";
        String adiciones = "$select=count(recuperado)&$where=";
        String and = " AND ";
        String edades = getEdadesString();
        String fechas = getFechas();
        String elemento = "count_recuperado";

        System.out.println(direccionBase + adiciones + fechas + and + edades);

        JSONArray serviceResult = requestWebService(
                direccionBase + adiciones + fechas + and + edades);

        try {
            conteo = serviceResult.getJSONObject(0).getInt(elemento);
        } catch (JSONException e) {
            // handle exception
            System.out.println("FUCK");
            System.out.println(e.toString());
        }
    }

    public void getCountCasesLite(){
        //https://www.datos.gov.co/resource/gt2j-8ykr.json?&$select=count(edad)&$where=fecha_reporte_web in('9/5/2020 0:00:00', '29/5/2020 0:00:00')
        String direccionBase = "https://www.datos.gov.co/resource/gt2j-8ykr.json?";
        String adiciones = "$select=count(recuperado)&unidad_medida=1&$where=";
        String and = " AND ";
        String edades = getEdadesString();

        String elemento = "count_recuperado";

        conteo = 0;
        List<Float> meses = medesSlider.getValues();
        for (int i = Math.round(meses.get(0)); i <= meses.get(1); i++) {
            String fechas = getMes(i);

            System.out.println(direccionBase + adiciones + fechas + and + edades);

            JSONArray serviceResult = requestWebService(
                    direccionBase + adiciones + fechas + and + edades);

            try {
                conteo += serviceResult.getJSONObject(0).getInt(elemento);
            } catch (JSONException e) {
                // handle exception
                System.out.println("FUCK");
                System.out.println(e.toString());
            }
        }

    }

    //Funciones del tutorial

    public static JSONArray requestWebService(String serviceUrl) {
        disableConnectionReuseIfNecessary();

        HttpURLConnection urlConnection = null;
        try {
            // create connection
            URL urlToRequest = new URL(serviceUrl);
            urlConnection = (HttpURLConnection)
                    urlToRequest.openConnection();
            urlConnection.setConnectTimeout(CONNECTION_TIMEOUT);
            urlConnection.setReadTimeout(DATARETRIEVAL_TIMEOUT);

            // handle issues
            int statusCode = urlConnection.getResponseCode();
            if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                // handle unauthorized (if service requires user login)
                System.out.println("Error de autorización");
            } else if (statusCode != HttpURLConnection.HTTP_OK) {
                // handle any other errors, like 404, 500,..
                System.out.println("Error Miscelaneo");
            }

            // create JSON object from content
            InputStream in = new BufferedInputStream(
                    urlConnection.getInputStream());
            return new JSONArray(getResponseText(in));

        } catch (MalformedURLException e) {
            // URL is invalid
            System.out.println("GOD");
        } catch (SocketTimeoutException e) {
            // data retrieval or connection timed out
            System.out.println("FUCKING");
        } catch (IOException e) {
            // could not read response body
            // (could not create input stream)
            System.out.println("HELL");
        } catch (JSONException e) {
            // response body is no valid JSON string
            System.out.println(":-(");
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return null;
    }

    /**
     * required in order to prevent issues in earlier Android version.
     */
    private static void disableConnectionReuseIfNecessary() {
        // see HttpURLConnection API doc
        if (Integer.parseInt(Build.VERSION.SDK)
                < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false");
        }
    }

    private static String getResponseText(InputStream inStream) {
        // very nice trick from
        // http://weblogs.java.net/blog/pat/archive/2004/10/stupid_scanner_1.html
        return new Scanner(inStream).useDelimiter("\\A").next();
    }

    public List<String> findAllItems() {
        JSONArray serviceResult = requestWebService(
                "http://url/to/findAllService");

        List<String> foundItems = new ArrayList<String>(20);

        try {
            JSONArray items = serviceResult.getJSONArray(0);

            for (int i = 0; i < items.length(); i++) {
                JSONObject obj = items.getJSONObject(i);
                foundItems.add(obj.getString("id_de_caso"));
            }

        } catch (JSONException e) {
            // handle exception
        }

        return foundItems;
    }

    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(MainActivity.this);
            pd.setMessage("Please wait");
            pd.setCancelable(false);
            pd.show();
        }

        protected String doInBackground(String... params) {
            try {
                getCountCasesLite();
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (pd.isShowing()) {
                pd.dismiss();
            }
            totalResult.setText(getStringCasos());
            conteo = 0;
        }
    }
}

