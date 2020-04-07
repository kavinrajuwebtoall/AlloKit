package com.allocare.thenibazzar;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.allocare.thenibazzar.kitmodule.GroceryKitModule;
import com.allocare.thenibazzar.kitmodule.Grocerykit;
import com.allocare.thenibazzar.kitmodule.OrderSumActivity;
import com.allocare.thenibazzar.kitmodule.SaveSharedPreference;
import com.allocare.thenibazzar.kitmodule.SignOTPUpActivity;

public class MainActivity extends AppCompatActivity {

    Animation anim;
    ImageView imageView;

    Context mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mActivity = this;

        imageView=(ImageView)findViewById(R.id.imageView2); // Declare an imageView to show the animation.
        anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in); // Create the animation.
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {


                if(!SaveSharedPreference.getPrefUserToken(mActivity).equalsIgnoreCase(""))
                {
                   // startActivity(new Intent(MainActivity.this, Grocerykit.class));

                    if(!SaveSharedPreference.getVerifystatus(mActivity).equalsIgnoreCase("No"))
                    {
                        startActivity(new Intent(MainActivity.this, OrderSumActivity.class));
                        overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                    }else {
                        Intent  i =new Intent(mActivity,SignOTPUpActivity.class);

                        i.putExtra("type","verify");

                        startActivity(i);
                        overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                    }

                    // HomeActivity.class is the activity to go after showing the splash screen.
                    finish();

                }else {

                    Intent  i =new Intent(mActivity,SignOTPUpActivity.class);
                    i.putExtra("type","normal");

                    startActivity(i);
                    overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                    // HomeActivity.class is the activity to go after showing the splash screen.
                    finish();
                }

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        imageView.startAnimation(anim);

    }
}
