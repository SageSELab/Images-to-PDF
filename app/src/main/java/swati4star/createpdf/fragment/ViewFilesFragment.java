package swati4star.createpdf.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;

import com.google.android.material.snackbar.Snackbar;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

import swati4star.createpdf.R;
import swati4star.createpdf.activity.MainActivity;
import swati4star.createpdf.adapter.ViewFilesAdapter;
import swati4star.createpdf.databinding.FragmentViewFilesBinding;
import swati4star.createpdf.interfaces.EmptyStateChangeListener;
import swati4star.createpdf.util.DirectoryUtils;
import swati4star.createpdf.util.MoveFilesToDirectory;
import swati4star.createpdf.util.PopulateList;
import swati4star.createpdf.util.ViewFilesDividerItemDecoration;

import static swati4star.createpdf.util.Constants.SORTING_INDEX;
import static swati4star.createpdf.util.FileSortUtils.NAME_INDEX;

public class ViewFilesFragment extends Fragment
        implements SwipeRefreshLayout.OnRefreshListener, EmptyStateChangeListener {

    // Directory operations constants
    public static final int NEW_DIR = 1;
    public static final int EXISTING_DIR = 2;

    private FragmentViewFilesBinding mBinding;

    private MenuItem mMenuItem;
    private Activity mActivity;
    private ViewFilesAdapter mViewFilesAdapter;

    private DirectoryUtils mDirectoryUtils;
    private SearchView mSearchView;
    private int mCurrentSortingIndex;
    private SharedPreferences mSharedPreferences;
    private boolean mIsChecked = false;
    private AlertDialog.Builder mAlertDialogBuilder;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
        mDirectoryUtils = new DirectoryUtils(mActivity);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentViewFilesBinding.inflate(inflater, container, false);
        View root = mBinding.getRoot();

        // Initialize variables
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
        mCurrentSortingIndex = mSharedPreferences.getInt(SORTING_INDEX, NAME_INDEX);
        mViewFilesAdapter = new ViewFilesAdapter(mActivity, null, this);
        mAlertDialogBuilder = new AlertDialog.Builder(mActivity)
                .setCancelable(true)
                .setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss());

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(root.getContext());
        mBinding.filesRecyclerView.setLayoutManager(mLayoutManager);
        mBinding.filesRecyclerView.setAdapter(mViewFilesAdapter);
        mBinding.filesRecyclerView.addItemDecoration(new ViewFilesDividerItemDecoration(root.getContext()));
        mBinding.swipe.setOnRefreshListener(this);

        checkIfListEmpty();

        mBinding.newDir.setOnClickListener(v -> moveToNewDirectory());
        mBinding.moveToDir.setOnClickListener(v -> moveToDirectory());
        mBinding.moveToHomeDir.setOnClickListener(v -> moveFilesToHomeDirectory());
        mBinding.deleteDir.setOnClickListener(v -> deleteDirectory());
        return root;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.activity_view_files_actions, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        mMenuItem = menu.findItem(R.id.select_all);
        mSearchView = (SearchView) item.getActionView();
        assert mSearchView != null;
        mSearchView.setQueryHint(getString(R.string.search_hint));
        mSearchView.setSubmitButtonEnabled(true);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                setDataForQueryChange(s);
                mSearchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                setDataForQueryChange(s);
                return true;
            }
        });
        mSearchView.setOnCloseListener(() -> {
            populatePdfList();
            return false;
        });
        mSearchView.setIconifiedByDefault(true);
    }

    private void setDataForQueryChange(String s) {
        ArrayList<File> searchResult = mDirectoryUtils.searchPDF(s);
        mViewFilesAdapter.setData(searchResult);
        mBinding.filesRecyclerView.setAdapter(mViewFilesAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.item_sort) {
            displaySortDialog();
        } else if (itemId == R.id.item_delete) {
            if (mViewFilesAdapter.areItemsSelected())
                deleteFiles();
            else
                showSnack(R.string.snackbar_no_pdfs_selected);
        } else if (itemId == R.id.item_share) {
            if (mViewFilesAdapter.areItemsSelected())
                mViewFilesAdapter.shareFiles();
            else
                showSnack(R.string.snackbar_no_pdfs_selected);
        } else if (itemId == R.id.select_all) {
            if (mIsChecked) {
                // method implementation
            } else {
                // method implementation
            }
            mIsChecked = !mIsChecked;
        }
        return true;
    }

    private void moveFilesToDirectory(int operation) {
        LayoutInflater inflater = getLayoutInflater();
        View alertView = inflater.inflate(R.layout.directory_dialog, null);
        final ArrayList<String> filePath = mViewFilesAdapter.getSelectedFilePath();
        if (filePath == null) {
            showSnack(R.string.snackbar_no_pdfs_selected);
        } else {
            final EditText input = alertView.findViewById(R.id.directory_editText);
            TextView message = alertView.findViewById(R.id.directory_textView);
            if (operation == NEW_DIR) {
                // method implementation
            } else if (operation == EXISTING_DIR) {
                // method implementation
            }
            mAlertDialogBuilder.create().show();
        }
    }

    private void deleteFiles() {
        AlertDialog.Builder dialogAlert = new AlertDialog.Builder(mActivity)
                .setCancelable(true)
                .setNegativeButton(R.string.cancel,
                        (dialogInterface, i) -> dialogInterface.dismiss())
                .setTitle(R.string.delete_alert)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    mViewFilesAdapter.deleteFiles();
                    checkIfListEmpty();
                });
        dialogAlert.create().show();
    }

    private void checkIfListEmpty() {
        onRefresh();
        final File[] files = mDirectoryUtils.getOrCreatePdfDirectory().listFiles();
        int count = 0;

        if (files == null) {
            setEmptyStateVisible();
            return;
        }

        for (File file : files)
            if (!file.isDirectory()) {
                count++;
                break;
            }
        if (count == 0)
            setEmptyStateVisible();
    }

    @Override
    public void onRefresh() {
        populatePdfList();
        mBinding.swipe.setRefreshing(false);
    }

    private void populatePdfList() {
        new PopulateList(mActivity, mViewFilesAdapter, this, mCurrentSortingIndex).execute();
    }

    private void displaySortDialog() {
        final File folder = mDirectoryUtils.getOrCreatePdfDirectory();
        mAlertDialogBuilder.setTitle(R.string.sort_by_title)
                .setItems(R.array.sort_options, (dialog, which) -> {
                    ArrayList<File> pdfsFromFolder = mDirectoryUtils.getPdfsFromPdfFolder(folder.listFiles());
                    ArrayList<File> pdfFromOtherDir = mDirectoryUtils.getPdfFromOtherDirectories();
                    if (pdfFromOtherDir != null) {
                        pdfsFromFolder.addAll(pdfFromOtherDir);
                    } else
                        // method implementation

                        mViewFilesAdapter.setData(pdfsFromFolder);
                    mCurrentSortingIndex = which;
                    mSharedPreferences.edit().putInt(SORTING_INDEX, which).apply();
                });
        mAlertDialogBuilder.create().show();
    }

    @Override
    public void setEmptyStateVisible() {
        mBinding.emptyStatusView.setVisibility(View.VISIBLE);
        mBinding.layoutMain.setVisibility(View.GONE);
    }

    @Override
    public void setEmptyStateInvisible() {
        mBinding.emptyStatusView.setVisibility(View.GONE);
        mBinding.layoutMain.setVisibility(View.VISIBLE);
    }

    private void loadHome() {
        Fragment fragment = new ImageToPdfFragment();
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content, fragment).commit();
        if (mActivity instanceof MainActivity) {
            ((MainActivity) mActivity).setDefaultMenuSelected(0);
        }
    }

    private void moveToNewDirectory() {
        if (mViewFilesAdapter.areItemsSelected())
            moveFilesToDirectory(NEW_DIR);
        else
            showSnack(R.string.snackbar_no_pdfs_selected);
    }

    private void moveToDirectory() {
        if (mViewFilesAdapter.areItemsSelected())
            moveFilesToDirectory(EXISTING_DIR);
        else
            showSnack(R.string.snackbar_no_pdfs_selected);
    }

    private void moveFilesToHomeDirectory() {
        if (!mViewFilesAdapter.areItemsSelected()) {
            showSnack(R.string.snackbar_no_pdfs_selected);
            return;
        }
        final ArrayList<String> filePath = mViewFilesAdapter.getSelectedFilePath();
        if (filePath == null) {
            showSnack(R.string.snackbar_no_pdfs_selected);
        } else {
            final File[] files = mDirectoryUtils.getOrCreatePdfDirectory().listFiles();
            for (File pdf : mDirectoryUtils.getPdfsFromPdfFolder(files)) {
                if (filePath.contains(pdf.getPath())) {
                    // method implementation
                }
            }
            new MoveFilesToDirectory(mActivity, filePath, null,
                    MoveFilesToDirectory.HOME_DIRECTORY).execute();
            populatePdfList();
        }
    }

    private void deleteDirectory() {
        LayoutInflater inflater = getLayoutInflater();
        View alertView = inflater.inflate(R.layout.directory_dialog, null);
        final ArrayList<String> pdfFiles = new ArrayList<>();
        final EditText input = alertView.findViewById(R.id.directory_editText);
        TextView message = alertView.findViewById(R.id.directory_textView);
        message.setText(R.string.dialog_delete_dir);
        mAlertDialogBuilder.setTitle(R.string.delete_directory)
                .setView(alertView)
                .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                    final String dirName = input.getText().toString();
                    final File directory = mDirectoryUtils.getDirectory(dirName);
                    if (directory == null || dirName.trim().isEmpty()) {
                        showSnack(R.string.dir_does_not_exists);
                    } else {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                        builder.setTitle(R.string.delete);
                        // method implementation
                        builder.create().show();
                    }
                });
        mAlertDialogBuilder.create().show();
    }

    private void showSnack(int resID) {
        Snackbar.make(Objects.requireNonNull(mActivity).findViewById(android.R.id.content),
                resID, Snackbar.LENGTH_LONG).show();
    }
}