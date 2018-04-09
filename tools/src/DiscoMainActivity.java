package targetpackagename;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import dalvik.system.DexClassLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;

/**
 * Created by ysht on 2016/4/9 0009.
 */
public class DiscoMainActivity extends Activity {
    public static ClassLoader ORIGINAL_LOADER;
    public static ClassLoader CUSTOM_LOADER = null;
    MyClassLoader cl;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String title = "enc.apk";
        String path = "apks/" + title;


        try {

            File dex = getDir("dex", Context.MODE_PRIVATE);
            dex.mkdir();
            File f = new File(dex, title);


            InputStream fis = getAssets().open(path);
            FileOutputStream fos = new FileOutputStream(f);
            int oneByte;
            while((oneByte = fis.read()) >= 0){
                fos.write(oneByte);
            }
            //EncDec.decryptStream("rutgers!rutgers!", fis, fos);

            fis.close();
            fos.close();

            try {
                Context mBase = new Smith<Context>(this, "mBase").get();

                Object mPackageInfo = new Smith<Object>(mBase, "mPackageInfo")
                        .get();

                Smith<ClassLoader> sClassLoader = new Smith<ClassLoader>(
                        mPackageInfo, "mClassLoader");
                ClassLoader mClassLoader = sClassLoader.get();
                ORIGINAL_LOADER = mClassLoader;

                MyClassLoader cl = new MyClassLoader(mClassLoader);
                sClassLoader.set(cl);
            } catch (Exception e) {
                e.printStackTrace();
            }

            File fo = getDir("outdex", Context.MODE_PRIVATE);
            fo.mkdir();


            DexClassLoader dcl = new DexClassLoader(f.getAbsolutePath(),
                    fo.getAbsolutePath(), null,
                    ORIGINAL_LOADER.getParent());

            CUSTOM_LOADER = dcl;

            try {
                Class<?> classToInvestigate = dcl.loadClass("targetpackagename.MainActivity");
                Method m = classToInvestigate.getMethod("onCreate", android.os.Bundle.class);
                Object o = classToInvestigate.newInstance();
                m.invoke(o);
            } catch (ClassNotFoundException e) {
                Log.i("DEBUG", "ClassNotFound " + e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
                Log.i("DEBUG", "Exception " + e.getMessage());
                e.printStackTrace();
            }

            Intent i = new Intent("targetactionname");
            startActivityForResult(i, 0);

        } catch (Exception e) {
            Toast.makeText(this, "Error " + e, Toast.LENGTH_SHORT)
                    .show();
            e.printStackTrace();
            CUSTOM_LOADER = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        finish();
    }

    class MyClassLoader extends ClassLoader {
        public MyClassLoader(ClassLoader parent) {
            super(parent);
        }

        @Override
        public Class<?> loadClass(String className)
                throws ClassNotFoundException {
            if (CUSTOM_LOADER != null) {
                if (className.startsWith("targetpackagename")) {
                    Log.i("classloader", "loadClass( " + className + " )");
                }
                try {
                    Class<?> c = CUSTOM_LOADER.loadClass(className);
                    if (c != null)
                        return c;
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            return super.loadClass(className);
        }
    }
}
