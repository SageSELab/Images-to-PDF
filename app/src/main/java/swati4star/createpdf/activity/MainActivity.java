package swati4star.createpdf.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;

import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;

import io.github.tonnyl.whatsnew.WhatsNew;
import io.github.tonnyl.whatsnew.item.WhatsNewItem;
import swati4star.createpdf.R;
import swati4star.createpdf.fragment.AboutUsFragment;
import swati4star.createpdf.fragment.ExtractImagesFragment;
import swati4star.createpdf.fragment.HistoryFragment;
import swati4star.createpdf.fragment.ImageToPdfFragment;
import swati4star.createpdf.fragment.MergeFilesFragment;
import swati4star.createpdf.fragment.TextToPdfFragment;
import swati4star.createpdf.fragment.ViewFilesFragment;
import swati4star.createpdf.util.FeedbackUtils;

import static swati4star.createpdf.util.Constants.WHATS_NEW1_TEXT;
import static swati4star.createpdf.util.Constants.WHATS_NEW1_TITLE;
import static swati4star.createpdf.util.Constants.WHATS_NEW2_TEXT;
import static swati4star.createpdf.util.Constants.WHATS_NEW2_TITLE;
import static swati4star.createpdf.util.Constants.WHATS_NEW3_TEXT;
import static swati4star.createpdf.util.Constants.WHATS_NEW3_TITLE;
import static swati4star.createpdf.util.Constants.WHATS_NEW4_TEXT;
import static swati4star.createpdf.util.Constants.WHATS_NEW4_TITLE;
import static swati4star.createpdf.util.Constants.WHATS_NEW5_TEXT;
import static swati4star.createpdf.util.Constants.WHATS_NEW5_TITLE;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FeedbackUtils mFeedbackUtils;
    private NavigationView mNavigationView;

    private boolean mDoubleBackToExitPressedOnce = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        setSupportActionBar(toolbar);

        // Set navigation drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        //Replaced setDrawerListener with addDrawerListener because it was deprecated.
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // To show what's new in our application
        //setWhatsNew();

        // Set ImageToPdfFragment fragment
        Fragment fragment = new ImageToPdfFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content, fragment).commit();

        // Check if  images are received
        handleReceivedImagesIntent(fragment);

        // initialize values
        initializeValues();
    }

    /**
     * Ininitializes default values
     */
    private void initializeValues() {
        mFeedbackUtils = new FeedbackUtils(this);
        mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);
        setDefaultMenuSelected(0);
    }

    /*
     * This will set default menu item selected at the position mentioned
     */
    public void setDefaultMenuSelected(int position) {
        if (mNavigationView != null && mNavigationView.getMenu() != null &&
                position < mNavigationView.getMenu().size()
                && mNavigationView.getMenu().getItem(position) != null) {
            mNavigationView.getMenu().getItem(position).setChecked(true);
        }
    }

    /**
     * To show the new features in the update
     */
    private void setWhatsNew() {
        WhatsNew whatsNew = WhatsNew.newInstance(
                new WhatsNewItem(WHATS_NEW1_TITLE, WHATS_NEW1_TEXT, R.drawable.baseline_done_24),
                new WhatsNewItem(WHATS_NEW2_TITLE, WHATS_NEW2_TEXT, R.drawable.baseline_done_24),
                new WhatsNewItem(WHATS_NEW3_TITLE, WHATS_NEW3_TEXT, R.drawable.baseline_done_24),
                new WhatsNewItem(WHATS_NEW4_TITLE, WHATS_NEW4_TEXT, R.drawable.baseline_done_24),
                new WhatsNewItem(WHATS_NEW5_TITLE, WHATS_NEW5_TEXT, R.drawable.baseline_done_24)
        );
        whatsNew.setButtonBackground(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        whatsNew.setButtonTextColor(ContextCompat.getColor(this, R.color.mb_white));
        whatsNew.presentAutomatically(this);
    }

    /**
     * Checks if images are received in the intent
     *
     * @param fragment - instance of current fragment
     */
    private void handleReceivedImagesIntent(Fragment fragment) {
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                handleSendMultipleImages(intent, fragment); // Handle multiple images
            }
        } else if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                handleSendImage(intent, fragment); // Handle single image
            }
        }
    }

    /**
     * Get image uri from intent and send the image to homeFragment
     *
     * @param intent   - intent containing image uris
     * @param fragment - instance of homeFragment
     */
    private void handleSendImage(Intent intent, Fragment fragment) {
        Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        ArrayList<Uri> imageUris = new ArrayList<>();
        imageUris.add(uri);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(getString(R.string.bundleKey), imageUris);
        fragment.setArguments(bundle);
    }

    /**
     * Get ArrayList of image uris from intent and send the image to homeFragment
     *
     * @param intent   - intent containing image uris
     * @param fragment - instance of homeFragment
     */
    private void handleSendMultipleImages(Intent intent, Fragment fragment) {
        ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (imageUris != null) {
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList(getString(R.string.bundleKey), imageUris);
            fragment.setArguments(bundle);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (getCurrentFragment() instanceof ImageToPdfFragment) {
                checkDoubleBackPress();
            } else {
                Fragment fragment = new ImageToPdfFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.content, fragment).commit();
                setDefaultMenuSelected(0);
            }
        }
    }

    private void checkDoubleBackPress() {

        if (mDoubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.mDoubleBackToExitPressedOnce = true;
        Toast.makeText(this, R.string.confirm_exit_message, Toast.LENGTH_SHORT).show();
    }

    Fragment getCurrentFragment() {
        return getSupportFragmentManager()
                .findFragmentById(R.id.content);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        Fragment fragment = null;
        FragmentManager fragmentManager = getSupportFragmentManager();

        if (id == R.id.nav_camera) {
            fragment = new ImageToPdfFragment();
        } else if (id == R.id.nav_gallery) {
            fragment = new ViewFilesFragment();
        } else if (id == R.id.nav_merge) {
            fragment = new MergeFilesFragment();
        } else if (id == R.id.nav_text_to_pdf) {
            fragment = new TextToPdfFragment();
        } else if (id == R.id.nav_history) {
            fragment = new HistoryFragment();
        } else if (id == R.id.nav_share) {
            mFeedbackUtils.shareApplication();
        } else if (id == R.id.nav_about) {
            fragment = new AboutUsFragment();
        } else if (id == R.id.nav_extract_images) {
            fragment = new ExtractImagesFragment();
        }

        if (fragment != null)
            fragmentManager.beginTransaction().replace(R.id.content, fragment).commit();

        return true;
    }

}