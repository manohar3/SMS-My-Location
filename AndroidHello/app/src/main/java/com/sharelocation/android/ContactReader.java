package com.sharelocation.android;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.Toast;


@SuppressLint("NewApi")
public class ContactReader extends ListFragment implements AdapterView.OnItemClickListener
{
    Activity activity;
    public List<String> names = new ArrayList<String>();
    public List<String> numbers = new ArrayList<String>();

    public void setNames(List<String> names)
    {
        this.names = names;
    }

    public void setNumbers(List<String> numbers)
    {
        this.numbers = numbers;
    }


    public ContactReader() {
		// TODO Auto-generated constructor stub
	}
    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        this.activity = activity;
    }
 
    ArrayAdapter<String> adapter;
    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
         adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_selectable_list_item, names);
        setListAdapter(adapter);
        getListView().setOnItemClickListener(this);
        getListView().setBackgroundColor(Color.LTGRAY);
        setHasOptionsMenu(true);
    }
 
    @Override 
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.grid_default, menu); 
        SearchView searchView = (SearchView)menu.findItem(R.id.grid_default_search).getActionView();
        searchView.setOnQueryTextListener(queryListener);
    }
    
    final private OnQueryTextListener queryListener = new OnQueryTextListener() {       

        @Override
        public boolean onQueryTextChange(String newText) {
        	adapter.getFilter().filter(newText);
            return false;
        }

        @Override
        public boolean onQueryTextSubmit(String query) {            
            adapter.getFilter().filter(query);
            return false;
        }
    };

   @Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		
	   		String name = adapter.getItem(position);
	   		String number = numbers.get(names.indexOf(name));
				
			LocationActivity act = ((LocationActivity)activity);
			act.onContactSelected(name,number);
			
			FragmentManager manager = ((Fragment) this).getFragmentManager();
            FragmentTransaction trans = manager.beginTransaction();
            trans.remove((Fragment)this);
            trans.commit();
	}
	
}
