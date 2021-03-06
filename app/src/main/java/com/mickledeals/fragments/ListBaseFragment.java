package com.mickledeals.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;

import com.mickledeals.R;
import com.mickledeals.adapters.CardAdapter;
import com.mickledeals.utils.DLog;
import com.mickledeals.utils.EndlessRecyclerOnScrollListener;
import com.mickledeals.utils.EventBus;
import com.mickledeals.utils.MDApiManager;

import java.util.List;

/**
 * Created by Nicky on 7/23/2015.
 */
public abstract class ListBaseFragment extends BaseFragment implements MDApiManager.MDResponseListener<List<Integer>>{

    protected View mNoResultLayout;
    protected View mNoNetworkLayout;
    protected ViewStub mNoNetworkStub;
    protected SwipeRefreshLayout mSwipeRefreshLayout;
    protected View mLoadingLayout;
    protected RecyclerView mListResultRecyclerView;
    protected CardAdapter mAdapter;
    protected List<Integer> mDataList;
    private EndlessRecyclerOnScrollListener mEndlessScrollListener;

    protected int mPageToLoad = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDataList = getDataList();
        mDataList.clear();
        //clea data list so that user will not see old data when reload activity / fragment
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        DLog.d(this, "onCreateView");
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                getFragmentLayoutRes(), container, false);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mListResultRecyclerView = (RecyclerView) view.findViewById(R.id.listResultRecyclerView);
        setRecyclerView();
        if (needLoadMore()) {
            mEndlessScrollListener =  new EndlessRecyclerOnScrollListener((LinearLayoutManager) mListResultRecyclerView.getLayoutManager()) {
                @Override
                public void onLoadMore() {
//if there is no result, scroll to bottom should not trigger page to load more, otherwise there is a issue after switch category
                    if (mDataList.size() == 0) return;

                    mPageToLoad++;
                    DLog.d(ListBaseFragment.this, "onloadmore page = " + mPageToLoad);
                    sendRequest();
                    //add progressbar after send request, otherwise data list size will be wrong
                    mDataList.add(null);
                    mAdapter.notifyItemInserted(mDataList.size());
                }

                @Override
                public boolean ignoreRemainder() {
                    //my coupon fragment cannot use the remainder logic
                    return ListBaseFragment.this instanceof MyCouponsFragment ? true : false;
                }
            };

            mListResultRecyclerView.addOnScrollListener(mEndlessScrollListener);
        }


        mNoResultLayout = view.findViewById(R.id.noResultLayout);
        mLoadingLayout = view.findViewById(R.id.loadingLayout);
        mNoNetworkStub = (ViewStub) view.findViewById(R.id.noNetworkStub);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.requestDisallowInterceptTouchEvent(false);
            mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary1, R.color.colorPrimary4);
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    prepareSendRequest();
                }
            });
        }

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                prepareSendRequest();
            }
        });
    }



    public void prepareSendRequest() {
        DLog.d(this, "prepareSendRequest");

        if (mNoResultLayout != null) mNoResultLayout.setVisibility(View.GONE);
        if (mNoNetworkLayout != null) mNoNetworkLayout.setVisibility(View.GONE);
        if (mLoadingLayout != null) mLoadingLayout.setVisibility(View.VISIBLE);
        if (mSwipeRefreshLayout != null) mSwipeRefreshLayout.setRefreshing(true);

        if (mEndlessScrollListener != null) mEndlessScrollListener.resetPreviousTotal();
        mPageToLoad = 1;
        sendRequest();
    }

    @Override
    public void onMDSuccessResponse(List<Integer> resultList) {
        if (mPageToLoad > 1) {
            //remove progress bar
            int removeIndex = mDataList.size() - 1;
            if (removeIndex >=0 && mDataList.get(removeIndex) == null) {
                mDataList.remove(mDataList.size() - 1);
                mAdapter.notifyItemRemoved(mAdapter.getItemCount());
            }
            mDataList.addAll(resultList);
            mAdapter.notifyItemRangeInserted(mAdapter.getItemCount() - resultList.size(), resultList.size());
            mAdapter.setPendingAnimated();
        } else {
            mDataList.clear();
            mDataList.addAll(resultList);
            mAdapter.notifyDataSetChanged();
            mAdapter.setPendingAnimated();
        }

        if (mSwipeRefreshLayout != null) mSwipeRefreshLayout.setRefreshing(false);
        if (mLoadingLayout != null) mLoadingLayout.setVisibility(View.GONE);
        if (mDataList.size() == 0 && mNoResultLayout != null) {
            mNoResultLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onMDNetworkErrorResponse(String errorMessage) {
        if (mSwipeRefreshLayout != null) mSwipeRefreshLayout.setRefreshing(false);
        if (mLoadingLayout != null) mLoadingLayout.setVisibility(View.GONE);
        showErrorLayout();
    }

    @Override
    public void onMDErrorResponse(String errorMessage) {
        if (mSwipeRefreshLayout != null) mSwipeRefreshLayout.setRefreshing(false);
        if (mLoadingLayout != null) mLoadingLayout.setVisibility(View.GONE);
        showErrorLayout();
    }

    private void showErrorLayout() {
        if (mNoNetworkLayout == null) mNoNetworkLayout = mNoNetworkStub.inflate();
        mNoNetworkLayout.setVisibility(View.VISIBLE);

        Button retryButton = (Button) mNoNetworkLayout.findViewById(R.id.retryButton);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prepareSendRequest();
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mSwipeRefreshLayout != null) mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onFragmentPause() {
        super.onFragmentPause();

        if (mSwipeRefreshLayout != null) mSwipeRefreshLayout.setRefreshing(false);
    }

    public abstract void sendRequest();

    public abstract boolean needLoadMore();

    public abstract int getFragmentLayoutRes();

    public abstract void setRecyclerView();

    public abstract List<Integer> getDataList();

    @Override
    public void onEventUpdate(int event, Bundle data) {
        super.onEventUpdate(event, data);
        if (event == EventBus.EVENT_ADD_SAVE || event == EventBus.EVENT_REMOVE_SAVE) {
            int couponId = data.getInt("couponId");
            int index = mDataList.indexOf(couponId);
            if (this instanceof FeaturedFragment) index++; //there is top feature as index 0
            mAdapter.notifyItemChanged(index);
        }
    }

//    @Override
//    public void onResume() {
//        super.onResume();
////        mAdapter.notifyDataSetChanged();
//    }
//
//
//    //this is for swiping in home, after saving a coupon in browse, it should reflect when swipe to feature
//    public void updateDataSet() {
//        DLog.d(this, "updateDataSet");
//        //there is a bug when animating when notify dataset changed
//        if (!mAdapter.isAnimating()) {
//            DLog.d(this, "notifydatasetchanged");
////            mAdapter.notifyDataSetChanged();
//        }
//    }
}
