package com.logansoft.UIEngine.Base.Entity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;

public class EntityArrayList extends ArrayList<UIEngineEntity> {
	private WeakReference<UIEngineEntity> parentEntityReference;
	public EntityArrayList(UIEngineEntity parentEntity) {
		super();
		parentEntityReference=new WeakReference<UIEngineEntity>(parentEntity);
	}
    public EntityArrayList(UIEngineEntity parentEntity,Collection<? extends UIEngineEntity> collection) {
    	super(collection);
		parentEntityReference=new WeakReference<UIEngineEntity>(parentEntity);
    	for(UIEngineEntity entity:collection){
    		entity.setParentEntity(parentEntity);
    	}
    }
    protected UIEngineEntity getParentEntity(){
		return parentEntityReference==null?null:parentEntityReference.get();
    }
    @Override public boolean add(UIEngineEntity object) {
    	boolean result= super.add(object);
    	if(result)
    		object.setParentEntity(getParentEntity());
    	return result;
    }
    @Override public void add(int index, UIEngineEntity object) {
    	object.setParentEntity(getParentEntity());
    	super.add(index,object);
    }
    @Override public boolean addAll(Collection<? extends UIEngineEntity> collection) {
    	UIEngineEntity parentEntity=getParentEntity();
    	for(UIEngineEntity entity:collection){
    		entity.setParentEntity(parentEntity);
    	}
    	return super.addAll(collection);
    }
    @Override
    public boolean addAll(int index, Collection<? extends UIEngineEntity> collection) {
    	UIEngineEntity parentEntity=getParentEntity();
    	for(UIEngineEntity entity:collection){
    		entity.setParentEntity(parentEntity);
    	}
    	return super.addAll(index,collection);
    }
    @Override public void clear() {
    	for(UIEngineEntity entity:this){
    		entity.setParentEntity(null);
    	}
    	super.clear();
    }
    @Override public UIEngineEntity remove(int index) {
    	UIEngineEntity result=super.remove(index);
    	if(result!=null)
    		result.setParentEntity(null);
    	return result;
    }
    @Override public boolean remove(Object object) {
    	boolean result=super.remove(object);
    	if(result && object instanceof UIEngineEntity)
    		((UIEngineEntity) object).setParentEntity(null);
    	return result;
    }
    @Override protected void removeRange(int fromIndex, int toIndex) {
    	if(fromIndex>=0 && fromIndex<=toIndex && toIndex<this.size()){
    		for(int i=fromIndex;i<=toIndex;i++){
    			this.get(i).setParentEntity(null);
    		}
    	}
    	super.removeRange(fromIndex, toIndex);
    }
    @Override public UIEngineEntity set(int index, UIEngineEntity object) {
    	if(index>=0 && index<this.size())
    		object.setParentEntity(getParentEntity());
    	UIEngineEntity entity=super.set(index, object);
    	if(entity instanceof UIEngineEntity)
    		entity.setParentEntity(null);
    	return entity;
    }
}
