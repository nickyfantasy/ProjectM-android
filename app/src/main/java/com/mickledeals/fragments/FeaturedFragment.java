package com.mickledeals.fragments;

import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.mickledeals.R;
import com.mickledeals.adapters.FeaturedAdapter;
import com.mickledeals.datamodel.DataListModel;
import com.mickledeals.utils.Constants;

/**
 * Created by Nicky on 11/28/2014.
 */
public class FeaturedFragment extends SwipeRefreshBaseFragment {

    private FeaturedAdapter mFeatureAdapter;

    @Override
    public void onPause() {
        super.onPause();
        stopAutoSliding();
    }

    @Override
    public void onResume() {
        super.onResume();
        mFeatureAdapter.notifyDataSetChanged();
        startAutoSliding();
    }

    public void stopAutoSliding() {
        if (mFeatureAdapter != null) mFeatureAdapter.stopAutoSliding();
    }

    public void startAutoSliding() {
        if (mFeatureAdapter != null) mFeatureAdapter.startAutoSliding();
    }

    @Override
    public int getFragmentLayoutRes() {
        return R.layout.fragment_featured;
    }

    public String getRequestURL() {
        return "http://www.cycon.com.mo/cafe_version_update.txt";
    }

    @Override
    public void onSuccessResponse() {
        super.onSuccessResponse();
    }

    public void setRecyclerView() {
        RecyclerView.LayoutManager layoutManager;
        if (mContext.getResources().getInteger(R.integer.dp_width_level) > 0) {
            layoutManager = new GridLayoutManager(mContext, 2);
            ((GridLayoutManager)layoutManager).setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int i) {
                    if (i == 0) return 2;
                    else return 1;
                }
            });
        } else {
            layoutManager = new LinearLayoutManager(mContext);
        }

        mListResultRecyclerView.setLayoutManager(layoutManager);
        mFeatureAdapter = new FeaturedAdapter(this, DataListModel.getInstance().getBestCouponList(), Constants.TYPE_BEST_LIST, R.layout.card_layout_featured);
        mListResultRecyclerView.setAdapter(mFeatureAdapter);
        mListResultRecyclerView.setItemAnimator(new DefaultItemAnimator());
//        mFeatureRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
//            @Override
//            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
//                int pos = parent.getChildPosition(view);
//                if (pos != 0) {
//                    boolean leftside = pos % 2 == 1;
//                    outRect.left = leftside ? margin : margin / 2;
//                    outRect.right = leftside ? margin / 2 : margin;
//                    outRect.bottom = bottomMargin;
//                }
//            }
//        });
    }
}
