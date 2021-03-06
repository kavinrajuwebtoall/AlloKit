package com.allocare.allokit.cartmodule;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.allocare.allokit.R;
import com.allocare.allokit.address.AddressActivity;
import com.allocare.allokit.database.AppDatabase;
import com.allocare.allokit.database.AppExecutors;
import com.allocare.allokit.kitmodule.Grocerykit;
import com.allocare.allokit.kitmodule.LocationValueModel;
import com.allocare.allokit.kitmodule.MyOrderActivity;
import com.allocare.allokit.kitmodule.OrderSumActivity;
import com.allocare.allokit.kitmodule.SaveSharedPreference;
import com.allocare.allokit.kitmodule.Utility;
import com.allocare.allokit.kitmodule.VolleySingleton;
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
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartActivity extends AppCompatActivity implements View.OnClickListener {

    RecyclerView recyclerView;
    MyCartListAdapter adpater;
    private List<CartModel> lists = new ArrayList<CartModel>();


    ProgressDialog dialog ;

    Context mActivity;

    ImageView backImg;

  //  ShimmerFrameLayout shimmer;

    LinearLayout emptylay;
    private AppDatabase mDb;

    TextView totalprice;

    int cartTotalRupees = 0;
    RelativeLayout bottomLay;

    TextView change;
    String selectedpaymentType;
    final int UPI_PAYMENT=0;

    RadioGroup paymentType;
    RadioButton cash, digital;
    TextInputEditText addressText;

    LinearLayout topLay;

    String pinCode="";

    public static ArrayList<String> cities = new ArrayList<String>();

    CardView checkOutLay;


    @Override
    protected void attachBaseContext(Context newBase) {
        //  String lang_code = "en"; //load it from SharedPref
        Log.e("topLAng","-->"+ SaveSharedPreference.getAppLanguage(newBase));

        // Context context = Utility.changeLang(newBase, SaveSharedPreference.getAppLanguage(newBase));
        super.attachBaseContext(newBase);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        mActivity=this;
        mDb = AppDatabase.getInstance(getApplicationContext());


        dialog = new ProgressDialog(mActivity);
        dialog.setMessage(getResources().getString(R.string.loading));

        try {
            cities.addAll(OrderSumActivity.cities);
        }catch (Exception e) {
            e.printStackTrace();
        }
        getPINCode();

        backImg = findViewById(R.id.backImg);
        recyclerView = findViewById(R.id.recyclerView);
        //shimmer = findViewById(R.id.shimmer_view_container);
        emptylay = findViewById(R.id.emptylay);
        bottomLay = findViewById(R.id.bottomLay);

        addressText = findViewById(R.id.addressText);
        paymentType = findViewById(R.id.paymentType);

        cash = findViewById(R.id.cash);
        digital = findViewById(R.id.digital);
        change = findViewById(R.id.change);
        topLay = findViewById(R.id.topLay);


        totalprice = findViewById(R.id.totalprice);
        checkOutLay = findViewById(R.id.checkOutLay);

        //totalprice.setText(getResources().getString(R.string.rupeesString, getResources().getInteger(R.integer.some_integer)));

        pinCode = SaveSharedPreference.getUserPIN(mActivity);

        if(!SaveSharedPreference.getUserArea(mActivity).equalsIgnoreCase(""))
        {
            addressText.setText(SaveSharedPreference.getUserArea(mActivity));
        }else {

            //  Toast.makeText(mActivity, getResources().getString(R.string.pleas_fil_adress), Toast.LENGTH_SHORT).show();
        }

        adpater =  new MyCartListAdapter(lists,this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adpater);



      //  showDialog();
       // getProducts();


        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CartActivity.super.onBackPressed();
            }
        });




        change.setOnClickListener(this);
        checkOutLay.setOnClickListener(this);

        /*
        //SWIPE DELETE
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            // Called when a user swipes left or right on a ViewHolder
            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {
                // Here is where you'll implement swipe to delete
                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        int position = viewHolder.getAdapterPosition();
                        List<Person> tasks = mAdapter.getTasks();
                        mDb.personDao().delete(tasks.get(position));

                    }
                });
            }
        }).attachToRecyclerView(mRecyclerView);*/

        retrieveTasks();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.change:
                Intent s =new Intent(mActivity, AddressActivity.class);
                startActivityForResult(s,23);
                break;

            case R.id.checkOutLay:
                if(cartTotalRupees!=0)
                {

                    if(!TextUtils.isEmpty(addressText.getText().toString().trim()))
                {


                    if(isValidPincode(pinCode)) {

                        int radioButtonID = paymentType.getCheckedRadioButtonId();
                        View radioButton = paymentType.findViewById(radioButtonID);
                        int idx = paymentType.indexOfChild(radioButton);
                        RadioButton r = (RadioButton) paymentType.getChildAt(idx);
                        selectedpaymentType = r.getText().toString();

                        if (selectedpaymentType.equalsIgnoreCase(getResources().getString(R.string.cash))) {
                            showDialog();

                            Log.e("selectedQuantity","-->"+getSelectedQuantity());
                            Log.e("selectedPrice","-->"+getSelectedPrice());
                            Log.e("selectedId","-->"+getSelectedId());
                              OrderProducts(addressText.getText().toString().trim(), getSelectedId(),getSelectedQuantity(),getSelectedPrice(),String.valueOf(cartTotalRupees));
                        } else if (selectedpaymentType.equalsIgnoreCase(getResources().getString(R.string.digital))) {
                            int total = cartTotalRupees;
                            // Log.e("Total Amount","------->"+total);
                            payUsingPay(String.valueOf(total), SaveSharedPreference.getUPIID(mActivity), "Admin", "AlloKit");
                        }
                    }else {
                        Toast.makeText(mActivity, getResources().getString(R.string.enter_valid_pincode), Toast.LENGTH_SHORT).show();

                    }



                }else {
                    Toast.makeText(mActivity, getResources().getString(R.string.enter_address), Toast.LENGTH_SHORT).show();
                }

                }else {
                    Toast.makeText(mActivity, getResources().getString(R.string.selct_item), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public class MyCartListAdapter extends RecyclerView.Adapter {

        public static final int TYPE_MENUS = 0, TYPE_POST = 1, VIEW_TYPE_LOADING = 2 , TYPE_DASHBOARD = 3;

        public static final int cutsomViewcount= 0;
        Context mContext;
        private List<CartModel> post = new ArrayList<CartModel>();


        public MyCartListAdapter(List<CartModel> data, Context context) {
            this.post = data;
            this.mContext = context;
            // viewPool = new RecyclerView.RecycledViewPool();
        }

        public void setTasks(List<CartModel> personList) {
            this.post = personList;
            notifyDataSetChanged();
        }

        public List<CartModel> getTasks() {
            return this.post;
        }



        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view;
            switch (viewType) {

                case TYPE_POST:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mycart_list, parent, false);
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


                            CartModel tempdata = this.post.get(listPosition - cutsomViewcount);

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

                                    CartModel tempdata = this.post.get(position - cutsomViewcount);

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


            ImageView avatharImg,increase, decrease;

            TextView date, title, quantity,totalprice,status, orderid, quantityText;
            CardView cancel,removeLay;

            public int quantityValue=1;
            public int pricetotal=1000;

            public List<CartModel.Prices> priceList = new ArrayList<CartModel.Prices>();

            CheckBox checkbox;


            public PostTypeViewHolder(View itemView) {
                super(itemView);


                quantity = itemView.findViewById(R.id.quantity);
                quantityText = itemView.findViewById(R.id.quantityText);
                increase = itemView.findViewById(R.id.increase);
                decrease = itemView.findViewById(R.id.decrease);
                checkbox = itemView.findViewById(R.id.checkbox);
                removeLay = itemView.findViewById(R.id.removeLay);

             //   date = itemView.findViewById(R.id.date);
                title = itemView.findViewById(R.id.title);
                totalprice = itemView.findViewById(R.id.totalprice);
               // status = itemView.findViewById(R.id.status);
                avatharImg = itemView.findViewById(R.id.avatharImg);
              //  orderid = itemView.findViewById(R.id.orderid);
             //   cancel = itemView.findViewById(R.id.buyLay);



                //articlecontent.setMovementMethod(LinkMovementMethod.getInstance());
                // articleheading.setMovementMethod(LinkMovementMethod.getInstance());

                ViewGroup.LayoutParams param = itemView.getLayoutParams();
                param.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                itemView.setLayoutParams(param);

                itemView.requestLayout();
                //   articlecontent.setVisibility(View.GONE);


            }

            private void incrementValue(CartModel tempdata) {

                quantityValue++;

                quantity.setText(quantityValue +" x " +tempdata.getProductName());

                quantityText.setText(String.valueOf(quantityValue));

                pricetotal = getQuatityPrice(tempdata);

                int total = quantityValue * pricetotal;

                totalprice.setText(getResources().getString(R.string.rupeesString, total));

                tempdata.setQuantity(String.valueOf(quantityValue));
                tempdata.setTotalprice(String.valueOf(total));

                if(tempdata.isSelected()) {
                    calculatePrice();

                }
            }

            private void decrementValue(CartModel tempdata)
            {
                if(quantityValue>1) {
                    quantityValue--;


                    quantity.setText(quantityValue +" x " +tempdata.getProductName());

                    quantityText.setText(String.valueOf(quantityValue));

                    pricetotal = getQuatityPrice(tempdata);

                    int total = quantityValue * pricetotal;

                    totalprice.setText(getResources().getString(R.string.rupeesString, total));

                    tempdata.setQuantity(String.valueOf(quantityValue));
                    tempdata.setTotalprice(String.valueOf(total));


                    if(tempdata.isSelected()) {
                        calculatePrice();

                    }

                }

            }


            private int getQuatityPrice(CartModel tempdata)
            {
                int p=Integer.parseInt(tempdata.getPrice());

                for(int i = 0 ;i<priceList.size();i++)
                {
                    CartModel.Prices cpdata = priceList.get(i);

                    Log.e("above","-->"+cpdata.getAbove());
                    Log.e("below","-->"+cpdata.getBelow());
                    Log.e("price","-->"+cpdata.getPrice());


                    if(quantityValue >= cpdata.getAbove() && quantityValue < cpdata.getBelow()) {
                         p = cpdata.getPrice();
                         break;
                    }
                }


                return p;
            }


            void firstBind(final CartModel tempdata, int listPosition) {


                try {
                    JSONObject pricelis = new JSONObject(tempdata.getPricelists());

                    Log.e("priceData","-->"+pricelis);

                    JSONArray data  = pricelis.getJSONArray("preice");
                    if(data.length()>0)
                    {
                        for (int i=0;i<data.length();i++)
                        {
                            JSONObject re = data.getJSONObject(i);


                            CartModel.Prices cp = new CartModel.Prices();

                            cp.setAbove(re.getInt("above"));
                            cp.setBelow(re.getInt("below"));
                            cp.setPrice(re.getInt("price"));
                            priceList.add(cp);

                        }

                       // if(priceList.size()>0)

                        Log.e("PricelistSize","-->"+priceList.size());


                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }


                try{
                    quantityValue = Integer.parseInt(tempdata.getQuantity().trim());
                    pricetotal = Integer.parseInt(tempdata.getTotalprice().trim());
                }catch (Exception e)
                {

                }
                quantityText.setText(tempdata.getQuantity().trim());

                totalprice.setText(getResources().getString(R.string.rupeesString, pricetotal));


                checkbox.setChecked(true);

                quantity.setText(tempdata.getQuantity() +" x " +tempdata.getProductName());
                //date.setText(tempdata.getDate());
               // status.setText(tempdata.getOrderStatus());
                title.setText(tempdata.getProductName());

              //  orderid.setText(tempdata.getOrderid());

                increase.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        incrementValue(tempdata);
                    }
                });

                decrease.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        decrementValue(tempdata);
                    }
                });

                removeLay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        removeClicked(listPosition);
                    }
                });

                if(!tempdata.getImage().equalsIgnoreCase("")) {

                    try {
                        Glide.with(avatharImg.getContext()).asBitmap() // Bind it with the context of the actual view used
                                .load(tempdata.getImage())
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


                checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                        tempdata.setSelected(isChecked);

                        if(isChecked)
                        {
                           itemSelected(listPosition);
                        }else {
                            itemUnSelected(listPosition);
                        }

                    }
                });



            }
        }







    }

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


    private void retrieveTasks() {

        mDb.cartDao().loadAllPersons().observe(this, new Observer<List<CartModel>>() {
            @Override
            public void onChanged(@Nullable List<CartModel> people) {

                adpater.setTasks(people);

               // calculatePrice();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(people.size()>0) {
                            calculatePrice();
                            CancelEmpty();
                        }else {
                            ShowEmpty();
                        }
                    }
                });



            }
        });
    }


    public void itemSelected(int pos) {
        //calculatePrice(pos);

        calculatePrice();

    }

    public void itemUnSelected(int pos)
    {
        calculatePrice();

    }

    public void removeClicked(int position)
    {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                //int position = viewHolder.getAdapterPosition();
                List<CartModel> tasks = adpater.getTasks();




                mDb.cartDao().delete(tasks.get(position));

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(tasks.size()>0) {
                            calculatePrice();
                            CancelEmpty();
                        }else {
                            ShowEmpty();
                        }

                        Toast.makeText(mActivity, getResources().getString(R.string.item_removed), Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });
    }

    public void removeCart(String id)
    {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                //int position = viewHolder.getAdapterPosition();
                List<CartModel> tasks = adpater.getTasks();



                for (int i= 0;i<tasks.size();i++) {


                    if(tasks.get(i).getId().equalsIgnoreCase(id)) {
                        mDb.cartDao().delete(tasks.get(i));
                    }

                }




            }
        });
    }

    private void calculatePrice() {
        if (adpater != null) {
            List<CartModel> tasks = adpater.getTasks();

            int totalamount = 0;

            for (int i = 0; i < tasks.size(); i++) {

                if(tasks.get(i).isSelected()) {
                    try {
                        totalamount += Integer.parseInt(tasks.get(i).getTotalprice());
                    } catch (Exception e) {

                    }
                }
            }

            cartTotalRupees = totalamount;

           // totalamount = cartTotalRupees;

            if (totalprice != null)
                totalprice.setText(getResources().getString(R.string.rupeesString, totalamount));

        }else {
            Toast.makeText(mActivity, "nulladapter", Toast.LENGTH_SHORT).show();
        }
    }

    private String getSelectedQuantity() {
        if (adpater != null) {
            List<CartModel> tasks = adpater.getTasks();

            String  quantity ="";

            for (int i = 0; i < tasks.size(); i++) {

                if(tasks.get(i).isSelected()) {
                    try {
                        quantity = quantity + tasks.get(i).getQuantity() + "#";
                    } catch (Exception e) {

                    }
                }
            }

            if(!quantity.equalsIgnoreCase("")) {
                quantity = quantity.substring(0,quantity.length()-1);
            }


            return quantity;
           // cartTotalRupees = totalamount;

            // totalamount = cartTotalRupees;

           // if (totalprice != null)
             //   totalprice.setText(getResources().getString(R.string.rupeesString, totalamount));

        }else {
            Toast.makeText(mActivity, "nulladapter", Toast.LENGTH_SHORT).show();

            return "";

        }
    }

    private String getSelectedPrice() {
        if (adpater != null) {
            List<CartModel> tasks = adpater.getTasks();

            String  quantity ="";

            for (int i = 0; i < tasks.size(); i++) {

                if(tasks.get(i).isSelected()) {
                    try {
                        quantity = quantity + tasks.get(i).getTotalprice() + "#";
                    } catch (Exception e) {

                    }
                }
            }

            if(!quantity.equalsIgnoreCase("")) {
                quantity = quantity.substring(0,quantity.length()-1);
            }


            return quantity;
            // cartTotalRupees = totalamount;

            // totalamount = cartTotalRupees;

            // if (totalprice != null)
            //   totalprice.setText(getResources().getString(R.string.rupeesString, totalamount));

        }else {
            Toast.makeText(mActivity, "nulladapter", Toast.LENGTH_SHORT).show();

            return "";

        }
    }


    private String getSelectedId() {
        if (adpater != null) {
            List<CartModel> tasks = adpater.getTasks();

            String  quantity ="";

            for (int i = 0; i < tasks.size(); i++) {

                if(tasks.get(i).isSelected()) {
                    try {
                        quantity = quantity + tasks.get(i).getId() + "#";
                    } catch (Exception e) {

                    }
                }
            }

            if(!quantity.equalsIgnoreCase("")) {
                quantity = quantity.substring(0,quantity.length()-1);
            }


            return quantity;
            // cartTotalRupees = totalamount;

            // totalamount = cartTotalRupees;

            // if (totalprice != null)
            //   totalprice.setText(getResources().getString(R.string.rupeesString, totalamount));

        }else {
            Toast.makeText(mActivity, "nulladapter", Toast.LENGTH_SHORT).show();

            return "";

        }
    }


    /*private void calculatePrice(int pos) {
        if (adpater != null) {
            List<CartModel> tasks = adpater.getTasks();

            int totalamount = 0;

            for (int i = 0; i < tasks.size(); i++) {

                if(i!=pos) {
                    try {
                        totalamount += Integer.parseInt(tasks.get(i).getTotalprice());
                    } catch (Exception e) {

                    }
                }
            }


            if (totalprice != null)
                totalprice.setText(getResources().getString(R.string.rupeesString, totalamount));

        }
    }*/

    public void ShowEmpty()
    {
        if(emptylay!=null)
        {
            if(emptylay.getVisibility() !=View.VISIBLE) {


                emptylay.setVisibility(View.VISIBLE);
            }
        }



        if(bottomLay != null) {
            if(bottomLay.getVisibility() == View.VISIBLE) {
                bottomLay.setVisibility(View.GONE);
            }
        }

        if(topLay !=null)
        {
            if(topLay.getVisibility() == View.VISIBLE) {
                topLay.setVisibility(View.GONE);
            }
        }


    }

    public void CancelEmpty()
    {
        if(emptylay!=null) {
            if(emptylay.getVisibility() == View.VISIBLE) {
                emptylay.setVisibility(View.GONE);
            }
        }

        if(bottomLay != null) {
            if(bottomLay.getVisibility() != View.VISIBLE) {
                bottomLay.setVisibility(View.VISIBLE);
            }
        }

        if(topLay !=null)
        {
            if(topLay.getVisibility() != View.VISIBLE) {
                topLay.setVisibility(View.VISIBLE);
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Activity.RESULT_OK)
        {
            switch (requestCode)
            {
                case 23:

                    if(data!=null)
                    {
                        addressText.setText(data.getStringExtra("addres"));
                        pinCode = data.getStringExtra("pincode");
                    }



                    break;
                case UPI_PAYMENT:
                    if (data!=null){
                        String text = data.getStringExtra("response");
                        ArrayList<String> dataList = new ArrayList<>();
                        dataList.add(text);
                        upiPaymentDataOperation(dataList);
                    } else {
                        ArrayList<String> dataList = new ArrayList<>();
                        dataList.add("nothing");
                        upiPaymentDataOperation(dataList);
                    }
                    break;
            }

        }

    }
    private void payUsingPay(String amount_str, String upi_str, String name_str, String note_str) {
        Uri uri = Uri.parse("upi://pay").buildUpon()
                .appendQueryParameter("pa",upi_str)
                .appendQueryParameter("pn",name_str)
                .appendQueryParameter("tn",note_str)
                .appendQueryParameter("am",amount_str)
                .appendQueryParameter("cu","INR")
                .build();
        Intent upiPayIntent = new Intent(Intent.ACTION_VIEW);
        upiPayIntent.setData(uri);
        Intent chooser = Intent.createChooser(upiPayIntent,"Pay with");

        if (null != chooser.resolveActivity(getPackageManager())){
            startActivityForResult(chooser,UPI_PAYMENT);
        }else {
            Toast.makeText(this,"No UPI app found,please install one to continue",Toast.LENGTH_SHORT).show();
        }
    }


    private void upiPaymentDataOperation(ArrayList<String> data) {

        String str = data.get(0);
        String paymentCancel = "";
        if (str == null) str = "discard";
        String status = "";
        String approvalRefNo ="";
        String response[] = str.split("&");
        for (int i=0;i<response.length;i++){
            String equalStr[] = response[i].split("=");
            if (equalStr.length>=2){
                if (equalStr[0].toLowerCase().equals("Status".toLowerCase())){
                    status = equalStr[1].toLowerCase();
                }else if (equalStr[0].toLowerCase().equals("ApprovalRefNo".toLowerCase())|| equalStr[0].toLowerCase().equals("tnxRef".toLowerCase())){
                    approvalRefNo = equalStr[1];
                }
            } else {
                paymentCancel = "Payment cancelled by User.";
            }
        }
        if (status.equals("success")){
            Toast.makeText(mActivity,"Transaction  Successful.",Toast.LENGTH_SHORT).show();
            showDialog();

            Log.e("selectedQuantity","-->"+getSelectedQuantity());
            Log.e("selectedPrice","-->"+getSelectedPrice());
            Log.e("selectedId","-->"+getSelectedId());
            OrderProducts(addressText.getText().toString().trim(), getSelectedId(),getSelectedQuantity(),getSelectedPrice(),String.valueOf(cartTotalRupees));
        }else if ("Payment cancelled by User.".equals(paymentCancel)){
            Toast.makeText(mActivity,"Payment cancelled by User.",Toast.LENGTH_SHORT).show();
            finish();
        }else {
            Toast.makeText(mActivity,"Transaction failed.Please try again",Toast.LENGTH_SHORT).show();
            finish();
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

    private boolean isValidPincode(String pincode)
    {
        if(cities.size()>0)
        {
            for(String na: cities)
            {
                if(na.equalsIgnoreCase(pincode))
                    return true;
            }
            return false;

        }else {
            return false;
        }
    }

    public void OrderProducts(String address,String id,String quantity,String price,String totalprice) {
        try{
            String language = SaveSharedPreference.getAppLanguage(mActivity);
            //Log.e("Language_Checking","---->"+language);

            String type = "english";
            if(language.equals("ta")) {
                type = "tamil";
            }else if(language.equals("en")) {
                type = "english";

            }

            /*{
                "productid":"1",
                    "registerid":"4",
                    "userid":"2",
                    "qty":"1",
                    "paytype":"Digital",
                    "location":"testing",
                    "address":"testing"
            }*/



            String link = "http://www.google.com/maps/place/" + String.valueOf(LocationValueModel.getmLatitude()) + "," + String.valueOf(LocationValueModel.getmLongitude());

            JSONObject map =new JSONObject();
            map.put("products",id);
            map.put("registerid",SaveSharedPreference.getPrefUserRegisterId(mActivity));
            map.put("userid",SaveSharedPreference.getHostFor(mActivity));
            map.put("quantity", quantity);
            map.put("paytype",selectedpaymentType);
            map.put("location",link);
            map.put("address",address);
            map.put("postcode",pinCode);
            map.put("price",price);
            map.put("totprice",totalprice);

           /* {
                "registerid":"4",
                    "userid":"2",
                    "paytype":"Digital",
                    "location":"testing",
                    "address":"testing",
                    "postcode":"625012",
                    "products":"7#6",
                    "quantity":"1#2",
                    "price":"50#150"
            }*/


            Log.e("Params","-->"+map);
            //String url = SaveSharedPreference.getBaseURL(mActivity)+Utility.APINAME_PRODUCT+"/"+type+"/"+SaveSharedPreference.getHostFor(mActivity)+"/1/5";
            String url = SaveSharedPreference.getBaseURL(mActivity)+Utility.APINAME_PLACEORDER;


            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, map, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {

                    Log.e("Respoonce","-->"+response);

                    cancelDialog();
                    try {
                        if(response.getString(Utility.API_RESPONCE_SUCCESS).equalsIgnoreCase("true"))
                        {

                            if(!id.equalsIgnoreCase(""))
                            {
                                if(id.contains("#"))
                                {
                                    String[] ids = id.split("#");
                                    if(ids.length>0)
                                    {
                                        for (int v=0;v<ids.length;v++) {
                                            removeCart(ids[v]);
                                        }
                                    }

                                }else {
                                    removeCart(id);
                                }

                            }




                            Toast.makeText(mActivity, getResources().getString(R.string.your_order_sucessfull), Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(mActivity, MyOrderActivity.class);
                            startActivity(intent);
                            // overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                           // mDb.cartDao().





                                   /* AppExecutors.getInstance().diskIO().execute(new Runnable() {
                                @Override
                                public void run() {
                                    mDb.cartDao().nullTable();
                                }
                            });*/

                            finish();


                        }else {
                            Toast.makeText(mActivity, response.getString("message"), Toast.LENGTH_SHORT).show();

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

                    error.printStackTrace();

                    Log.d("TAG", "Error: " + error
                            + "\nStatus Code " + error.networkResponse.statusCode
                            + "\nResponse Data " + error.networkResponse.data
                            + "\nCause " + error.getCause()
                            + "\nmessage" + error.getMessage());
                }
            }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String,String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("token", SaveSharedPreference.getPrefUserToken(mActivity));
                    Log.e("Header",""+headers);

                    return headers;

                }
                @Override
                public String getBodyContentType() {
                    return "application/json";
                }


            };

            request.setRetryPolicy(new DefaultRetryPolicy(0,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance(mActivity).addToRequestQueue(request);




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


}
