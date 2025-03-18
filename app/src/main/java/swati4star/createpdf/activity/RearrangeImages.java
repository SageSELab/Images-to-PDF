package swati4star.createpdf.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.Objects;


import swati4star.createpdf.R;
import swati4star.createpdf.adapter.RearrangeImagesAdapter;
import swati4star.createpdf.databinding.ActivityRearrangeImagesBinding;
import swati4star.createpdf.util.Constants;

import static swati4star.createpdf.util.Constants.PREVIEW_IMAGES;

public class RearrangeImages extends AppCompatActivity implements RearrangeImagesAdapter.OnClickListener {

    ActivityRearrangeImagesBinding binding;
    RecyclerView mRecyclerView;

    private ArrayList<String> mImages;
    private RearrangeImagesAdapter mRearrangeImagesAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRearrangeImagesBinding.inflate(LayoutInflater.from(this));
        View view = binding.getRoot();
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        setContentView(view);
        mRecyclerView = binding.recyclerView;
        Intent intent = getIntent();
        mImages = intent.getStringArrayListExtra(PREVIEW_IMAGES);
        initRecyclerView(mImages);
    }

    private void initRecyclerView(ArrayList<String> images) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(layoutManager);
        mRearrangeImagesAdapter = new RearrangeImagesAdapter(this, images, this);
        recyclerView.setAdapter(mRearrangeImagesAdapter);
    }

    @Override
    public void onUpClick(int position) {
        mImages.add(position - 1, mImages.remove(position));
        mRearrangeImagesAdapter.positionChanged(mImages);
    }

    @Override
    public void onDownClick(int position) {
        mImages.add(position + 1, mImages.remove(position));
        mRearrangeImagesAdapter.positionChanged(mImages);

    }

    private void passUris() {
        Intent returnIntent = new Intent();
        returnIntent.putStringArrayListExtra(Constants.RESULT, mImages);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        passUris();
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                passUris();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

