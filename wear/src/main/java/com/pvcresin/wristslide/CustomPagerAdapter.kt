package com.pvcresin.wristslide

import android.content.Context
import android.graphics.Color
import android.support.v4.view.PagerAdapter
import android.text.Layout
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import java.util.ArrayList
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView


class CustomPagerAdapter(val mContext: Context) : PagerAdapter() {

    val mList = ArrayList<Recipe>()

    fun add(recipe: Recipe): CustomPagerAdapter {
        mList.add(recipe)
        return this@CustomPagerAdapter
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {

        val recipe = mList[position]

        // コンテキストからレイアウトインフレーターを取得
        val inflater = LayoutInflater.from(mContext)

        // レイアウトXMLからビューを取得
        val view = inflater.inflate(R.layout.recipe, null)

        (view.findViewById(R.id.imageView) as ImageView).setImageResource(recipe.image)
        (view.findViewById(R.id.dishName) as TextView).text = recipe.dishName
        (view.findViewById(R.id.comment) as TextView).text = recipe.comment

        recipe.ingredients.forEach {
            val tv = TextView(mContext).apply {
                text = it
                textSize = 16f
                setTextColor(Color.BLACK)
            }
            (view.findViewById(R.id.ingredients) as LinearLayout).addView(tv)
        }

        recipe.quantities.forEach {
            val tv = TextView(mContext).apply {
                text = it
                textSize = 16f
                setTextColor(Color.BLACK)
                gravity = Gravity.RIGHT
            }
            (view.findViewById(R.id.quantities) as LinearLayout).addView(tv)
        }


        recipe.procedures.forEach {
            val tv = TextView(mContext).apply {
                text = it
                textSize = 16f
                setPadding(0, 6, 0, 6)
                setTextColor(Color.BLACK)
            }
            (view.findViewById(R.id.procedures) as LinearLayout).addView(tv)
        }


        // コンテナに追加
        container.addView(view)

        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) { // コンテナから View を削除
        container.removeView(obj as View)
    }

    override fun getCount(): Int = mList.size

    override fun isViewFromObject(view: View, obj: Any): Boolean { // Object内にViewが存在するか判定
        return view === obj as View
    }

}