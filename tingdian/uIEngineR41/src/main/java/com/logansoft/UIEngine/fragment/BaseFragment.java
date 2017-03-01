
package com.logansoft.UIEngine.fragment;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.keplerproject.luajava.LuaException;
import org.keplerproject.luajava.LuaObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.logansoft.UIEngine.Base.Entity.UIEngineEntity;
import com.logansoft.UIEngine.DB.DBManager;
import com.logansoft.UIEngine.parse.XmlParser;
import com.logansoft.UIEngine.parse.UIEntity.UnLoadLua;
import com.logansoft.UIEngine.parse.xmlview.BaseView;
import com.logansoft.UIEngine.parse.xmlview.GroupView;
import com.logansoft.UIEngine.parse.xmlview.PageView;
import com.logansoft.UIEngine.parse.xmlview.RunableHelper;
import com.logansoft.UIEngine.provider.LuaProvider;
import com.logansoft.UIEngine.utils.GlobalConstants;
import com.logansoft.UIEngine.utils.LogUtil;
import com.logansoft.UIEngine.utils.MyDialog;
import com.logansoft.UIEngine.utils.ProgressDialogUtil;
import com.logansoft.UIEngine.utils.Statistics;
import com.logansoft.UIEngine.utils.StreamUtil;
import com.logansoft.UIEngine.utils.StringUtil;
import com.logansoft.UIEngine.utils.ThreadUtil;
import com.logansoft.UIEngine.utils.http.RequestListener;
import com.logansoft.UIEngine.utils.http.RequestManager;
import com.nostra13.universalimageloader.cache.disc.DiskCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.analytics.MobclickAgent;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Toast;


public class BaseFragment extends Fragment implements OnTouchListener {
    private PageView rootView;// 缓存Fragment view
    public BaseFragmentManager manager;
    private String mId;
    public BaseFragmentHandler mHandler;
    public int baseId = 0x000001;
    public static final String BUNDLE_URL = "URL";
    public static final String BUNDLE_MANAGER = "Manager";
    public static final byte MSG_RETURNKEY = 9;//密码键盘完成按钮
    private Map<String, Element> templates;
    private ArrayList<UnLoadLua> unLoadLuaMap;
    private String luaUrl;
    protected BackHandledInterface mBackHandledInterface;  
    private boolean ViewDidLoad;

    private LuaProvider luaProvider;
    
    protected boolean viewAppearance;

    /** 
     * 所有继承BackHandledFragment的子类都将在这个方法中实现物理Back键按下后的逻辑 
     * FragmentActivity捕捉到物理返回键点击事件后会首先询问Fragment是否消费该事件 
     * 如果没有Fragment消息时FragmentActivity自己才会消费该事件 
     */  
    public void onBackPressed(){
        loadLua(null, "onBackPressed(page)");
    };  
    
    /**
     * 创建新实例
     * 
     * @param url
     * @return
     */
    public static BaseFragment newInstance(Bundle bundle) {
        BaseFragment f = new BaseFragment();
        f.setArguments(bundle);
        return f;
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity!=null&&activity instanceof BackHandledInterface) {
            mBackHandledInterface = (BackHandledInterface)activity;
        }
    }
    

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {  
        LogUtil.d("pageLife",this.toString()+" "+mId+" onCreateView");
        Date start=new Date();

        if (manager == null) {
            manager = (BaseFragmentManager)getArguments().getSerializable(BUNDLE_MANAGER);
            manager.init(getFragmentManager(), getActivity());
        }       
        try {
            hideProgreesDialog();
            if (rootView == null) {
                byte[] pageData=getArguments().getByteArray("pageData");
                if(pageData!=null){
                    InputStream is = new ByteArrayInputStream(pageData);
                    if (is != null) {
    	                loadLua(rootView, "PageInit(page)");
                        rootView = XmlParser.getParser().readXMLByDOM(is, this);
                        LogUtil.d("pageLife",this.toString()+" "+mId+" parse rootView");
                    }
                }
                if (rootView!=null) {
                    NodeList luaNode = rootView.mElement.getElementsByTagName(GlobalConstants.XML_LUA);
                    if (luaNode.getLength() > 0) {
                        Element childElement = (Element)luaNode.item(0);
                        luaUrl = childElement.getAttribute("url");
                        if(luaUrl!=null && luaUrl.length()!=0)
                        	luaProvider=new LuaProvider(this.getActivity(),luaUrl){
								@Override
								protected void didLoadLua(final LuaProvider lp) {
									if(unLoadLuaMap!=null && !unLoadLuaMap.isEmpty()){
										if(luaProvider==null)
											luaProvider=lp;
										Runnable r=new Runnable(){
											@Override
											public void run() {
												if (unLoadLuaMap != null && unLoadLuaMap.size()>0) {
													ArrayList<UnLoadLua> temp=unLoadLuaMap;
													unLoadLuaMap=new ArrayList<UnLoadLua>();
													for(UnLoadLua ul:temp)
										                loadLua(null,ul.getLuaStr(),ul.getMap());
													if (unLoadLuaMap != null && unLoadLuaMap.size()>0) 
														this.run();
										        }
											}
					    				};
					    				r.run();
									}
								}
                        	
                        };
                    }
                }
            }

            if (rootView != null) {
                /*
                 * 缓存的rootView需要判断是否已经被加过parent， 如果有parent需要从parent删除，
                 * 要不然会发生这个rootview已经有parent的错误。
                 */
                ViewGroup parent = (ViewGroup)rootView.getView().getParent();
                if (parent != null) {
                    parent.removeView(rootView.getView());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        loadLua(rootView, "onPageCreate(page)");
        Date end=new Date();
    	Statistics.addItemNameandTimeMillis("page", end.getTime()-start.getTime());
    	LogUtil.d(Statistics.TAG,"page " +mId +" cost:"+ (end.getTime()-start.getTime())+"ms");
//        Statistics.show();
    	Statistics.showMemoryInfo();
        return rootView == null ? null : rootView.getView();
    }

    public PageView getRootView() {
        return rootView;
    }
	public void setValueForKey(String key,String value){
		if(rootView!=null)
			rootView.setValueForKey(key,value);
	}
	public String getValueForKey(String key){
		if(rootView!=null)
			return rootView.getValueForKey(key);
		return null;
	}

    /**
     * 加载lua
     * 
     * @param view 执行对象
     * @param luaStr 执行函数
     */
    public boolean loadLua(BaseView view, String luaStr) {
        return loadLua(view, luaStr, null);
    }

    /**
     * 加载lua
     * 
     * @param view 执行对象
     * @param luaStr 执行函数
     */
    public boolean loadLua(BaseView view, final String luaStr,Map<String, Object> map) {
        if (map == null) {
            map = new HashMap<String, Object>();
        }
        if(view!=null)
        	map.put("this", view);
        map.put("page", BaseFragment.this);
        if (luaProvider==null) {
            if (unLoadLuaMap == null) {
                unLoadLuaMap = new ArrayList<UnLoadLua>();
            }
            UnLoadLua un = new UnLoadLua(luaStr, map);
            unLoadLuaMap.add(un);
            return false;
        }
        final Map<String, Object> tempMap=map;
    	Runnable r=new Runnable(){
			@Override
			public void run() {
				if(luaProvider!=null)
					luaProvider.callLua(luaStr, tempMap);
			}
		};
        //if(Looper.myLooper() != Looper.getMainLooper())
        //	mHandler.post(r);
        //else
        	r.run();
        return true;
    }

    /**
     * Get an instance of {@linkplain XmlParser}. Usually be used by lua.
     * 
     * @return
     */
    public XmlParser getXmlParser() {
        return XmlParser.getParser();
    }

    public BaseFragment getPageById(String id) {
        String tag = manager.getPageMap().get(id);
        BaseFragment page = manager.findFragmentByTag(tag);
        return page;
    }


    public InputStream stringToInputStream(String str) {
        return StreamUtil.stringToInStream(str);
    }
   
    public void putShareHeadler(String key, String value) {
        RequestManager.putShareHeader(key, value);
    }
    public void removeShareHeadler(String key) {
        RequestManager.removerShareHeader(key);
    }

    public void log(Object object) {
        if (!LogUtil.getGate()) {
        	return;
        }
        if(object instanceof LuaObject){
			try {
				object=LuaProvider.toObject((LuaObject) object);
			} catch (LuaException e) {
				e.printStackTrace();
			}
		}
        if(object instanceof Map)
			LogUtil.d("\n"+LogUtil.logMap((Map<String, Object>) object));
    	else if(object instanceof ArrayList){
			LogUtil.d("\n"+LogUtil.logArray((List<Object>) object));
    	}
		else if(object instanceof Document){
			LogUtil.d("\n"+XmlParser.DocumentToString(((Document)object).getDocumentElement()));
		}    	
		else if(object instanceof Element){
			LogUtil.d("\n"+XmlParser.DocumentToString((Element)object));
		}
		else if(object==null)
    		LogUtil.d("null");
		else
    		LogUtil.d(object.toString());
    }

    private String message=null;
    private boolean cancelable;
    public void showProgreesDialog(String message) {
        if (!StringUtil.isEmpty(message)) {
        	this.message=message;
          	this.cancelable=false;
          	if(this.ViewDidLoad)
          		ProgressDialogUtil.shouPD(getActivity(), message);
        }
    }
    public void showProgreesDialog(String message,boolean cancelable){
    	  if (!StringUtil.isEmpty(message)) {
          	this.message=message;
          	this.cancelable=cancelable;
          	if(this.ViewDidLoad)
          		ProgressDialogUtil.shouPD(getActivity(), message,cancelable);
          }
    }

    public void hideProgreesDialog() {
      	this.message=null;
        ProgressDialogUtil.dismissPD();
    }
    
    public void request(Map<String, Object> params) {
    	RequestListener requestListener=null;
    	try {
    		
    		String Message=(String)params.get("Message");
    		if(Message!=null && Message.length()!=0){
    			if(params.containsKey("cancelable"))
    				showProgreesDialog(Message);
    			else{
    				String cacelable=(String)params.get("cancelable");
    				showProgreesDialog(Message,"true".equals(cacelable)?true:false);
    			}
    		}
//	    	LogUtil.d(params.toString());
    		requestListener=new RequestListener(){
    			public void hideMessageifNeed(){
    				final String Message=(String)this.params.get("Message");
    				if(Message!=null && Message.length()!=0){
    					hideProgreesDialog();
    				}
    			}
    			@Override
    			public void DidCanceled(){
    				hideMessageifNeed();
    			}
    			@Override
    			public void exception(Exception e){
    				LogUtil.e("exception");
    				hideMessageifNeed();
    				final String LuaExceptionCallBack=(String)this.params.get("LuaExceptionCallBack");
    				if(LuaExceptionCallBack!=null) {
    					mHandler.post(new Runnable() {
    						@Override
    						public void run(){
    							loadLua(null,LuaExceptionCallBack,new HashMap<String, Object>());
    						}
    					});
    				}
    			}
    			@Override
    			public void DidLoad(Object result,final Map<String,Object>responseLuaArgs,int callBackType){
    				Object ho=responseLuaArgs.get("headers");
    				Object rc=responseLuaArgs.get("resultCode");
    				String luaCallBack=null;
					Object tempLuaCallBack=null;
    				if(callBackType==NetCallBackType)
    					tempLuaCallBack=this.params.get("LuaNetCallBack");
    				else if(callBackType==LocalCallBackType)
    					tempLuaCallBack=this.params.get("LuaLocalCallBack");
					else if(callBackType==CacheDidUpdateCallBackType)
						tempLuaCallBack=this.params.get("LuaCacheDidUpdateCallBack");
    				if(tempLuaCallBack instanceof String)
    					luaCallBack=(String)tempLuaCallBack;
    				if(ho instanceof Map && "Login".equals(((Map<String,String>) ho).get("nextAction"))
    						|| (rc instanceof Integer && rc==(Integer)(-2))){
    					Activity activity=BaseFragment.this.getActivity();
    					if(activity instanceof UIEngineBaseActivity){
    						hideMessageifNeed();
    						Runnable r=new Runnable(){
    							@Override
    							public void run(){
    								Activity activity=BaseFragment.this.getActivity();
    								((UIEngineBaseActivity)activity).onRelogin();
    							}
    						};
    						mHandler.post(r);
    						return;
    					}
						luaCallBack="onLogin(page,{response})";
//						loadLua(null,"onLogin(page,{response})",responseLuaArgs);
    				}
    				if(result instanceof Document){
    					XmlParser.getParser().checkUpdate((Document)result);
    					upDateEntity(result);
    				}
    				if(luaCallBack!=null && luaProvider!=null){
    					RunableHelper r=new RunableHelper(luaCallBack){
    						@Override
    						public void run(){
    							HashMap<String,Object> luaparams=new HashMap<String,Object>();
    							try {
    								luaparams.put("response",luaProvider.getToLuaObject(responseLuaArgs));
    							} catch (LuaException e) {
    								e.printStackTrace();
    							}
    							loadLua(null,(String)objectValue,luaparams);
    							hideMessageifNeed();
    						}
    					};
    					mHandler.post(r);
    				}
    				else
    					hideMessageifNeed();                    
    			}
    		};
    		//warrring be carefull!
    		params.put(RequestManager.REQUESTLISTENER, requestListener);
    		RequestManager.request(params);
    	} catch (Exception e) {
    		e.printStackTrace();
    		if(requestListener!=null)
    			requestListener.exception(e);
    	}   
    }    
    
    @SuppressWarnings("unchecked")
	public void request(LuaObject luaParams) {
    	Object object=null;
    	Map<String,Object> params=null;
    	try {
			object = LuaProvider.toObject(luaParams);
		} catch (LuaException e) {
			e.printStackTrace();
		}
		if(!(object instanceof Map))
			return;
		else if (object instanceof Map){
			params=(Map<String, Object>) object;
			request(params);
		}
    }
    
    public void upDateEntity(Object object){
    	if(rootView==null)
    		return;
    	if(object instanceof String){
    		  DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
              try {
				DocumentBuilder builder = factory.newDocumentBuilder();
				object = builder.parse(StreamUtil.stringToInStream((String)object));
			} catch (Exception e) {
				e.printStackTrace();
			}
    	}
    	if(object instanceof Document ){
			Document response=(Document)object;
			NodeList dataElements=response.getElementsByTagName(GlobalConstants.XML_DATA);
			UIEngineEntity entity=rootView.getEntity();
			if(entity==null)
				entity=new UIEngineEntity();
	        for (int i = 0; i < dataElements.getLength(); i++) {
				Node dataElement=dataElements.item(i);
				if(dataElement.getNodeType()==Node.ELEMENT_NODE){
					NodeList DataChildElements=dataElement.getChildNodes();
					for(int dceI=0;dceI<DataChildElements.getLength();dceI++){
						Node DataChildElement=DataChildElements.item(dceI);
						if(DataChildElement.getNodeType()==Node.ELEMENT_NODE){
							if(DataChildElement.getNodeName().equals("item")){
								String id=((Element)DataChildElement).getAttribute("id");
								String value=((Element)DataChildElement).getAttribute("value");
								if(id!=null && id.length()!=0 && value!=null && value.length()!=0){
									entity.setSubEntityValue(id, value);
								}
							}else if(DataChildElement.getNodeName().equals("list")){
								entity.setEntityData(DataChildElement);
							}else if(DataChildElement.getNodeName().equals("hidden")){
								String id=((Element)DataChildElement).getAttribute("id");
								String value=((Element)DataChildElement).getAttribute("value");
								if(id!=null && id.length()!=0 && value!=null && value.length()!=0){
									hidden.put(id, value);
								}
							}
						}
					}
				}
			}
	        rootView.upDateEntity(entity);
    	}
    	else
	        rootView.upDateEntity(object);
    }

    /**
     * 执行增删改语句
     * 
     * @param sql
     */
    public void executeSQL(final String sql) {
        ThreadUtil.sThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                DBManager.getDBInstance().execSQL(sql);
                ProgressDialogUtil.dismissPD();
            }
        });
    }

    public void postDelayed(final String lua, long delayMillis) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadLua(rootView, lua);
            }
        }, delayMillis);
    }

    /**	
     * 执行查询语句
     * 
     * @param sql
     * @return
     */
    public void selecteSQL(final String sql, final String luaStr) {
        ThreadUtil.sThreadPool.execute(new Runnable() {
            @Override
            public void run() {
            	Cursor cursor = DBManager.getDBInstance().selected(sql);
            	  List<Map<String, String>> selectList = new ArrayList<Map<String, String>>();
                  while (cursor.moveToNext()) {
                  	Map<String, String> map = new HashMap<String, String>();
                  	for (String columnName : cursor.getColumnNames()) {
                  		int columnIndex = cursor.getColumnIndex(columnName);
                  		String string = cursor.getString(columnIndex);
                  		map.put(columnName, string);
                  	}
                  	selectList.add(map);
                  }
                  cursor.close();
                final Map<String, Object> map = new HashMap<String, Object>();
                map.put("data", selectList);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        loadLua(null, luaStr, map);
                    }
                });
            }
        });
    }

    /**
     * 提示 Toast
     * 
     * @param content
     */
    public void showToast(String content) {
    	showToast(content,Toast.LENGTH_LONG);
    }
    public void showToast(String content,int time) {
    	if(content!=null && content.length()>0)
    		Toast.makeText(getActivity(), content,time).show();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 初始化Lua
        if (hidden == null) 
        	hidden = new HashMap<String, String>();
        if (mHandler == null) {
            mHandler = new BaseFragmentHandler(this);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mBackHandledInterface!=null) {
            mBackHandledInterface.setSelectedFragment(this);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }


    
    @Override
    public void onResume() {
        super.onResume();
        viewAppearance=true;
        if(rootView!=null)
        	rootView.viewWillAppear();
        MobclickAgent.onPageStart("MainScreen"); //统计页面
        loadLua(rootView, "onPageResume(page)");
    }

    @Override
    public void onPause() {
        super.onPause();
        viewAppearance=false;
        if(rootView!=null)
        	rootView.viewDidDisappear();
        MobclickAgent.onPageEnd("MainScreen"); 
        loadLua(rootView, "onPagePause(page)");
        hideKeyboard();
    }
    @Override
    public void onLowMemory()
    {
    	super.onLowMemory();
    	if(rootView!=null)
     		rootView.onLowMemory();
        loadLua(rootView, "onLowMemory(page)");
    	LogUtil.e("onLowMemory");
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Statistics.showMemoryInfo();
        loadLua(rootView, "onPageDestroy(page)");
        LogUtil.d("pageLife",this.toString()+" "+mId+" onDestroyView()");

    }

    public void removeViewTree(View v){
    	if(v instanceof ViewGroup && !(v instanceof AdapterView)){
    		ViewGroup vg=(ViewGroup)v;
    		int count=vg.getChildCount();
    		for(int i=0;i<count;i++){
    			View tv=vg.getChildAt(i);
    			this.removeViewTree(tv);
    		}
    		vg.removeAllViews();
    	}
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        loadLua(rootView, "onDestroy(page)");
        if(rootView!=null){
        	rootView.Destory();
        	View v=rootView.getView();
        	if(v instanceof ViewGroup){
        		((ViewGroup)v).removeAllViews();
        	}
        }
        rootView=null;
        if (luaProvider!= null) {
        	luaProvider.close();
        }
        if(unLoadLuaMap!=null)
        	unLoadLuaMap.clear();
        RequestManager.cancelAll(this);
        manager.removePageMap(mId);
        
        LogUtil.d("pageLife",this.toString()+" "+mId+" onDestroy()");
    }
    @Override
    public void onDetach(){
    	super.onDetach();
    	ImageLoader.getInstance().clearMemoryCache();
    	//System.gc();
    }

    public Bundle initBundle() {
        return new Bundle();
    }

    public void nextPage(String url) {
        nextPage(url, false);
    }

    public void nextPage(String url, boolean remove) {
        nextPage(url, remove, null);
    }

    public void nextPage(String url, boolean remove, Bundle bundle) {
        switchFragment(url, null, remove, bundle);
    }

    /**
     * 延时执行切换
     * 
     * @param url
     * @param l 延长的时间
     */
    public void nextPage(final String url, int l, final boolean remove, final Bundle bundle) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                switchFragment(url, null, remove, bundle);
            }
        }, l);
    }

    /**
     * 跳转上一页面
     * 
     * @param url
     * @param l
     */
    public void goBack() {
//        mHandler.post(new Runnable() {
//            @Override
//            public void run() {
                 String last=manager.getlastFragment();
                if (TextUtils.isEmpty(last)){
                    getActivity().finish();
                }else {
                    switchFragment(manager.getlastFragment(), null, true, null);
                }
            }
//        });
//    }

    /**
     * 跳转上一页面带参数
     * 
     * @param url
     * @param l
     */
    public void goBack(final Bundle bunle) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                 String last=manager.getlastFragment();
                if (TextUtils.isEmpty(last)){
                    getActivity().finish();
                }else {
                    switchFragment(manager.getlastFragment(), null, true, bunle);
                }
            }
        });
    }


    /**
     * 切换fragment
     * 
     * @param url
     * @param className
     * @param isRemove
     */
    public void switchFragment(String url, String className, boolean isRemove, Bundle bundle) {
        
//    	LogUtil.d(this+"  ===switchFragment===== "+className);
//    	LogUtil.d(this+"  ===switchFragment url===== "+url);
    	
    	try {
            Class<?> cla = null;
            if (StringUtil.isEmpty(className)) {
                cla = BaseFragment.class;
            } else {
                cla = Class.forName(className);
            }

            manager.switchFragment(isRemove, url, cla, bundle);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            LogUtil.i("传入的calssName的值不正确");
        }
    }

    public void setCustomAnimations(String anim) {
        manager.setCustomAnimations(anim);
    }
    
    /**
     * 获取子BaseView
     * 
     * @param key
     * @return
     */
    public BaseView getElementById(String id){
    	 BaseView view = null;
         try {
             view = rootView.getElementById(id);
         } catch (Exception e) {
             e.printStackTrace();
         }
         return view;
    }
    
    public Map<String, MyDialog> dialogs = new HashMap<String, MyDialog>();

    public MyDialog getDialog(String droplistKey) {
        MyDialog dialog = null;
        if (templates != null && !StringUtil.isEmpty(droplistKey)
                && !dialogs.containsKey(droplistKey)) {
            Element droplist = templates.get(droplistKey);
            BaseView view = new GroupView(this,rootView,droplist);
            dialog = new MyDialog(getActivity());
            dialog.setContentView(view);
            dialogs.put(droplistKey, dialog);
        } else if (!StringUtil.isEmpty(droplistKey) && dialogs.containsKey(droplistKey)) {
            dialog = dialogs.get(droplistKey);
        }
        return dialog;
    }
    
    public void removeDialog(String droplistKey){
        dialogs.remove(droplistKey);
    }

    /**
     * 调用系统打电话
     * 
     * @param phoneNum
     */
    public void callPhone(String phoneNum) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + phoneNum));
        getActivity().startActivity(intent);
    }

    /**
     * 调用系统发短信
     * 
     * @param phoneNum
     */
    public void sendMessage(String phoneNum) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("smsto:" + phoneNum));
        getActivity().startActivity(intent);
    }
    

    public void sendMessageByContent(String content) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setType("vnd.android-dir/mms-sms");
        intent.putExtra("sms_body", content);
        getActivity().startActivity(intent);
    }

    public void sendMessage(String phoneNum,String content) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("smsto:" + phoneNum));
        intent.putExtra("sms_body", content);
        getActivity().startActivity(intent);
    }

    
    public void putDialogTemplate(String key, Element value) {
        if (templates == null) {
            templates = new HashMap<String, Element>();
        }
        templates.put(key, value);
    }

    public Map<String, Element> getDialogTemplate() {
        return templates;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // 拦截触摸事件防止斜路下去
        view.setOnTouchListener(this);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return true;
    }

  

    public void post(String thread,String luaString){
    	if(thread!=null || "main".equals(thread)){
    		RunableHelper r=new RunableHelper(luaString){
    			@Override
				public void run() {
					loadLua(null,(String)objectValue);
				}
    		};
    		mHandler.post(r);
    	}else if("background".equals(thread)){
    		
    	}
    }

    public String getmId() {
        return mId;
    }

    public void setmId(String mId) {
        this.mId = mId;
        manager.putPageMap(mId, getArguments().getString(BaseFragment.BUNDLE_URL));
    }
    
  

    public class BaseFragmentHandler extends Handler {

        private WeakReference<BaseFragment> mWeakReference;

        @SuppressLint("HandlerLeak")
        public BaseFragmentHandler(BaseFragment fragment) {
            mWeakReference = new WeakReference<BaseFragment>(fragment);
        }

        @Override
        public void handleMessage(Message message) {
            BaseFragment fragment = mWeakReference.get();
            if (fragment == null) {
                return;
            }
            switch (message.what) {
                case MSG_RETURNKEY:
                 	//loadLua(null, "returnKey(page)");  
                	if (manager.getCurrentFragment()!=null){
                	 	manager.getCurrentFragment().loadLua(null, "returnKey(page)");
                   }
                	break;    
                case 404:
                    // fragment.stopLoadPage = true;
                    break;
            }
        }
    }

    Map<String, String> hidden;

    public void putHidden(Map<String, String> mHidden) {
        hidden.putAll(mHidden);
    }

    public Map<String, String> getHiddenMap() {
        return hidden;
    }
    public String getHidden(String key){
    	return hidden.get(key);
    }
    public LuaObject getHidden(LuaObject keyArray){
		try {
			Object object = LuaProvider.toObject(keyArray);
			if(object instanceof ArrayList){
				HashMap<String,String> result=getHidden((ArrayList)object);
			    return luaProvider.getToLuaObject(result);
			}
		} catch (LuaException e) {
			e.printStackTrace();
		}
		return null;
    }
    public HashMap<String,String> getHidden(ArrayList<String> keyArray){
    	HashMap<String,String> result=new HashMap<String,String>();
    	for(String key :keyArray){
    		String value=hidden.get(key);
    		if(value!=null) result.put(key, value);
    	}
		return result;
    } 
    
    
  
  //不要用
    public void putTempStorage(String name,LuaObject tempStorage){
    	try {
			Object object = LuaProvider.toObject(tempStorage);
			if(object instanceof Map){
				putTempStorage(name,(Map)object);
			}
		} catch (LuaException e) {
			e.printStackTrace();
		}
    }
    //不要用
    public void putTempStorage(String name,Map<String, String> tempStorage){
    	SharedPreferences sp=getActivity().getSharedPreferences(name,Context.MODE_PRIVATE);
    	Editor editor=sp.edit();
    	Iterator<String> iterator = tempStorage.keySet().iterator();
    	while (iterator.hasNext()) {
    		String key = iterator.next();
    		editor.putString(key,tempStorage.get(key));
    	}
    	editor.commit();
    }
    //不要用
    public LuaObject getTempStorage(String name,LuaObject keyArray){
    	try {
  			Object object = LuaProvider.toObject(keyArray);
  			if(object instanceof ArrayList){
  				HashMap<String,String> result=getTempStorage(name,(ArrayList)object);
  			    return luaProvider.getToLuaObject(result);
  			}
  		} catch (LuaException e) {
  			e.printStackTrace();
  		}
  		return null;
    }
    //不要用
    public HashMap<String,String> getTempStorage(String name,ArrayList<String> keys){
    	SharedPreferences sp=getActivity().getSharedPreferences(name,Context.MODE_PRIVATE);
    	HashMap<String,String> result=new HashMap<String,String>();
    	for(String key:keys){
    		if(sp.contains(key))
    			result.put(key, sp.getString(key,""));
    	}
    	return result;
    }
   
    
    /**
     * 获取android偏好设置
     * 
     * @param shareName
     * @return
     */
    public SharedPreferences getSharedPreferences(String shareName) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(shareName,
                Context.MODE_PRIVATE);
        return sharedPreferences;
    }

    boolean animaLoad = false;

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
    	if (nextAnim <= 0) {
    		if(!ViewDidLoad){
            	ViewDidLoad=true;
    			if(message!=null)
            		BaseFragment.this.showProgreesDialog(message, cancelable);
                loadLua(rootView, "onViewDidLoad(page)");
    		}
            return super.onCreateAnimation(transit, enter, nextAnim);
        }
        final Animation anim = AnimationUtils.loadAnimation(getActivity(), nextAnim);
        anim.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
            @Override
            public void onAnimationEnd(Animation animation) {   
        		if(!ViewDidLoad){
        			ViewDidLoad=true;
            		if(message!=null)
            			BaseFragment.this.showProgreesDialog(message, cancelable);
                	loadLua(rootView, "onViewDidLoad(page)");
        		}
            }
        });

        return anim;
    }
    
    /**
     * 清除图片缓存
     * 
     * @param url
     * @return
     */
    public void clearCache(){
    	DiskCache dataCache = ImageLoader.getInstance().getDiskCache();
    	dataCache.clear();
    }
    /**
     * 获取图片缓存大小并转化成对应单位的String类型
     * 
     */
    public String getCacheSize(){
    	DiskCache dataCache = ImageLoader.getInstance().getDiskCache();
    	File file = dataCache.getDirectory();
    	long cacheSize = 0;
    	try {
    		cacheSize = getFileSizes(file);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
    	String sizeString = FormetFileSize(cacheSize);
    	return sizeString;
    }
    /**
     * 获取指定文件夹大小
     */
	private long getFileSizes(File f) throws Exception {
		long size = 0;
		File flist[] = f.listFiles();
		for (int i = 0; i < flist.length; i++) {
			if (flist[i].isDirectory()) {
				size = size + getFileSizes(flist[i]);
			} else {
				size = size + getFileSize(flist[i]);
			}
		}
		return size;
	}
	/**
     * 获取指定文件夹大小
     */
	private long getFileSize(File file) throws Exception {
		long size = 0;
		if (file.exists()) {
			FileInputStream fis = null;
			fis = new FileInputStream(file);
			size = fis.available();
		} else {
			file.createNewFile();
			LogUtil.i("获取文件大小", "文件不存在!");
		}
		return size;
	}
	
	public boolean isViewAppearance(){
		return viewAppearance;
	}
	/**
     * 转换大小
     */
	private String FormetFileSize(long size) {
		DecimalFormat df = new DecimalFormat("#.00");
		String fileSizeString = "";
		String wrongSize = "0B";
		if (size == 0) {
			return wrongSize;
		}
		if (size < 1024) {
			fileSizeString = df.format((double) size) + "B";
		} else if (size < 1048576) {
			fileSizeString = df.format((double) size / 1024) + "KB";
		} else if (size < 1073741824) {
			fileSizeString = df.format((double) size / 1048576) + "MB";
		} else {
			fileSizeString = df.format((double) size / 1073741824) + "GB";
		}
		return fileSizeString;
	}
	
    
    public void showKeyBoardWithView(Object view)
    {
    	if(rootView!=null)
    		rootView.showKeyBoardWithView(view);
    }
    public void hideKeyboard()
    {
    	if(rootView!=null)
    		rootView.hideKeyboard();
    }
    
    protected void finalize(){
        LogUtil.d("pageLife",this.toString()+" "+mId+" finalize()");
    }
}
