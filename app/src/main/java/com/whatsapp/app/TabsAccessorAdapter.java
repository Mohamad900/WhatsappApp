package com.whatsapp.app;

import com.whatsapp.app.Fragments.CallsFragment;
import com.whatsapp.app.Fragments.StatusFragment;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class TabsAccessorAdapter extends FragmentPagerAdapter
{


    public TabsAccessorAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position){

            case 0 :
                return new ChatsFragment();

            case 1 :
                //return new GroupsFragment();
                return  new StatusFragment();

            case 2 :
                //return new ContactsFragment();
                return  new CallsFragment();
           /* case 3 :
                return new RequestsFragment();*/

                default:
                    return  null;
        }

    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {

        switch (position){

            case 0 :
                return "Chats";

            case 1 :
                return "Status";

            case 2 :
                return "Calls";
            /*case 3 :
                return "Requests";*/

            default:
                return  null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }
}
