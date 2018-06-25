package example.home;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.style.base.BaseFragment;
import com.style.bean.User;
import com.style.data.prefs.AccountManager;
import com.style.framework.R;
import com.style.framework.databinding.FragmentHome3Binding;

import example.activity.AnimatorActivity;
import example.activity.DataBindingActivity;
import example.activity.MsgToSubActivity;
import example.activity.ReadAssetsActivity;
import example.activity.TestDBActivity;
import example.aidl.AidlActivity;
import example.db.EncryptActivity;
import example.ndk.JniTestActivity;
import example.web.WebViewActivity;
import example.web.WebViewAndJSActivity;
import example.web.WebViewFeedbackActivity;


public class HomeFragment3 extends BaseFragment {
    FragmentHome3Binding bd;
    private User curUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        bd = DataBindingUtil.inflate(inflater, R.layout.fragment_home_3, container, false);
        return bd.getRoot();
    }

    @Override
    protected void initData() {
        curUser = AccountManager.getInstance().getCurrentUser();
        bd.setOnItemClickListener(new OnItemClickListener());
        bd.refreshLayout.setEnableLoadMore(false);
    }

    public class OnItemClickListener {
        public void ski(View v) {
            skip(MsgToSubActivity.class);
        }

        public void skip418(View v) {
            skip(TestDBActivity.class);
        }

        public void skip419(View v) {
            skip(DataBindingActivity.class);
        }


        public void skip414(View v) {
            skip(AnimatorActivity.class);
        }

        public void skip7(View v) {
            skip(WebViewActivity.class);
        }

        public void skip8(View v) {
            skip(WebViewAndJSActivity.class);
        }

        public void skip81(View v) {
            skip(WebViewFeedbackActivity.class);
        }

        public void skip9(View v) {
            skip(JniTestActivity.class);
        }

        public void skip10(View v) {
            skip(AidlActivity.class);
        }

        public void skip11(View v) {
            skip(ReadAssetsActivity.class);
        }
        public void skip12(View v) {
            skip(EncryptActivity.class);
        }
    }
}
