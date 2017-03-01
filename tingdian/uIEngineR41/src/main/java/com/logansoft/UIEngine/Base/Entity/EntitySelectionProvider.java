package com.logansoft.UIEngine.Base.Entity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class EntitySelectionProvider{
	public static final int MODE_NONE = 0;	 //没有
	public static final int MODE_SINGLE = 1; //单选
	public static final int MODE_MORE = 2; 	 //多选
    private int selectedMode;
    private WeakReference<UIEngineEntity> entityReference;
    private ArrayList<UIEngineEntity> selectedEntities;
    
    public EntitySelectionProvider(UIEngineEntity entity){
    	selectedMode=MODE_NONE;
    	entityReference=new WeakReference<UIEngineEntity>(entity);
    	selectedEntities=new ArrayList<UIEngineEntity>();
    }
    protected UIEngineEntity getEntity(){
    	if(entityReference==null)
    		return null;
    	else
    		return entityReference.get();
    }
    
    public void setSelectedMode(String selectedMethod){
        int i = MODE_NONE;
        if ("multi".equals(selectedMethod)) 
            i = MODE_MORE;
        else if ("single".equals(selectedMethod)) 
            i = MODE_SINGLE;
        setSelectedMode(i);
    }
    public void setSelectedMode(int selectedMode) {
        this.selectedMode = selectedMode;
    }
    public void reloadData(){
    	selectedEntities.clear();
    	for(UIEngineEntity itemEntity:getEntity().getTemplateData()){
    		if(itemEntity.isSelected())
    			setSelected(itemEntity,true);
    	}
    }
    public List<UIEngineEntity> getSelectedEntities(){
        return new ArrayList<UIEngineEntity>(selectedEntities);
    }
    public void setSelected(int position,boolean selected){
    	List<UIEngineEntity> templateData=getEntity().getTemplateData();
    	if(position<templateData.size()){
    		setSelected(templateData.get(position),selected);
    	}
    }
    public void setSelected(UIEngineEntity entity,boolean selected){
    	if(selected==true){
    		if(selectedMode==MODE_SINGLE){
    			if(! (selectedEntities.size()==1 && selectedEntities.get(0)==entity))
    				clearSelectedEntities();
    		}
    		if(entity.isSelected()!=true)	
    			entity._setSelected(true);
    		if(!selectedEntities.contains(entity))
    			selectedEntities.add(entity);
    	}else{
    		if(selectedEntities.contains(entity))
        		selectedEntities.remove(entity);
    		if(entity.isSelected()!=false)	
    			entity._setSelected(false);
    	}
    }
    public void clearSelectedEntities(){
    	for(UIEngineEntity itemEntity:selectedEntities){
			itemEntity._setSelected(false);
		}
		selectedEntities.clear();
    }
    public void Destory(){
    	clearSelectedEntities();
    	entityReference=null;
    	selectedEntities=null;
    }
}
