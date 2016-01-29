package battery.zmx.car.com.zmxbattery.map;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.MyLocationStyle;
import com.amap.api.services.cloud.CloudItem;
import com.amap.api.services.cloud.CloudItemDetail;
import com.amap.api.services.cloud.CloudResult;
import com.amap.api.services.cloud.CloudSearch;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;

import java.util.ArrayList;

import battery.zmx.car.com.zmxbattery.ListView.listView;
import battery.zmx.car.com.zmxbattery.R;

public class myLocation extends AppCompatActivity implements LocationSource , AMapLocationListener , CloudSearch.OnCloudSearchListener ,View.OnClickListener
,AMap.OnMarkerClickListener{

    private MapView mapView;
    private AMap aMap;

    private OnLocationChangedListener mListener;
    private AMapLocationClient mLocationClient;
    private AMapLocationClientOption mLocationOption;

    private AMapLocation mLocation;
    private CloudSearch mCloudSearch;
    private LatLonPoint centerPoint;
    private String mKeyWord = "充电桩";
    private String tableId = "56945224305a2a6e2836f2ed";
    private ArrayList<CloudItem> items = new ArrayList<CloudItem>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_location);

        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        init();



        Button button = (Button) findViewById(R.id.button_2);
        button.setOnClickListener(this);


    }

    private void init() {
        if (aMap == null){
            aMap = mapView.getMap();
            setUpMap();

            mCloudSearch = new CloudSearch(this.getApplicationContext());
            mCloudSearch.setOnCloudSearchListener(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        deactivate();
        Log.d("button","onPause");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    //定位样式
    private void setUpMap() {
        Log.d("button","setUpMap");
        // 自定义系统定位小蓝点
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        // 设置小蓝点的图标
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.location_map));
        // 设置圆形的边框颜色
        myLocationStyle.strokeColor(Color.BLACK);
        myLocationStyle.strokeWidth(50000);
        myLocationStyle.radiusFillColor(Color.argb(100, 0, 0, 180));// 设置圆形的填充颜色
        myLocationStyle.strokeWidth(1.0f);// 设置圆形的边框粗细
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.setLocationSource(this);// 设置定位监听
        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        // aMap.setMyLocationType()
    }

    //LocationSourced定位激活
    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        Log.d("button","定位激活");
        mListener = onLocationChangedListener;
        if(mLocationClient == null){
            mLocationClient = new AMapLocationClient(this);
            mLocationOption = new AMapLocationClientOption();
            //设置定位监听
            mLocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //设置定位参数
            mLocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mLocationClient.startLocation();
        }

    }
    //LocationSource停止定位
    @Override
    public void deactivate() {
        mListener = null;
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
        mLocationClient = null;

    }

    //AMapLocationListener定位成功后回调函数
    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        Log.d("button","定位成功后回调");
        mLocation = aMapLocation;
        if (mListener != null && aMapLocation != null) {
            if (aMapLocation.getErrorCode() == 0) {
                mListener.onLocationChanged(aMapLocation);//系统显示小蓝点
            } else {
                int data = aMapLocation.getErrorCode();
                Toast.makeText(myLocation.this, data, Toast.LENGTH_SHORT).show();
            }
        }
        searchByBound(findViewById(R.id.map));
    }


    public void searchByBound(View view) {
        Log.d("button","searchByBound");
        items.clear();
        LatLonPoint centerPoint = new LatLonPoint(mLocation.getLatitude(),mLocation.getLongitude());
        CloudSearch.SearchBound bound = new CloudSearch.SearchBound(centerPoint,5000);
        try {
            CloudSearch.Query query = new CloudSearch.Query(tableId,mKeyWord, bound);
            mCloudSearch.searchCloudAsyn(query);
        } catch (AMapException e) {
            e.printStackTrace();
        }

    }

    //返回Cloud搜索异步处理的结果
    @Override
    public void onCloudSearched(CloudResult cloudResult, int i) {
        Log.d("button","返回Cloud搜索异步处理的结果  i:"+i);
        if(i == 0){
            if(cloudResult != null){
                ArrayList<CloudItem> items = cloudResult.getClouds();
                for (int j = 0; j < items.size(); j++){
                    CloudItem item = items.get(j);
                    LatLng latLng = new LatLng(item.getLatLonPoint().getLatitude(),item.getLatLonPoint().getLongitude());
                    MarkerOptions option = new MarkerOptions();
                    option.position(latLng);
                    option.title(item.getTitle());
                    aMap.addMarker(option);
                }
                Log.d("button"," Cloud success");
                Toast.makeText(myLocation.this,"成功",Toast.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(myLocation.this, "显示错误", Toast.LENGTH_SHORT).show();

        }

    }

    //返回Cloud详情搜索异步处理的结果
    @Override
    public void onCloudItemDetailSearched(CloudItemDetail cloudItemDetail, int i) {

    }


    @Override
    public void onClick(View v) {
        Intent intent = new Intent(myLocation.this , listView.class);
        startActivityForResult(intent , 1);

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }
}
