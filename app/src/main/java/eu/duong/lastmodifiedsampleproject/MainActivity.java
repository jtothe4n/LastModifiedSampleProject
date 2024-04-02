package eu.duong.lastmodifiedsampleproject;

import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Bundle;

import com.anggrayudi.storage.file.DocumentFileUtils;
import com.anggrayudi.storage.file.FileUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import androidx.core.view.WindowCompat;
import androidx.documentfile.provider.DocumentFile;

import eu.duong.lastmodifiedsampleproject.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    String TAG = "lastmodifiedsampleproject";

    private static String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    File selectedFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getUSBDevice();
        binding.refesh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getUSBDevice();
            }
        });

        binding.selectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("*/*");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);


                mGetContent.launch(intent);

            }
        });

        binding.permission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestAllFilesAccess();
            }
        });

        binding.setLastmodified.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d(TAG, "setLastModified");
                Log.d(TAG, "path:" + selectedFile.getAbsolutePath());
                boolean result= selectedFile.setLastModified(new Date().getTime());
                Log.d(TAG, "result: " + result);

                binding.result.setText(result ? "Success" : "Failed");
                binding.result.setTextColor(getColor(result ? R.color.green :  R.color.red));
            }
        });
    }
    ActivityResultLauncher<Intent> mGetContent = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult o) {


            Intent intent = o.getData();
            if(intent != null) {

                Uri fileUri = intent.getData();
                DocumentFile documentFile = DocumentFile.fromSingleUri(getApplicationContext(), fileUri);

                String filePath = DocumentFileUtils.getAbsolutePath(documentFile, getApplicationContext());

                selectedFile = new File(filePath);

                if(!documentFile.exists())
                    return;

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                binding.currentDate.setText(sdf.format(new Date(documentFile.lastModified())));
                binding.filename.setText(filePath);
                binding.setLastmodified.setEnabled(true);

            }

        }
    });

    @Override
    public void onResume() {
        super.onResume();

        boolean hasAllFilesAccess = Environment.isExternalStorageManager();
        binding.refesh.setEnabled(hasAllFilesAccess);
        binding.permission.setEnabled(!hasAllFilesAccess);
        getUSBDevice();
    }

    public void getUSBDevice() {
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);

        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();


        binding.selectFile.setEnabled(false);
        if(deviceList.size() == 0)
        {
            binding.usbDevice.setText("No Device connected");
            return;
        }

        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();

            String name = device.getProductName();

            String actionString = getPackageName() + ".action.USB_PERMISSION";

            PendingIntent permissionIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, new
                    Intent(actionString), PendingIntent.FLAG_IMMUTABLE);
            manager.requestPermission(device, permissionIntent);

            binding.usbDevice.setText(name);
            binding.selectFile.setEnabled(binding.refesh.isEnabled());
            return;

        }}


    private void requestAllFilesAccess()
    {

        try {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        }
        catch (Exception ex)
        {
            Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }


    }


}