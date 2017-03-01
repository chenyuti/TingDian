package com.logansoft.UIEngine.Base.Entity;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.keplerproject.luajava.LuaException;
import org.keplerproject.luajava.LuaObject;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.text.TextUtils;
import android.view.View;

import com.logansoft.UIEngine.Base.Interface.EntityInterface;
import com.logansoft.UIEngine.parse.xmlview.BaseView;
import com.logansoft.UIEngine.parse.xmlview.GroupView;
import com.logansoft.UIEngine.parse.xmlview.Text.MButton;
import com.logansoft.UIEngine.parse.xmlview.Text.MLabel;
import com.logansoft.UIEngine.provider.LuaProvider;
import com.logansoft.UIEngine.utils.GlobalConstants;
import com.logansoft.UIEngine.utils.LogUtil;

public class UIEngineEntity implements Serializable, EntityInterface {
	final private HashMap<String, String> attributes;
	final private HashMap<String, Object> subEntityValues;// id为自己的时候呢？
	final private EntityArrayList templateData;// baseView?
	private Object originalData;
	private String entityStyle;
	private String templateDataStyle;
	private int pageIndex;
	private int maxPage;
	private WeakReference<BaseView> viewReference;
	private WeakReference<UIEngineEntity> parentEntityReference;
	private EntitySelectionProvider SelectionProvider;

	// 设计需求：
	// api可以兼容IOS与android
	// 便捷lua使用设计
	// 满足旧版xml生成，以及新版json数据源生成。
	// json数据源模式时，attribute的值不是String时需要自动转换String
	// 可以直接对entity操作同步到view上。
	// JSON的服务器与界面id转换模板支持
	// 1通过数据源新建entity/2已有entity基础上通过数据源更新
	// 满足一般view使用以及ListTable view复用
	// 简单传值时轻量化，String代替？
	// 要一定程度满足不同控件使用时自定义继承hook？
	// boolean呢？
	// 只增加一个templateData呢
	// potition?
	// 直接获取某个id的value呢？

	public UIEngineEntity() {
		attributes = new HashMap<String, String>();
		subEntityValues = new HashMap<String, Object>();
		templateData = new EntityArrayList(this);
	}

	public UIEngineEntity(Object data) {
		super();
		attributes = new HashMap<String, String>();
		subEntityValues = new HashMap<String, Object>();
		templateData = new EntityArrayList(this);
		setEntityData(data);
	}

	public void setEntityData(Object data) {
		if (data instanceof LuaObject) {
			try {
				data = LuaProvider.toObject((LuaObject) data);
			} catch (LuaException e) {
				e.printStackTrace();
				return;
			}
		}
		if (data instanceof String)
			setValue((String) data);
		else if (data instanceof Number)
			setValue(data.toString());
		else if (data instanceof UIEngineEntity) {
			UIEngineEntity Data = (UIEngineEntity) data;
			setAttributes(Data.attributes);
			HashMap<String, Object> tempMap = new HashMap<String, Object>();
			Iterator<Map.Entry<String, Object>> it = Data.subEntityValues
					.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, Object> entry = it.next();
				if (entry.getValue() instanceof UIEngineEntity)
					tempMap.put(entry.getKey(),
							new UIEngineEntity(entry.getValue()));
				else
					tempMap.put(entry.getKey(), entry.getValue());
			}
			setSubEntityValues(tempMap);
			// setSubEntityValues((Map)Data.subEntityValues);//warnning
			setPageIndex(Data.getPageIndex());
			setMaxPage(Data.getMaxPage());
			setStyle(Data.getStyle());
			setTemplateDataStyle(Data.getTemplateDataStyle());
			ArrayList<UIEngineEntity> tempList = new ArrayList<UIEngineEntity>();
			for (UIEngineEntity entity : Data.getTemplateData()) {
				tempList.add(new UIEngineEntity(entity));
			}
			addTemplateData(tempList);// warnning
			// addTemplateData(Data.getTemplateData());//warnning
		} else if (data instanceof List) {
			List<UIEngineEntity> Data = (List<UIEngineEntity>) data;
			ArrayList<UIEngineEntity> tds = new ArrayList<UIEngineEntity>();
			for (Object itemData : (List<?>) Data) {
				tds.add(new UIEngineEntity(itemData));
			}
			addTemplateData(tds);
		} else if (data instanceof Map) {
			Map<String, Object> Data = (Map<String, Object>) data;
			Map<String, Object> simple = new HashMap<String, Object>();
			Object attributes = Data.get("attributes");
			if (attributes instanceof Map)
				simple.putAll((Map) attributes);
			if (Data.get("value") != null)
				simple.put("value", Data.get("value"));
			if (Data.get("selected") != null)
				simple.put("selected", Data.get("selected"));
			if (Data.get("hidden") != null)
				simple.put("hidden", Data.get("hidden"));
			setAttributes(simple);
			createSelectionProvider(Data.get("selectMode"));
			Object subEntityValues = Data.get("subEntityValues");
			if (subEntityValues instanceof Map)
				setSubEntityValues((Map) subEntityValues);
			setPageIndex(Data.get(GlobalConstants.ATTR_INDEX));
			setMaxPage(Data.get(GlobalConstants.ATTR_MAX));
			setStyle(Data.get("style"));
			setTemplateDataStyle(Data.get("templateDataStyle"));
			Object datalist = Data.get("templateData");
			if (datalist instanceof List) {
				setEntityData(datalist);
			}
		} else if (data instanceof Element) {
			Element Data = (Element) data;
			if (Data.getNodeName().equals(GlobalConstants.XML_ITEM)) {
				NamedNodeMap attrs = Data.getAttributes();
				for (int i = 0; i < attrs.getLength(); i++) {
					Node attrr = attrs.item(i);
					String id = attrr.getNodeName();
					if (id.equals("selected")) {
						attributes.put(id, attrr.getNodeValue());
						if (getBaseView() != null)
							getBaseView().setSelectedString(
									attrr.getNodeValue());
					} else if (id.equals("hidden")) {
						attributes.put(id, attrr.getNodeValue());
						if (getBaseView() != null)
							getBaseView().setHidden(attrr.getNodeValue());
					} else if (id.equals("value"))
						setValue(attrr.getNodeValue());
					else if (id.equals("selectMode"))
						createSelectionProvider(attrr.getNodeValue());
					else if (id.equals("style"))
						setStyle(attrr.getNodeValue());
					else if (id.equals("id"))
						continue;
					else
						setSubEntityValue(id, attrr.getNodeValue());
				}
				NodeList nodelist = Data.getChildNodes();
				for (int i = 0; i < nodelist.getLength(); i++) {
					Node node = nodelist.item(i);
					if (node.getNodeType() == Node.ELEMENT_NODE) {
						Element element = (Element) node;
						if (element.getNodeName().equals(
								GlobalConstants.XML_LIST)) {
							this.setEntityData(element);
						} else {
							String id = element.getNodeName();
							Object subAttributesEntity = subEntityValues
									.get(id);
							if (!(subAttributesEntity instanceof UIEngineEntity)) {
								subAttributesEntity = new UIEngineEntity(
										subAttributesEntity);
								setSubEntityValue(id, subAttributesEntity);
							}
							((UIEngineEntity) subAttributesEntity)
									.setAttributes(UIEngineEntity
											.getAttributesMap(element));
						}
					}
				}
			} else if (Data.getNodeName().equals(GlobalConstants.XML_LIST)) {
				String id = Data.getAttribute("id");
				UIEngineEntity listEntity = this;
				if (id != null && id.length() != 0) {
					Object leo = subEntityValues.get(id);
					if (!(leo instanceof UIEngineEntity)) {
						listEntity = new UIEngineEntity(leo);
						setSubEntityValue(id, listEntity);
					} else
						listEntity = (UIEngineEntity) leo;
				}
				NamedNodeMap attrs = Data.getAttributes();
				for (int i = 0; i < attrs.getLength(); i++) {
					Node attrr = attrs.item(i);
					listEntity.setSubEntityValue(attrr.getNodeName(),
							attrr.getNodeValue());
				}
				NodeList listnodelist = Data.getChildNodes();
				listEntity.setTemplateDataStyle(Data.getAttribute("style"));
				ArrayList<UIEngineEntity> listEntitylist = new ArrayList<UIEngineEntity>();
				for (int listi = 0; listi < listnodelist.getLength(); listi++) {
					Node listnode = listnodelist.item(listi);
					if (listnode.getNodeType() == Node.ELEMENT_NODE) {
						listEntitylist.add(new UIEngineEntity(listnode));
					}
				}
				listEntity.setPageIndex(Data
						.getAttribute(GlobalConstants.ATTR_INDEX));
				listEntity.setMaxPage(Data
						.getAttribute(GlobalConstants.ATTR_MAX));
				listEntity.addTemplateData(listEntitylist);
			}
		}
		if (data instanceof String || data instanceof Number
				|| data instanceof List || data instanceof Map
				|| data instanceof Element)
			setOriginalData(data);
	}

	@Override
	// about attribute
	public void setSelected(boolean selected) {
		attributes.put("selected", selected ? "true" : "false");
		BaseView bv = getBaseView();
		if (bv != null) {
			bv.setSelected(selected);
			UIEngineEntity parentEntity = this.getParentEntity();
			if (parentEntity != null) {
				EntitySelectionProvider selectionProvider = parentEntity
						.getSelectedProvider();
				if (selectionProvider != null
						&& parentEntity.getTemplateData().contains(this)) {
					selectionProvider.setSelected(this, selected);
				}
			}
		}
	}

	// call by entitySelectionProvider
	protected void _setSelected(boolean selected) {
		attributes.put("selected", selected ? "true" : "false");
		BaseView bv = getBaseView();
		if (bv != null) {
			bv.setSelected(selected);
		}
	}

	@Override
	public boolean isSelected() {
		String selectedString = attributes.get("selected");
		if ((selectedString == null || selectedString.length() == 0)
				&& getBaseView() != null)
			return getBaseView().isSelected();
		return "true".equals(selectedString) ? true : false;
	}

	@Override
	public void setValue(String value) {
		attributes.put("value", value);
		BaseView bv = getBaseView();
		if (bv != null)
			bv.setValue(value);
	}

	@Override
	public String getValue() {
		String value = attributes.get("value");
		if (value != null && value.length() != 0)
			return value;
		else if (getBaseView() != null)
			return getBaseView().getValue();
		return null;
	}

	@Override
	public void setValueForKey(String key, String value) {
		setSubEntityValue(key, value);
	}

	@Override
	public String getValueForKey(String key) {
		Object value = getSubEntityValue(key);
		if (value instanceof UIEngineEntity) {
			return ((UIEngineEntity) value).getValue();
		} else if (value instanceof String)
			return (String) value;
		return null;
	}

	@Override
	public void setAttributes(Object attributeMap) {
		attributeMap = LuaProvider.toObjectDefault(attributeMap);
		if (attributeMap instanceof Map)
			setAttributes((Map<String, String>) attributeMap);
	}

	public void setAttributes(Map<String, String> attributes) {
		Iterator<Map.Entry<String, String>> it = attributes.entrySet()
				.iterator();
		while (it.hasNext()) {
			Map.Entry<String, String> entry = it.next();
			Object value = entry.getValue();
			if (value instanceof Number)
				entry.setValue(value.toString());
		}
		this.attributes.putAll(attributes);
		BaseView bv = getBaseView();
		if (bv != null) {
			bv.setAttributes(attributes);
			if (attributes.containsKey("selceted")) {
				UIEngineEntity parentEntity = this.getParentEntity();
				if (parentEntity != null) {
					if (parentEntity.getTemplateData().contains(this)) {
						EntitySelectionProvider selectionProvider = parentEntity
								.getSelectedProvider();
						selectionProvider.setSelected(this, bv.isSelected());
					}
				}
			}
		}
	}

	@Override
	public String getAttribute(String key) {
		return attributes.get(key);
	}

	@Override
	public Map<String, String> getAttributes() {
		return attributes;
	}

	// about SubEntityValue
	public void setSubEntityValue(String key, Object SubEntityValue) {
		Object valueEntity = this.subEntityValues.get(key);
		BaseView bv = getBaseView();
		if ((SubEntityValue instanceof UIEngineEntity)
				|| ((valueEntity == null || valueEntity instanceof String) && SubEntityValue instanceof String)) {
			this.subEntityValues.put(key, SubEntityValue);
			if (bv != null) {
				BaseView subBv = bv.getElementById(key);
				if (subBv == bv) {
					if (SubEntityValue instanceof String)
						setValue((String) SubEntityValue);
				} else if (subBv != null)
					subBv.upDateEntity(SubEntityValue);
			}
		} else if (valueEntity == null) {
			UIEngineEntity subValue = new UIEngineEntity(SubEntityValue);
			this.subEntityValues.put(key, subValue);
			if (bv != null) {
				BaseView subBv = bv.getElementById(key);
				if (subBv == bv) {
					if (SubEntityValue instanceof String)
						setValue((String) SubEntityValue);
				} else if (subBv != null)
					subBv.upDateEntity(subValue);
			}
		} else
			((UIEngineEntity) valueEntity).setEntityData(SubEntityValue);
	}

	public void setSubEntityValues(LuaObject values) {
		try {
			Object val = LuaProvider.toObject(values);
			if (val instanceof Map)
				setSubEntityValues((Map) val);
		} catch (LuaException e) {
			e.printStackTrace();
		}
	}

	public void setSubEntityValues(Map<String, Object> valuesMap) {
		Iterator<Map.Entry<String, Object>> it = valuesMap.entrySet()
				.iterator();
		while (it.hasNext()) {
			Map.Entry<String, Object> entry = it.next();
			setSubEntityValue(entry.getKey(), entry.getValue());
		}
	}

	public Object getSubEntityValue(String key) {
		return subEntityValues.get(key);
	}

	public Map<String, Object> getSubEntityValues() {
		return subEntityValues;
	}

	// addTemplateData 添加templateData
	// clearTemplateData 清空templateData
	// reloadTemplateData 重新加载templateData
	// IOS写个特殊类吗
	// about templateDatas
	public void setStyle(Object style) {
		if (style instanceof String && ((String) style).length() != 0)
			entityStyle = (String) style;
		else
			entityStyle = null;
	}

	public String getStyle() {
		return entityStyle;
	}

	public void setTemplateDataStyle(Object style) {
		if (style instanceof String && ((String) style).length() != 0)
			templateDataStyle = (String) style;
		else
			templateDataStyle = null;
	}

	public String getTemplateDataStyle() {
		return templateDataStyle;
	}

	public void setPageIndex(Object pageIndex) {
		if (pageIndex instanceof Number)
			this.pageIndex = ((Number) pageIndex).intValue();
		else if (pageIndex instanceof String
				&& ((String) pageIndex).matches("\\d*")) {
			if ("".equals(pageIndex))
				this.pageIndex = 1;
			else
				this.pageIndex = Integer.parseInt((String) pageIndex);
		}
		if (this.pageIndex == 1)
			clearTemplateData();
	}

	public int getPageIndex() {
		return pageIndex;
	}

	public void setMaxPage(Object MaxPage) {
		if (MaxPage instanceof Number)
			this.maxPage = ((Number) MaxPage).intValue();
		else if (MaxPage instanceof String
				&& ((String) MaxPage).matches("\\d*")) {
			if ("".equals(MaxPage))
				this.maxPage = 0;
			else
				this.maxPage = Integer.parseInt((String) MaxPage);
		}
	}

	public int getMaxPage() {
		return maxPage;
	}

	public void addTemplateData(Object dataList) {
		dataList = LuaProvider.toObjectDefault(dataList);
		if (dataList instanceof List)
			addTemplateData((List) dataList);
	}

	public void addTemplateData(List<UIEngineEntity> data) {
		for (UIEngineEntity subEntity : data) {
			if (subEntity.getStyle() == null)
				subEntity.setStyle(getTemplateDataStyle());
		}
		this.templateData.addAll(data);
		BaseView bv = getBaseView();
		if (bv instanceof GroupView)
			((GroupView) bv).addTemplateData(data);
		if (SelectionProvider != null)
			SelectionProvider.reloadData();
	}

	public UIEngineEntity getTemplateData(int position) {
		if (templateData.size() > position)
			return templateData.get(position);
		return null;
	}

	public List<UIEngineEntity> getTemplateData() {
		return templateData;
	}

	public void clearTemplateData() {
		this.templateData.clear();
		BaseView bv = getBaseView();
		if (bv instanceof GroupView)
			((GroupView) bv).clearTemplateData();
		if (SelectionProvider != null)
			SelectionProvider.clearSelectedEntities();
	}

	public void reloadTemplateData() {
		if (SelectionProvider != null)
			SelectionProvider.reloadData();
		BaseView bv = getBaseView();
		if (bv instanceof GroupView)
			((GroupView) bv).reloadTemplateData();
		if (SelectionProvider != null)
			SelectionProvider.reloadData();
	}

	public int getTemplateDataSize() {
		return templateData.size();
	}

	// about OriginalData
	public void setOriginalData(Object originalData) {
		this.originalData = LuaProvider.toObjectDefault(originalData);
	};

	public Object getOriginalData() {
		return originalData;
	}

	// 由baseView调用，不要直接用
	public void setBaseView(BaseView bv, UIEngineEntity entity) {
		if (bv == null) {
			if (getBaseView() instanceof GroupView) {
				((GroupView) getBaseView()).clearTemplateData();
			}
			viewReference = null;
			Iterator<Map.Entry<String, Object>> it = subEntityValues.entrySet()
					.iterator();
			if (entity != null) {
				LogUtil.d("cyt", "fdsfdsaf1111" + entity.getOriginalData());
			}
			while (it.hasNext()) {
				Map.Entry<String, Object> entry = it.next();
				Object value = entry.getValue();
				if (value instanceof UIEngineEntity
						&& ((UIEngineEntity) value).getBaseView() != null)
					((UIEngineEntity) value).getBaseView().upDateEntity(null);
			}
			for (UIEngineEntity subEntity : templateData) {
				if (subEntity.getBaseView() != null)
					subEntity.getBaseView().upDateEntity(null);
			}
		} else if (viewReference == null || viewReference.get() != bv) {
			viewReference = new WeakReference<BaseView>(bv);
			bv.setAttributes(attributes);
			if (bv instanceof GroupView) {
				createSelectionProvider(((GroupView) bv).getAttributes().get(
						"selectMode"));
			}
			Iterator<Map.Entry<String, Object>> it = subEntityValues.entrySet()
					.iterator();

			if (bv != null && entity != null) {
				setTag(entity, bv);
			}

			while (it.hasNext()) {
				Map.Entry<String, Object> entry = it.next();
				BaseView idView = bv.getElementById(entry.getKey());
				if (idView == bv) {
					if (entry.getValue() instanceof String)
						setValue((String) entry.getValue());
				} else if (idView != null)
					idView.upDateEntity(entry.getValue());
			}
			if (bv instanceof GroupView) {
				((GroupView) bv).addTemplateData(templateData);
			}

		}
	}

	private void setTag(UIEngineEntity entity, BaseView bv) {
		HashMap<String, String> attr = new HashMap<>();
		if (bv == null) {
			return;
		}
		if (bv instanceof GroupView) {
			attr = bv.getAttributes();
			List<BaseView> list = ((GroupView) bv).getChildViews();
			if (list != null) {
				for (BaseView baseView : list) {
					setTag(entity, baseView);
				}
			}

			if (!TextUtils.isEmpty(attr.get(BaseView.ON_CHILD_ITEM_CLICK))) {
				if (entity != null) {
					String serverData = entity.getOriginalData().toString();
					if(bv instanceof MLabel){
						((MLabel)bv).setTag(serverData);
					}
				}
				if(bv instanceof MLabel){
					LogUtil.d(
							"cyt",
							"resut in child item click = "
									+ ((MLabel)bv).getTag());
				}
			}
		} else {
			attr = bv.getAttributes();

			if (!TextUtils.isEmpty(attr.get(BaseView.ON_CHILD_ITEM_CLICK))) {
				if (entity != null) {
					String serverData = entity.getOriginalData().toString();
					if(bv instanceof MLabel){
						((MLabel)bv).setTag(serverData);
					}
				}
				if(bv instanceof MLabel){
					LogUtil.d(
							"cyt",
							"resut in child item click = "
									+ ((MLabel)bv).getTag());
				}
			}
		}
	}

	public BaseView getBaseView() {
		return viewReference == null ? null : viewReference.get();
	}

	public void createSelectionProvider(Object selectedMode) {
		if (SelectionProvider != null
				|| !(selectedMode instanceof String && ((String) selectedMode)
						.length() != 0))
			return;
		SelectionProvider = new EntitySelectionProvider(this);
		SelectionProvider.setSelectedMode((String) selectedMode);
	}

	public EntitySelectionProvider getSelectedProvider() {
		return SelectionProvider;
	}

	public void setParentEntity(UIEngineEntity parent) {
		parentEntityReference = new WeakReference<UIEngineEntity>(parent);
	}

	public UIEngineEntity getParentEntity() {
		return parentEntityReference == null ? null : parentEntityReference
				.get();
	}

	// @Override
	// public String toString(){
	// StringBuilder sb=new StringBuilder();
	// sb.append(super.toString());
	// sb.append("\n attributes:");
	// sb.append(attributes.toString());
	// sb.append("\n");
	// sb.append("subEntityValues:");
	// sb.append(subEntityValues.toString());
	// sb.append("\n");
	// sb.append("templateData:");
	// sb.append(templateData.toString());
	// return sb.toString();
	// }
	public void Destory() {
		// subEntityValues?
		originalData = null;
		entityStyle = null;
		templateDataStyle = null;
		viewReference = null;
		if (SelectionProvider != null)
			SelectionProvider.Destory();
	}

	// ------------------------------------
	static public HashMap<String, String> getAttributesMap(Element element) {
		HashMap<String, String> attrMap = new HashMap<String, String>();
		NamedNodeMap attrs = element.getAttributes();
		for (int i = 0; i < attrs.getLength(); i++) {
			Node attrr = attrs.item(i);
			String n = attrr.getNodeName();
			String v = attrr.getNodeValue();
			attrMap.put(n, v);
		}
		return attrMap;
	}

}
