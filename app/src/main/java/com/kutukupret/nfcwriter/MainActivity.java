package com.kutukupret.nfcwriter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
//import android.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {
    private NFCManager nfcMger;

    private View v;
    private NdefMessage message = null;
    private ProgressDialog dialog;
    Tag currentTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        nfcMger = new NFCManager(this);

        v = findViewById(R.id.mainLyt);

        final Spinner sp = (Spinner) findViewById(R.id.tagType);
        ArrayAdapter<CharSequence> aa = ArrayAdapter.createFromResource(this, R.array.tagContentType, android.R.layout.simple_spinner_dropdown_item);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(aa);

        final EditText et = (EditText) findViewById(R.id.content);

        FloatingActionButton btn = (FloatingActionButton) findViewById(R.id.fab);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = sp.getSelectedItemPosition();
                String content = et.getText().toString();

                switch (pos) {
                    case 0:
                        message =  nfcMger.createUriMessage(content, "http://");
                        break;
                    case 1:
                        message =  nfcMger.createUriMessage(content, "tel:");
                        break;
                    case 2:
                        message =  nfcMger.createTextMessage(content);
                        break;
                }

                if (message != null) {

                    dialog = new ProgressDialog(MainActivity.this);
                    dialog.setMessage("Tag NFC Tag please");
                    dialog.show();;
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }




    @Override
    protected void onResume() {
        super.onResume();

        try {
            nfcMger.verifyNFC();
            //nfcMger.enableDispatch();

            Intent nfcIntent = new Intent(this, getClass());
            nfcIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, nfcIntent, 0);
            IntentFilter[] intentFiltersArray = new IntentFilter[] {};
            String[][] techList = new String[][] { { android.nfc.tech.Ndef.class.getName() }, { android.nfc.tech.NdefFormatable.class.getName() } };
            NfcAdapter nfcAdpt = NfcAdapter.getDefaultAdapter(this);
            nfcAdpt.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techList);
        }
        catch(NFCManager.NFCNotSupported nfcnsup) {
            Snackbar.make(v, "NFC not supported", Snackbar.LENGTH_LONG).show();
        }
        catch(NFCManager.NFCNotEnabled nfcnEn) {
            Snackbar.make(v, "NFC Not enabled", Snackbar.LENGTH_LONG).show();
        }

    }


    @Override
    protected void onPause() {
        super.onPause();
        nfcMger.disableDispatch();
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d("Nfc", "New intent");
        // It is the time to write the tag
        currentTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (message != null) {
            nfcMger.writeTag(currentTag, message);
            dialog.dismiss();
            Snackbar.make(v, "Tag written", Snackbar.LENGTH_LONG).show();

        } else {
            // Handle intent

        }
    }

}
