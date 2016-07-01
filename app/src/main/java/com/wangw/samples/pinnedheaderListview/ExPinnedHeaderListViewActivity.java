package com.wangw.samples.pinnedheaderListview;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.exlogcat.L;
import com.wangw.pinnedheaderlistview_lib.PinnedHeaderListView;
import com.wangw.samples.BaseActivity;
import com.wangw.samples.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ExPinnedHeaderListViewActivity extends BaseActivity {

    @Bind(R.id.listview)
    PinnedHeaderListView mlvView;
    @Bind(R.id.ll_indexs)
    LinearLayout mLlIndexs;

    private TestSectionedAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ex_pinned_header_list_view);

        mAdapter = new TestSectionedAdapter(this);
        mlvView.setAdapter(mAdapter);
        onInitIndexView();
    }

    private void onInitIndexView() {
        int count = mAdapter.getSectionCount();
        for (int i=0;i<count;i++){
            TextView txt = new TextView(this);
            txt.setText("H "+i);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
            params.topMargin = 20;
            txt.setTag(i);
            txt.setTextColor(getResources().getColorStateList(R.color.selector_pinnedheaderlist_index));
            txt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int index = (int) v.getTag();
                    int position = 0;
                    for (int j =0;j<index;j++){
                        position += mAdapter.getCountForSection(j)+1;
                    }
                    mlvView.setSelection(position);
                    setIndexState(index);
                }
            });
            mLlIndexs.addView(txt,params);
        }
    }


    public void setIndexState(int position){
        L.d("setIndexState >> position = %s",position);
        int count = mLlIndexs.getChildCount();
        for (int i=0;i<count;i++){
            View vi = mLlIndexs.getChildAt(i);
            vi.setActivated(i == position);
        }
        mLlIndexs.invalidate();
    }

    @Override
    public String getSampleName() {
        return "pinnedHeaderListView的扩展";
    }
}
