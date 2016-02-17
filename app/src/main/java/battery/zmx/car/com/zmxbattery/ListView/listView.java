package battery.zmx.car.com.zmxbattery.ListView;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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

public class listView extends AppCompatActivity implements CloudSearch.OnCloudSearchListener{


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
        CloudSearch.SearchBound bound = new CloudSearch.SearchBound(centerPoint,5000);
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
        Log.d("List" , "onCloudSearched");
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
            Toast.makeText(listView.this, "显示错误", Toast.LENGTH_SHORT).show();

        }

    }

    @Override
    public void onCloudItemDetailSearched(CloudItemDetail cloudItemDetail, int i) {

    }


    static class ViewHolder
    {
        public TextView title;
        public TextView snippet;
        public TextView distance;
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
            Log.i("items", "getData  " + String.valueOf(item.getTitle()) + i);
            Log.i("items", "getData  " + String.valueOf(item.getDistance()));

            map = new HashMap<String, Object>();
            map.put("title", item.getTitle());
            map.put("snippet", item.getSnippet());
            map.put("distance" , item.getDistance() + "km   ");
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
        public View getView(int position, View convertView, ViewGroup parent) {
            Log.d("List" , "getView");
            ViewHolder holder = null;
            //如果缓存convertView为空，则需要创建View
            if(convertView == null)
            {
                holder = new ViewHolder();
                //根据自定义的Item布局加载布局
                convertView = mInflater.inflate(R.layout.content_list_view, null);
                holder.title = (TextView)convertView.findViewById(R.id.Title);
                holder.snippet = (TextView)convertView.findViewById(R.id.Snippet);
                holder.distance = (TextView)convertView.findViewById(R.id.Distance);
                //将设置好的布局保存到缓存中，并将其设置在Tag里，以便后面方便取出Tag
                convertView.setTag(holder);
            }else
            {
                holder = (ViewHolder)convertView.getTag();
            }
            holder.title.setText((String)data.get(position).get("title"));
            holder.snippet.setText((String) data.get(position).get("snippet"));
            holder.distance.setText((String) data.get(position).get("distance"));

            return convertView;
        }
    }

}
