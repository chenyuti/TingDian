
package com.logansoft.UIEngine.fragment;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.logansoft.UIEngine.R;
import com.logansoft.UIEngine.utils.GlobalConstants;
import com.logansoft.UIEngine.utils.LogUtil;
import com.logansoft.UIEngine.utils.ProgressDialogUtil;
import com.logansoft.UIEngine.utils.http.RequestListener;
import com.logansoft.UIEngine.utils.http.RequestManager;
import com.logansoft.UIEngine.utils.http.RequestOptions;

public class BaseFragmentManager implements Serializable {
    private static final long serialVersionUID = 1L;

    public LinkedList<String> linkedList;

    public String nowFragmentId;

    private int layoutId;

    transient private FragmentManager fm;

    transient private Context mContext;

    private Map<String, String> pageMap;

    public boolean load = false;

    private Runnable myRunnable;

    private String customAnimations = "left";

    private String kye;
    
    // public static ArrayMap<String, BaseFragmentManager>
    // BaseFragmentManagerMap =new ArrayMap<String, BaseFragmentManager>();

    /**
     * 实例化BaseFragment
     * 
     * @param key
     * @param context
     * @param fm
     * @param layoutId
     * @return
     */
    public static BaseFragmentManager instance(String key, Context context, FragmentManager fm,
            int layoutId) {
        BaseFragmentManager baseFragmentManager = new BaseFragmentManager(context, fm, layoutId);
        baseFragmentManager.setKye(key);
        // BaseFragmentManagerMap.put(key, baseFragmentManager);
        return baseFragmentManager;
    }


    private BaseFragmentManager(Context context, FragmentManager fm, int layoutId) {
        init(fm, context);
        setLayoutId(layoutId);
    }

    public void init(FragmentManager fm, Context context) {
        this.fm = fm;
        mContext = context;
    }

    /**
     * 设置layoutId
     * 
     * @param layoutId
     */
    public void setLayoutId(int layoutId) {
        this.layoutId = layoutId;
    }

    /**
     * 获得当前的layoutId
     * 
     * @return
     */
    public int getLayoutId() {
        return layoutId;
    }

    public BaseFragment findFragmentByTag(String tag) {
        return (BaseFragment)fm.findFragmentByTag(tag);
    }

    /**
     * 获得现在的fragment tag
     * 
     * @return
     */
    public BaseFragment getCurrentFragment() {
        return (BaseFragment)fm.findFragmentByTag(nowFragmentId);
    }	

    public String getCurrentFragmentId(){
    	return nowFragmentId;
    }
    /**
     * 移除上一个fragment
     */
    public void removeLastFragment() {
        if (linkedList != null && !linkedList.isEmpty()) {
            FragmentTransaction ft = fm.beginTransaction();
            Fragment lastFragment = fm.findFragmentByTag(linkedList.getLast());
            ft.remove(lastFragment);
            ft.commitAllowingStateLoss();
            linkedList.removeLast();
        }
    }
    
    /**
     * 移除指定的fragment
     */
    public void removeFragment(String tag) {
        if (linkedList != null && !linkedList.isEmpty()&&linkedList.contains(tag)) {
            FragmentTransaction ft = fm.beginTransaction();
            Fragment lastFragment = fm.findFragmentByTag(tag);
            ft.remove(lastFragment);
            ft.commitAllowingStateLoss();
            linkedList.remove(tag);
        }
    }

    /**
     * 移除除了当前fragment外的其他fragment
     */
    public void removeAllFragment() {
        FragmentTransaction ft = fm.beginTransaction();
        if (linkedList != null && !linkedList.isEmpty()) {
            while (!linkedList.isEmpty()) {
                Fragment lastFragment = fm.findFragmentByTag(linkedList.getLast());
                ft.remove(lastFragment);
                linkedList.removeLast();
            }

        }
        Fragment now = fm.findFragmentByTag(nowFragmentId);
        ft.remove(now);
        nowFragmentId = null;
        ft.commitAllowingStateLoss();
    }

    /**
     * 获得上一个fragment tag
     * 
     * @return
     */
    public String getlastFragment() {
        if (linkedList.isEmpty()) {
            return "";
        }
        return linkedList.getLast();
    }

    public void setLoad(boolean load) {
        this.load = load;
    }

    /**
     * 切换Fragment
     * 
     * @param layoutId 需要加入的layout的id
     * @param isRemoveLast 是否移除上前一次的Fragment
     * @param class1 继承baseFragment的类
     * @param tag Fragment的Tag,需要显示的fragment名字
     * @param bundle 需要传入的数据,
     */
    public void switchFragment(final boolean isRemoveLast,final String url, final Class<?> class1,final Bundle bundle) {
    	try {
	    	HashMap<String,Object> params=new HashMap<String,Object>();
	    	params.put(RequestOptions.REQUESTURL,url);
	    	params.put(RequestOptions.RESPONSETYPE, RequestOptions.RESPONSEPAGE);
	    	params.put(RequestOptions.REQUESTNET,"NetSelector:");
	    	params.put(RequestOptions.REQUESTLOCAL,"LocalSelector:");
	    	RequestListener requestListener=new RequestListener(){
	    		@Override
	    		public void DidLoad(Object result,final Map<String,Object>responseLuaArgs,int callBackType){
	    			if(callBackType==CacheDidUpdateCallBackType)return;
	    			if(result instanceof byte[]){
	    		        switchFragmentExecute(isRemoveLast,url,(byte[])result,class1,bundle);
	    		        ProgressDialogUtil.dismissPD();
                    }
	    		}
            };
            //warrring be carefull!
	    	params.put(RequestManager.REQUESTLISTENER, requestListener);
	    	RequestManager.request(params);
		} catch (Exception e) {
			e.printStackTrace();
		}
    	


    }
    
    public Handler getHandler(boolean isRemoveLast,String url,Class<?> class1,Bundle bundle){
         Handler mHandler =new BFMHandler(isRemoveLast, url, class1, bundle);
         return mHandler;
    }
    
    public class BFMHandler extends Handler{
        private boolean isRemoveLast;
        private String url;
        private Class<?> class1;
        private Bundle bundle;
        
        public BFMHandler(boolean isRemoveLast,String url,Class<?> class1,Bundle bundle){
               super();
               this.isRemoveLast=isRemoveLast;
               this.url=url;
               this.class1=class1;
               this.bundle=bundle;
        }
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
              case GlobalConstants.HANDLER_COMPARE_SUCCEED:

                  break;
              case GlobalConstants.HANDLER_COMPARE_FAILED:
                  ProgressDialogUtil.dismissPD();
                  break;
          }
      };
    }

    public void handlerJ() {
        if (myRunnable != null) {
            new Handler().post(myRunnable);
        }
    }

    private void switchFragmentExecute(boolean isRemoveLast,String url,byte[] data,Class<?> class1,Bundle bundle) {
        FragmentTransaction ft = fm.beginTransaction();
        try {
            // /* 设置动画 */
            if ("right".equals(customAnimations)) {
                // ft.setCustomAnimations(R.anim.left_in, R.anim.right_out,
                // R.anim.right_in, R.anim.left_out);
                ft.setCustomAnimations(R.anim.left_in, R.anim.right_out);
            } else if ("left".equals(customAnimations)) {
                // ft.setCustomAnimations(R.anim.right_in, R.anim.left_out,
                // R.anim.left_in, R.anim.right_out);
                ft.setCustomAnimations(R.anim.right_in, R.anim.left_out);
            } else if ("fade".equals(customAnimations)) {
                ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
            } else if ("scale".equals(customAnimations)) {
                ft.setCustomAnimations(R.anim.scale_in, R.anim.scale_out);
            }

            customAnimations = "";

            if (linkedList == null) {
                linkedList = new LinkedList<String>();
            } else {
                Fragment lastF = fm.findFragmentByTag(nowFragmentId);
                if (lastF != null) {
                    // 如果上一个需要移除上一个,就将fragment tag从 链表中移除
                    if (isRemoveLast) {
                        ft.remove(lastF);
                       // ((BaseFragment)lastF).closeLuaState();
                    } else {
                        linkedList.add(nowFragmentId);
                        ft.detach(lastF);
                    }
                }
            }

            BaseFragment fragment = (BaseFragment)fm.findFragmentByTag(url);
            if (bundle == null) {
                bundle = new Bundle();
            }
            // 传入要解析的page路径
            bundle.putString(BaseFragment.BUNDLE_URL, url);
            bundle.putByteArray("pageData",data);
            bundle.putSerializable(BaseFragment.BUNDLE_MANAGER, this);
            if (fragment == null || fragment.isRemoving()) {
                fragment = (BaseFragment)Fragment.instantiate(mContext, class1.getName(), bundle);
                ft.add(layoutId, fragment, url);
            } else {
                if (bundle != null) {
                    fragment.getArguments().putAll(bundle);
                }
                ft.attach(fragment);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        ft.commitAllowingStateLoss();
        if (linkedList.contains(url)) {
            linkedList.remove(url);
        }
        nowFragmentId = url;
    }

    public Map<String, String> getPageMap() {
        return pageMap;
    }

    /**
     * 绑定pageId与Url
     * 
     * @param key
     * @param page
     */
    public void putPageMap(String key, String page) {
        if (pageMap == null) {
            pageMap = new HashMap<String, String>();
        }
        pageMap.put(key, page);
    }

    public void removePageMap(String key) {
        if (pageMap == null) {
            return;
        }
        pageMap.remove(key);
    }

    public void setPageMap(Map<String, String> pageMap) {
        this.pageMap = pageMap;
    }

    public void setCustomAnimations(String anim) {
        if ("left".equals(anim) || "right".equals(anim) || "top".equals(anim)
                || "bottom".equals(anim) || "fade".equals(anim)) {
            customAnimations = anim;
        }
    }

    public String getKye() {
        return kye;
    }

    public void setKye(String kye) {
        this.kye = kye;
    }

}
