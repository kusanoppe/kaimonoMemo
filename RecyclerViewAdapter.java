package com.example.kusano.kaimonomemo2018;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static com.example.kusano.kaimonomemo2018.MainFragment.dbAdapter;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{
    Context mContext;

    private LayoutInflater mInflater;
    private List<Note> mNote;
    private Listener mListener;
    private List<Note> mArrayListFull;

    public interface Listener {
        void onRecyclerClicked(View v, int position);
        void onCheckboxClicked(View v, int position, boolean isChecked);
    }

    public RecyclerViewAdapter(Context context, List<Note> note, Listener listener)
    {
        mInflater = LayoutInflater.from(context);
        mNote = note;
        mListener = listener;
        mContext = context;

        mArrayListFull = new ArrayList<>(note);
    }

    // 表示するレイアウトを設定
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        return new ViewHolder(mInflater.inflate(R.layout.list_item, parent, false));
    }

    // データ表示
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position)
    {
        final Note mNote2 = mNote.get(position);

        //2023.12.31 追加
        //優先チェックボックスの表示
        if(mNote2.getPriority() == 1)
        {
            holder.chkPriority.setChecked(true);
        }else
        {
            holder.chkPriority.setChecked(false);
        }

        //購入対象外の場合
        if(mNote2.getToBuyOrNot() == 1)
        {
            holder.textGoodsView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorWillnotBuy));
            holder.textGoodsView.setTextColor(ContextCompat.getColor(mContext, R.color.colorCell));
        }else{
            holder.textGoodsView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorCell));
            holder.textGoodsView.setTextColor(ContextCompat.getColor(mContext, R.color.colorWillnotBuy));
        }

        //ビューに文字列をセット
        holder.textGoodsView.setText(mNote2.getNote());
        holder.textBrandView.setText(mNote2.getBrand());
        holder.textShopView.setText(mNote2.getShop());

//Log.d("xxx:", "☆★☆★☆★☆★☆ mNote2.getNote() mNote2.getNumber() "+mNote2.getNote()+" "+mNote2.getNumber()+"★☆★☆★☆★☆★");

        //2022.12.25 追加
        //２個以上の場合、個数を表示
        if(mNote2.getNumber() > 1)
        {
            holder.textNumberView.setText("× " + String.valueOf(mNote2.getNumber()));
        }else{
            holder.textNumberView.setText("");
        }

        //holder.textView.setText(mNote2.getNote()+"  ID:"+mNote2.getId()+"  ToBuyOrNot:"+mNote2.getToBuyOrNot());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onRecyclerClicked(view, position);
            }
        });

        //2024.1.1 setOnCheckedChangeListenerを使うと、スクロール時にチェックが勝手に更新されてしまう事象が起きるため、このやり方を参考にした。
        holder.chkPriority.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                boolean isChecked = ((CheckBox) view).isChecked();
                //mNote2.priority.setSelected(isChecked);
                //holder.chkPriority.setChecked(isChecked);
//Log.d("xxx:", "☆★☆★☆★☆★☆ mNote2.getNote() mNote2.getId() "+mNote2.getNote()+" "+mNote2.getId()+"★☆★☆★☆★☆★");
                mListener.onCheckboxClicked(view, position, isChecked);

            }
        });
    /*
            holder.chkPriority.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    //2024.1.1 スクロールすると、チェックが勝手に更新されてしまう
                    // mListener.onCheckboxClicked(buttonView, position, isChecked);
                }
            });
    */
    }

    @Override
    public int getItemCount()
    {
        if (mNote.isEmpty()) {
            return 0;
        }
        else {
            return mNote.size();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        //2023.12.31 追加
        CheckBox chkPriority;

        TextView textGoodsView;
        TextView textShopView;
        TextView textBrandView;
        TextView textNumberView;

        public ViewHolder(View itemView) {
            super(itemView);

            //2023.12.31 追加
            chkPriority=(CheckBox) itemView.findViewById(R.id.chkPriority);

            textGoodsView = (TextView)itemView.findViewById(R.id.txtGoods);
            textShopView = (TextView)itemView.findViewById(R.id.txtShop);
            textBrandView = (TextView)itemView.findViewById(R.id.txtBrand);
            textNumberView = (TextView)itemView.findViewById(R.id.txtNumber);
        }
    }

    public void deleteGoods(int pos)
    {
        //GET ID
        Note mNote2 = mNote.get(pos);
        int intId = mNote2.getId();
        String strGoods =mNote2.getNote();

        //Toast.makeText(mContext, Integer.toString(intId), Toast.LENGTH_SHORT).show();

        dbAdapter.open();
        if(dbAdapter.deleteNote(intId))
        {
            Toast.makeText(mContext, strGoods + "  は削除されました", Toast.LENGTH_SHORT).show();
        }
        dbAdapter.close();

        this.notifyItemRemoved(pos);
    }

    //検索時に使用
    public Filter getFilter()
    {
        return new Filter()
        {
            @Override
            //Filterのfilterメソッドを実行
            protected FilterResults performFiltering(CharSequence charSequence) {
                List<Note> filteredList = new ArrayList<>();
//                Toast.makeText(mContext, charSequence+ "  " + "☆★☆★☆★☆★☆ passed ★☆★☆★☆★☆★", Toast.LENGTH_SHORT).show();

//                mArrayListFull=mNote;

                if (charSequence == null || charSequence.length() == 0)
                {
Log.d("検索文字なし:", "☆★☆★☆★☆★☆ passed ★☆★☆★☆★☆★");
                    filteredList.addAll(mArrayListFull);
//                  filteredList.addAll(mNote);
                } else {
                    String filterPattern = charSequence.toString().toLowerCase().trim();
//Log.d("filterPattern:", filterPattern+"  "+"☆★☆★☆★☆★☆ passed ★☆★☆★☆★☆★");

                    //拡張for文(for-each文)
                    for (Note mNote : mArrayListFull)
//                    for (Note mNote : mNote)
                    {
                        if (mNote.getNote().toLowerCase().contains(filterPattern)) {
//Log.d("mNote.goods:", mNote.goods+"  "+"☆★☆★☆★☆★☆ passed ★☆★☆★☆★☆★");
                            filteredList.add(mNote);
                        }
                    }
                }

                FilterResults filterResults = new FilterResults();
//Log.d("filterResults:", filterResults+"  "+"☆★☆★☆★☆★☆ passed ★☆★☆★☆★☆★");
                filterResults.values = filteredList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults)
            {
                // Adapterのメソッドでデータの内容を更新する
                //List<Note> filteredList = new ArrayList<>();
                List<Note> items = (List<Note>) filterResults.values;

                mNote.clear();
                mNote.addAll(items);
                notifyDataSetChanged();
            }
        };
    }
}