package com.tiomamaster.customizableconverter.data;

import android.content.Context;
import android.util.Xml;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.Volley;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Loading currency from the http://www.cbr.ru.
 */

final class CurrencyLoader {

    private Context mContext;

    private String mUrl;

    CurrencyLoader(Context context, String locale) {
        this.mContext = context;
        String lang = "";
        if (locale.equals("en")) lang = "_eng";
        mUrl = "http://www.cbr.ru/scripts/XML_daily" + lang + ".asp";
    }

    void getFreshCourses(Response.Listener<List<CurrencyConverter.CurrencyUnit>> responseListener,
                         Response.ErrorListener errListener) {

        RequestQueue queue = Volley.newRequestQueue(mContext.getApplicationContext());
        Request request = new CurrencyUnitsRequest(Request.Method.GET, mUrl, responseListener, errListener);
        queue.add(request);
    }

    private static class CurrencyUnitsRequest extends Request<List<CurrencyConverter.CurrencyUnit>> {

        private final Response.Listener<List<CurrencyConverter.CurrencyUnit>> mListener;

        CurrencyUnitsRequest(int method, String url,
                             Response.Listener<List<CurrencyConverter.CurrencyUnit>> listener,
                             Response.ErrorListener errorListener) {
            super(method, url, errorListener);
            mListener = listener;
        }

        @Override
        protected Response<List<CurrencyConverter.CurrencyUnit>> parseNetworkResponse(NetworkResponse response) {
            XmlPullParser xpp = Xml.newPullParser();
            List<CurrencyConverter.CurrencyUnit> units = new ArrayList<>(35);
            units.add(new CurrencyConverter.CurrencyUnit("Рубль", 1d, true, "RUB"));
            try {
                xpp.setInput(new ByteArrayInputStream(response.data), null);

                int event;
                String charCode = "";
                int nominal = 1;
                String name = "";
                double value;
                while ((event = xpp.next()) != XmlPullParser.END_DOCUMENT) {
                    if (event == XmlPullParser.START_TAG) {
                        switch (xpp.getName()) {
                            case "CharCode":
                                xpp.next();
                                charCode = xpp.getText();
                                break;
                            case "Nominal":
                                xpp.next();
                                nominal = Integer.parseInt(xpp.getText());
                                break;
                            case "Name":
                                xpp.next();
                                name = xpp.getText();
                                break;
                            case "Value":
                                xpp.next();
                                value = Double.parseDouble(xpp.getText().replace(",", "."));
                                units.add(new CurrencyConverter.CurrencyUnit(name, value / nominal, true, charCode));
                        }
                    }
                }
            } catch (XmlPullParserException e) {
                return Response.error(new ParseError(e));
            } catch (IOException e) {
                return Response.error(new ParseError(e));
            }

            return Response.success(units, HttpHeaderParser.parseCacheHeaders(response));
        }

        @Override
        protected void deliverResponse(List<CurrencyConverter.CurrencyUnit> response) {
            mListener.onResponse(response);
        }
    }
}