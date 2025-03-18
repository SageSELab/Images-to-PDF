package swati4star.createpdf.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Objects;

import swati4star.createpdf.R;
import swati4star.createpdf.databinding.FragmentAboutUsBinding;

public class AboutUsFragment extends Fragment {

    private Activity mActivity;
    private FragmentAboutUsBinding mBinding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentAboutUsBinding.inflate(inflater, container, false);
        View rootview = mBinding.getRoot();
        try {
            PackageInfo packageInfo = mActivity.getPackageManager().getPackageInfo(mActivity.getPackageName(), 0);
            TextView versionText = rootview.findViewById(R.id.version_value);
            versionText.setText(packageInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        mBinding.layoutEmail.setOnClickListener(v -> sendmail());
        mBinding.layoutWebsite.setOnClickListener(v -> openWeb());
        mBinding.layoutSlack.setOnClickListener(v -> joinSlack());
        mBinding.layoutGithub.setOnClickListener(v -> githubRepo());
        mBinding.layoutContri.setOnClickListener(v -> contributorsList());
        mBinding.layoutPlaystore.setOnClickListener(v -> openPlaystore());
        mBinding.layoutPrivacy.setOnClickListener(v -> privacyPolicy());
        mBinding.layoutLicense.setOnClickListener(v -> license());

        return rootview;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }

    private void sendmail() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"swati4star@gmail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, mActivity.getResources().getString(R.string.feedback_subject));
        intent.putExtra(Intent.EXTRA_TEXT, mActivity.getResources().getString(R.string.feedback_text));
        try {
            mActivity.startActivity(Intent.createChooser(intent, mActivity.getString(R.string.feedback_chooser)));
        } catch (android.content.ActivityNotFoundException ex) {
            Snackbar.make(Objects.requireNonNull(mActivity).findViewById(android.R.id.content),
                    R.string.snackbar_no_email_clients,
                    Snackbar.LENGTH_LONG).show();
        }
    }

    private void openWeb() {
        openWebPage("http://swati4star.github.io/Images-to-PDF/");
    }

    private void joinSlack() {
        openWebPage("https://join.slack.com/t/imagestopdf/shared_invite/" +
                "enQtNDA2ODk1NDE3Mzk3LTUwNjllYzY5YWZkZDliY2FmNDhkNmM1NjIwZTc1Y" +
                "jU4NTgxNWI0ZDczMWQxMTEyZjA0M2Y5N2RlN2NiMWRjZGI");
    }

    private void githubRepo() {
        openWebPage("https://github.com/Swati4star/Images-to-PDF");
    }

    private void contributorsList() {
        openWebPage("https://github.com/Swati4star/Images-to-PDF/graphs/contributors");
    }

    private void openPlaystore() {
        openWebPage("https://play.google.com/store/apps/details?id=swati4star.createpdf");
    }

    private void privacyPolicy() {
        openWebPage("https://sites.google.com/view/privacy-policy-image-to-pdf/home");
    }

    private void license() {
        openWebPage("https://github.com/Swati4star/Images-to-PDF/blob/master/LICENSE.md");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
    }

    private void openWebPage(String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        if (intent.resolveActivity(mActivity.getPackageManager()) != null)
            startActivity(intent);
    }
}