package com.example.kusano.kaimonomemo2018;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, MainFragment.OnClickListener
{
    ActionBarDrawerToggle toggle;
    DrawerLayout drawer;

    FragmentTransaction transaction;
    Fragment mFragment;

    static DBAdapter dbAdapter;

//    String strTtlPopup;
//    String strTtlRowDarkColor;

    //2023.01.01 --- 追加
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbAdapter = new DBAdapter(this);

        //SYSTEM_UI_FLAG_HIDE_NAVIGATION：呼ばれた画面でナビゲーションバーを隠す方法。
        //SYSTEM_UI_FLAG_IMMERSIVE_STICKY：一定時間経過すると、再度非表示にする。
        //SYSTEM_UI_FLAG_FULLSCREEN：ステータスバーを非表示にする
//        View decor = this.getWindow().getDecorView();
//        decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

//        strTtlPopup = getString(R.string.edit_mode);
//        strTtlRowDarkColor = getString(R.string.reverse_mode);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        //toolbar.setNavigationIcon(R.drawable.ic_navigation_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ナビゲーションアイコンクリック時の処理

            }
        });

        toolbar.inflateMenu(R.menu.main);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // アイテムクリック時の処理
                return true;
            }
        });

        //2018.07.15 追加
        setSupportActionBar(toolbar);

        //2023.01.02 修正
        //タイトルバーのアプリ名を非表示にする
        //getSupportActionBar().setTitle(R.string.app_name);
        getSupportActionBar().setTitle("");

        //ナビゲーションドロワーの設定
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        //第三引数でHomeAsUpアイコンを指定。
        //第四・第五引数は、String.xmlで適当な文字列を。
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //2023.01.01 --- 追加
        fab = (FloatingActionButton) findViewById(R.id.fab_add_new);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_main);
                if (fragment != null && fragment instanceof MainFragment) {
                    ((MainFragment) fragment).add_menu();
                }
            }
        });

        // 画面がはじめて作成された時にinflaterだけ、Fragmentを追加する
        if (savedInstanceState == null)
        {
            // Fragmentを作成します
            FragmentManager fragmentManager = getSupportFragmentManager();
            transaction = fragmentManager.beginTransaction();

            // 実際に使用するFragmentの作成
            mFragment = new MainFragment();
//            Fragment mFragment = new MainFragment();

            //ActivityへFragmentを組み込む
//            Transaction transaction.replace(R.id.fragment_main, mFragment);
            transaction.replace(R.id.fragment_main, mFragment);

            Log.v("xxx:", "☆★☆★☆★☆★☆ MainActivity.onCreate ★☆★☆★☆★☆★");

            // 最後にcommitします
            transaction.commit();
        }
    }
/*
    @Override
    public void onPause()
    {
        Log.v("xxx:", "☆★☆★☆★☆★☆ MainActivity.onPause ★☆★☆★☆★☆★");

    }
*/
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        String strMyFileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/KaimonoMemo.csv";

        if (id == R.id.all_items)
        {
            // データを渡す為のBundleを生成し、渡すデータを内包させる
            Bundle bundle = new Bundle();

            // Fragmentを作成します
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();

            // 実際に使用するFragmentの作成
            Fragment mFragment = new MainFragment();

            bundle.putString("bdl_key", "ToBuyOrNot");
            bundle.putString("bdl_value", null);
            mFragment.setArguments(bundle);

            //ActivityへFragmentを組み込む
            transaction.replace(R.id.fragment_main, mFragment);

            // 最後にcommitします
            transaction.commit();
//Log.v("xxx:", "☆★☆★☆★☆★☆ passed ★☆★☆★☆★☆★");
        }
        else if (id == R.id.need_to_buy)
        {
            // データを渡す為のBundleを生成し、渡すデータを内包させる
            Bundle bundle = new Bundle();

            // Fragmentを作成します
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();

            // 実際に使用するFragmentの作成
            Fragment mFragment = new MainFragment();

            bundle.putString("bdl_key", "ToBuyOrNot");
            bundle.putString("bdl_value", "0");
            mFragment.setArguments(bundle);

            //ActivityへFragmentを組み込む
            transaction.replace(R.id.fragment_main, mFragment);

            // 最後にcommitします
            transaction.commit();
//Log.v("xxx:", "☆★☆★☆★☆★☆ passed ★☆★☆★☆★☆★");
        }
        else if (id == R.id.NOT_need_to_buy)
        {
            // データを渡す為のBundleを生成し、渡すデータを内包させる
            Bundle bundle = new Bundle();

            // Fragmentを作成します
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();

            // 実際に使用するFragmentの作成
            Fragment mFragment = new MainFragment();

            bundle.putString("bdl_key", "ToBuyOrNot");
            bundle.putString("bdl_value", "1");
            mFragment.setArguments(bundle);

            //ActivityへFragmentを組み込む
            transaction.replace(R.id.fragment_main, mFragment);

            // 最後にcommitします
            transaction.commit();
//Log.v("xxx:", "☆★☆★☆★☆★☆ passed ★☆★☆★☆★☆★");

        }
        else if (id == R.id.navCSV_import)
        {
            //ＣＳＶ入力

                //2023.01.01 修正
                final AlertDialog alt = new AlertDialog.Builder(this)
                        .setTitle("復元の確認")
                        .setIcon(android.R.drawable. ic_dialog_alert)

                        //setViewにてビューを設定します。
                        .setPositiveButton("OK", new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int whichButton)
                            {
                                //2020.08.10 追加
                                try{
                                    // CSVファイルの読み込み
                                    FileInputStream inputStream = new FileInputStream(strMyFileName);

                                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                                    BufferedReader bufferReader = new BufferedReader(inputStreamReader);
                                    String line;

                                    dbAdapter.open();

                                    //一旦ＤＢ内全削除
                                    dbAdapter.deleteAllNotes();
                                    while ((line = bufferReader.readLine()) != null)
                                    {
                                        String[] RowData = line.split(",");
                                        dbAdapter.InertFromCSV( Integer.parseInt(RowData[0]), RowData[1], RowData[2], Integer.parseInt(RowData[3]), Integer.parseInt(RowData[4]), RowData[5], Integer.parseInt(RowData[6]), Integer.parseInt(RowData[7]), RowData[8], Integer.parseInt(RowData[9]));
                                    }
                                    dbAdapter.close();
                                    bufferReader.close();

                                    //2021.12.31 追加
                                    //フラグメントの再表示
                                    FragmentManager fragmentManager = getSupportFragmentManager();
                                    transaction = fragmentManager.beginTransaction();
                                    mFragment = new MainFragment();
                                    transaction.replace(R.id.fragment_main, mFragment).commit();

                                    Toast ts = Toast.makeText(getBaseContext(), getString(R.string.csv_recovery)+"が完了しました", Toast.LENGTH_SHORT);
                                    ts.show();
                                    //return true;


                                } catch (IOException e) {
                                    e.printStackTrace();
                                    //return false;
                                }

                            }
                        })
                        .setNegativeButton("キャンセル", null)
                        .create();
                alt.show();

        }
        else if (id == R.id.navCSV_export)
        {
            //ＣＳＶ出力
            Context con = getApplicationContext();
            final int MY_PERMISSIONS_REQUEST_CODE = 200;

            //権限があるか検査する
            if (con.checkSelfPermission(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                //権限がないので要求する
                requestPermissions(
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_CODE);
            }

            // 外部ストレージがマウントされている事を確認
            String state = Environment.getExternalStorageState();
            if (!Environment.MEDIA_MOUNTED.equals(state))
            {
                return false;
            }else{
                File exportDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                if (!exportDir.exists())
                {
                    exportDir.mkdirs();
                }

                File file;
                PrintWriter printWriter = null;
                try
                {
                    file = new File(strMyFileName);
                    file.createNewFile();
                    printWriter = new PrintWriter(new FileWriter(file));

                    dbAdapter.open();
                    Cursor curCSV = dbAdapter.getAllNotes(null, null);
                    startManagingCursor(curCSV);

                    // CSVファイルのヘッダーを書き出し
                    //printWriter.println("ID,TITLE,ACCOUNT,PASSWORD,MEMO,INPUTDATE");
                    // データの行数分CSV形式でデータを書き出し

                    if (curCSV.moveToFirst())
                    {
                        do
                            {
                                String csv_id = curCSV.getString(curCSV.getColumnIndex(DBAdapter.COL_ID));
                                String csv_goods = curCSV.getString(curCSV.getColumnIndex(DBAdapter.COL_NOTE));
                                String csv_lastupdate = curCSV.getString(curCSV.getColumnIndex(DBAdapter.COL_LASTUPDATE));
                                String csv_priority = curCSV.getString(curCSV.getColumnIndex(DBAdapter.COL_PRIORITY));
                                String csv_number = curCSV.getString(curCSV.getColumnIndex(DBAdapter.COL_NUMBER));
                                String csv_shop = curCSV.getString(curCSV.getColumnIndex(DBAdapter.COL_SHOP));
                                String csv_listprice = curCSV.getString(curCSV.getColumnIndex(DBAdapter.COL_LISTPRICE));
                                String csv_ToBuyOrNot = curCSV.getString(curCSV.getColumnIndex(DBAdapter.COL_TOBUYORNOT));
                                String csv_brand = curCSV.getString(curCSV.getColumnIndex(DBAdapter.COL_BRAND));
                                String csv_sort_id = curCSV.getString(curCSV.getColumnIndex(DBAdapter.COL_SORTID));

                                String record = csv_id + "," + csv_goods + "," + csv_lastupdate + "," + csv_priority + ","
                                        + csv_number + "," + csv_shop + "," + csv_listprice + "," + csv_ToBuyOrNot + "," + csv_brand + "," + csv_sort_id;
                                printWriter.println(record);
                        }
                        while (curCSV.moveToNext());
                    }

                    curCSV.close();
                    dbAdapter.close();
                }
                catch (FileNotFoundException exc)
                {
                    // フォルダへのアクセス権限がない場合の表示
                    Toast ts = Toast.makeText(this, "アクセス権限がありません", Toast.LENGTH_SHORT);
                    ts.show();
                    return false;
                }
                catch (Exception exc)
                {
                    Toast ts = Toast.makeText(this, "CSV出力が失敗しました: "+exc.getMessage(), Toast.LENGTH_SHORT);
                    ts.show();
//Log.v("xxx:", "☆★☆★☆★☆★☆ "+exc.getMessage()+" ★☆★☆★☆★☆★");
                    return false;
                }
                finally
                {
                    if (printWriter != null) printWriter.close();
                }

                //PC\SC-02K\Phone\Download
                Toast ts = Toast.makeText(this, getString(R.string.csv_backup)+"が完了しました", Toast.LENGTH_SHORT);
                ts.show();
                return true;
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        //ナビゲーションドロワーを閉じる
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed()
    {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(){}
}
