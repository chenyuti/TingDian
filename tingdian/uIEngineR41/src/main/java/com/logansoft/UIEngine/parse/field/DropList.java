

package com.logansoft.UIEngine.parse.field;

import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.logansoft.UIEngine.R;
import com.logansoft.UIEngine.Base.UIEngineDrawable;
import com.logansoft.UIEngine.Base.Entity.UIEngineEntity;
import com.logansoft.UIEngine.Base.Interface.AdapterInterface;
import com.logansoft.UIEngine.fragment.BaseFragment;
import com.logansoft.UIEngine.parse.field.adapterGroup.AdapterGroup;
import com.logansoft.UIEngine.parse.xmlview.GroupView;
import com.logansoft.UIEngine.parse.xmlview.Text.MLabel;
import com.logansoft.UIEngine.utils.Configure;
import com.logansoft.UIEngine.utils.DisplayUtil;
import com.logansoft.UIEngine.utils.StringUtil;

import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;

public class DropList extends AdapterGroup {
    private PopupWindow popupWindow;
    private ListView listView;
    protected MLabel Label;
    
    private ImageView mImageView;
    private UIEngineEntity selected;

    public static final String ATTR_ON_VALUE_CHANGE = "onValueChange";

    private static final String ATTR_LABEL = "label";
    
    public DropList(BaseFragment fragment,GroupView parentView,Element element) {
        super(fragment,parentView,element);
    }
   
    @Override
    protected void parseView() {
    	super.parseView();
    	attrMap.put("selectedMode", "single");
    	defaultClickable=true;
    	Label=new MLabel(baseFragment,this,mElement);
    	((ViewGroup)mView).addView(Label.getView());
    	//Label.getView().setLayoutParams(mLayoutParams);
    	//Label.getView().setBackgroundColor(Color.BLUE);
    	String image = attrMap.get(ATTR_IMAGE);
    	if (!StringUtil.isEmpty(image)) {
    		mImageView = new ImageView(mContext);
    	}
       
    	initDroplist();
    	mView.setOnClickListener(new OnClickListener() {
    		@Override
            public void onClick(View v) {
                if (popupWindow.isShowing()) {
                    popupWindow.dismiss();
                } else {
                    if (mImageView != null) {
                        Animation animation = AnimationUtils.loadAnimation(mContext,R.anim.image_rotate);
                        mImageView.startAnimation(animation);
                    }
                    popupWindow.setFocusable(true);
                    popupWindow.showAtLocation(baseFragment.getView(), Gravity.BOTTOM, 0, 0);
                }
            }
        });

    }
    @Override
    protected void parseAttributes(){
    	super.parseAttributes();
    	if(mImageView!=null){
             mImageView.setImageDrawable(new UIEngineDrawable(mContext)); 
             LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,
                     LayoutParams.WRAP_CONTENT);
             mImageView.setLayoutParams(params);
             setImageSize(mImageView, params);
             setImage(mImageView, attrMap);
          //   ((LinearLayout)mView).addView(mImageView);
         }
    }

    public void initDroplist() {
        LinearLayout layout = new LinearLayout(mContext);
        layout.setOrientation(LinearLayout.VERTICAL);
        TextView tv = new TextView(mContext);
        LayoutParams tvParams = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        tv.setPadding(0, DisplayUtil.dip2px(mContext, 7), 0, DisplayUtil.dip2px(mContext, 7));
        tv.setGravity(Gravity.CENTER);
        tv.setLayoutParams(tvParams);
        tv.setText(attrMap.get(MLabel.ATTR_LABEL));
        tv.setTextColor(0xff999999);
        tv.setTextSize(18);
        tv.setBackgroundColor(0xff333333);
        layout.addView(tv);
        
        NodeList nl=mElement.getChildNodes();
        if(!(entity instanceof UIEngineEntity))
        	entity=new UIEngineEntity(entity);
        List<UIEngineEntity> templateData=entity.getTemplateData();
        for(int nli=0;nli<nl.getLength();nli++){
        	Node n=nl.item(nli);
        	if(n.getNodeType()==Node.ELEMENT_NODE){
        		UIEngineEntity itemEntity=new UIEngineEntity((Element)n);
        		templateData.add(itemEntity);
        	}
        }

        listView = new ListView(mContext);
        listView.setDivider(new ColorDrawable(0xff565656));
        listView.setDividerHeight(1);
        ViewGroup.LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        layout.addView(listView);
        layout.setLayoutParams(layoutParams);

        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                UIEngineEntity bankE = entity.getTemplateData(position);
                setContentText(bankE);
                popupWindow.dismiss();
            }
        });

        popupWindow = new PopupWindow(layout, LayoutParams.MATCH_PARENT, Configure.screenHeight / 3);
        popupWindow.setBackgroundDrawable(new ColorDrawable(0xff666666));
        popupWindow.setOutsideTouchable(true);
        popupWindow.setAnimationStyle(R.style.popDownAnim);
        popupWindow.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                if (mImageView != null) {
                    Animation animation = AnimationUtils.loadAnimation(mContext,
                            R.anim.image_rotate2);
                    mImageView.startAnimation(animation);
                }
            }
        });
        if (entity != null && entity.getTemplateData().size() > 0) {
            setContentText(entity.getTemplateData(0));
        }
    }
	@Override
	public AdapterInterface createAdapter() {
	   return new DroplistAdapter();
	}
	@Override
	public void setAdapter(AdapterInterface adapter){
        listView.setAdapter((DroplistAdapter)adapter);
	}

    private void setContentText(UIEngineEntity selected) {
    	selected.setSelected(true);
        Label.setValue((String)selected.getSubEntityValue(ATTR_LABEL));
        String luaStr = attrMap.get(ATTR_ON_VALUE_CHANGE);
        if (!StringUtil.isEmpty(luaStr)) {
        	executeLua(luaStr);
        }
    }
    @Override
    public void setOnClick(String onClickString){
    }    
    
    public void setValue(String value){
    	super.setValue(value);
        if (!TextUtils.isEmpty(value)) {
            for (UIEngineEntity item :entity.getTemplateData()) {
               if(value.equals(item.getSubEntityValue("value"))){
                   setContentText(item);
                   return;
               }
            }
        }
    }

     /**
     * 获取被选中的value
     * 
     * @return
     */
    public String getValue() {
        if (selected != null) {
            return (String)selected.getSubEntityValue("value");
        }
        return "";
    };


    class DroplistAdapter extends BaseAdapter implements AdapterInterface {

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            UIEngineEntity baseEntity = getItem(position);
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                holder.tv = new TextView(mContext);
                AbsListView.LayoutParams params = new AbsListView.LayoutParams(
                        LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                holder.tv.setGravity(Gravity.CENTER);
                holder.tv.setTextSize(16);
                holder.tv.setPadding(0, DisplayUtil.dip2px(mContext, 7), 0,
                        DisplayUtil.dip2px(mContext, 7));
                holder.tv.setLayoutParams(params);
                convertView = holder.tv;
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }
            holder.tv.setText((String)baseEntity.getSubEntityValue(ATTR_LABEL));
            return convertView;
        }

        class ViewHolder {
            TextView tv;
        }

		@Override
		public int getCount() {
			return entity.getTemplateData().size();
		}

		@Override
		public UIEngineEntity getItem(int position) {
			return entity.getTemplateData().get(position);
		}

		@Override
		public long getItemId(int position) {
	        return position;
		}
    }



}
