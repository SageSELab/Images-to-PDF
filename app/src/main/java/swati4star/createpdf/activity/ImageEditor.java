package swati4star.createpdf.activity;

import static swati4star.createpdf.util.BrushUtils.getBrushItems;
import static swati4star.createpdf.util.Constants.IMAGE_EDITOR_KEY;
import static swati4star.createpdf.util.Constants.RESULT;
import static swati4star.createpdf.util.ImageFilterUtils.getFiltersList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

import ja.burhanrashid52.photoeditor.PhotoEditor;
import ja.burhanrashid52.photoeditor.PhotoFilter;
import swati4star.createpdf.R;
import swati4star.createpdf.adapter.BrushItemAdapter;
import swati4star.createpdf.adapter.ImageFiltersAdapter;
import swati4star.createpdf.databinding.ActivityPhotoEditorBinding;
import swati4star.createpdf.interfaces.OnFilterItemClickedListener;
import swati4star.createpdf.interfaces.OnItemClickListner;
import swati4star.createpdf.model.BrushItem;
import swati4star.createpdf.model.FilterItem;

public class ImageEditor extends AppCompatActivity implements OnFilterItemClickedListener, OnItemClickListner {

    private ArrayList<String> mFilterUris = new ArrayList<>();
    private final ArrayList<String> mImagepaths = new ArrayList<>();
    private ArrayList<FilterItem> mFilterItems;
    private ArrayList<BrushItem> mBrushItems;

    private int mImagesCount;
    private int mDisplaySize;
    private int mCurrentImage = 0;
    private String mFilterName;

    private ActivityPhotoEditorBinding mBinding;

    private boolean mClicked = false;
    private boolean mClickedFilter = false;
    private boolean mDoodleSelected = false;

    private PhotoEditor mPhotoEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityPhotoEditorBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        // Extract images
        mFilterUris = getIntent().getExtras().getStringArrayList(IMAGE_EDITOR_KEY);
        mDisplaySize = mFilterUris.size();
        mImagesCount = mFilterUris.size() - 1;
        mBinding.photoEditorView.getSource()
                .setImageBitmap(BitmapFactory.decodeFile(mFilterUris.get(0)));
        setImageCount();
        if (mDisplaySize == 1) {
            mBinding.nextimageButton.setVisibility(View.INVISIBLE);
        }
        mFilterItems = getFiltersList(this);
        mBrushItems = getBrushItems();
        mImagepaths.addAll(mFilterUris);
        initRecyclerView();

        mPhotoEditor = new PhotoEditor.Builder(this, mBinding.photoEditorView)
                .setPinchTextScalable(true)
                .build();
        mPhotoEditor.setBrushSize(30);
        mBinding.doodleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mPhotoEditor.setBrushSize(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        mPhotoEditor.setBrushDrawingMode(false);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        mBinding.nextimageButton.setOnClickListener(v -> nextImg());
        mBinding.previousImageButton.setOnClickListener(v -> previousImg());
        mBinding.savecurrent.setOnClickListener(v -> saveC());
        mBinding.resetCurrent.setOnClickListener(v -> resetCurrent());
    }

    private void nextImg() {
        //Proceed to next if Save Current has been clicked
        if (mClicked == mClickedFilter) {
            next();
            incrementImageCount();
            mClicked = false;
            mClickedFilter = false;
        } else
            Toast.makeText(getApplicationContext(), R.string.save_first, Toast.LENGTH_SHORT).show();
    }

    private void previousImg() {
        //move to previous if Save Current has been clicked
        if (mClicked == mClickedFilter) {
            previous();
            decrementImageCount();
            mClicked = false;
            mClickedFilter = false;
        } else
            Toast.makeText(getApplicationContext(), R.string.save_first, Toast.LENGTH_SHORT).show();
    }

    private void saveC() {
        mClicked = true;
        if (mClickedFilter || mDoodleSelected) {
            saveCurrentImage();
        } else {
            applyFilter(PhotoFilter.NONE);
            saveCurrentImage();
        }
    }

    private void resetCurrent() {
        String originalPath = mFilterUris.get(mCurrentImage);
        mImagepaths.set(mCurrentImage, originalPath);
        mBinding.photoEditorView.getSource()
                .setImageBitmap(BitmapFactory.decodeFile(originalPath));
        mPhotoEditor.clearAllViews();
        mPhotoEditor.undo();
    }

    /**
     * Increment image count to display in textView
     */
    private void incrementImageCount() {
        if (mCurrentImage < mImagesCount) {
            setImageCount();
            mBinding.previousImageButton.setVisibility(View.VISIBLE);
        } else if (mCurrentImage == mImagesCount) {
            setImageCount();
            mBinding.nextimageButton.setVisibility(View.INVISIBLE);
            mBinding.previousImageButton.setVisibility(View.VISIBLE);
        } else {
            mBinding.nextimageButton.setEnabled(false);
        }
    }

    /**
     * Decrement image count to display in textView
     */
    private void decrementImageCount() {
        if (mCurrentImage > 0) {
            setImageCount();
            mBinding.nextimageButton.setVisibility(View.VISIBLE);
        } else if (mCurrentImage == 0) {
            setImageCount();
            mBinding.previousImageButton.setVisibility(View.INVISIBLE);
            mBinding.nextimageButton.setVisibility(View.VISIBLE);
        } else {
            mBinding.previousImageButton.setEnabled(false);
        }
    }

    private void setImageCount() {
        String sText = "Showing " + String.valueOf(mCurrentImage + 1) + " of " + mDisplaySize;
        mBinding.imagecount.setText(sText);
    }

    /**
     * Saves Current Image with applied filter
     */
    private void saveCurrentImage() {
        try {
            File sdCard = Environment.getExternalStorageDirectory();
            File dir = new File(sdCard.getAbsolutePath() + "/PDFfilter");
            dir.mkdirs();
            String fileName = String.format(getString(R.string.filter_file_name),
                    String.valueOf(System.currentTimeMillis()), mFilterName);
            File outFile = new File(dir, fileName);
            String imagePath = outFile.getAbsolutePath();

            mPhotoEditor.saveAsFile(imagePath, new PhotoEditor.OnSaveListener() {
                @Override
                public void onSuccess(@NonNull String imagePath) {
                    mImagepaths.remove(mCurrentImage);
                    mImagepaths.add(mCurrentImage, imagePath);
                    mBinding.photoEditorView.getSource()
                            .setImageBitmap(BitmapFactory.decodeFile(mImagepaths.get(mCurrentImage)));
                    Toast.makeText(getApplicationContext(), R.string.filter_saved, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(getApplicationContext(), R.string.filter_not_saved, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }


    /**
     * Intent to Send Back final edited URIs
     *
     * @param mImagepaths - the images array to be send pack
     */
    private void passUris(ArrayList<String> mImagepaths) {
        Intent returnIntent = new Intent();
        returnIntent.putStringArrayListExtra(RESULT, mImagepaths);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    /**
     * Display next image on nextImage button click
     */
    private void next() {
        try {
            if (mCurrentImage + 1 <= mImagesCount) {
                mBinding.photoEditorView.getSource()
                        .setImageBitmap(BitmapFactory.decodeFile(mImagepaths.get(mCurrentImage + 1)));
                mCurrentImage++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Display Previous image on previousImage button click
     */
    private void previous() {
        try {
            if (mCurrentImage - 1 >= 0) {
                mBinding.photoEditorView.getSource()
                        .setImageBitmap(BitmapFactory.decodeFile(mImagepaths.get((mCurrentImage - 1))));
                mCurrentImage--;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Initialize Recycler View
     */
    private void initRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(layoutManager);
        ImageFiltersAdapter adapter = new ImageFiltersAdapter(mFilterItems, this, this);
        recyclerView.setAdapter(adapter);

        LinearLayoutManager layoutManager2 = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mBinding.doodleColors.setLayoutManager(layoutManager2);
        BrushItemAdapter brushItemAdapter = new BrushItemAdapter(this,
                this, mBrushItems);
        mBinding.doodleColors.setAdapter(brushItemAdapter);
    }

    /**
     * Get Item Position and call Filter Function
     *
     * @param view     - view which is clicked
     * @param position - position of item clicked
     */
    @Override
    public void onItemClick(View view, int position) {
        // Brush effect is in second position
        if (position == 1) {
            mPhotoEditor = new PhotoEditor.Builder(this, mBinding.photoEditorView)
                    .setPinchTextScalable(true)
                    .build();
            if (mBinding.doodleSeekBar.getVisibility() == View.GONE
                    && mBinding.doodleColors.getVisibility() == View.GONE) {
                mPhotoEditor.setBrushDrawingMode(true);
                mBinding.doodleSeekBar.setVisibility(View.VISIBLE);
                mBinding.doodleColors.setVisibility(View.VISIBLE);
                mDoodleSelected = true;
            } else if (mBinding.doodleSeekBar.getVisibility() == View.VISIBLE &&
                    mBinding.doodleColors.getVisibility() == View.VISIBLE) {
                mPhotoEditor.setBrushDrawingMode(false);
                mBinding.doodleSeekBar.setVisibility(View.GONE);
                mBinding.doodleColors.setVisibility(View.GONE);
            }
        } else {
            PhotoFilter filter = mFilterItems.get(position).getFilter();
            applyFilter(filter);
        }
    }

    /**
     * Apply Filter to Image
     */
    private void applyFilter(PhotoFilter filterType) {
        try {
            mPhotoEditor = new PhotoEditor.Builder(this, mBinding.photoEditorView)
                    .setPinchTextScalable(true)
                    .build();
            mPhotoEditor.setFilterEffect(filterType);
            mFilterName = filterType.name();
            mClickedFilter = filterType != PhotoFilter.NONE;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.activity_filter_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.finish) {
            passUris(mImagepaths);
            return true;
        } else if (itemId == android.R.id.home) {
            new MaterialDialog.Builder(this)
                    .onPositive((dialog, which) -> finish())
                    .title(R.string.filter_cancel_question)
                    .content(R.string.filter_cancel_description)
                    .positiveText(R.string.ok)
                    .negativeText(R.string.cancel).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        passUris(mImagepaths);
    }

    @Override
    public void onItemClick(int position) {
        int color = mBrushItems.get(position).getColor();
        mBinding.doodleSeekBar.setBackgroundColor(getResources().getColor(color));
        mPhotoEditor.setBrushColor(getResources().getColor(color));
    }

}