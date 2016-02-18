package battery.zmx.car.com.zmxbattery.navigation;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.services.cloud.CloudItem;
import com.amap.api.services.cloud.CloudItemDetail;
import com.amap.api.services.cloud.CloudResult;
import com.amap.api.services.cloud.CloudSearch;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import battery.zmx.car.com.zmxbattery.R;
import battery.zmx.car.com.zmxbattery.map.LocationActivity;

public class ListActivity extends AppCompatActivity implements CloudSearch.OnCloudSearchListener{


    private AMapLocationClientOption mLocationOption;
    private AMapLocationClient mLocationClient;

    private AMapLocation mLocation;
    private CloudSearch mCloudSearch;
    private LatLonPoint centerPoint;
    private String mKeyWord = "充电桩";
    private String tableId = "56945224305a2a6e2836f2ed";
    private ArrayList<CloudItem> items = new ArrayList<CloudItem>();

    private ListView listView;
    private List<Map<String , Object>> data;

    private double Lat;
    private double Lot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("List", "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);

        AMapLocationListener mLocationListener = new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                Log.d("List","定位成功后回调");
                mLocation = aMapLocation;
                if (aMapLocation != null) {
                    if (aMapLocation.getErrorCode() == 0) {
                        //定位成功回调信息，设置相关消息
                        Log.i("List" , aMapLocation.toString());
                        searchByBound();

                    } else {
                        //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                        Log.e("List","location Error, ErrCode:"
                                + aMapLocation.getErrorCode() + ", errInfo:"
                                + aMapLocation.getErrorInfo());
                    }
                }
            }
        };

        mLocationClient = new AMapLocationClient(getApplicationContext());
        mLocationClient.setLocationListener(mLocationListener);

       init();


    }

    private void init() {
        setUpMap();

        mCloudSearch = new CloudSearch(this.getApplicationContext());
        mCloudSearch.setOnCloudSearchListener(this);

    }

    private void setUpMap() {
        Log.d("List", "setUpMap");
        mLocationOption = null;
        //初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);
        //设置是否只定位一次,默认为false
        mLocationOption.setOnceLocation(false);
        //设置是否强制刷新WIFI，默认为强制刷新
        mLocationOption.setWifiActiveScan(true);
        //设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.setMockEnable(false);
        //设置定位间隔,单位毫秒,默认为2000000ms
        mLocationOption.setInterval(2000000);
        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();
    }



    public void searchByBound() {
        Log.d("List", "searchByBound");
        items.clear();
        LatLonPoint centerPoint = new LatLonPoint(mLocation.getLatitude(),mLocation.getLongitude());
        Log.i("items" , centerPoint.toString());
        CloudSearch.SearchBound bound = new CloudSearch.SearchBound(centerPoint,500000000);
        try {
            CloudSearch.Query query = new CloudSearch.Query(tableId,mKeyWord, bound);
            mCloudSearch.searchCloudAsyn(query);
        } catch (AMapException e) {
            e.printStackTrace();
        }

    }

    //
    @Override
    public void onCloudSearched(CloudResult cloudResult, int i) {
        Log.d("List", "onCloudSearched");
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map;
        if(i == 0){
            if(cloudResult != null){
                Log.i("items" , cloudResult.toString());
                ArrayList<CloudItem> items = cloudResult.getClouds();
                Log.i("items", "ononCloudSearched" + String.valueOf(items.size()));
                dataList(items);
            }
        }else {
            Toast.makeText(ListActivity.this, "显示错误", Toast.LENGTH_SHORT).show();

        }

    }

    @Override
    public void onCloudItemDetailSearched(CloudItemDetail cloudItemDetail, int i) {

    }


    static class ViewHolder
    {
        public LinearLayout listLinear;
        public TextView title;
        public TextView snippet;
        public TextView distance;
        public Button bt1;
        public Button bt2;
    }

    public void dataList(ArrayList<CloudItem> items){
        ListView listView = (ListView) findViewById(R.id.listView);
        Log.i("items" ,"dataList  "+ String.valueOf(items.size()));

        data = getData(items);
        Log.d("dataList" , String.valueOf(data.size()));
        MyAdapter adapter = new MyAdapter(this);
        listView.setAdapter(adapter);
    }

    private List<Map<String, Object>> getData(ArrayList<CloudItem> items) {
        Log.d("List" , "getData");
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map;

        for(int i=0;i< items.size();i++)
        {
            CloudItem item = items.get(i);

            map = new HashMap<String, Object>();
            map.put("title", item.getTitle());
            map.put("snippet", item.getSnippet());
            map.put("distance" , item.getDistance()/1000.0f + "km ");
            map.put("lat" , item.getLatLonPoint().getLatitude());
            map.put("lot" , item.getLatLonPoint().getLongitude());
            list.add(map);
        }
        return list;
    }

    public class MyAdapter extends BaseAdapter{

        private LayoutInflater mInflater;
        private MyAdapter(Context context){
            this.mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            Log.d("List" , "getView");
            ViewHolder holder = null;
            //如果缓存convertView为空，则需要创建View
            if(convertView == null)
            {
                holder = new ViewHolder();
                //根据自定义的Item布局加载布局
                convertView = mInflater.inflate(R.layout.content_list_view, null);
                holder.title = (TextView)convertView.findViewById(R.id.title);
                holder.snippet = (TextView)convertView.findViewById(R.id.snippet);
                holder.distance = (TextView)convertView.findViewById(R.id.distance);
                holder.listLinear = (LinearLayout) convertView.findViewById(R.id.list_linear);
                holder.bt1 = (Button)convertView.findViewById(R.id.bt1);
                holder.bt2 = (Button)convertView.findViewById(R.id.bt2);
                //将设置好的布局保存到缓存中，并将其设置在Tag里，以便后面方便取出Tag
                convertView.setTag(holder);
            }else
            {
                holder = (ViewHolder)convertView.getTag();
            }
            holder.title.setText((String)data.get(position).get("title"));
            holder.snippet.setText((String) data.get(position).get("snippet"));
            holder.distance.setText((String) data.get(position).get("distance"));
            holder.listLinear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ListActivity.this, LocationActivity.class);
                    startActivityForResult(intent, 1);
                }
            });
            holder.bt1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Lat = (double) data.get(position).get("lat");
                    Lot = (double) data.get(position).get("lot");
                    Log.e("List" ,"定位" + String.valueOf(mLocation.getLatitude()));
                    Log.e("List", String.valueOf(mLocation.getLongitude()));

                    Log.e("List", "地点" + String.valueOf(Lat));
                    Log.e("List", String.valueOf(Lot));
                    Intent intent = new Intent("android.intent.action.VIEW",
                            android.net.Uri.parse("androidamap://navi?sourceApplication=appname&lat=" + Lat +"&lon=" + Lot +"&dev=1&style=2"));
                    intent.setPackage("com.autonavi.minimap");
                    startActivity(intent);
                }
            });
            holder.bt2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

            return convertView;
        }
    }

}
