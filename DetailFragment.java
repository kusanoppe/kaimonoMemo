package com.example.kusano.kaimonomemo2018;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

public class DetailFragment extends Fragment
{
    private View mView;
    private TextView txtvwGoods;
    private TextView txtvwNumber;
    private TextView txtvwListprice;
    private TextView txtvwShop;
    private TextView txtvwBrand;

    //2022.12.25 --- 追加
    private CheckBox chkvwPriority;

    private Button btnBack;
    private Button btnUpdate;

    static DBAdapter dbAdapter;

    //2020.01.02 追加
    //詳細画面に使用
    int intArgID;
    String strArgGoods;
    int intArgNumber;
    int intArgListprice;
    String strArgShop;
    String strArgBrand;

    //2022.12.25 --- 追加
//    int intArgPriority;

    public static DetailFragment newInstance()
    {
        //インスタンス生成
        DetailFragment dtlFragment = new DetailFragment();
        return dtlFragment;
    }

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
            intArgID = args.getInt("bdl_ArgID");
            strArgGoods = args.getString("bdl_ArgGoods");
            intArgNumber = args.getInt("bdl_ArgNumber");
            intArgListprice = args.getInt("bdl_ArgListprice");
            strArgShop= args.getString("bdl_ArgShop");
            strArgBrand= args.getString("bdl_ArgBrand");

            //2022.12.25 --- 追加
            //2024.01.01 --- コメント化
//            intArgPriority = args.getInt("bdl_ArgPriority");

//Log.d("xxx:", "☆★☆★☆★☆★☆ intArgPriority "+intArgPriority+"★☆★☆★☆★☆★");
        }
    }

    // Fragmentで表示するViewを作成するメソッド
    // このFragmentのメインコンテンツとなるViewを生成して返す必要があるライフサイクルイベント
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        mView = inflater.inflate(R.layout.detail, container, false);
        return mView;
    }

    // Viewが生成し終わった時に呼ばれるメソッド
    // Viewの初期化とFragmentの状態の復元はここで行うことを推奨
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        txtvwGoods = (TextView) mView.findViewById(R.id.txtGoods);
        txtvwGoods.setText(strArgGoods);

        txtvwNumber = (TextView) mView.findViewById(R.id.txtNumber);
        txtvwNumber.setText(String.valueOf(intArgNumber));

        txtvwListprice = (TextView) mView.findViewById(R.id.txtListprice);
        txtvwListprice.setText(String.valueOf(intArgListprice));

        txtvwShop = (TextView) mView.findViewById(R.id.txtShop);
        txtvwShop.setText(strArgShop);

        txtvwBrand = (TextView) mView.findViewById(R.id.txtBrand);
        if(strArgBrand == null)
        {
            txtvwBrand.setText(" ");
        }else{
            txtvwBrand.setText(strArgBrand);
        }

        //2022.12.25 --- 追加
        //2024.01.01 --- コメント化
        /*
                chkvwPriority = (CheckBox) mView.findViewById(R.id.chkPriority);
                Log.d("xxx:", "☆★☆★☆★☆★☆ chkvwPriority "+chkvwPriority+" ★☆★☆★☆★☆★");

                if(intArgPriority == 1)
                {
                    chkvwPriority.setChecked(true);
                }else
                {
                    chkvwPriority.setChecked(false);
                }
        */

        //戻るボタン
        btnBack = (Button) mView.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                FragmentManager fragmentManager = getFragmentManager();
Log.v("xxx:", "☆★☆★☆★☆★☆ DetailFragment "+fragmentManager.getBackStackEntryCount()+" ★☆★☆★☆★☆★");
                if(fragmentManager != null)
                {
                    //2023.01.01 --- 追加
                    //画面スクロールしないと、画面が更新されない・・・
                    ((MainFragment) getParentFragment()).loadNote();
                    fragmentManager.popBackStack();
                }
            }
        });

        //更新ボタン
        btnUpdate = (Button) mView.findViewById(R.id.btnUpdate);
        btnUpdate.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view)
            {
                //更新処理
                dbAdapter.open();

                //2022.12.31 --- 追加
                //2024.01.01 --- コメント化
                /*
                    //優先購入のチェック更新処理
                    if(chkvwPriority.isChecked()==true)
                    {
                        intArgPriority = 1;
                    }else
                    {
                        intArgPriority = 2;
                    }
                */
                //2023.01.01 --- 追加
                //購入数空白時の０埋め処理
                if(txtvwNumber.getText().toString().equals(""))
                {
                    intArgNumber = 1;
                }else{
                    intArgNumber = Integer.valueOf(txtvwNumber.getText().toString());
                }

                //定価空白時の０埋め処理
                if(txtvwListprice.getText().toString().equals(""))
                {
                    intArgListprice = 0;
                }else{
                    intArgListprice = Integer.valueOf(txtvwListprice.getText().toString());
                }

                //商品名空白時は変更無しに
                if(!txtvwGoods.getText().toString().equals(""))
                {
                    strArgGoods = txtvwGoods.getText().toString();
                }
                //strArgGoods

                if(dbAdapter.updateNote(intArgID,
                        strArgGoods,
                        intArgNumber,
                        intArgListprice,
                        txtvwShop.getText().toString(),
                        txtvwBrand.getText().toString()))


                {
                    Cursor c = dbAdapter.getAllNotes("_id", String.valueOf(intArgID));

                    while( c.moveToNext() )
                    {
                        txtvwGoods.setText(c.getString(c.getColumnIndex(DBAdapter.COL_NOTE)));
                        txtvwNumber.setText(String.valueOf(c.getInt(c.getColumnIndex(DBAdapter.COL_NUMBER))));
                        txtvwListprice.setText(String.valueOf(c.getInt(c.getColumnIndex(DBAdapter.COL_LISTPRICE))));
                        txtvwShop.setText(c.getString(c.getColumnIndex(DBAdapter.COL_SHOP)));
                        txtvwBrand.setText(c.getString(c.getColumnIndex(DBAdapter.COL_BRAND)));

                        //                            c.getInt(c.getColumnIndex(DBAdapter.COL_PRIORITY));
                    }
                    //Log.v("xxx:", "☆★☆★☆★☆★☆"+ note.goods +"★☆★☆★☆★☆★");

                    Toast.makeText(getActivity().getBaseContext(), "編集が完了しました", Toast.LENGTH_SHORT).show();

                }
                dbAdapter.close();

            }
        });
    }

    @Override
    public void onStart()
    {
        super.onStart();
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }

//--------------------------------------------------------------------------------------------------

    // FragmentがActivityから離れたら呼ばれるメソッド
    @Override
    public void onDetach()
    {
        super.onDetach();
    }

}