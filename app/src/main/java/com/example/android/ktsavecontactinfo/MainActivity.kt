package com.example.android.ktsavecontactinfo

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat


class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "LOG_TAG"
    }

    private val requestSinglePermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            // Do something if permission granted
            if (isGranted) {
                Log.d("LOG_TAG", "permission granted by the user")

                // Do something as the permission is not granted
            } else {
                Log.d("LOG_TAG", "permission denied by the user")
            }
        }


    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                Log.e("LOG_TAG", "${it.key} = ${it.value}")
            }
        }


    private var allNumLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data = result.data?.data

                data?.let {
                    val cursor = contentResolver.query(
                        data,
                        null,
                        null,
                        null,
                        null
                    )

                    cursor?.let {
                        if (it.moveToFirst()) {
                            val name =
                                it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))

                            if (Integer.parseInt(
                                    it.getString(
                                        it.getColumnIndex(
                                            ContactsContract.Contacts.HAS_PHONE_NUMBER
                                        )
                                    )
                                ) > 0 // Check if the contact has phone numbers
                            ) {

                                val id =
                                    it.getString(it.getColumnIndex(ContactsContract.Contacts._ID))

                                val phonesCursor = contentResolver.query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                    null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,
                                    null,
                                    null
                                )

                                val numbers = mutableSetOf<String>()
                                phonesCursor?.let {
                                    while (phonesCursor.moveToNext()) {
                                        val phoneNumber =
                                            phonesCursor.getString(
                                                phonesCursor.getColumnIndex(
                                                    ContactsContract.CommonDataKinds.Phone.NUMBER
                                                )
                                            ).replace("-", "").replace(" ", "")
                                        numbers.add(phoneNumber)
                                    }
                                    Toast.makeText(
                                        this@MainActivity,
                                        "$name $numbers",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    Log.d(TAG, "$name $numbers")
                                }

                                phonesCursor?.close()

                            } else {
                                Toast.makeText(
                                    this@MainActivity,
                                    "$name - No numbers",
                                    Toast.LENGTH_LONG
                                ).show()
                                Log.d(TAG, "$name - No numbers")
                            }
                        }

                        cursor.close()
                    }

                }
            }
        }


    private var oneNumLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data?.data
                data?.let {
                    val cursor = contentResolver.query(
                        data,
                        null,
                        null,
                        null,
                        null
                    )

                    while (cursor?.moveToNext()!!) {
                        val name =
                            cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))
                        val number =
                            cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                        Toast.makeText(this@MainActivity, "$name $number", Toast.LENGTH_LONG).show()
                        Log.d(TAG, ": Name $name $number")
                    }

                    cursor.close()
                }
            }
        }


    private var temp =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {

                val cursor1: Cursor
                val cursor2: Cursor?

                // get data from intent
                val uri = result.data?.data
                cursor1 = contentResolver.query(uri!!, null, null, null, null)!!
                if (cursor1.moveToFirst()) {
                    // get contact details
                    val contactId =
                        cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts._ID))
                    val contactName =
                        cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                    val contactThumbnail =
                        cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI))
                    val idResults =
                        cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                    val idResultHold = idResults.toInt()
                    // check if contact has a phone number or not
                    if (idResultHold == 1) {
                        cursor2 = contentResolver.query(
//                            ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI, // WRONG
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId,
                            null,
                            null
                        )

                        // a contact may have multiple phone numbers
                        val numbers = mutableSetOf<String>()
                        cursor2?.let {
                            while (cursor2.moveToNext()) {
                                val phoneNumber =
                                    cursor2.getString(
                                        cursor2.getColumnIndex(
                                            ContactsContract.CommonDataKinds.Phone.NUMBER
                                        )
                                    ).replace("-", "").replace(
                                        " ",
                                        ""
                                    )  // Remove the dash sign & spaces from the numbers

                                numbers.add(phoneNumber)
                            }
                            Toast.makeText(
                                this@MainActivity,
                                "$contactName $numbers",
                                Toast.LENGTH_LONG
                            ).show()

                            cursor2.close()
                        }
                        cursor1.close()
                    }
                }

            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnGetContactAllNum = findViewById<Button>(R.id.btnGetContactAllNum)
        val btnGetContactSingleNum = findViewById<Button>(R.id.btnGetContactSingleNum)


        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestSinglePermission.launch(
                Manifest.permission.READ_CONTACTS
            )
//            requestMultiplePermissions.launch(
//                arrayOf(
//                    Manifest.permission.READ_CONTACTS,
//                    Manifest.permission.READ_PHONE_NUMBERS,
//                    Manifest.permission.READ_PHONE_STATE
//                )
//            )
        }


        btnGetContactAllNum.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI
            ) // Get all contact phone numbers
            allNumLauncher.launch(intent)
//            temp.launch(intent)
        }

        btnGetContactSingleNum.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_PICK,
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI // // Get a single phone number
            )
            oneNumLauncher.launch(intent)
        }

    }
}