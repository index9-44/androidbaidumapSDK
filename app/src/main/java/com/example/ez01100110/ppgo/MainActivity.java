package com.example.ez01100110.ppgo;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import java.util.Map;
public class MainActivity extends AppCompatActivity implements SensorEventListener {
    // 定位相关
    private LocationClient mLocClient;
    private MyLocationListener myListener = new MyLocationListener();
    // 定位图层显示方式
    private MyLocationConfiguration.LocationMode mCurrentMode;
    private SensorManager mSensorManager;
    private Double lastX = 0.0;
    private int mCurrentDirection = 0;
    private double mCurrentLat = 0.0;
    private double mCurrentLon = 0.0;
    private float mCurrentAccracy;
    // 初始化地图
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    // 是否首次定位
    private boolean isFirstLoc = true;
    // 是否开启定位图层
    private boolean isLocationLayerEnable = true;
    private MyLocationData myLocationData;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("Tag","MainActivity1");
        setContentView(R.layout.activity_location_type);
        // 地图初始化
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        Log.e("Tag","MainActivity2");
        Log.e("Tag","MainActivity3");
        // 定位初始化
        initLocation();
        Log.e("Tag","MainActivity4");
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        double x = sensorEvent.values[SensorManager.DATA_X];
        if (Math.abs(x - lastX) > 1.0) {
            mCurrentDirection = (int) x;
            myLocationData = new MyLocationData.Builder()
                    .accuracy(mCurrentAccracy)// 设置定位数据的精度信息，单位：米
                    .direction(mCurrentDirection)// 此处设置开发者获取到的方向信息，顺时针0-360
                    .latitude(mCurrentLat)
                    .longitude(mCurrentLon)
                    .build();
            mBaiduMap.setMyLocationData(myLocationData);
        }
        lastX = x;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    /**
     * 定位初始化
     */
    public  void initLocation(){
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        // 定位初始化 注册LocationClient类
        mLocClient = new LocationClient(this);
        //注册监听
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        //如果不写，默认为false。
        //如果开发者需要获得当前点的地址信息，此处必须为true
        option.setIsNeedAddress(true);
        // 打开gps
        option.setOpenGps(true);
        // 设置坐标类型
        option.setCoorType("bd09ll");
        //设置坐标监控每一次的时间长短
        option.setScanSpan(5000);

        //mLocClient为第二步初始化过的LocationClient对象
        //需将配置好的LocationClientOption对象，通过setLocOption方法传递给LocationClient对象使用
        //更多LocationClientOption的配置，请参照类参考中LocationClientOption类的详细说明
        mLocClient.setLocOption(option);
        mLocClient.start();
    }

    /**
     * 设置定位图层的开启和关闭
     */
    public void setLocEnable(View v){
        if(isLocationLayerEnable){
            mBaiduMap.setMyLocationEnabled(false);
            ((Button) v).setText("开启定位图层");
            isLocationLayerEnable = !isLocationLayerEnable;
        }else{
            mBaiduMap.setMyLocationEnabled(true);
            mBaiduMap.setMyLocationData(myLocationData);
            ((Button) v).setText("关闭定位图层");
            isLocationLayerEnable = !isLocationLayerEnable;
        }
    }

    /**
     * 设置普通模式
     */
    public void setNormalType(View v){
        mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
        // 传入null，则为默认图标
        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(mCurrentMode, true, null));
        MapStatus.Builder builder1 = new MapStatus.Builder();
        builder1.overlook(0);
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder1.build()));
    }

    /**
     * 设置跟随模式
     */
    public void setFollowType(View v){
        mCurrentMode = MyLocationConfiguration.LocationMode.FOLLOWING;
        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(mCurrentMode, true, null));
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.overlook(0);
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
    }
    /**
     * 定位SDK监听函数
     */
    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            // MapView 销毁后不在处理新接收的位置
            if (location == null || mMapView == null) {
                return;
            }
            mCurrentLat = location.getLatitude();
            mCurrentLon = location.getLongitude();
            mCurrentAccracy = location.getRadius();
            myLocationData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())// 设置定位数据的精度信息，单位：米
                    .direction(mCurrentDirection)// 此处设置开发者获取到的方向信息，顺时针0-360
                    .latitude(location.getLatitude())
                    .longitude(location.getLongitude())
                    .build();
            mBaiduMap.setMyLocationData(myLocationData);
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(18.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }
            String locationDescribe = location.getLocationDescribe();
            Log.e("Tag","位置信息："+locationDescribe);

            Log.e("Tag","纬度："+location.getLatitude());
            Log.e("Tag","\n 经线："+location.getLongitude());
            Log.e("Tag","\n 国家："+location.getCountry());
            Log.e("Tag","\n 省："+location.getProvince());
            Log.e("Tag","\n 市："+location.getCity());
            Log.e("Tag","\n 区："+location.getDistrict());
            Log.e("Tag","\n 街道："+location.getStreet());
            if (location.getLocType() == BDLocation.TypeGpsLocation) {
                Log.e("Tag","GPS");
            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
                Log.e("Tag","网络");
            } else if (location.getLocType() == BDLocation.TypeOffLineLocation){
                Log.e("Tag","离线定位成功，离线定位结果也是有效的");
            } else if (location.getLocType() == BDLocation.TypeServerError){
                Log.e("Tag","服务端网络定位失败,错误代码："+location.getLocType());
            } else if (location.getLocType() == BDLocation.TypeNetWorkException){
                Log.e("Tag","网络不通导致定位失败，请检查网络是否通畅");
            } else if (location.getLocType() == BDLocation.TypeCriteriaException){
                Log.e("Tag","无法获取有效定位依据导致定位失败");
            } else {
                Log.e("Tag","未知原因，请向百度地图SDK论坛求助，location.getLocType()错误代码："+location.getLocType());
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 在activity执行onResume时必须调用mMapView. onResume ()
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 在activity执行onPause时必须调用mMapView. onPause ()
        mMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 取消注册传感器监听
        mSensorManager.unregisterListener(this);
        // 退出时销毁定位
        mLocClient.stop();
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        // 在activity执行onDestroy时必须调用mMapView.onDestroy()
        mMapView.onDestroy();
    }



}
