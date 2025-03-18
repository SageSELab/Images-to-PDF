package swati4star.createpdf.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.snackbar.Snackbar;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;

import java.io.File;
import java.util.List;
import java.util.Objects;

import swati4star.createpdf.R;
import swati4star.createpdf.activity.MainActivity;
import swati4star.createpdf.adapter.HistoryAdapter;
import swati4star.createpdf.database.AppDatabase;
import swati4star.createpdf.database.History;
import swati4star.createpdf.databinding.FragmentHistoryBinding;
import swati4star.createpdf.util.FileUtils;
import swati4star.createpdf.util.ViewFilesDividerItemDecoration;

public class HistoryFragment extends Fragment implements HistoryAdapter.OnClickListener {

    FragmentHistoryBinding mBinding;
    ConstraintLayout mEmptyStatusLayout;

    RecyclerView mHistoryRecyclerView;
    private Activity mActivity;
    private List<History> mHistoryList;
    private HistoryAdapter mHistoryAdapter;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentHistoryBinding.inflate(inflater, container, false);
        View root = mBinding.getRoot();
        mBinding.getStarted.setOnClickListener(v -> loadHome());
        mEmptyStatusLayout = mBinding.emptyStatusView;
        mHistoryRecyclerView = mBinding.historyRecyclerView;
        new LoadHistory(mActivity).execute();
        return root;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_history_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.actionDeleteHistory) {
            deleteHistory();
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteHistory() {
        new MaterialDialog.Builder(mActivity)
                .title(R.string.warning)
                .content(R.string.delete_history_message)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .onPositive((dialog, which) -> new DeleteHistory().execute())
                .show();
    }


    public void loadHome() {
        Fragment fragment = new ImageToPdfFragment();
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content, fragment).commit();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setDefaultMenuSelected(0);
        }
    }

    @Override
    public void onItemClick(String path) {
        FileUtils fileUtils = new FileUtils(mActivity);
        File file = new File(path);
        if (file.exists()) {
            fileUtils.openFile(path);
        } else {
            Snackbar.make(Objects.requireNonNull(mActivity).findViewById(android.R.id.content),
                    R.string.pdf_does_not_exist_message,
                    Snackbar.LENGTH_LONG).show();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class LoadHistory extends AsyncTask<Void, Void, Void> {
        private final Context mContext;

        LoadHistory(Context mContext) {
            this.mContext = mContext;
        }


        @Override
        protected Void doInBackground(Void... voids) {
            AppDatabase db = AppDatabase.getDatabase(mActivity.getApplicationContext());
            mHistoryList = db.historyDao().getAllHistory();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (mHistoryList != null && !mHistoryList.isEmpty()) {
                mEmptyStatusLayout.setVisibility(View.GONE);
                mHistoryAdapter = new HistoryAdapter(mActivity, mHistoryList, HistoryFragment.this);
                RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(mContext);
                mHistoryRecyclerView.setLayoutManager(mLayoutManager);
                mHistoryRecyclerView.setAdapter(mHistoryAdapter);
                mHistoryRecyclerView.addItemDecoration(new ViewFilesDividerItemDecoration(mContext));
            } else {
                mEmptyStatusLayout.setVisibility(View.VISIBLE);
            }
            super.onPostExecute(aVoid);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class DeleteHistory extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            AppDatabase db = AppDatabase.getDatabase(mActivity.getApplicationContext());
            db.historyDao().deleteHistory();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (mHistoryAdapter != null) {
                mHistoryAdapter.deleteHistory();
            }
            mEmptyStatusLayout.setVisibility(View.VISIBLE);
        }
    }
}