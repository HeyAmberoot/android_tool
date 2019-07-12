package com.example.cuanbo.ListView;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Created by xww on 18/4/2.
 */

public class LeftSlideDeleteListView extends ListView {
    /**
     * 用户使用setAdapter传入的adapter
     */
    private ListAdapter targetAdapter;
    /**
     * interface: ListViewItemDeleteClikListener
     */
    private OnListViewItemDeleteClikListener deleteListener;
    /**
     * 动画持续时间（毫秒）
     */
    private final int SLIDE_ANIM_TIME=300;
    /**
     * interface: ItemClickListener
     */
    private OnItemClickListener itemClickListener;
    /**
     * interface:  a callback to be invoked when the list or grid has been scrolled
     */
    private OnScrollListener scrollListener;
    /**
     * 设置滑动触发itemDeleteClick事件
     */
    public interface OnListViewItemDeleteClikListener{
        public void onListViewItemDeleteClick(int position) ;
    }
    /**
     * 重写该方法，拦截itemclick事件处理对象，点击SlideContainer后触发itemClick事件
     */
    @Override
    public void setOnItemClickListener(android.widget.AdapterView.OnItemClickListener listener) {
        this.itemClickListener=listener;
    }
    public void setOnListViewItemDeleteClikListener(OnListViewItemDeleteClikListener listener) {
        deleteListener=listener;
    }
    /**
     * 点击删除按钮后交给该函数处理
     * @param v 删除按钮
     */
    private void delectClick(View v) {
        int first=getFirstVisiblePosition();
        int last=getLastVisiblePosition();
        /*
         * 查找点击的删除按钮所在的position。
         *  每次滚动时会把getView中传入的position设为SlideContainer中删除按钮的tag
         */
        for (int i = 0; i <= last-first; i++) {
            /*
             * SlideContainer的结构详见图片。
             * 根据删除按钮的tag查找删除按钮所在的SlideContainer，找到后让其子View复位
             */
            SlideContainer sc=(SlideContainer) getChildAt(i);
            ViewGroup deleteContainer=(ViewGroup) sc.getChildAt(0);
            Integer positionTag=(Integer)deleteContainer.getChildAt(0) .getTag();
            if (positionTag==v.getTag()) {//v就是触发点击事件的按钮
                //找到删除按钮所在的SlideContainer，让其下标为1的
                //子View（用户定义的View）复位到初始位置（0位置）
                sc.getChildAt(1).setTranslationX(0);
                break;
            }
        }
        //调用该方法通知用户要删除的位置
        deleteListener.onListViewItemDeleteClick((Integer) v.getTag());
    }

    /**
     * listView中直接操作的其实是ProxyAdapter。
     * 拦截用户的adapter，为其adapter设置代理，拦截getView方法。
     *
     */
    @Override
    public void setAdapter(ListAdapter adapter) {  //把我自己用来绑定数据和view的DeviceAdapter传进来
        targetAdapter=adapter;//记录用户的adapter
        super.setAdapter(new ProxyAdapter());//listView中直接操作的其实是ProxyAdapter，
    }


    public LeftSlideDeleteListView(Context context) {
        super(context );

        init();
    }

    public LeftSlideDeleteListView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public LeftSlideDeleteListView(Context context, AttributeSet attrs,
                                   int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    public LeftSlideDeleteListView(Context context, AttributeSet attrs,
                                   int defStyleAttr, int defStyleRes) {
        //super(context, attrs, defStyleAttr, defStyleRes);
        super(context, attrs, defStyleRes);
        init();
    }

    /**
     * 重写该方法，拦截滚动事件，需要在onScrollStateChanged把控件复位
     */
    @Override
    public void setOnScrollListener(OnScrollListener l) {
        scrollListener=l;
    }

    private void init() {
        //监听滚动开始事件，开始滚动时把所有控件复位，由于重写过setOnScrollListener，
        //所以要调用super.setOnScrollListener添加滚动监听
        super.setOnScrollListener(new OnScrollListener() {

            /**
             *监听着ListView的滑动状态改变。官方的有以下三种状态：
             * SCROLL_STATE_TOUCH_SCROLL：手指正拖着ListView滑动
             * SCROLL_STATE_FLING：ListView正自由滑动
             * SCROLL_STATE_IDLE：ListView滑动后静止
             * */
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // TODO Auto-generated method stub
                if (scrollState==1) {
                    int first =getFirstVisiblePosition();
                    int last=getLastVisiblePosition();
                    for (int i = 0; i <= last-first; i++) {
                        SlideContainer sc=(SlideContainer) getChildAt(i);
                        sc.getChildAt(1).setTranslationX(0);
                    }
                }
                if (scrollListener!=null) {
                    /*
                     * 若用户调用了setOnScrollListener（LeftSlideDeleteListView
                     * 已经重写过setOnScrollListener方法）
                     * 为ListView添加滚动监听，则需要在这调用用户的
                     * onScrollStateChanged方法把事件交给用户去处理
                     */

                    scrollListener.onScrollStateChanged(view, scrollState);
                }
            }

            /**
             * firstVisibleItem: 表示在屏幕中第一条显示的数据在adapter中的位置 ；
             * visibleItemCount：则表示屏幕中最后一条数据在adapter中的数据；
             * totalItemCount则是在adapter中的总条数 。
             * */
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                if (scrollListener!=null) {
                    //同样也要交给用户去处理
                    scrollListener.onScroll(view, firstVisibleItem,
                            visibleItemCount, totalItemCount);
                }
            }
        });
    }

    /**
     *
     * @author young
     *</br>
     *SlideContainer继承自FrameLayout，用于包裹用户的Listview中的每一个条目View，
     *每一个条目View对应一个SlideContainer。
     *SlideContainer具有左滑移动的功能。SlideContainer中下面是一个LinearLayout，
     *用于包裹删除按钮，SlideContainer中上面就是用户的View
     *
     *该SlideContainer不需要用户调用，当用户设置adapter后会自动为用户View外面
     *包裹SlideContainer
     */
    private class SlideContainer extends FrameLayout {
        private int dis;
        private TextView delete;//删除按钮
        public SlideContainer(Context context ,View topChild) {
            super(context);
            //获取肉眼可见的最小移动距离
            dis= ViewConfiguration.get(getContext()).getScaledTouchSlop();


            //用于包裹删除按钮
            LinearLayout bottomContainer=new LinearLayout(context);
            //居右，竖直居中
            bottomContainer.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);

            delete=new TextView(context);
            delete.setText("删除");
            delete.setBackgroundColor(Color.RED);
            delete.setGravity(Gravity.CENTER);
            int padding=dip2px(getContext(), 30);
            delete.setPadding(padding, 0, padding, 0);
            delete.setTextColor(Color.WHITE);

            LinearLayout.LayoutParams params=new LinearLayout.
                    LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            delete.setLayoutParams(params);
            bottomContainer.addView(delete);

            //为删除按钮添加点击事件
            delete.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    delectClick(v);


                }
            });
            /*
             * 为每个条目添加点击事件。
             * 若不处理会导致用户设置的setOnItemClickListener无效
             */
            setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    int first=getFirstVisiblePosition();
                    int last=getLastVisiblePosition();
                    //同样是根据tag查找位置
                    for (int i = 0; i <= last-first; i++) {
                        SlideContainer sc=(SlideContainer) LeftSlideDeleteListView.this.getChildAt(i);
                        ViewGroup deleteContainer=(ViewGroup) sc.getChildAt(0);//获取到包裹删除按钮的LinearLayout
                        Integer positionTag=(Integer)deleteContainer.getChildAt(0) .getTag();//获取删除按钮的tag
                        if (positionTag==v.getTag()) {//如果点击的按钮v的tag和positionTag则找到了位置
                            if (itemClickListener!=null) {
                                itemClickListener.onItemClick(null, v, i+first, 0);  //null改变v，获取item的view
                            }

                            return;
                        }
                    }

                }
            });
            //先添加删除按钮所在的布局，让其位于底部
            addView(bottomContainer);
            //再添加用户的控件，覆盖在删除按钮上部
            addView(topChild);
        }

        private int dip2px(Context context, float dpValue) {
            final float scale = context.getResources().getDisplayMetrics().density;
            return (int) (dpValue * scale + 0.5f);
        }
        public void setPositionTag(int  position) {

            delete.setTag(position);

        }
        private float downX;
        private float downY;

        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {

            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downX=ev.getX();
                    downY=ev.getY();
                    break;

                case MotionEvent.ACTION_MOVE:
                    float moveX=ev.getX();
                    float moveY=ev.getY();
                    float dx= Math.abs(moveX-downX);
                    float dy=Math.abs(moveY-downY);

                    if (dx>dis&&dx>dy) {
                        return true;//拦截到事件，交给自己的onTouchEvent处理，同时不再向下传递给子控件
                    }
            }


            return super.onInterceptTouchEvent(ev);//对事件放行，交给子控件处理
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            float moveX=ev.getX();
            float moveY=ev.getY();
            switch (ev.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    float dx= Math.abs(moveX-downX);
                    float dy=Math.abs(moveY-downY);
                    if (dx>dis&&dx>dy) {
                        //不让ListView响应事件，否则会出现左上或左下划时Listview跟着上下移动
                        getParent().requestDisallowInterceptTouchEvent(true);
                        if (moveX<downX) {
                            getChildAt(1).setTranslationX(moveX-downX);
                        }else
                            getChildAt(1).setTranslationX(0);

                    }

                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    //若控件未移动并且手指移动距离小于dis，则视为是itemClick事件，
                    //调用performClick后回触发该控件的点击事件，在点击事件内部触发itemClick事件
                    if (Math.abs(moveX-downX)<dis&& Math.abs(moveY-downY)<dis&&
                            Math.abs(getChildAt(1).getTranslationX())<dis) {
                        performClick();
                    }
                    //如果超过了删除按钮宽度，抬起手指时就把用户View向左移动删除按钮宽度的距离
                    if (moveX<downX&& downX-moveX>delete.getWidth() ) {
                        translationTopView(getChildAt(1), getChildAt(1).
                                getTranslationX(), -delete.getWidth());
                    }else{

                        translationTopView(getChildAt(1), getChildAt(1).getTranslationX(), 0);
                    }

                    break;
            }



            return true;
        }

        /**
         * 把控件v以属性动画形式从start移动到end。 属性动画中不管怎么移动，
         * 只要调用setTranslationX(0),控件就会回到原来位置。
         * 属性动画没有累加效果，即：调用setTranslationX(x1)
         * 后再调用setTranslationX(x2)结果和setTranslationX(x1+x2)不一样
         * @param v
         * @param start
         * @param end
         */
        private void translationTopView(View v,float start,float end){
            ObjectAnimator animator= ObjectAnimator.ofFloat(v, "translationX", start,end);
            animator.setDuration(SLIDE_ANIM_TIME);
            animator.start();

        }
    }

    ///////////////////////////////
    /**
     *
     * @author young
     *</br></br>
     *代理adapter,拦截getView方法，为用户View包裹SlideContainer父布局。
     *ListView直接操作的是该ProxyAdapter,再由ProxyAdapter转发到对应方法处理
     */
    private class ProxyAdapter implements ListAdapter{

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView==null) {//null说明之前没缓存过（第一屏显示的View都是调用该方法得到的）
                View v=targetAdapter.getView(position, convertView, parent);//调用用户adapter的getView方法得到用户布局
                SlideContainer sc= new SlideContainer(getContext(), v);//为用户布局包裹支持左划的控件
                sc.setPositionTag(position);//为删除按钮设置tag。由于listview中控件是复用的，所以每次getView时都需要修改tag值，通过tag值可以找到该删除按钮对应的位置
                sc.setTag(position);//同样设置tag，用于把找到SlideContainer所在位置，转发给onItemClick方法处理item点击事件
                return sc;
            }else{
                ViewGroup vg=(ViewGroup) convertView;
                // vg.getChildAt(1)获取的是用户定义的View，所以调用用户adapter的getView时要把用户View传进去
                View v=targetAdapter.getView(position, vg.getChildAt(1), parent);
                SlideContainer sc;
                if (v!=vg.getChildAt(1)) {//判断用户的getView是否使用了缓存的view（部分用户可能每次getView中都创建新的控件）
                    sc= new SlideContainer(getContext(), v);//用户并没有使用缓存控件而是新创建的控件，所以要包裹SlideContainer
                }else
                    sc=(SlideContainer) convertView;
                sc.setPositionTag(position);//设置tag
                sc.setTag(position);
                return sc;
            }

        }


        /*下面的方法不需要拦截，直接返回用户adapter对应方法的值或调用用户adapter对应方法即可
         */

        @Override
        public void registerDataSetObserver(DataSetObserver observer) {
            targetAdapter.registerDataSetObserver(observer);
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {
            targetAdapter.unregisterDataSetObserver(observer);
        }

        @Override
        public int getCount() {
            return targetAdapter.getCount();
        }

        @Override
        public Object getItem(int position) {
            return targetAdapter.getItem(position);
        }

        @Override
        public long getItemId(int position) {
            return targetAdapter.getItemId(position);
        }

        @Override
        public boolean hasStableIds() {
            return targetAdapter.hasStableIds();
        }


        @Override
        public int getItemViewType(int position) {
            return targetAdapter.getItemViewType(position);
        }

        @Override
        public int getViewTypeCount() {
            return targetAdapter.getViewTypeCount();
        }

        @Override
        public boolean isEmpty() {
            return targetAdapter.isEmpty();
        }

        @Override
        public boolean areAllItemsEnabled() {
            return targetAdapter.areAllItemsEnabled();
        }

        @Override
        public boolean isEnabled(int position) {
            return targetAdapter.isEnabled(position);
        }



    }

}
