package nearchat.xiaoxiong.com.nearchat.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by Administrator on 2017/5/19.
 */

public class FragmentAdapter extends FragmentPagerAdapter {

    private List<Fragment> mList;
    public FragmentAdapter(FragmentManager fragmentManager, List<Fragment> mList) {
        super(fragmentManager);
        this.mList = mList;
    }
    @Override
    public Fragment getItem(int position) {
        return mList.get(position);
    }

    @Override
    public int getCount() {
        return mList.size();
    }
}
