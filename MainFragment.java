package com.example.kusano.kaimonomemo2018;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

//import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;

public class MainFragment extends Fragment implements RecyclerViewAdapter.Listener
{
    private RecyclerView mRecyclerView;
    private RecyclerViewAdapter mRecyclerViewAdapter;
    private View mView;

    String txtInsert;
    private final String SAVE_KEY = "save_key";

    static DBAdapter dbAdapter;
    private List<Note> lstGoodsList;
//    List<Note> lstGoodsList = new ArrayList<Note>();

    String strTtlPopup;
    String strTtlRowDarkColor;
    String strTitleNow;		//メニューバーの現在表示文字列

    //2019.12.30 追加
    //リスト登録時に使用
    String strArgKey;
    String strArgValue;

    ItemTouchHelper mIth;

    private OnClickListener mListener;
    private SearchView searchView;

    //2020.12.31 --- 追加
    ImageView imgAdd;

    //2020.12.30 --- 追加
    SharedPreferences pref;
//    Gson gson;
//    String json;

    //2023.01.01 --- 追加
    FloatingActionButton fab;

    public interface OnClickListener
    {
        void onClick();
    }

    //--------------------------------------------------------------------------------------------------

    // FragmentがActivityに追加されたら呼ばれるメソッド
    // ContextにFragmentがアタッチされた際に呼び出されるライフサイクルイベントです。このタイミング以降でActivityなどのContextを参照することが可能になります。
   @Override
   public void onAttach(Context context)
   {
       super.onAttach(context);
       dbAdapter = new DBAdapter(getActivity());

       //2020.12.30 --- 追加
       pref = this.getActivity().getSharedPreferences("pref", MODE_PRIVATE);
//       gson = new Gson();
//       json = pref.getString(SAVE_KEY, "");

       //2020.12.30 --- sqliteとJSONとの同期がうまくいかず、既存の機能が死んでしまうので断念
       //マスタなど、メイン以外のリストに使うかもしれない
       //loadFromGson();
       lstGoodsList = new ArrayList<Note>();

       try
       {
           mListener = (OnClickListener) context;
       }
       catch (ClassCastException e)
       {
           throw new ClassCastException(getActivity().toString() + "must implement OnArticleSelectedListener.");
       }
       Log.v("xxx:", "☆★☆★☆★☆★☆ onAttach ★☆★☆★☆★☆★");
   }

   // Fragmentが生成される際に呼び出されるライフサイクルイベント
   // この時点ではViewの生成がまだ終わっていないので、状態の復元をするのはお勧めしません。
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Bundleの値を受け取る際はonCreateメソッド内で行う
        Bundle args = getArguments();

        // Bundleがセットされていなかった時はNullなのでNullチェックをする
        if (args != null)
        {
            Log.v("xxx:", "☆★☆★☆★☆★☆ onCreate ★☆★☆★☆★☆★");
            strArgKey = args.getString("bdl_key");
            strArgValue = args.getString("bdl_value");
            txtInsert = args.getString("bdlTxtInsert");
        }

        loadNote();
    }

    // Fragmentで表示するViewを作成するメソッド
    // このFragmentのメインコンテンツとなるViewを生成して返す必要があるライフサイクルイベント
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);

        strTtlPopup = getString(R.string.edit_mode);
        strTtlRowDarkColor = getString(R.string.reverse_mode);

        //2020.05.06 --- 追加
        strTitleNow = strTtlRowDarkColor;

        mView = inflater.inflate(R.layout.activity_main, container, false);

        // FragmentでMenuを表示する為に必要
        setHasOptionsMenu(true);

        Log.v("xxx:", "☆★☆★☆★☆★☆ onCreateView ★☆★☆★☆★☆★");

        return mView;
    }

    // Viewが生成し終わった時に呼ばれるメソッド
    // Viewの初期化とFragmentの状態の復元はここで行うことを推奨
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView = (RecyclerView) mView.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mRecyclerViewAdapter = new RecyclerViewAdapter(getActivity(), lstGoodsList, this);
        mRecyclerView.setAdapter(mRecyclerViewAdapter);

        //2018.06.26 追加
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST);
        mRecyclerView.addItemDecoration(itemDecoration);

        //行反転モードの時
        mIth = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT) {
                    public boolean onMove(RecyclerView recyclerView, ViewHolder viewHolder, ViewHolder target) {
//                        return false;// true if moved, false otherwise
                        return true;// true if moved, false otherwise
                    }

                    @Override
                    //長押しによる遷移処理→無効化予定
                    public void onMoved(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, int fromPos, RecyclerView.ViewHolder target, int toPos, int x, int y) {
//                        super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y);
                    }

                    //スワイプによる削除処理
                    public void onSwiped(ViewHolder viewHolder, int direction) {
                        final int fromPos = viewHolder.getAdapterPosition();
                        //Toast.makeText(getBaseContext(), Integer.toString(fromPos) , Toast.LENGTH_SHORT).show();

                        //2021.12.31 --- 修正
                        if(strTitleNow != strTtlRowDarkColor)
                        {
                            mRecyclerViewAdapter.deleteGoods(fromPos);
                            lstGoodsList.remove(fromPos);
                        }
                    }
                });
        mIth.attachToRecyclerView(mRecyclerView);
    }

    @Override
    public void onStart()
    {
        super.onStart();
        if(txtInsert != null)
        {
//Log.v("xxx:", strArgKey+"  "+strArgValue+"  "+"☆★☆★☆★☆★☆onCreate passed ★☆★☆★☆★☆★");
//Log.v("xxx:", "☆★☆★☆★☆★☆ txtInsert: " +txtInsert+ "★☆★☆★☆★☆★");
            mListener.onClick();
            saveItem();

            //新しい商品が追加されたら、一番下の画面へスクロール
            mRecyclerView.scrollToPosition(mRecyclerViewAdapter.getItemCount() - 1);
        }
        Log.v("xxx:", "☆★☆★☆★☆★☆ onStart ★☆★☆★☆★☆★");
    }

    @Override
    public void onResume()
    {
        super.onResume();

        loadNote();

        //2021.12.31 --- 追加
        mRecyclerViewAdapter.notifyDataSetChanged();

        Log.v("xxx:", "☆★☆★☆★☆★☆ onResume ★☆★☆★☆★☆★");
    }

//--------------------------------------------------------------------------------------------------

    //2024.1.1 --- 追加
    public void onCheckboxClicked(View view, int position,boolean isChecked)
    {
//        Log.v("xxx:", position+"  "+"☆★☆★☆★☆★☆ onCheckboxClicked ★☆★☆★☆★☆★");

        Note note = lstGoodsList.get(position);

        final int intNoteId = note.getId();					//noteID
//        final int intPriority = note.getPriority();		    //優先順位フラグ
        final String strNote = note.getNote();				//メモ

//        Log.d("xxx:", "☆★☆★☆★☆★☆ strNote intNoteId "+strNote+" "+intNoteId+"★☆★☆★☆★☆★");
//        Toast.makeText(getActivity().getBaseContext(), "☆★☆★☆★☆★☆ strNote intNoteId isChecked "+strNote+" "+intNoteId+" "+isChecked+"★☆★☆★☆★☆★", Toast.LENGTH_SHORT).show();

        //        TextView textView = (TextView)view.findViewById(R.id.txtGoods);
        dbAdapter.open();

        //優先購入のチェック更新処理
        if(isChecked == true)
        {
            dbAdapter.updatePriority(intNoteId, strNote, 1);
        }else
        {
            dbAdapter.updatePriority(intNoteId, strNote, 2);
        }

        mRecyclerViewAdapter.notifyDataSetChanged();
        dbAdapter.close();
        loadNote();
    }

    //セルが１タップされた時の処理
    @Override
    public void onRecyclerClicked(View v, int position)
    {
        TextView textView = (TextView)v.findViewById(R.id.txtGoods);
        Note note = lstGoodsList.get(position);

        Log.d("xxx:", "☆★☆★☆★☆★☆ onRecyclerClicked ★☆★☆★☆★☆★");

        //行反転モードの時
        if(strTitleNow == strTtlRowDarkColor)
        {
            changeBGColor(v, note);
        }else{
            //編集モードの時
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
//            int count = fragmentManager.getBackStackEntryCount();
//            Log.v("xxx:", "☆★☆★☆★☆★☆ "+count+" ★☆★☆★☆★☆★");
            openPopupMenu(textView, note);
        }
    }

    public void changeBGColor(View v, Note note)
    {
        final int intNoteId = note.getId();					//noteID
        final int intToBuyOrNot = note.getToBuyOrNot();		//購買予定フラグ
        final String strNote = note.getNote();				//メモ

        TextView textView = (TextView)v.findViewById(R.id.txtGoods);
        dbAdapter.open();

//Log.v("xxx:", "☆★☆★☆★☆★☆ passed ★☆★☆★☆★☆★");

        //購買予定フラグが０ならフラグ→１・背景色→黒、１ならフラグ→０・背景色→白
        if(intToBuyOrNot==0)
        {
            //購入予定外へ
            textView.setBackgroundColor(ContextCompat.getColor(getActivity().getBaseContext(), R.color.colorWillnotBuy));
            textView.setTextColor(ContextCompat.getColor(getActivity().getBaseContext(), R.color.colorCell));
            dbAdapter.updateNoteToBuyOrNot(intNoteId, strNote, 1);

            //2021.12.31 --- 購入必要・不要フィルター実行時に、タップ都度反映させるために追加
            //検索結果時には、実行されない必要あり
            if(this.searchView.hasFocus()==false)
            {
                mRecyclerViewAdapter.notifyDataSetChanged();
            }
        }else{
            textView.setBackgroundColor(ContextCompat.getColor(getActivity().getBaseContext(), R.color.colorCell));
            textView.setTextColor(ContextCompat.getColor(getActivity().getBaseContext(), R.color.colorWillnotBuy));
            dbAdapter.updateNoteToBuyOrNot(intNoteId, strNote, 0);

            //2021.12.31 --- 購入必要・不要フィルター実行時に、タップ都度反映させるために追加
            if(this.searchView.hasFocus()==false){
                mRecyclerViewAdapter.notifyDataSetChanged();
            }
        }

        dbAdapter.close();
        loadNote();
    }

    //ポップアップメニュー表示
    public void openPopupMenu(View v, Note note)
    {
        final Note mNote = note;
        final View mView = v;
        final int intId = mNote.getId();                    //ID取得
        final  String strGoods = mNote.getNote();           //商品名取得
        final int intNumber = mNote.getNumber();			//購入数量取得

        //2020.01.01 --- 追加
        final int intListprice= mNote.getListprice();		//定価取得
        final String strShop= mNote.getShop();			    //販売店取得

        //2020.01.13 --- 追加
        final String strBrand= mNote.getBrand();			    //ブランド名取得

        //2022.12.25 --- 追加
        final int intPriority = mNote.getPriority();			    //優先度取得

        TextView textView = (TextView)mView.findViewById(R.id.txtGoods);
        PopupMenu pm = new PopupMenu(getActivity(), textView);

        pm.getMenuInflater().inflate(R.menu.popup_menu, pm.getMenu());
        pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    //商品名の編集
                    case R.id.menu_edit:
                        //Toast.makeText(MainActivity.this, "Clicked First Menu Item", Toast.LENGTH_SHORT).show();

                        //テキスト入力を受け付けるビューを作成します。
                        final EditText editView = new EditText(getActivity());

                        //2015.6.6 --- 追加
                        editView.setText(strGoods);

                        final AlertDialog alt = new AlertDialog.Builder(getActivity())
                                .setTitle(R.string.menu_edit)
                                .setIcon(android.R.drawable. ic_dialog_info)

                                //setViewにてビューを設定します。
                                .setView(editView)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener()
                                {
                                    public void onClick(DialogInterface dialog, int whichButton)
                                    {
                                        dbAdapter.open();

                                        if(dbAdapter.updateNote(intId, editView.getText().toString(), intNumber, intListprice ,strShop, strBrand))
                                        {
                                            loadNote();
                                            Toast.makeText(getActivity().getBaseContext(), strGoods + " ⇒ " + editView.getText().toString() + " に編集されました", Toast.LENGTH_SHORT).show();
                                            mRecyclerViewAdapter.notifyDataSetChanged();
                                        }
                                        dbAdapter.close();
                                    }
                                })
                                .setNegativeButton("キャンセル", null)
                                .create();

                        editView.setOnFocusChangeListener(new View.OnFocusChangeListener()
                        {
                            @Override
                            public void onFocusChange(View v, boolean hasFocus)
                            {
                                if (hasFocus)
                                {
                                    alt.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                                }
                            }
                        });

                        editView.setInputType(InputType.TYPE_CLASS_TEXT);
                        alt.show();

                        return true;

                    //行反転
                    case R.id.menu_changebgColor:
                        changeBGColor(mView, mNote);
                        return true;

                    //商品詳細
                    case R.id.menu_detail:

                        //2020.01.02 追加
                        // データを渡す為のBundleを生成し、渡すデータを内包させる
                        Bundle bundle = new Bundle();

                        bundle.putInt("bdl_ArgID", intId);
                        bundle.putString("bdl_ArgGoods", strGoods);
                        bundle.putInt("bdl_ArgNumber", intNumber);
                        bundle.putInt("bdl_ArgListprice", intListprice);
                        bundle.putString("bdl_ArgShop", strShop);
                        bundle.putString("bdl_ArgBrand", strBrand);

                        //2022.12.25 --- 追加
                        bundle.putInt("bdl_ArgPriority", intPriority);

                        //2020.01.01 --- 追加
                        // 実際に使用するFragmentの作成
                        Fragment dtlFragment = new DetailFragment();
                        dtlFragment.setArguments(bundle);

                        FragmentManager fragmentManager = getChildFragmentManager();
                        if(fragmentManager != null)
                        {
                            FragmentTransaction transaction = fragmentManager.beginTransaction();

                            //ActivityへFragmentを組み込む
                            transaction.replace(R.id.container, dtlFragment);
                            mRecyclerViewAdapter.notifyDataSetChanged();
                            transaction.addToBackStack(null);

//Log.v("xxx:", "☆★☆★☆★☆★☆ MainFragment "+fragmentManager.getBackStackEntryCount()+" ★☆★☆★☆★☆★");
                            // 最後にcommitします
                            transaction.commit();

                        }
                        return true;
                }
                return true;
            }
        });
        pm.show();
    }

    //ＤＢを開いて、カーソルからメモリに格納
    protected void loadNote()
    {
        lstGoodsList.clear();

        //Read
        dbAdapter.open();

        //2019.12.30 修正
        //strArgValueがnullの場合、全件取得
        Cursor c = dbAdapter.getAllNotes(strArgKey, strArgValue);
//Log.v("xxx:", strArgKey+"  "+strArgValue+"  "+"☆★☆★☆★☆★☆loadNote passed ★☆★☆★☆★☆★");

        //2021.12.31 --- コメント化
        //getActivity().startManagingCursor(c);

        if (c.moveToFirst())
        {
            do {
                Note note = new Note
                    (
                            c.getInt(c.getColumnIndex(DBAdapter.COL_ID)),
                            c.getString(c.getColumnIndex(DBAdapter.COL_NOTE)),
                            c.getString(c.getColumnIndex(DBAdapter.COL_LASTUPDATE)),
                            c.getInt(c.getColumnIndex(DBAdapter.COL_PRIORITY)),
                            c.getInt(c.getColumnIndex(DBAdapter.COL_NUMBER)),
                            c.getString(c.getColumnIndex(DBAdapter.COL_SHOP)),
                            c.getInt(c.getColumnIndex(DBAdapter.COL_LISTPRICE)),
                            c.getInt(c.getColumnIndex(DBAdapter.COL_TOBUYORNOT)),
                            c.getString(c.getColumnIndex(DBAdapter.COL_BRAND)),
                            c.getInt(c.getColumnIndex(DBAdapter.COL_SORTID))
                    );

//Log.v("xxx:", "リスト一覧 "+ note.goods + " note.id: " + note.id + " sort_id: "+note.sort_id);
                lstGoodsList.add(note);
            }
            while (c.moveToNext());
        }
//        c.close();

        dbAdapter.close();
    }

    //新規メモ保存処理
    protected void saveItem ()
    {
        //2015.05.05 -- 追加
        //2020.01.01 -- 修正
//        if (edtInsert.getText().toString().length() != 0)
        if (txtInsert.length() != 0)
        {
            dbAdapter.open();

            //2016.1.2 修正
            //dbAdapter.saveNote(edtInsert.getText().toString(), 0);
//Log.v("xxx:", "☆★☆★☆★☆★☆ txtInsert: " +txtInsert+ "★☆★☆★☆★☆★");
            dbAdapter.saveNote(txtInsert, 0);
            txtInsert = "";

            loadNote();
        }
    }

    //GSONへの書き込み処理
    //2020.12.30 --- 追加
/*
    public void saveToGson()
    {
        SharedPreferences pref = this.getActivity().getSharedPreferences("pref", MODE_PRIVATE);
        Gson gson = new Gson();
        pref.edit().putString(SAVE_KEY, gson.toJson(lstGoodsList)).apply();
    }

    //GSONからの読み込み処理
    public void loadFromGson()
    {
        //取得できなかった場合、枠だけ返ってくる
        //if(json.equals("[]"))
        if(json.equals(""))
        {
            lstGoodsList = new ArrayList<Note>();
        } else{
//           lstGoodsList = gson.fromJson(pref.getString(SAVE_KEY, ""), new TypeToken<ArrayList<String>>(){}.getType());
            lstGoodsList = gson.fromJson(pref.getString(SAVE_KEY, ""), new TypeToken<ArrayList<Note>>(){}.getType());

        }
    }
*/


    //メニューバーのメニュークリック時の処理
    @Override
    public boolean onOptionsItemSelected (MenuItem item)
    {
        switch (item.getItemId()) {
            //メニューバー文字列がクリックされた時
            case R.id.change_op:

                //行反転モードの時
                if (item.getTitle() == strTtlRowDarkColor) {
                    //編集モードに変更
                    item.setTitle(strTtlPopup);
                    strTitleNow = strTtlPopup;
                } else {  //編集モードの時
                    //行反転モードに変更
                    item.setTitle(strTtlRowDarkColor);
                    strTitleNow = strTtlRowDarkColor;
                }
                break;

            //2020.12.31 --- 追加
/*
            case R.id.add_memo:

                add_menu();
                break;
*/
        }

        return true;
    }

    public void add_menu()
    {
//Log.v("xxx:", "☆★☆★☆★☆★☆ add_memo ★☆★☆★☆★☆★");
        //テキスト入力を受け付けるビューを作成します。
        final EditText editView = new EditText(getActivity());
        //ダイアログボックスを表示して、登録処理を行う
        final AlertDialog alt = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.menu_save)
                .setIcon(android.R.drawable. ic_dialog_info)
                .setView(editView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton)
                    {
                        //txtInsertに値を渡してfragment生成
                        Bundle bundle = new Bundle();
                        bundle.putString("bdlTxtInsert", editView.getText().toString());
//Log.v("xxx:", "☆★☆★☆★☆★☆ "+editView.getText().toString()+" ★☆★☆★☆★☆★");
                        // Fragmentを作成します
                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                        FragmentTransaction transaction = fragmentManager.beginTransaction();

                        // 実際に使用するFragmentの作成
                        Fragment mFragment = new MainFragment();
                        mFragment.setArguments(bundle);

                        //ActivityへFragmentを組み込む
                        transaction.replace(R.id.fragment_main, mFragment);

                        // 最後にcommitします
                        transaction.commit();
                    }
                })
                .setNegativeButton("キャンセル", null)
                .create();

        editView.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if (hasFocus)
                {
                    alt.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });

        editView.setInputType(InputType.TYPE_CLASS_TEXT);
//Log.v("xxx:", "☆★☆★☆★☆★☆ passed ★☆★☆★☆★☆★");
        alt.show();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu, inflater);

        // Menuの設定
        inflater.inflate(R.menu.main, menu);

        //2020.12.31 追加
        //2018.09.25 追加
        MenuItem menuItem = menu.findItem(R.id.search_menu_search_view);
        this.searchView = (SearchView) menuItem.getActionView();

        // 虫眼鏡アイコンを最初表示するかの設定
        this.searchView.setIconifiedByDefault(true);

        // Submitボタンを表示するかどうか
        this.searchView.setSubmitButtonEnabled(false);
        this.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
        {
            @Override
            // SubmitボタンorEnterKeyを押されたら呼び出されるメソッド
            public boolean onQueryTextSubmit(String searchWord) { return false; }

            @Override
            public boolean onQueryTextChange(String newText)
            {
                // 入力される度に呼び出される
                mRecyclerViewAdapter.getFilter().filter(newText);
                mRecyclerViewAdapter.notifyDataSetChanged();
                return false;
            }
        });

/*
        this.searchView.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                // searchView
                if (hasFocus == false)
                {
                    // searchViewのフォーカルから外れた時に、ソフトキーボードを非表示にする
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        });
*/
    }
//--------------------------------------------------------------------------------------------------



//--------------------------------------------------------------------------------------------------

    // Fragmentが破棄されるタイミングで呼び出されるメソッド
    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

//--------------------------------------------------------------------------------------------------

    // FragmentがActivityから離れたら呼ばれるメソッド
    @Override
    public void onDetach()
    {
        super.onDetach();
    }
}
