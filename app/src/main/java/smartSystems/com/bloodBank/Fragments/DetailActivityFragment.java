package smartSystems.com.bloodBank.Fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import smartSystems.com.bloodBank.Model.User;
import smartSystems.com.bloodBank.R;

public class DetailActivityFragment extends Fragment {

    private User mUser;
    private static final String ARG_USER_ID = "userId";

    public DetailActivityFragment() {
        // Required empty public constructor
    }

    public static DetailActivityFragment newInstance(String id) {
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, id);
        DetailActivityFragment fragment = new DetailActivityFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String userId = getArguments().getString(ARG_USER_ID);
            mUser = SearchResultFragment.userMap.get(userId);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_detail, container, false);

        TextView tvUsername = (TextView) view.findViewById(R.id.tvUsername);
        tvUsername.setText(mUser.getUsername());

        TextView tvBloodType = (TextView) view.findViewById(R.id.tvBloodType);
        tvBloodType.setText(mUser.getBloodType());

        TextView tvAddress = (TextView) view.findViewById(R.id.tvAddress);
        tvAddress.setText(mUser.getAddress());

        TextView tvPhone = (TextView) view.findViewById(R.id.tvPhone);
        tvPhone.setText(mUser.getPhone());

        Button btnSendEmail = (Button) view.findViewById(R.id.btnSendEmail);

        btnSendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] addresses = {mUser.getUsername()};
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:"));
                intent.putExtra(Intent.EXTRA_EMAIL, addresses);
                intent.putExtra(Intent.EXTRA_SUBJECT, "Blood donation request");
                intent.putExtra(Intent.EXTRA_TEXT, "I saw your profile on the blood bank application");
                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });
        return view;
    }

}
