package com.example.kusano.kaimonomemo2018;

public class Note
{
    protected int id;
    protected String goods;		//商品名
    protected String lastupdate;	//更新日

    //2015.3.8 追加
    //0: 備忘録
    //1: 最優先で必要
    //2:
    //3: 急ぎではない
    //2024.1.1 仕様変更
    //1: 優先購入
    //2: 購入予定無し

    protected int priority;		//購入する優先順位
    protected int number;		//購入予定数
    protected String shop;		//購入店
    protected int listprice;	//定価

    //2016.1.2 追加
    protected int ToBuyOrNot;   // 購入対象フラグ

    //2020.1.13 追加
    protected String brand;     // ブランド

    //2020.5.03 追加
    protected int sort_id;     // ソート用

    public Note(int id, String goods, String lastupdate, int priority, int number, String shop, int listprice, int ToBuyOrNot, String brand, int sort_id)
    {
        this.id = id;
        this.goods = goods;
        this.lastupdate = lastupdate;

        //2015.3.8 追加
        this.priority = priority;
        this.number = number;
        this.shop = shop;
        this.listprice = listprice;

        //2016.1.2 追加
        this.ToBuyOrNot = ToBuyOrNot;

        //2020.1.13 追加
        this.brand = brand;

        //2020.5.03 追加
        this.sort_id = sort_id;
    }

    public int getId(){ return id; }
    public String getNote(){ return goods; }
    public String getLastupdate(){ return lastupdate; }

    //2015.3.8 追加
    public int getPriority(){ return priority; }
    public int getNumber(){ return number; }
    public String getShop(){ return shop; }
    public int getListprice(){ return listprice; }

    //2016.1.2 追加
    public int getToBuyOrNot(){ return ToBuyOrNot; }

    //2020.1.13 追加
    public String getBrand(){ return brand; }

    //2020.5.03 追加
    public int getSort_id(){ return sort_id; }
}
