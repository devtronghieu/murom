package com.example.murom;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.murom.Recycler.ReelsAdapter;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ReelsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReelsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ReelsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ReelsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ReelsFragment newInstance(String param1, String param2) {
        ReelsFragment fragment = new ReelsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_reels, container, false);
        final ViewPager2 reelViewPager = rootView.findViewById(R.id.reels_viewpager);
        ArrayList<ReelsAdapter.ReelModel> reels = new ArrayList<>();

        ReelsAdapter.ReelModel reel1 = new ReelsAdapter.ReelModel("https://picsum.photos/200","yoshie1","https://cdn.pixabay.com/video/2022/07/10/123664-728698002_large.mp4","I’m a passionate journalist, blogger & vlogger based in Germany.");
        reels.add(reel1);
        ReelsAdapter.ReelModel reel2 = new ReelsAdapter.ReelModel("https://picsum.photos/200","yoshie2","https://cdn.pixabay.com/video/2024/03/26/205683-927672625_large.mp4","Yellow flower");
        reels.add(reel2);
        ReelsAdapter.ReelModel reel3 = new ReelsAdapter.ReelModel("https://picsum.photos/200","yoshie3","https://cdn.pixabay.com/video/2023/10/11/184510-873463500_large.mp4","Passionate about photography and AI Editing.");
        reels.add(reel3);
        ReelsAdapter.ReelModel reel4 = new ReelsAdapter.ReelModel("https://picsum.photos/200","yoshie4","https://cdn.pixabay.com/video/2024/03/23/205344-926737178_large.mp4","I am a nature and travel photographer from Japan. I introduce you relatively unfamiliar landscapes, wildlife of my country, Indonesia, and Mongolia.");
        reels.add(reel4);
        ReelsAdapter.ReelModel reel5 = new ReelsAdapter.ReelModel("https://picsum.photos/200","yoshie5","https://cdn.pixabay.com/video/2024/03/26/205681-927672609_large.mp4","I am photographer, photoshop and typing working");
        reels.add(reel5);
        ReelsAdapter.ReelModel reel6 = new ReelsAdapter.ReelModel("https://picsum.photos/200","yoshie6","https://cdn.pixabay.com/video/2024/03/19/204756-925166550_large.mp4","Sunset");
        reels.add(reel6);
        ReelsAdapter.ReelModel reel7 = new ReelsAdapter.ReelModel("https://picsum.photos/200","yoshie7","https://cdn.pixabay.com/video/2024/03/03/202753-918944243_large.mp4","I'm simply trying to make a difference in the world one free stock video at a time to help everyone visualize their stories.");
        reels.add(reel7);
        reelViewPager.setAdapter(new ReelsAdapter(reels));
        return rootView;
    }
}