package com.fy.amapdemo;

import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.AMapOptions;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.UiSettings;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.LatLngBounds;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.MyLocationStyle;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AMap.InfoWindowAdapter,
        GeocodeSearch.OnGeocodeSearchListener, View.OnClickListener {

    private MapView mAMap;
    /*******高德地图View控制器***************/
    private AMap aMap;

    /******存放所有点的经纬度****************/
    private LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();

    /*******经纬度对象**********/
    private LatLng mLatLng0 = new LatLng(38.982345, 119.181072);
    private LatLng mLatLng1 = new LatLng(37.972345, 102.189472);
    private LatLng mLatLng2 = new LatLng(41.983345, 120.189066);
    private LatLng mLatLng3 = new LatLng(42.985365, 126.184372);
    private LatLng mLatLng4 = new LatLng(42.982785, 118.182172);
    private LatLng mLatLng5 = new LatLng(40.985645, 117.185672);
    /*******存储所有Marker**********/
    List<Marker> mAllMarker = new ArrayList<>();
    /*******地理编码控制器***************/
    private GeocodeSearch mGeocodeSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView(savedInstanceState);
        initMapStyle();
    }

    private void initView(Bundle savedInstanceState) {
        mAMap = findViewById(R.id.a_map);
        mAMap.onCreate(savedInstanceState);
        findViewById(R.id.map_location_btn).setOnClickListener(this);
        findViewById(R.id.draw_btn).setOnClickListener(this);
        findViewById(R.id.clear_btn).setOnClickListener(this);
    }

    /**
     * 初始化设置
     */
    private void initMapStyle() {
        if (aMap == null) {
            aMap = mAMap.getMap();
            UiSettings uiSettings = aMap.getUiSettings();
            uiSettings.setScaleControlsEnabled(true);//比例尺
            uiSettings.setZoomControlsEnabled(false);//是否显示logo
            uiSettings.setLogoPosition(AMapOptions.LOGO_POSITION_BOTTOM_LEFT);
            uiSettings.setScrollGesturesEnabled(true);//是否允许滑动
            uiSettings.setMyLocationButtonEnabled(false);//是否显示定位蓝点
            aMap.setInfoWindowAdapter(this);
        }

        //初始化定位蓝点样式类myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_SHOW);//只定位一次。
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.gps_point));

        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。

        aMap.setOnMarkerClickListener(new AMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                getAddress(marker.getPosition());
                return false;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.map_location_btn:
                //自定义定位到当前地点,通过获取经纬度,移动到地图上的位置
                Location location = aMap.getMyLocation();
                aMap.animateCamera(CameraUpdateFactory.changeLatLng(new LatLng(location.getLatitude(), location.getLongitude())));

                //另外还可以通过定位后回调的方式进行控制,详见3D地图demo中的 Location几种模式_5.0.0之前的实现
                //http://lbs.amap.com/api/android-sdk/download/
                break;
            case R.id.draw_btn:
                drawMarker(mLatLng0);
                drawMarker(mLatLng1);
                drawMarker(mLatLng2);
                drawMarker(mLatLng3);
                drawMarker(mLatLng4);
                drawMarker(mLatLng5);
                //通过此方法缩小或扩大地图,将所有坐标显示在页面内
                aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 15));//第二个参数为四周留空宽度
                break;
            case R.id.clear_btn:
                clearAllMarker();
                //除了clearAllMarker()方法,
                //使用  mAMap.removeAllViews() 也可以删除全部Marker
                //但是这样也会删除地图中定位蓝点
                break;
            default:
                break;
        }
    }

    /**
     * 清除所有Marker
     */
    private void clearAllMarker() {
        for (Marker marker : mAllMarker) {
            marker.remove();
        }
        mAllMarker.clear();
    }

    /**
     * 绘制Marker
     *
     * @param latLng 经纬度
     */
    private void drawMarker(LatLng latLng) {
        MarkerOptions markerOption = new MarkerOptions()
//                .icon(BitmapDescriptorFactory.fromView(textView)) 可以自定义View
                .title("标题")
                .snippet("经度 = " + latLng.longitude + "纬度 = " + latLng.latitude)
                .position(latLng)//经纬度
                .draggable(true);

        Marker marker = aMap.addMarker(markerOption);
        mAllMarker.add(marker);//将Marker添加到列表中
        boundsBuilder.include(marker.getPosition());//绘制Marker
    }

    /**
     * 不可自定义Marker背景
     *
     * @param marker Marker
     * @return 显示的View
     */
    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    /**
     * 可自定义Marker背景
     *
     * @param marker Marker
     * @return 显示的View
     */
    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    /**
     * 响应逆地理编码
     */
    public void getAddress(LatLng position) {
        LatLonPoint latLonPoint = new LatLonPoint(position.latitude, position.longitude);
        if (mGeocodeSearch == null) {
            mGeocodeSearch = new GeocodeSearch(this);
            mGeocodeSearch.setOnGeocodeSearchListener(this);
        }

        RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200,
                GeocodeSearch.AMAP);// 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
        mGeocodeSearch.getFromLocationAsyn(query);// 设置异步逆地理编码请求
    }

    /**
     * 逆地理编码回调
     *
     * @param regeocodeResult
     * @param i
     */
    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
        if (i == 1000) {//1000成功
            String formatAddress = regeocodeResult.getRegeocodeAddress().getFormatAddress();
            Toast.makeText(this, formatAddress, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mAMap.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mAMap.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mAMap.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAMap.onDestroy();
    }

}
