package com.allocare.allokit.kitmodule;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.allocare.allokit.R;
import com.allocare.allokit.address.BottomSheetFragment;
import com.allocare.allokit.address.UpdateApp;
import com.allocare.allokit.cartmodule.CartActivity;
import com.allocare.allokit.cartmodule.CartModel;
import com.allocare.allokit.database.AppDatabase;
import com.allocare.allokit.notification.NotificationActivity;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class OrderSumActivity extends AppCompatActivity implements View.OnClickListener, IGPSActivity {

    BottomNavigationView bottomLay;


    Context mActivity;

    ArrayList<String> banner = new ArrayList<>();
    View badge = null;
    View cartbadge = null;

    ViewPager pager;
    private static final int PERMISSION_REQUEST_CODE_MAIN = 300;



    LinearLayout pageIndicator;

    int currentPage = 0;
    Timer timer;
    final long DELAY_MS = 10000;//delay in milliseconds before task is to be executed
    final long PERIOD_MS = 10000;
    GalleryAdapter adapter;

    ProgressDialog dialog ;

    TextView languageText;

    private GPS gps;

    private AppDatabase mDb;


    ArrayList<GroceryKitModule> data = new ArrayList<GroceryKitModule>();

    RecyclerView recyclerView;
    MyorderListAdapter adpaterRecycler;



    public static ArrayList<String> cities = new ArrayList<String>();


    ImageView backImg;




    TextView badageTex;
    TextView cartbadageTex;

    UpdateApp updateApp;


    String currentVersion = Utility.APPCODEVERSION;



    @Override
    protected void attachBaseContext(Context newBase) {

        Log.e("topLAng","-->"+ SaveSharedPreference.getAppLanguage(newBase));

        // Context context = Utility.changeLang(newBase, SaveSharedPreference.getAppLanguage(newBase));
        super.attachBaseContext(newBase);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_sum);
        mActivity = this;
        mDb = AppDatabase.getInstance(getApplicationContext());

        getPINCode();
        dialog = new ProgressDialog(mActivity);
        dialog.setMessage(getResources().getString(R.string.loading));

        updateApp = new UpdateApp(mActivity);



        bottomLay = findViewById(R.id.bottomLay);
        pageIndicator = findViewById(R.id.pageIndicator);
        languageText = findViewById(R.id.languageText);
       // languageText = findViewById(R.id.languageText);
        //avatharImg = findViewById(R.id.avatharImg);



        BottomNavigationMenuView bottomNavigationMenuView =
                (BottomNavigationMenuView) bottomLay.getChildAt(0);
        View v = bottomNavigationMenuView.getChildAt(1);
        BottomNavigationItemView itemView = (BottomNavigationItemView) v;

        badge = LayoutInflater.from(this).inflate(R.layout.notification_badge, itemView, true);

        badageTex = badge.findViewById(R.id.notifications_badge);



        View vs = bottomNavigationMenuView.getChildAt(2);
        BottomNavigationItemView itemViewss = (BottomNavigationItemView) vs;

        cartbadge = LayoutInflater.from(this).inflate(R.layout.cart_badge, itemViewss, true);

        cartbadageTex = cartbadge.findViewById(R.id.cartbadge_badge);


        bottomLay.setOnNavigationItemSelectedListener(navigationItemSelectedListener);



        //banner.add("https://i.ytimg.com/vi/yiJv_vz3zZ0/maxresdefault.jpg");
       // banner.add("https://i.ytimg.com/vi/yiJv_vz3zZ0/maxresdefault.jpg");
       // banner.add("https://i.ytimg.com/vi/yiJv_vz3zZ0/maxresdefault.jpg");

        pager = (ViewPager) findViewById(R.id.viewPager);
        //PagerAdapter adapter = new PhotosAdapter(getChildFragmentManager(), photosUrl);

        adapter = new GalleryAdapter(mActivity, banner);
        pager.setAdapter(adapter);

        //  TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        // tabLayout.setupWithViewPager(pager, true);

        recyclerView = findViewById(R.id.recyclerView);

        adpaterRecycler =  new MyorderListAdapter(data,this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adpaterRecycler);


        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                // viewHolder.addbottomdots(position,tempdata.getArrayData().size());
                //  current_page_no.setText("" + (position + 1));
                addbottomdots((position), banner.size(), pageIndicator);
                // viewPageStates.put((listPosition), position);

                ////Log.e("viewPageStates", "--->" + viewPageStates);

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        // addbottomdots((viewPageStates.get(listPosition)), tempdata.getArrayData().size(), ll_dots);

        showDialog();
        getBanners();
        getProducts();


        languageText.setOnClickListener(this);
     //   buyLay.setOnClickListener(this);
       // info.setOnClickListener(this);
       // items.setOnClickListener(this);

        updateApp.setUpdateLayClick(new UpdateApp.UpdateClick() {
            @Override
            public void onClicked() {
                finish();

            }
        });



    }

    @Override
    protected void onPause() {

        this.unregisterReceiver(this.mConnReceiver);

        super.onPause();

    }

    BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.navigation_home:

                            return true;
                        case R.id.navigation_cart:
                           // openFragment(SmsFragment.newInstance("", ""));

                            Intent i = new Intent(mActivity,MyOrderActivity.class);
                            startActivity(i);
                           // overridePendingTransition(R.anim.fade_in,R.anim.fade_out);

                            return true;
                        case R.id.navigation_notifications:
                          //  openFragment(NotificationFragment.newInstance("", ""));

                            Intent j = new Intent(mActivity, NotificationActivity.class);
                            startActivity(j);
                            //overridePendingTransition(R.anim.fade_in,R.anim.fade_out);

                            return true;

                        case R.id.navigation_mycart:

                            Intent v = new Intent(mActivity, CartActivity.class);
                            startActivity(v);

                            return true;

                        case R.id.navigation_profile:
                               // updateApp.show();


                                Intent l = new Intent(mActivity, ProfileActivity.class);
                                l.putExtra("type","home");
                                startActivity(l);
                               // overridePendingTransition(R.anim.fade_in,R.anim.fade_out);

                                //    openFragment(NotificationFragment.newInstance("", ""));
                            return true;
                    }
                    return false;
                }
            };


    public void showDialog() {
        if(dialog!=null) {
            if(!dialog.isShowing())
                dialog.show();
        }
    }

    public void cancelDialog()
    {
        if(dialog!=null) {
            if(dialog.isShowing())
                dialog.dismiss();
        }
    }


    private void setTimer(int NUM_PAGES)
    {

        addbottomdots((0), NUM_PAGES, pageIndicator);

        final Handler handler = new Handler();
        final Runnable Update = new Runnable() {
            public void run() {
                if (currentPage == NUM_PAGES) {
                    currentPage = 0;
                }

                if(pager!=null) {
                    pager.setCurrentItem(currentPage++, true);

                }

            }
        };

        timer = new Timer(); // This will create a new Thread
        timer.schedule(new TimerTask() { // task to be scheduled
            @Override
            public void run() {
                handler.post(Update);
            }
        }, DELAY_MS, PERIOD_MS);
    }

    private void addbottomdots(int currentItem, int len, ViewGroup parent) {
        TextView[] dots = new TextView[len];
        parent.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            if (dots.length > 1) {
                dots[i] = new TextView(mActivity);
                dots[i].setText(Html.fromHtml("&#8226;"));
                dots[i].setTextSize(35);

                if (i == currentItem) {
                    dots[i].setTextColor(getResources().getColor(R.color.colorPrimary));

                } else {
                    dots[i].setTextColor(Color.GRAY);

                }
                parent.addView(dots[i]);
            }


        }


    }

    @Override
    public void onClick(View v) {

        switch (v.getId())
        {
            case R.id.languageText:
                languageSelect();
                break;

            case R.id.buyLay:

                if(!SaveSharedPreference.getUserArea(mActivity).equalsIgnoreCase(""))
                {
                    Intent i = new Intent(mActivity,Grocerykit.class);
                    startActivity(i);
                }else {
                    Intent l = new Intent(mActivity, ProfileActivity.class);
                    l.putExtra("type","kit");
                    startActivity(l);
                }



                break;
            case R.id.items:
            case R.id.info:
                BottomSheetFragment bottomSheetDialog = BottomSheetFragment.newInstance("");
                bottomSheetDialog.show(getSupportFragmentManager(), "Bottom Sheet Dialog Fragment");
                break;
        }

    }

    private void languageSelect()
    {
        String language = SaveSharedPreference.getAppLanguage(this);
        //Log.e("Language_Checking","---->"+language);

        // String type = "english";
        if(language.equals("en")) {
            // type = "tamil";
            Utility.changeLang(mActivity, "ta");
            SaveSharedPreference.setAppLanguge(mActivity,"ta");

        }else {
            Utility.changeLang(mActivity, "en");
            SaveSharedPreference.setAppLanguge(mActivity,"en");


        }

        Log.e("selectedLanguage","-->"+ SaveSharedPreference.getAppLanguage(mActivity));

        updateView(SaveSharedPreference.getAppLanguage(this));

    }

    public void updateView(String language) {
        //Log.e("Button Clicked","----->UpdateView");
        //Log.e("Update View"," ");
        //LocaleHelper.setLocale(this, language);
        //Tovuti.from(MainActivity.this).stop();

        Utility.setLocale(this,language);

        Intent i = getIntent();
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(getIntent());
        // Tovuti.from(MainActivity.this).stop();
        overridePendingTransition(0, 0);
        overridePendingTransition(0, 0);
        finish();
        //call_police.setText(resources.getString(R.string.Call_Namakkal_Police));
    }

    @Override
    public void locationChanged(double longitude, double latitude) {
            Log.e("Location","Long-->"+longitude + "Lat-->"+ latitude);
            if(latitude != 0.0) {
                LocationValueModel.setmLatitude(latitude);
            }

            if(longitude != 0.0) {
                LocationValueModel.setmLongitude(longitude);
            }

    }

    @Override
    public void displayGPSSettingsDialog() {

    }


    public class GalleryAdapter extends PagerAdapter {

        private static final String TAG = "GalleryAdapter";

        private final ArrayList<String> mItems;
        private final LayoutInflater mLayoutInflater;


        /**
         * The click event listener which will propagate click events to the parent or any other listener set
         */
        //  private ItemClickSupport.SimpleOnItemClickListener mOnItemClickListener;

        /**
         * Constructor for gallery adapter which will create and screen slide of images.
         *
         * @param context      The context which will be used to inflate the layout for each page.
         * @param mediaGallery The list of items which need to be displayed as screen slide.
         */
        public GalleryAdapter(@NonNull Context context,
                              @NonNull ArrayList<String> mediaGallery) {
            super();

            // Inflater which will be used for creating all the necessary pages
            mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            // The items which will be displayed.
            mItems = mediaGallery;

        }

        @Override
        public int getCount() {
            // Just to be safe, check also if we have an valid list of items - never return invalid size.
            return null == mItems ? 0 : mItems.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            // The object returned by instantiateItem() is a key/identifier. This method checks whether
            // the View passed to it (representing the page) is associated with that key or not.
            // It is required by a PagerAdapter to function properly.
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            // This method should create the page for the given position passed to it as an argument.
            // In our case, we inflate() our layout resource to create the hierarchy of view objects and then
            // set resource for the ImageView in it.
            // Finally, the inflated view is added to the container (which should be the ViewPager) and return it as well.

            // inflate our layout resource
            View itemView = mLayoutInflater.inflate(R.layout.frament_multiple_article, container, false);

            // Display the resource on the view
            displayGalleryItem(itemView.findViewById(R.id.post_image), mItems.get(position));

            // Add our inflated view to the container
            container.addView(itemView);

            // Detect the click events and pass them to any listeners
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   /* if (null != mOnItemClickListener) {
                      //  mOnItemClickListener.onItemClicked(position);
                    }*/

                    ////Log.e("GalleryAdapter", "-->");
                }
            });

            // Return our view
            return itemView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            // Removes the page from the container for the given position. We simply removed object using removeView()
            // but could’ve also used removeViewAt() by passing it the position.
            try {
                // Remove the view from the container
                container.removeView((View) object);

                // Try to clear resources used for displaying this view
                // Glide.clear(((View) object).findViewById(R.id.post_image));
                // Remove any resources used by this view
                unbindDrawables((View) object);
                // Invalidate the object
                object = null;
            } catch (Exception e) {
                Log.w(TAG, "destroyItem: failed to destroy item and clear it's used resources", e);
            }
        }


        @Override
        public int getItemPosition(@NonNull Object object) {
            return super.getItemPosition(object);
        }

        /**
         * Recursively unbind any resources from the provided view. This method will clear the resources of all the
         * children of the view before invalidating the provided view itself.
         *
         * @param view The view for which to unbind resource.
         */
        protected void unbindDrawables(View view) {
            if (view.getBackground() != null) {
                view.getBackground().setCallback(null);
            }
            if (view instanceof ViewGroup) {
                for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                    unbindDrawables(((ViewGroup) view).getChildAt(i));
                }
                ((ViewGroup) view).removeAllViews();
            }
        }

        /**
         * Set an listener which will notify of any click events that are detected on the pages of the view pager.
         *
         * @param onItemClickListener
         *         The listener. If {@code null} it will disable any events from being sent.
         */
       /* public void setOnItemClickListener(ItemClickSupport.SimpleOnItemClickListener onItemClickListener) {
            mOnItemClickListener = onItemClickListener;
        }*/

        /**
         * Display the gallery image into the image view provided.
         *
         * @param galleryView The view which will display the image.
         * @param galleryItem The item from which to get the image.
         */
        private void displayGalleryItem(ImageView galleryView, String galleryItem) {
            if (null != galleryItem) {
                if (!galleryItem.equalsIgnoreCase("")) {

                    try {
                       /* val requestOptions = RequestOptions()
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .signature(ObjectKey(signature))
                                .override(100, 100)*/
                        /*BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeFile(galleryItem.getLink(), options);
                        int imgWidth = options.outWidth;
                        int imgHeight = options.outHeight;

                        if (width_px > 0) {
                            int imageViewHeight = (imgHeight * width_px) / imgWidth;
                            if (imageViewHeight > 0) {
                                // set fixed height to the imageView
                                ViewGroup.LayoutParams imgParams = galleryView.getLayoutParams();
                                imgParams.height = imageViewHeight;
                                galleryView.setLayoutParams(imgParams);
                            }
                        }*/

                        /*RequestOptions options = new RequestOptions()
                                .signature(currentItem.getSignature())
                                .format(DecodeFormat.PREFER_RGB_565)
                                .centerCrop()
                                .placeholder(placeholder)
                                .diskCacheStrategy(DiskCacheStrategy.RESOURCE);

                        Glide.with(imageView.getContext())
                                .load(currentItem.getUri())
                                .apply(options)
                                .thumbnail(0.5f)
                                .into(imageView);*/



                        Glide.with(galleryView.getContext()).asBitmap() // Bind it with the context of the actual view used
                                .load(galleryItem)
                                //.placeholder(ContextCompat.getDrawable(galleryView.getContext(), R.drawable.who))
                                //.error(ContextCompat.getDrawable(galleryView.getContext(), R.drawable.who))
                                .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                                // .diskCacheStrategy(DiskCacheStrategy.ALL)// Load the image
                                // .asBitmap() // All our images are static, we want to display them as bitmaps
                                //.signature(new ObjectKey(String.valueOf(System.currentTimeMillis())))
                                .format(DecodeFormat.PREFER_RGB_565)
                                .dontAnimate()// the decode format - this will not use alpha at all
                                .listener(new RequestListener<Bitmap>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                      //  pg.setVisibility(View.GONE);
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                       // pg.setVisibility(View.GONE);
                                        return false;
                                    }
                                })
                                //.centerCrop() // scale type
                                //.placeholder(R.drawable.default_product_400_land) // temporary holder displayed while the image loads
                                //.animate(R.anim.fade_in) // need to manually set the animation as bitmap cannot use cross fade
                                .thumbnail(0.2f) // make use of the thumbnail which can display a down-sized version of the image
                                .into(new SimpleTarget<Bitmap>() {
                                    @Override
                                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                        galleryView.setImageBitmap(resource);
                                    }

                                });
                    } catch (Exception e) {
                        galleryView.setImageDrawable(null);

                    }
                } else {

                    // make sure Glide doesn't load anything into this view until told otherwise
                    // Glide.clear(holder.imageView);
                    // remove the placeholder (optional); read comments below
                    galleryView.setImageDrawable(null);
                }
                /*Glide.with(galleryView.getContext()) // Bind it with the context of the actual view used
                        .load(galleryItem.getLink()) // Load the image
                        // .asBitmap() // All our images are static, we want to display them as bitmaps
                        .format(DecodeFormat.PREFER_RGB_565) // the decode format - this will not use alpha at all
                        // .centerCrop() // scale type
                        //.placeholder(R.drawable.default_product_400_land) // temporary holder displayed while the image loads
                        //.animate(R.anim.fade_in) // need to manually set the animation as bitmap cannot use cross fade
                        .thumbnail(0.2f) // make use of the thumbnail which can display a down-sized version of the image
                        .into(galleryView);*/ // Voilla - the target view
            }
        }


    }


    public void getBanners() {
        try{
            String language = SaveSharedPreference.getAppLanguage(this);
            //Log.e("Language_Checking","---->"+language);

            String type = "english";
            if(language.equals("ta")) {
                type = "tamil";
            }else if(language.equals("en")) {
                type = "english";

            }

            JSONObject map =new JSONObject();
            map.put("hostfor",SaveSharedPreference.getHostFor(mActivity));
            map.put("language",type);

            String url = SaveSharedPreference.getBaseURL(mActivity)+Utility.APINAME_BANNER;


            Log.e("bannerparams","-->"+map);
            Log.e("bannerurL","-->"+url);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, map, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {

                    Log.e("Respoonce","-->"+response);

                    cancelDialog();
                    try {
                        if(response.getString(Utility.API_RESPONCE_SUCCESS).equalsIgnoreCase("true"))
                        {

                            JSONArray datas = response.getJSONArray("data");
                            if(datas.length()>0)
                            {
                                for (int i=0;i<datas.length();i++)
                                {
                                    JSONObject obj= datas.getJSONObject(i);

                                    String image="";
                                     image = Utility.NullCheckJson(obj,"image");

                                    if(!image.equalsIgnoreCase("")) {
                                        banner.add(image);
                                    }



                                }
                            }


                            adapter.notifyDataSetChanged();
                            pager.invalidate();
                            if(banner.size()>0) {
                                setTimer(banner.size());
                            }


                        }

                    } catch (JSONException e)
                    {
                        e.printStackTrace();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    cancelDialog();

                    Log.e("error",error.toString());

                    error.printStackTrace();
                }
            }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String,String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json; charset=utf-8");
                    headers.put("token", SaveSharedPreference.getPrefUserToken(mActivity));
                    Log.e("Header",""+headers);

                    return headers;
                }
            };

            request.setRetryPolicy(new DefaultRetryPolicy(0,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance(mActivity).addToRequestQueue(request);

            /*{
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> headers = new HashMap<>();
                headers.put("token", SaveSharedPreference.getPrefUserToken(mActivity));
                return headers;
            }
            }*/


            /*String language = SaveSharedPreference.getAppLanguage(this);
            //Log.e("Language_Checking","---->"+language);

            String type = "english";
            if(language.equals("ta")) {
                type = "tamil";
            }else if(language.equals("en")) {
                type = "english";

            }*/

        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(checkPermissionMain()) {
            if(gps!=null) {
                if (!gps.isRunning()) gps.resumeGPS();
            }
        }

        getLocationWithPermission();

        checkForUpdate();

        getNotifications();

        if(bottomLay!=null)
            bottomLay.setSelectedItemId(R.id.navigation_home);

        this.registerReceiver(this.mConnReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        getCartCount();
    }

    private void getNotifications()
    {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("registerid",SaveSharedPreference.getPrefUserRegisterId(mActivity));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url = SaveSharedPreference.getBaseURL(mActivity)+Utility.APINAME_COUNT;

        Log.e("Params Count","---->"+jsonObject);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("Notification_RESPONSE","----->"+response);
                        try {
                            if(response.getString("success").equalsIgnoreCase("true"))
                            {
                                //  notification_count.setText(response.getString("count"));
                                if(badageTex!=null)
                                    badageTex.setVisibility(View.VISIBLE);
                            }else
                            {
                                // notification_count.setText("0");

                                if(badageTex!=null)
                                    badageTex.setVisibility(View.INVISIBLE);

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Notification_ERROR","----->"+error.getMessage());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> headers = new HashMap<>();
                headers.put("token", SaveSharedPreference.getPrefUserToken(mActivity));
                return headers;
            }
        };


        VolleySingleton.getInstance(mActivity).addToRequestQueue(jsonObjectRequest);

    }


    public void getProducts() {
        try{
            String language = SaveSharedPreference.getAppLanguage(this);
            //Log.e("Language_Checking","---->"+language);

            String type = "english";
            if(language.equals("ta")) {
                type = "tamil";
            }else if(language.equals("en")) {
                type = "english";

            }

            JSONObject map =new JSONObject();
            map.put("hostfor",SaveSharedPreference.getHostFor(mActivity));
            map.put("language",type);
            map.put("offset","1");
            map.put("limit","50");

            String url = SaveSharedPreference.getBaseURL(mActivity)+Utility.APINAME_PRODUCT;


            Log.e("Productparams","-->"+map);
            Log.e("ProducturL","-->"+url);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, map, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {

                    Log.e("Respoonce","-->"+response);

                    cancelDialog();
                    try {
                        if(response.getString(Utility.API_RESPONCE_SUCCESS).equalsIgnoreCase("true"))
                        {
                            data.clear();

                            JSONArray datas = response.getJSONArray("data");
                            if(datas.length()>0)
                            {
                                for (int i=0;i<datas.length();i++)
                                {
                                    JSONObject obj= datas.getJSONObject(i);
                                    GroceryKitModule gm = new GroceryKitModule();

                                    gm.setId(Utility.NullCheckJson(obj,Utility.ID));
                                    gm.setName(Utility.NullCheckJson(obj,Utility.NAME));
                                    gm.setDetails(Utility.NullCheckJson(obj,Utility.APINAME_DETAILS));
                                    gm.setPrice(Utility.NullCheckJson(obj,Utility.PRICE));
                                    gm.setImage(Utility.NullCheckJson(obj,Utility.IMAGE));
                                    gm.setItemsImage(Utility.NullCheckJson(obj,"detailedimage"));

                                    data.add(gm);

                                }
                            }


                            adpaterRecycler.notifyDataSetChanged();
                           // setDetails();

                        }

                    } catch (JSONException e)
                    {
                        e.printStackTrace();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    cancelDialog();

                    Log.e("error",error.toString());

                    error.printStackTrace();
                }
            }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String,String> headers = new HashMap<>();
                    headers.put("token", SaveSharedPreference.getPrefUserToken(mActivity));
                    Log.e("Header",""+headers);

                    return headers;
                }
            };

            request.setRetryPolicy(new DefaultRetryPolicy(0,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance(mActivity).addToRequestQueue(request);

            /*{
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> headers = new HashMap<>();
                headers.put("token", SaveSharedPreference.getPrefUserToken(mActivity));
                return headers;
            }
            }*/


            /*String language = SaveSharedPreference.getAppLanguage(this);
            //Log.e("Language_Checking","---->"+language);

            String type = "english";
            if(language.equals("ta")) {
                type = "tamil";
            }else if(language.equals("en")) {
                type = "english";

            }*/

        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }



    /*private void setDetails()
    {
        GroceryKitModule tempdat = data.get(0);
        if(tempdat!=null)
        {
            //pricetotal = Integer.parseInt(tempdat.getPrice());
           // productid = tempdat.getId();
            productPrice.setText("Rs : "+tempdat.getPrice());
            productTitle.setText(tempdat.getName());
            type.setText(tempdat.getName());

            description.setText(tempdat.getDetails());

            //quantityText.setText(String.valueOf(quantity));
            // otalprice.setText(String.valueOf("Rs : "+(pricetotal * quantity)));

            if(!tempdat.getImage().equalsIgnoreCase("")) {

                try {
                    Glide.with(avatharImg.getContext()).asBitmap() // Bind it with the context of the actual view used
                            .load(tempdat.getImage())
                            //.placeholder(ContextCompat.getDrawable(galleryView.getContext(), R.drawable.who))
                            //.error(ContextCompat.getDrawable(galleryView.getContext(), R.drawable.who))
                            .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                            .format(DecodeFormat.PREFER_RGB_565)
                            .dontAnimate()// the decode format - this will not use alpha at all
                            .listener(new RequestListener<Bitmap>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                    return false;
                                }
                            })
                            .centerCrop() // scale type
                            //.placeholder(R.drawable.default_product_400_land) // temporary holder displayed while the image loads
                            //.animate(R.anim.fade_in) // need to manually set the animation as bitmap cannot use cross fade
                            .thumbnail(0.2f) // make use of the thumbnail which can display a down-sized version of the image
                            .into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                    avatharImg.setImageBitmap(resource);
                                }

                            });
                } catch (Exception e) {
                    avatharImg.setImageDrawable(null);

                }
            }else {

            }

        }
    }*/


    @Override
    protected void onDestroy() {

       // SaveSharedPreference.clearAllData(mActivity);

        super.onDestroy();

    }



    private void getLocationWithPermission() {

        if(checkPermissionMain()) {
            gps = new GPS(this);
        }else {
            requestPermissionMain();
        }

    }

    @Override
    protected void onStop() {
        gps.stopGPS();
        super.onStop();
    }

    public boolean checkPermissionMain() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION);
        //int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA);

        return result == PackageManager.PERMISSION_GRANTED; //&& result1 == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissionMain() {

        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE_MAIN);

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {


            case PERMISSION_REQUEST_CODE_MAIN:
                if (grantResults.length > 0) {

                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    // boolean cameraAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (locationAccepted) {

                        gps = new GPS(this);


                    }
                        /*////Here to do the task
                        lm = (LocationManager) mActivity.getSystemService(Context.LOCATION_SERVICE);
                        boolean gps_enabled = false;
                        try {
                            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
                        } catch (Exception ex) {
                        }
                        if (gps_enabled) {
                            lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, locationListener);
                            //if(latitude.equals("") && longitude.equals(""))
                            //{
                            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if(location!=null)
                            {
                                if(location.getLatitude() != 0.0) {
                                    LocationValueModel.setmLatitude(location.getLatitude());
                                }

                                if(location.getLongitude()!= 0.0) {
                                    LocationValueModel.setmLongitude(location.getLongitude());
                                }

                                // longitude = String.valueOf(location.getLongitude());
                                // latitude = String.valueOf(location.getLatitude());
                            }else
                            {
                                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, locationListener);
                            }
                            //  }
                            // doAction();
                        } else {
                            new AlertDialog.Builder(mActivity)
                                    .setTitle(getResources().getString(R.string.LocationisNotEnable))  // GPS not found
                                    .setMessage(getResources().getString(R.string.Wanttoenable)) // Want to enable?
                                    .setPositiveButton(getResources().getString(R.string.Yes), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                        }
                                    })
                                    .setNegativeButton(getResources().getString(R.string.No), null)
                                    .show();
                        }
                    }*/
                }
                break;


        }
    }

    public class MyorderListAdapter extends RecyclerView.Adapter {

        public static final int TYPE_MENUS = 0, TYPE_POST = 1, VIEW_TYPE_LOADING = 2 , TYPE_DASHBOARD = 3;

        public static final int cutsomViewcount= 0;
        Context mContext;
        private ArrayList<GroceryKitModule> post = new ArrayList<GroceryKitModule>();


        public MyorderListAdapter(ArrayList<GroceryKitModule> data, Context context) {
            this.post = data;
            this.mContext = context;
            // viewPool = new RecyclerView.RecycledViewPool();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view;
            switch (viewType) {

                case TYPE_POST:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_list, parent, false);
                    //  new PostTypeViewHolder(view).setIsRecyclable(true);
                    //  .innerRecyclerView.setRecycledViewPool(viewPool);
                    return new PostTypeViewHolder(view);

                case VIEW_TYPE_LOADING:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.loading_lay, parent, false);
                    return new LoadingViewHolder(view);




            }
            return null;
        }

        @Override
        public int getItemViewType(int position) {

            /*if (position == 0) {
                return TYPE_MENUS;
            } else if(position == 1) {
                return TYPE_DASHBOARD;
            }else*/
            if(this.post.get(position - cutsomViewcount) != null) {
                // return TYPE_POST;
                // if (!this.post.get(position-1).getArrayData().get(0).getLink().equalsIgnoreCase("")) {
                return TYPE_POST;

                // return this.post.get(position-1) != null ? TYPE_POST : VIEW_TYPE_LOADING;

            } else {
                return VIEW_TYPE_LOADING;
            }


            //  return this.post.get(position-1) == null ? VIEW_TYPE_LOADING : TYPE_POST;


        }

        //inside the adapter class
        @Override
        public long getItemId(int position) {
            return position;
        }


        @Override
        public int getItemCount() {
            return this.post == null ? 0 : this.post.size() + cutsomViewcount;
        }


        @Override
        public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
            super.onViewAttachedToWindow(holder);

            // Descriptor d = descriptors.get(holder.getAdapterPosition());
            /*if (d != null) {
                if (d.getHeight() > 0) {
                    LinearLayout.LayoutParams params = new
                            LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    // Set the height by params
                    ////Log.e("Pos:-->" + holder.getAdapterPosition(), "--->" + d.getHeight());

                    params.height = d.getHeight();
                    // set height of RecyclerView
                    holder.itemView.setMinimumHeight(params.height);

                }
            }*/

        }

        @Override
        public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {

            ////Log.e("detachedPostion","-->"+holder.getAdapterPosition());

            // Descriptor d = descriptors.get(holder.getAdapterPosition());
            /*Descriptor d = new Descriptor();
            d.setWidth(holder.itemView.getWidth());
            d.setHeight(holder.itemView.getHeight());
            descriptors.put(holder.getAdapterPosition(), d);*/


            super.onViewDetachedFromWindow(holder);
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int listPosition) {

            try {


                if (holder instanceof PostTypeViewHolder) {

                    try {
                        if (holder.itemView.getTag(listPosition - cutsomViewcount) == null) {


                            GroceryKitModule tempdata = this.post.get(listPosition - cutsomViewcount);

                            PostTypeViewHolder viewHolder = (PostTypeViewHolder) holder;
                            if (tempdata != null) {
                                viewHolder.firstBind(tempdata, listPosition - cutsomViewcount);
                            }

                        } else {
                            holder.itemView.getTag(listPosition - cutsomViewcount);

                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }else if (holder instanceof LoadingViewHolder) {
                    LoadingViewHolder viewHolder = (LoadingViewHolder) holder;
                }

            } catch (Exception e) {

            }

        }


        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List payloads) {
            if (payloads.isEmpty()) {
                super.onBindViewHolder(holder, position, payloads);
            } else {
                Bundle o = (Bundle) payloads.get(0);
                for (String key : o.keySet()) {
                    if (key.equals("articelid")) {

                        if (holder instanceof PostTypeViewHolder) {

                            try {
                                if (holder.itemView.getTag(position - cutsomViewcount) == null) {

                                    GroceryKitModule tempdata = this.post.get(position - cutsomViewcount);

                                    PostTypeViewHolder viewHolder = (PostTypeViewHolder) holder;

                                    if (tempdata != null) {


                                        viewHolder.firstBind(tempdata, position - cutsomViewcount);


                                    } else {
                                        viewHolder.firstBind(tempdata, position - cutsomViewcount);
                                    }
                                } else {
                                    holder.itemView.getTag(position - cutsomViewcount);

                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    }
                }
            }
        }




        public class LoadingViewHolder extends RecyclerView.ViewHolder {




            public LoadingViewHolder(@NonNull View itemView) {
                super(itemView);



            }
        }




        public class PostTypeViewHolder extends RecyclerView.ViewHolder {


            ImageView avatharImg;

            CardView buyLay;

            TextView type,description,productTitle,productPrice;

            TextView  items;

            ImageView info;

            public PostTypeViewHolder(View itemView) {
                super(itemView);




                type = itemView.findViewById(R.id.type);
                description = itemView.findViewById(R.id.description);
                productTitle = itemView.findViewById(R.id.productTitle);
                productPrice = itemView.findViewById(R.id.productPrice);
                buyLay = itemView.findViewById(R.id.buyLay);
                info = itemView.findViewById(R.id.info);
                items = itemView.findViewById(R.id.items);
                avatharImg = itemView.findViewById(R.id.avatharImg);


                //articlecontent.setMovementMethod(LinkMovementMethod.getInstance());
                // articleheading.setMovementMethod(LinkMovementMethod.getInstance());

                ViewGroup.LayoutParams param = itemView.getLayoutParams();
                param.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                itemView.setLayoutParams(param);

                itemView.requestLayout();
                //   articlecontent.setVisibility(View.GONE);


            }


            void firstBind(final GroceryKitModule tempdat, int listPosition) {


                //pricetotal = Integer.parseInt(tempdat.getPrice());
                // productid = tempdat.getId();
                productPrice.setText("Rs : " + tempdat.getPrice());
                productTitle.setText(tempdat.getName());
                type.setText(tempdat.getName());

                description.setText(Html.fromHtml(tempdat.getDetails()));

                buyLay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if(!SaveSharedPreference.getUserArea(mActivity).equalsIgnoreCase(""))
                        {
                            Intent i = new Intent(mActivity,Grocerykit.class);
                            i.putExtra("productid",tempdat.getId());
                            startActivity(i);
                        }else {
                            Intent l = new Intent(mActivity, ProfileActivity.class);
                            l.putExtra("type","kit");
                            startActivity(l);
                        }

                    }
                });

                items.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BottomSheetFragment bottomSheetDialog = BottomSheetFragment.newInstance(tempdat.getItemsImage());
                        bottomSheetDialog.show(getSupportFragmentManager(), "Bottom Sheet Dialog Fragment");
                    }
                });

                //quantityText.setText(String.valueOf(quantity));
                // otalprice.setText(String.valueOf("Rs : "+(pricetotal * quantity)));

                if (!tempdat.getImage().equalsIgnoreCase("")) {

                    try {
                        Glide.with(avatharImg.getContext()).asBitmap() // Bind it with the context of the actual view used
                                .load(tempdat.getImage())
                                //.placeholder(ContextCompat.getDrawable(galleryView.getContext(), R.drawable.who))
                                //.error(ContextCompat.getDrawable(galleryView.getContext(), R.drawable.who))
                                .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                                .format(DecodeFormat.PREFER_RGB_565)
                                .dontAnimate()// the decode format - this will not use alpha at all
                                .listener(new RequestListener<Bitmap>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                        return false;
                                    }
                                })
                                .centerCrop() // scale type
                                //.placeholder(R.drawable.default_product_400_land) // temporary holder displayed while the image loads
                                //.animate(R.anim.fade_in) // need to manually set the animation as bitmap cannot use cross fade
                                .thumbnail(0.2f) // make use of the thumbnail which can display a down-sized version of the image
                                .into(new SimpleTarget<Bitmap>() {
                                    @Override
                                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                        avatharImg.setImageBitmap(resource);
                                    }

                                });
                    } catch (Exception e) {
                        avatharImg.setImageDrawable(null);

                    }
                } else {

                }


            }


        }





    }



    private void checkForUpdate() {

        /*PackageManager pm = this.getPackageManager();
        PackageInfo pInfo = null;

        try {
            pInfo =  pm.getPackageInfo(this.getPackageName(),0);
            currentVersion = pInfo.versionCode;



        } catch (PackageManager.NameNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }*/

        String url = SaveSharedPreference.getBaseURL(mActivity)+Utility.APINAME_APPUPDATE+"/"+SaveSharedPreference.getHostFor(mActivity);


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        cancelUpdateDialog();

                        Log.e("Update_reponse","----->"+response);
                        try {
                            int cv = Integer.parseInt(currentVersion);
                            int uv = Integer.parseInt(response.getString("message").trim());
                            if (cv < uv) {
                                //Intent i = new Intent(MainActivity.this, UpdateActivity.class);
                               // startActivity(i);
                              //  finish();

                                showUpdateDialog();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Log.e("ANNOUNCEMENT_ERROR","----->"+error.getMessage());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("token", SaveSharedPreference.getPrefUserToken(mActivity));
                return headers;
            }
        };

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(0,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }


    public boolean isShowingUpdateDialog(){
        if(updateApp!=null)
        {
           return updateApp.isShowing();
        }else {
            return false;
        }

    }


    public void showUpdateDialog() {
        if(updateApp!=null) {
            if(!updateApp.isShowing()) {
                updateApp.show();
            }
        }
    }

    public void cancelUpdateDialog() {
        if(updateApp!=null) {
            if(updateApp.isShowing()) {
                updateApp.cancel();
            }
        }
    }



    public void getPINCode() {
        try{
            String language = SaveSharedPreference.getAppLanguage(this);
            //Log.e("Language_Checking","---->"+language);

            String type = "english";
            if(language.equals("ta")) {
                type = "tamil";
            }else if(language.equals("en")) {
                type = "english";

            }

            JSONObject map =new JSONObject();
            // map.put("hostfor",SaveSharedPreference.getHostFor(mActivity));
            // map.put("language",type);
            // map.put("offset","1");
            // map.put("limit","5");

            String url = Utility.BASE_POSTCODE;


            Log.e("Productparams","-->"+map);
            Log.e("ProducturL","-->"+url);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, map, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {

                    Log.e("Respoonce","-->"+response);

                    //cancelDialog();
                    try {
                        if(response.getString(Utility.API_RESPONCE_SUCCESS).equalsIgnoreCase("true"))
                        {

                            JSONArray datas = response.getJSONArray("data");
                            if(datas.length()>0)
                            {
                                cities.clear();

                                for (int i=0;i<datas.length();i++)
                                {
                                    JSONObject obj= datas.getJSONObject(i);

                                    cities.add(Utility.NullCheckJson(obj,"postcode"));

                                    //  GroceryKitModule gm = new GroceryKitModule();

                                    //gm.setId(Utility.NullCheckJson(obj,Utility.ID));
                                    //gm.setName(Utility.NullCheckJson(obj,Utility.NAME));
                                    // gm.setDetails(Utility.NullCheckJson(obj,Utility.APINAME_DETAILS));
                                    // gm.setPrice(Utility.NullCheckJson(obj,Utility.PRICE));
                                    // gm.setImage(Utility.NullCheckJson(obj,Utility.IMAGE));

                                    // data.add(gm);

                                }
                            }

                            //  adapterCities.notifyDataSetChanged();

                            //  setDetails();

                        }

                    } catch (JSONException e)
                    {
                        e.printStackTrace();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // cancelDialog();

                    Log.e("error",error.toString());

                    error.printStackTrace();
                }
            }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String,String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json; charset=utf-8");
                    headers.put("token", SaveSharedPreference.getPrefUserToken(mActivity));
                    Log.e("Header",""+headers);

                    return headers;
                }
            };

            request.setRetryPolicy(new DefaultRetryPolicy(0,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance(mActivity).addToRequestQueue(request);

            /*{
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> headers = new HashMap<>();
                headers.put("token", SaveSharedPreference.getPrefUserToken(mActivity));
                return headers;
            }
            }*/


            /*String language = SaveSharedPreference.getAppLanguage(this);
            //Log.e("Language_Checking","---->"+language);

            String type = "english";
            if(language.equals("ta")) {
                type = "tamil";
            }else if(language.equals("en")) {
                type = "english";

            }*/

        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void getCartCount()
    {
        if(mDb!=null) {

            mDb.cartDao().loadAllPersons().observe(this, new Observer<List<CartModel>>() {
                @Override
                public void onChanged(@Nullable List<CartModel> people) {
                    //adpater.setTasks(people);

                    if(people.size()>0)
                    {
                        if(cartbadageTex!=null) {
                            cartbadageTex.setVisibility(View.VISIBLE);
                            cartbadageTex.setText(String.valueOf(people.size()));
                        }

                    }else {

                        if(cartbadageTex!=null)
                            cartbadageTex.setVisibility(View.INVISIBLE);
                    }



                }
            });

            /*mDb.cartDao().getCartViews(false).observe(this, new Observer<List<CartModel>>() {
                @Override
                public void onChanged(List<CartModel> cartModels) {

                    if (cartModels.size() > 0) {

                    }else {

                    }

                }
            });*/
        }
    }


   // private BroadcastReceiver mConnReceiver = Utility.iConnectionReceiver(mActivity);
   private BroadcastReceiver mConnReceiver = new BroadcastReceiver() {
       public void onReceive(Context context, Intent intent) {
           boolean noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
           String reason = intent.getStringExtra(ConnectivityManager.EXTRA_REASON);
           boolean isFailover = intent.getBooleanExtra(ConnectivityManager.EXTRA_IS_FAILOVER, false);

           NetworkInfo currentNetworkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
           NetworkInfo otherNetworkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_OTHER_NETWORK_INFO);

           if(currentNetworkInfo.isConnected()){
             //  Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_LONG).show();
           }else{
              // Toast.makeText(getApplicationContext(), "Not Connected", Toast.LENGTH_LONG).show();
           }
       }
   };

}
