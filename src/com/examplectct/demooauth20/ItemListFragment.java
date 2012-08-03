package com.examplectct.demooauth20;

import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.examplectct.demooauth20.dummy.DummyContent;

public class ItemListFragment extends ListFragment {

    private static final String STATE_ACTIVATED_POSITION = "activated_position";
    private static final String[] FROM					 = {ItemListActivity.NAME_KEY, ItemListActivity.EMAILADDRESS_KEY};
	private static final int[] TO						 = {android.R.id.text1, android.R.id.text2};

    private Callbacks mCallbacks = sDummyCallbacks;
    private int mActivatedPosition = ListView.INVALID_POSITION;

    public interface Callbacks {

        public void onItemSelected(String id);
    }

    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(String id) {
        }
    };

    public ItemListFragment() {
    }
    
    private static class MySimpleAdapter extends SimpleAdapter {
    	MySimpleAdapter(Context context, List<HashMap<String, String>> data, int layout, String[] from, int[] to) {
    		super(context, data, layout, from, to);
    	}

		@Override
		// ellipsize email address in middle if too long
		public View getView(int position, View convertView, ViewGroup parent) {
			// null if not a re-used view, so setEllipsize()
			if (convertView == null) {
				LayoutInflater layoutinflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = layoutinflater.inflate(android.R.layout.two_line_list_item, parent, false);
				TextView text2 = (TextView) convertView.findViewById(android.R.id.text2);
				text2.setSingleLine(); // needed to force ellipsize in tablet landscape mode
				text2.setEllipsize(TruncateAt.MIDDLE);
			}
			return super.getView(position, convertView, parent);
		}
    }

    void doSetListAdapter() {
    	MySimpleAdapter sa = new MySimpleAdapter(
    			getActivity(),
    			ItemListActivity.contactHashmaps,
    			android.R.layout.two_line_list_item,
    			FROM,
    			TO);
    	setListAdapter(sa);
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setListAdapter(new ArrayAdapter<DummyContent.DummyItem>(getActivity(),
//                android.R.layout.simple_list_item_activated_1,
//                android.R.id.text1,
//                DummyContent.ITEMS));
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null && savedInstanceState
                .containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = sDummyCallbacks;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);
        mCallbacks.onItemSelected(DummyContent.ITEMS.get(position).id);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    public void setActivateOnItemClick(boolean activateOnItemClick) {
        getListView().setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }

    public void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }
}
