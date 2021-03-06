package com.example.nikhil.roadsafety;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AlertDialog;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;


import com.android.internal.telephony.ITelephony;

import java.lang.reflect.Method;

public class IncomingCallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        ITelephony telephonyService;
        Log.i("Messages", "Entered onReceive");
        try {
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);

            if(state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_RINGING)){
                TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                try {
                    Method m = tm.getClass().getDeclaredMethod("getITelephony");

                    m.setAccessible(true);
                    telephonyService = (ITelephony) m.invoke(tm);

                    if ((number != null)) {
                        ContactDatabaseHelper myDB = new ContactDatabaseHelper(context);
                        Cursor cr = myDB.getNumbers();
                        Boolean hasNumber = false;
                        while (cr.moveToNext()){
                            String allowedNumber = cr.getString(cr.getColumnIndexOrThrow("contact_no"));
                            long origNo = Long.parseLong(number.substring(number.length()-10));
                            long allowedNo = Long.parseLong(allowedNumber.substring(allowedNumber.length()-11, allowedNumber.length()-6)
                                    + allowedNumber.substring(allowedNumber.length()-5, allowedNumber.length()));
                            hasNumber = (origNo == allowedNo);
                            Log.i("NumberLogs", String.valueOf(origNo)+"\t"+String.valueOf(allowedNo)+"\t"+String.valueOf(hasNumber)+"\t");
                        }
                        if(!hasNumber) {
                            telephonyService.endCall();
                            sendSMS(number, context);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i("Messages", "Catch exception 1 "+ e.toString());
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.i("Messages", "Catch exception 2 "+ e.toString());
        }
    }

    private void sendSMS(String number, Context context){
        DrivingModeFragment.incomingCallList.add(number);
        String message = "Hi, I am driving at the moment. Kindly leave a message or call again later.";
        try {
            SmsManager.getDefault().sendTextMessage(number, null,
                    message, null, null);
        } catch (Exception e) {
            AlertDialog.Builder alertDialogBuilder = new
                    AlertDialog.Builder(context);
            AlertDialog dialog = alertDialogBuilder.create();
            dialog.setMessage(e.getMessage());
            dialog.show();
        }
    }
}