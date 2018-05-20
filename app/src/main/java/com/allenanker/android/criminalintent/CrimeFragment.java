package com.allenanker.android.criminalintent;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class CrimeFragment extends Fragment {

    private static final String AGE_CRIME_ID = "crime_id";
    private static final String DIALOG_DATE = "DialogDate";
    private static final String CALL_BUTTON_ENABLED = "callButtonState";

    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_TIME = 1;
    private static final int REQUEST_CONTACT = 2;
    private static final int REQUEST_PHOTO = 3;

    private Crime mCrime;
    private File mPhotoFile; //photo file for crime
    private EditText mTitleField;
    private Button mDateButton;
    private CheckBox mSolvedCheckBox;
    private Button mReportButton;
    private Button mSuspectButton;
    private Button mCallSuspectButton;
    private ImageButton mPhotoButton;
    private ImageView mPhotoView;

    private boolean callButtonState = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        UUID crimeId = (UUID) getArguments().getSerializable(AGE_CRIME_ID);
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);
        mPhotoFile = CrimeLab.get(getActivity()).getPhotoFile(mCrime);

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[] { Manifest.permission.READ_CONTACTS}, 1);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        CrimeLab.get(getActivity()).updateCrime(mCrime);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_crime:
                if (mCrime != null) {
                    CrimeLab.get(getActivity()).deleteCrime(mCrime);
                    Toast.makeText(getActivity(), "Successfully Deleted", Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
                return true;
                default:
                    return super.onOptionsItemSelected(item);

        }
    }

    public static CrimeFragment newInstance(UUID crimeId) {
        Bundle args = new Bundle();
        args.putSerializable(AGE_CRIME_ID, crimeId);
        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime, container, false);

        mTitleField = v.findViewById(R.id.crime_title);
        mDateButton = v.findViewById(R.id.crime_date);
        mSolvedCheckBox = v.findViewById(R.id.crime_solved);
        mReportButton = v.findViewById(R.id.send_report_button);
        mSuspectButton = v.findViewById(R.id.choose_suspect_button);
        mCallSuspectButton = v.findViewById(R.id.call_suspect_button);
        mPhotoView = v.findViewById(R.id.crime_photo);
        mPhotoButton = v.findViewById(R.id.crime_camera);
        updatePhotoView();

        final Intent pickContact = new Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI);
        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        PackageManager packageManager = getActivity().getPackageManager();
        boolean canTakePhoto = mPhotoFile != null && captureImage.resolveActivity(packageManager) != null;
        mPhotoButton.setEnabled(canTakePhoto);

        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = FileProvider.getUriForFile(getActivity(),
                        "com.allenanker.android.criminalintent.fileprovider", mPhotoFile);
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                List<ResolveInfo> cameraActivities = getActivity()
                        .getPackageManager().queryIntentActivities(captureImage,
                                PackageManager.MATCH_DEFAULT_ONLY);

                for (ResolveInfo activity : cameraActivities) {
                    getActivity().grantUriPermission(activity.activityInfo.packageName,
                            uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }

                startActivityForResult(captureImage, REQUEST_PHOTO);
            }
        });

        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //This space is intentionally left blank
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCrime.setTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                //This is too
            }
        });

        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                DatePickerFragment dialog = DatePickerFragment.newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                dialog.show(manager, DIALOG_DATE);
            }
        });
        updateDate();
        mTitleField.setText(mCrime.getTitle());
        mSolvedCheckBox.setChecked(mCrime.isSolved());
        mSolvedCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCrime.setSolved(isChecked);
            }
        });
        mReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setText(getCrimeReport())
                        .setChooserTitle("Share crime report via")
                        .setSubject(getString(R.string.crime_report_subject))
                        .createChooserIntent();

                startActivity(intent);
            }
        });
        mSuspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(pickContact, REQUEST_CONTACT);
            }
        });
        mCallSuspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri contentUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
                String selectClause = ContactsContract.CommonDataKinds.Phone._ID + " = ?";
                String[] fields = {ContactsContract.CommonDataKinds.Phone.NUMBER};
                String[] selectParams = new String[] {
                        Long.toString(mCrime.getContactId())
                };

                Cursor cursor = getActivity().getContentResolver()
                        .query(contentUri, fields, selectClause, selectParams, null);

                if (cursor != null && cursor.getCount() != 0) {
                    try {
                        cursor.moveToFirst();
                        String number = cursor.getString(0);
                        Uri phoneUri = Uri.parse("tel:" + number);
                        Intent intent = new Intent(Intent.ACTION_DIAL, phoneUri);
                        startActivity(intent);
                    } finally {
                        cursor.close();
                    }
                }

            }
        });

        if (mCrime.getSuspect() != null) {
            mSuspectButton.setText(mCrime.getSuspect());
        }

        //double check if there is an application that can respond to this intent

        if (!checkIntent(pickContact)) {
            mSuspectButton.setEnabled(false);
        }

        return v;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(CALL_BUTTON_ENABLED, callButtonState);
    }

    private boolean checkIntent(Intent intent) {
        PackageManager packageManager = getActivity().getPackageManager();
        if (packageManager.resolveActivity(intent,
                PackageManager.MATCH_DEFAULT_ONLY) == null) {
            return false;
        }

        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_DATE) {
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(date);
            updateDate();
        } else if (requestCode == REQUEST_TIME) {

        } else if (requestCode == REQUEST_CONTACT) {
            Uri contactUri = data.getData();
            String[] queryFields = new String[] {
                    ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.Contacts._ID
            };
            Cursor c = getActivity().getContentResolver().query(contactUri, queryFields,
                    null, null, null);

            try {
                if (c.getCount() == 0) {
                    return;
                }

                c.moveToFirst();
                String suspect = c.getString(0);
                long contactId = c.getLong(1);
                mCrime.setSuspect(suspect);
                mCrime.setContactId(contactId);
                mSuspectButton.setText(suspect);

                callButtonState = true;
            } finally {
                c.close();
            }
        } else if (requestCode == REQUEST_PHOTO) {
            Uri uri = FileProvider.getUriForFile(getActivity(),
                    "com.allenanker.android.criminalintent.fileprovider",
                    mPhotoFile
            );

            getActivity().revokeUriPermission(uri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            updatePhotoView();
        }
    }

    private void updateDate() {
        mDateButton.setText(mCrime.getDate().toString());
    }

    private String getCrimeReport() {
        String solvedString;
        if (mCrime.isSolved()) {
            solvedString = getString(R.string.crime_report_solved);
        } else {
            solvedString = getString(R.string.crime_report_unsolved);
        }

        String dateFormat = "EEE, MMM dd";
        String dateString = DateFormat.format(dateFormat, mCrime.getDate()).toString();

        String suspect = mCrime.getSuspect();
        if (suspect == null) {
            suspect = getString(R.string.crime_report_no_suspect);
        } else {
            suspect = getString(R.string.crime_report_suspect, suspect);
        }

        String report = getString(R.string.crime_report,
                mCrime.getTitle(), dateString, solvedString, suspect);

        return report;
    }

    /**
     * Method for updating the image view of the crime to scaled down version
     */
    public void updatePhotoView() {
        if (mPhotoFile == null || !mPhotoFile.exists()) {
            mPhotoView.setImageDrawable(null);
        } else {
            Bitmap bitmap = PictureUtils.getScaledBitmap(
                    mPhotoFile.getPath(), getActivity()
            );
            mPhotoView.setImageBitmap(bitmap);
        }
    }
}
