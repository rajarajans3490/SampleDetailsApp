package com.android.test.sampledetailsapp;


import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** A class to parse json data */
public class JsonDataParser {
    private String TAG = "JsonDataParser";

    public List<HashMap<String,Object>> parse(JSONObject jObject){
        JSONArray mRows = null;
        try {
            mRows = jObject.getJSONArray("rows");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return getRows(mRows);
    }

    private List<HashMap<String, Object>> getRows(JSONArray mRows){
        int mRowsCount = mRows.length();
        List<HashMap<String, Object>> mRowsList = new ArrayList<HashMap<String,Object>>();
        HashMap<String, Object> mRow = null;

        for(int i=0; i<mRowsCount;i++){
            try {
                mRow = getRow((JSONObject) mRows.get(i));
               if(!(mRow.get("title").equals("")))
                   mRowsList.add(mRow);
               else if(!(mRow.get("description").equals("")))
                   mRowsList.add(mRow);
               else if(!(mRow.get("imageHref").equals("")))
                   mRowsList.add(mRow);

            } catch (JSONException e) {
                Log.e(TAG, " JSONException " + e.toString());
                e.printStackTrace();
            }
        }

        return mRowsList;
    }

    private HashMap<String, Object> getRow(JSONObject mRowobj){

        HashMap<String, Object> row = new HashMap<String, Object>();
        String mTitle = "";
        String mDesc="";
        String mImageHref = "";

        try {
            mTitle = mRowobj.getString("title");
            mDesc = mRowobj.getString("description");
            mImageHref = mRowobj.getString("imageHref");

            if(mTitle != "null")
            row.put("title", mTitle);
            else
            row.put("title", "");
            if(mDesc != "null")
            row.put("description", mDesc);
            else
            row.put("description", "");
            if(mImageHref != "null") {
                row.put("imageHref", mImageHref);
                row.put("imageview", R.drawable.empty);
            }else{
                row.put("imageHref", "");
           }

        } catch (JSONException e) {
            Log.e(TAG, " JSONException " + e.toString());
            e.printStackTrace();
        }
        return row;
    }

    public String getTitle(JSONObject mJObj){
        String mAcTitle="";
        try{
            mAcTitle = mJObj.getString("title");

        }catch (JSONException e){
             e.printStackTrace();
        }
        return mAcTitle;
    }
}
