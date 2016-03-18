package com.android.test.sampledetailsapp;


import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;;
import android.widget.Toast;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class ListViewActivity extends AppCompatActivity {
    private String TAG ="ListViewActivity";
    private AlertDialog.Builder mAlertDialogbuild= null;
    private Context mContext = null;
    private String mServerURL = null;
    private ListView mListView = null;
    private SwipeRefreshLayout refreshLayout;
    private AsyncTask mAsyncTask ;
    private AsyncTask mListViewUpdate;
    private AsyncTask mImageUpdate;
    private ActionBar actionBar;


    public ListViewActivity(){
        mContext = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        actionBar = getSupportActionBar();
        if(actionBar!=null)
            actionBar.setTitle("");

        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mListView = (ListView)findViewById(R.id.listview);
        mServerURL = "https://dl.dropboxusercontent.com/u/746330/facts.json";
        ConnectivityManager mConncMgr =  (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = mConncMgr.getActiveNetworkInfo();
            if (mNetworkInfo == null || (!mNetworkInfo.isConnected()))
            {
                mAlertDialogbuild = new AlertDialog.Builder(mContext);
                mAlertDialogbuild.setTitle(getResources().getString(R.string.app_name));
                mAlertDialogbuild.setMessage(getResources().getString(R.string.nointernet));
                mAlertDialogbuild.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ListViewActivity.this.finish();
                    }
                });
                AlertDialog mAlertDialog = mAlertDialogbuild.create();
                mAlertDialog.show();
            } else {
                    AsyncOperation mAsyncTask = new AsyncOperation();
                    mAsyncTask.execute(mServerURL);
            }
        refreshLayout.setOnRefreshListener(onRefreshListener());

        //set animation colors
        refreshLayout.setColorSchemeResources(R.color.orange, R.color.green, R.color.blue);
    }

    private void getJSONFromURL(String Url) {
            mAsyncTask = new AsyncOperation().execute(Url);
        }

    private SwipeRefreshLayout.OnRefreshListener onRefreshListener() {
        return new SwipeRefreshLayout.OnRefreshListener() {


            @Override
            public void onRefresh() {

                if(mAsyncTask!=null && mAsyncTask.getStatus().equals(AsyncTask.Status.RUNNING)) {
                    mAsyncTask.cancel(true);
                }
                if(mListViewUpdate!=null && mListViewUpdate.getStatus().equals(AsyncTask.Status.RUNNING)) {
                    mListViewUpdate.cancel(true);
                }
                if(mImageUpdate!=null && mImageUpdate.getStatus().equals(AsyncTask.Status.RUNNING)) {
                    mImageUpdate.cancel(true);
                }

                    getJSONFromURL(mServerURL);
                    mListView.setVisibility(View.GONE);

            }
        };
    }

    // Async Task operation to get the Data from Server
    private class AsyncOperation extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String data = "";
            InputStream mInputStream = null;
            HttpURLConnection mHttpURLConnection = null;

            try {
                URL url = new URL(params[0]);
                mHttpURLConnection = (HttpURLConnection) url.openConnection();
                mHttpURLConnection.connect();
                int responseCode = mHttpURLConnection.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    mInputStream = mHttpURLConnection.getInputStream();

                    BufferedReader br = new BufferedReader(new InputStreamReader(mInputStream));
                    StringBuffer sb = new StringBuffer();
                    String line = "";
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                    data = sb.toString();
                    br.close();
                } else {
                    data = "";
                }
            } catch (Exception e) {
                Log.e(TAG,"Exception while downloading url" +  e.toString());
            } finally {
                mHttpURLConnection.disconnect();
                try {
                    if(mInputStream != null)
                        mInputStream.close();
                } catch (IOException e) {
                    Log.e(TAG,"Exception closing inputstream" +  e.toString());
                }
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            if (refreshLayout.isRefreshing()) {
                refreshLayout.setRefreshing(false);
            }

            if(result != null) {
                mListView.setVisibility(View.VISIBLE);
                ListViewUpdate mListViewUpdate = new ListViewUpdate();
                mListViewUpdate.execute(result);
            } else {
                Toast.makeText(mContext, R.string.nodata, Toast.LENGTH_SHORT).show();
            }
        }
    }


    /** AsyncTask to parse json data and load ListView */
    private class ListViewUpdate extends AsyncTask<String, Void, CustomListViewAdapter>{

        JSONObject jObject;
        CustomListViewAdapter mAdapter = null;
        String mActionbarTitle = "";
        @Override
        protected CustomListViewAdapter doInBackground(String... JsonString) {
            try{
                jObject = new JSONObject(JsonString[0]);
                JsonDataParser mJsonParser = new JsonDataParser();
                mJsonParser.parse(jObject);
            }catch(Exception e){
                Log.d("JSON Exception1",e.toString());
            }

            // Instantiating json parser class
            JsonDataParser mJsonParser = new JsonDataParser();

            List<HashMap<String, Object>> rows = null;

            try{
                // Getting the parsed data as a List construct
                rows = mJsonParser.parse(jObject);
                mActionbarTitle= mJsonParser.getTitle(jObject);
            }catch(Exception e){
                Log.e(TAG,"Exception rows " + e.toString());
            }
            String[] from = { "title","description","imageview"};
            int[] to = { R.id.title,R.id.description,R.id.image};
            if(rows !=null && rows.size() > 0) {
                mAdapter = new CustomListViewAdapter(getBaseContext(), rows, R.layout.list_row, from, to);
            }
            return mAdapter;
        }

        @Override
        protected void onPostExecute(CustomListViewAdapter adapter) {
            if(actionBar!=null)
                actionBar.setTitle(mActionbarTitle);
            if(adapter !=null){
                mListView.setAdapter(adapter);

            for(int i=0;i<adapter.getCount();i++) {
                HashMap<String, Object> hm = (HashMap<String, Object>) adapter.getItem(i);
                String mImageUrl = (String) hm.get("imageHref");
                ImageUpdate mImageUpdate = new ImageUpdate();
                HashMap<String, Object> hmDownload = new HashMap<String, Object>();
                hm.put("imageHref", mImageUrl);
                hm.put("position", i);
                mImageUpdate.execute(hm);
                }
            }
        }

    }

    private class ImageUpdate extends AsyncTask<HashMap<String, Object>, Void, HashMap<String, Object>>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected HashMap<String, Object> doInBackground(HashMap<String, Object>... hm) {

            InputStream mInputStream = null;
            String mImageUrl = "";
            HttpURLConnection mUrlConnection = null;
            mImageUrl = (String) hm[0].get("imageHref");
            int position = (Integer) hm[0].get("position");
            URL url;
            try {
                url = new URL(mImageUrl);
                mUrlConnection = (HttpURLConnection) url.openConnection();
                mUrlConnection.setReadTimeout(18000);
                mUrlConnection.setConnectTimeout(18000);
                mUrlConnection.setInstanceFollowRedirects(true);
                mUrlConnection.connect();
                    mInputStream = mUrlConnection.getInputStream();
                    if (mInputStream != null) {
                        File cacheDirectory = getBaseContext().getCacheDir();
                        File tmpFile = new File(cacheDirectory.getPath() + "/image_" + position + ".png");
                        FileOutputStream fOutStream = new FileOutputStream(tmpFile);
                        Bitmap mBitmap = BitmapFactory.decodeStream(mInputStream);
                        if (mBitmap != null)
                            mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOutStream);
                        fOutStream.flush();
                        fOutStream.close();
                        HashMap<String, Object> hmBitmap = new HashMap<String, Object>();
                        hmBitmap.put("imageview", tmpFile.getPath());
                        hmBitmap.put("position", position);
                        return hmBitmap;
                    }
                }catch(MalformedURLException e){
                    Log.e(TAG, "Exception MalformedURLException " + e.toString());
                    e.printStackTrace();
                }catch(Exception ex){
                    Log.e(TAG, "Exception bitmap" + ex.toString());
                    ex.printStackTrace();
                }finally{
                    if (mUrlConnection != null)
                        mUrlConnection.disconnect();
                    if (mInputStream != null)
                        try {
                            mInputStream.close();
                        } catch (IOException e) {
                            Log.e(TAG, "Exception while closing InputStream " + e.toString());
                            e.printStackTrace();
                        }
                }
                return null;
            }



        @Override
        protected void onPostExecute(HashMap<String, Object> result) {
            if(result != null) {
                String path = (String) result.get("imageview");
                int position = (Integer) result.get("position");
                CustomListViewAdapter adapter = (CustomListViewAdapter) mListView.getAdapter();
                HashMap<String, Object> hm = (HashMap<String, Object>) adapter.getItem(position);
                hm.put("imageview", path);
                adapter.notifyDataSetChanged();
            }
        }
    }


}
