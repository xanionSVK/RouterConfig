package sk.xanion.routerconfig.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.Charset;

import sk.xanion.routerconfig.R;
import sk.xanion.routerconfig.RequestServerData;
import sk.xanion.routerconfig.util.Settings;
import sk.xanion.routerconfig.util.SettingsValidator;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SetupWirelessFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    public void save() {
        String url = "";
        String login = "";
        String pass = "";
        String macAdress = "";
        String ssId = "";
        TextView tv = (TextView) getActivity().findViewById(R.id.tvRouterUrl);
        url = tv.getText().toString();
        tv = (TextView) getActivity().findViewById(R.id.tvRouterLogin);
        login = tv.getText().toString();
        tv = (TextView) getActivity().findViewById(R.id.tvRouterHeslo);
        pass = tv.getText().toString();
        tv = (TextView) getActivity().findViewById(R.id.tvMcAdressKatka);
        macAdress = tv.getText().toString();
        tv = (TextView) getActivity().findViewById(R.id.tvRouterSsid);
        ssId = tv.getText().toString();
        boolean passwordRequired = TextUtils.isEmpty(Settings.readPassword(getActivity()));
        String error;
        if (passwordRequired) {
            error = SettingsValidator.validate(getActivity(), url, login, pass, macAdress, ssId);
        } else {
            error = SettingsValidator.validate(getActivity(), url, "*****", "*****", macAdress, ssId);
        }
        if (TextUtils.isEmpty(error)) {
            Settings.saveUrl(getActivity(), url);
            Settings.saveBlockedMac(getActivity(), macAdress, 1);
            if (passwordRequired) {
                String hashedPass = new String(Base64.encode((login + ":" + pass).getBytes(), Base64.NO_WRAP), Charset.forName("UTF-8"));
                Settings.savePassword(getActivity(), hashedPass);
            }
            Settings.saveSSID(getActivity(), ssId);
        } else {
            Toast.makeText(getActivity(), error, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        TextView tv = (TextView) getActivity().findViewById(R.id.tvRouterUrl);
        tv.setText(Settings.readUrl(getActivity()));
        tv = (TextView) getActivity().findViewById(R.id.tvRouterLogin);
        tv.setText(null);
        tv = (TextView) getActivity().findViewById(R.id.tvRouterHeslo);
        tv.setText(null);
        tv = (TextView) getActivity().findViewById(R.id.tvMcAdressKatka);
        tv.setText(Settings.readBlockedMac(getActivity(), 1));
        tv = (TextView) getActivity().findViewById(R.id.tvRouterSsid);
        tv.setText(Settings.readSSID(getActivity()));

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
