package swati4star.createpdf.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.itextpdf.text.pdf.PRStream;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfImageObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import swati4star.createpdf.R;
import swati4star.createpdf.adapter.MergeFilesAdapter;
import swati4star.createpdf.databinding.FragmentExtractImagesBinding;
import swati4star.createpdf.util.DirectoryUtils;
import swati4star.createpdf.util.FileUtils;
import swati4star.createpdf.util.MorphButtonUtility;
import swati4star.createpdf.util.ViewFilesDividerItemDecoration;

import static android.app.Activity.RESULT_OK;

public class ExtractImagesFragment extends Fragment implements MergeFilesAdapter.OnClickListener {

    private Activity mActivity;
    private String mPath;
    private MorphButtonUtility mMorphButtonUtility;
    private FileUtils mFileUtils;
    private DirectoryUtils mDirectoryUtils;
    private static final int INTENT_REQUEST_PICKFILE_CODE = 10;
    BottomSheetBehavior sheetBehavior;
    private FragmentExtractImagesBinding mBinding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentExtractImagesBinding.inflate(inflater, container, false);
        View rootview = mBinding.getRoot();
        sheetBehavior = BottomSheetBehavior.from(mBinding.bottomSheet.bottomSheet);
        sheetBehavior.setBottomSheetCallback(new ExtractImagesFragment.BottomSheetCallback());

        ArrayList<String> mAllFilesPaths = mDirectoryUtils.getAllFilePaths();
        if (mAllFilesPaths == null || mAllFilesPaths.size() == 0) {
            mBinding.bottomSheet.layout.setVisibility(View.GONE);
        }

        // Init recycler view
        MergeFilesAdapter mergeFilesAdapter = new MergeFilesAdapter(mActivity, mAllFilesPaths, this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(mActivity);
        mBinding.bottomSheet.recyclerViewFiles.setLayoutManager(mLayoutManager);
        mBinding.bottomSheet.recyclerViewFiles.setAdapter(mergeFilesAdapter);
        mBinding.bottomSheet.recyclerViewFiles.addItemDecoration(new ViewFilesDividerItemDecoration(mActivity));

        mBinding.bottomSheet.viewFiles.setOnClickListener(this::onViewFilesClick);
        mBinding.selectFile.setOnClickListener(v -> showFileChooser());
        mBinding.extractImages.setOnClickListener(v -> parse());

        return rootview;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }

    private void onViewFilesClick(View view) {
        if (sheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
            sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        } else {
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    }

    /**
     * Displays file chooser intent
     */
    private void showFileChooser() {
        String folderPath = Environment.getExternalStorageDirectory() + "/";
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        Uri myUri = Uri.parse(folderPath);
        intent.setDataAndType(myUri, getString(R.string.pdf_type));
        Intent intentChooser = Intent.createChooser(intent, getString(R.string.merge_file_select));
        startActivityForResult(intentChooser, INTENT_REQUEST_PICKFILE_CODE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) throws NullPointerException {
        if (data == null || resultCode != RESULT_OK || data.getData() == null)
            return;
        if (requestCode == INTENT_REQUEST_PICKFILE_CODE) {
            setTextAndActivateButtons(getFilePath(data.getData()));
        }
    }

    //Returns the complete filepath of the PDF as a string
    private String getFilePath(Uri uri) {
        String uriString = uri.toString();
        File file = new File(uri.toString());
        String path = file.getPath();
        String returnPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        Boolean success;
        String name = null;
        if (uriString.startsWith("content://") && uriString.contains("com.google.android.")) {
            success = false;
        } else {
            success = true;
            name = mFileUtils.getFileName(uri);
        }
        if (success) {
            String folname = mDirectoryUtils.getParentFolder(path);
            if (folname != null) {
                String c = getString(R.string.path_seperator);
                returnPath = returnPath + c + folname + c + name;
            }
        }
        return returnPath;
    }

    private void parse() {
        PdfReader reader = null;
        int imagesCount = 0;
        try {
            reader = new PdfReader(mPath);
            Log.v("path", mPath);
            PdfObject obj;
            for (int i = 1; i <= reader.getXrefSize(); i++) {
                obj = reader.getPdfObject(i);
                if (obj != null && obj.isStream()) {
                    PRStream stream = (PRStream) obj;
                    PdfObject type = stream.get(PdfName.SUBTYPE); //get the object type
                    if (type != null && type.toString().equals(PdfName.IMAGE.toString())) {
                        PdfImageObject pio = new PdfImageObject(stream);
                        byte[] image = pio.getImageAsBytes();
                        Bitmap bmp = BitmapFactory.decodeByteArray(image, 0,
                                image.length);
                        imagesCount++;
                        mFileUtils.saveImage(bmp);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (imagesCount == 0) {
            showSnackbar(getString(R.string.extract_images_failed));
        } else {
            showSnackbar(String.format(getString(R.string.extract_images_success),
                    imagesCount));
        }
        mPath = "";
        mBinding.selectFile.setText(R.string.merge_file_select);
        mBinding.selectFile.setBackgroundColor(getResources().getColor(R.color.colorGray));
        mMorphButtonUtility.morphToGrey(mBinding.extractImages, mMorphButtonUtility.integer());
        mBinding.extractImages.setEnabled(false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
        mMorphButtonUtility = new MorphButtonUtility(mActivity);
        mFileUtils = new FileUtils(mActivity);
        mDirectoryUtils = new DirectoryUtils(mActivity);
    }

    @Override
    public void onItemClick(String path) {
        sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        setTextAndActivateButtons(path);
    }

    private void setTextAndActivateButtons(String path) {
        mPath = path;
        mBinding.selectFile.setText(mPath);
        mBinding.selectFile.setBackgroundColor(getResources().getColor(R.color.mb_green_dark));
        mBinding.extractImages.setEnabled(true);
        mMorphButtonUtility.morphToSquare(mBinding.extractImages, mMorphButtonUtility.integer());
    }

    private class BottomSheetCallback extends BottomSheetBehavior.BottomSheetCallback {

        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            switch (newState) {
                case BottomSheetBehavior.STATE_EXPANDED:
                    mBinding.bottomSheet.upArrow.setVisibility(View.GONE);
                    mBinding.bottomSheet.downArrow.setVisibility(View.VISIBLE);
                    break;
                case BottomSheetBehavior.STATE_COLLAPSED:
                    mBinding.bottomSheet.upArrow.setVisibility(View.VISIBLE);
                    mBinding.bottomSheet.downArrow.setVisibility(View.GONE);
                    break;
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        }
    }

    private void showSnackbar(String resID) {
        Snackbar.make(Objects.requireNonNull(mActivity).findViewById(android.R.id.content),
                resID, Snackbar.LENGTH_LONG).show();
    }
}