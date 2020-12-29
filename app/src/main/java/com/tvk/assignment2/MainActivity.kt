package com.tvk.assignment2

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec


class MainActivity : AppCompatActivity() {

    val MY_PREFERENCES ="MY_PREFS";
    val KEY_NAME = "USERNAME";
    val KEY_PASSWORD ="PASSWORD";
    val KEY_PASSWORDIV = "PASSWORDIV";
    private val file = "mydata"

 var sharedPreferences: SharedPreferences? =null;



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        findViewById<Button>(R.id.clear_text).setOnClickListener {
            findViewById<TextView>(R.id.usernameview).text = null;
            findViewById<TextView>(R.id.passwordView).text = null;
        }

        sharedPreferences = this.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
//Key
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        val keyGenParameterSpec = KeyGenParameterSpec.Builder("MyKeyAlias",
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()

        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()

    /*    val pair = encryptData("Test this encryption")
      val decryptedData = decryptData(pair.first, pair.second)
      val encrypted = pair.second.toString(Charsets.UTF_8)
        println("Encrypted data: $encrypted")
       println("Decrypted data: $decryptedData")*/

        findViewById<Button>(R.id.internalview).setOnClickListener {
            try {
                val fin: FileInputStream = openFileInput(file)
                var  c:Int =0;
                var temp = ""
                while (fin.read().also({ c = it }) != -1) {
                    temp = temp + Character.toString(c.toChar())
                }
                findViewById<TextView>(R.id.usernameview).text= (temp)
                Toast.makeText(baseContext, "file read", Toast.LENGTH_SHORT).show()
            }
            catch (e: java.lang.Exception) {
            }
        }

        findViewById<Button>(R.id.sharedview).setOnClickListener {

            findViewById<TextView>(R.id.usernameview).text = sharedPreferences?.getString(KEY_NAME,"default");
           var passwordencry = sharedPreferences?.getString(KEY_PASSWORD,null);
            var passwordIV = sharedPreferences?.getString(KEY_PASSWORDIV,null);
           // var passwordcry :String? = sharedPreferences?.getString("encryptionIv","default");

            if(passwordencry!=null && passwordIV!=null) {
                val decryptedData = decryptData(Base64.decode(passwordIV, Base64.DEFAULT), Base64.decode(passwordencry, Base64.DEFAULT))
                //   val encrypted_data =
                findViewById<TextView>(R.id.passwordView).text = decryptedData;
                Log.d("Password Encrypted", passwordIV);
            }

        }


        findViewById<Button>(R.id.savebutton).setOnClickListener {view->

            val editor = sharedPreferences!!.edit();

                val username = findViewById<EditText>(R.id.editname).text;
                val password = findViewById<EditText>(R.id.editPassword).text.toString();

                val pair = encryptData(password)
                val encrypted = pair.first.toString(Charsets.UTF_8)
               // println("Encrypted data: $encrypted")
                Log.d("Encrypted",encrypted);
            val decryptedData = decryptData(pair.first, pair.second)
            println("Decrypted data: $decryptedData")

                    editor.putString(KEY_PASSWORD,Base64.encodeToString(pair.second,Base64.DEFAULT));
             // editor.putString(KEY_PASSWORD, pair.first.toString(Charsets.UTF_8));

                //     editor.putString(KEY_PASSWORDIV,pair.second.toString(Charsets.UTF_8) );
                     editor.putString(KEY_PASSWORDIV,Base64.encodeToString(pair.first,Base64.DEFAULT));

                //Shared
                editor.putString(KEY_NAME,username.toString());
                editor.apply();
                editor.commit();

                //File
                val fOut: FileOutputStream = openFileOutput(file, Context.MODE_PRIVATE);
                fOut.write(findViewById<EditText>(R.id.editname).text.toString().toByteArray());

                fOut.close();
                Toast.makeText(baseContext, "file saved", Toast.LENGTH_SHORT).show()

                Snackbar.make(view,"Saved",Snackbar.LENGTH_LONG)
                    .setAction("Action",null).show();


        }



    }



    fun getKey(): SecretKey {
        val keystore = KeyStore.getInstance("AndroidKeyStore")
        keystore.load(null)

        val secretKeyEntry = keystore.getEntry("MyKeyAlias", null) as KeyStore.SecretKeyEntry
        return secretKeyEntry.secretKey
    }

    fun encryptData(data: String): Pair<ByteArray, ByteArray> {
        val cipher = Cipher.getInstance("AES/CBC/NoPadding")

        var temp = data
        while (temp.toByteArray().size % 16 != 0)
            temp += "\u0020"

        cipher.init(Cipher.ENCRYPT_MODE, getKey())

        val ivBytes = cipher.iv
        val encryptedBytes = cipher.doFinal(temp.toByteArray(Charsets.UTF_8))

        return Pair(ivBytes, encryptedBytes)
    }

    fun decryptData(ivBytes: ByteArray, data: ByteArray): String{
        val cipher = Cipher.getInstance("AES/CBC/NoPadding")
        val spec = IvParameterSpec(ivBytes)

        cipher.init(Cipher.DECRYPT_MODE, getKey(), spec)
        return cipher.doFinal(data).toString(Charsets.UTF_8).trim()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> {
                Toast.makeText(this,"dk",Toast.LENGTH_SHORT).show();
               var intent = Intent(this,SettingsActivity::class.java);
                 startActivity(intent)
                return true;
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}
