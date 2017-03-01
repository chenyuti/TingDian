require ("public")
local userInfoSP
local fallbackListView
local reasonPopupView
local imsiPopupView

local listType
local fallbackData

function PageInit(page)
  userInfoSP=page:getSharedPreferences("NEWUSERINDO")
  fallbackListView=page:getElementById("fallback_listID")
  reasonPopupView=page:getElementById("reason_popupID")
  imsiPopupView=page:getElementById("imsi_popupID")
  
  onTodayList(page)
end

--今天的一键回退列表
function onTodayList(page)
  listType="today"
  local Parameters={
    NetSelector="NetData:",                
    URL="/client/checkOrderByPageNo.do",
    LuaNetCallBack="onTodayListNetCallBack(page,{response})",
    LuaExceptionCallBack="onTodayListExceptionCallBack(page)",
    RequestParams={pageNo="1"},
    Method="POST",
    ResponseType="JSON",    
    Message="正在加载"
  }
  page:request(Parameters);
end

function onTodayListNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 then
    local data=response["response"]["data"]
    if #data == 0 then
      setElementHidden(page,"group_notFallbackID",false)
      setElementHidden(page,"fallback_listID",true)
    elseif #data > 0 then
      setElementHidden(page,"group_notFallbackID",true)
      setElementHidden(page,"fallback_listID",false)
      
      fallbackData=data
      fallbackListView:getEntity():getTemplateData():clear()
      fallbackListView:getEntity():setMaxPage(response["response"]["pageSize"])
      local entityData={}
      for i=1,#data,1 do
        local itemData=data[i]
        if itemData["IsReturn"] == "1" then
          entityData[i]={subEntityValues={imsiID=itemData["IMSI"],mobileID=itemData["PhoneNum"],nameID=itemData["RealName"],order_numID=itemData["OrderSeq"],business_nameID=itemData["BusinessType"],actualID=(itemData["TotalPrice"]/100).."元",acceptID=itemData["Operator"],timeID=itemData["TradeTime"],validateID=itemData["LoginType"],remarkID="备注："..itemData["Memo"],isExitID=itemData["IsReturn"],btn_fallbackID={selected="true"}}}
        else
          entityData[i]={subEntityValues={imsiID=itemData["IMSI"],mobileID=itemData["PhoneNum"],nameID=itemData["RealName"],order_numID=itemData["OrderSeq"],business_nameID=itemData["BusinessType"],actualID=(itemData["TotalPrice"]/100).."元",acceptID=itemData["Operator"],timeID=itemData["TradeTime"],validateID=itemData["LoginType"],remarkID="备注："..itemData["Memo"],isExitID=itemData["IsReturn"],btn_fallbackID={selected="false"}}}
        end
      end
      fallbackListView:upDateEntity(entityData)
    end
  else  
    page:showToast(response["resultMsg"])
  end
end

function onTodayListExceptionCallBack(page)
  page:showToast("网络异常！")
  page:hideProgreesDialog()
end

--搜索
function onSearch(page)
  listType="search"
  local searchWord=getElementValue(page,"search_wordID")
  if searchWord == "" or searchWord == nil then
    page:showToast("请输入搜索内容")
  return
  end
  
  local Parameters={
    NetSelector="NetData:",                
    URL="/client/checkCancelOrders.do",
    LuaNetCallBack="onSearchListNetCallBack(page,{response})",
    LuaExceptionCallBack="onSearchExceptionCallBack(page)",
    RequestParams={keyWord=searchWord,channel="",IP=""},
    Method="POST",
    ResponseType="JSON",    
    Message="正在搜索"
  }
  page:request(Parameters);
end

function onSearchListNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 then
    local data=response["response"]["data"]
    if #data == 0 then
      setElementHidden(page,"group_notFallbackID",false)
      setElementHidden(page,"fallback_listID",true)
    elseif #data > 0 then
      setElementHidden(page,"group_notFallbackID",true)
      setElementHidden(page,"fallback_listID",false)
      
      fallbackData=data
      fallbackListView:getEntity():getTemplateData():clear()
      local entityData={}
      for i=1,#data,1 do
        local itemData=data[i]
        if itemData["IsReturn"] == "1" then
          entityData[i]={subEntityValues={imsiID=itemData["IMSI"],mobileID=itemData["PhoneNum"],nameID=itemData["RealName"],order_numID=itemData["OrderSeq"],business_nameID=itemData["BusinessType"],actualID=(itemData["TotalPrice"]/100).."元",acceptID=itemData["Operator"],timeID=itemData["TradeTime"],validateID=itemData["LoginType"],remarkID="备注："..itemData["Memo"],isExitID=itemData["IsReturn"],btn_fallbackID={selected="true"}}}
        else
          entityData[i]={subEntityValues={imsiID=itemData["IMSI"],mobileID=itemData["PhoneNum"],nameID=itemData["RealName"],order_numID=itemData["OrderSeq"],business_nameID=itemData["BusinessType"],actualID=(itemData["TotalPrice"]/100).."元",acceptID=itemData["Operator"],timeID=itemData["TradeTime"],validateID=itemData["LoginType"],remarkID="备注："..itemData["Memo"],isExitID=itemData["IsReturn"],btn_fallbackID={selected="false"}}}
        end
      end
      fallbackListView:upDateEntity(entityData)
    end
  else  
    page:showToast(response["resultMsg"])
  end
end

function onSearchExceptionCallBack(page)
  page:showToast("网络异常！")
  page:hideProgreesDialog()
end

--补换卡、开户查看IMSI
function onIMSI(page,this)
  local businessName=this:getRootView():getElementById("business_nameID"):getValue()
  local imsi=this:getRootView():getElementById("imsiID"):getValue()
  if businessName == "补换卡" or businessName == "开户" then
    setElementValue(page,"imsi_numID","IMSI号码为：\n"..imsi)
    imsiPopupView:showInFragment("center",0,0)
  end
end

function onIMSISure(page)
  imsiPopupView:dismiss()
end
 
--回退
local phoneNum
local orderNum
local timeValue
function onFallback(page,this)
  local isExit=this:getRootView():getElementById("isExitID"):getValue()
  if isExit == "1" then
    phoneNum=this:getRootView():getElementById("mobileID"):getValue()
    orderNum=this:getRootView():getElementById("order_numID"):getValue()
    timeValue=this:getRootView():getElementById("timeID"):getValue()
    reasonPopupView:showInFragment("center",0,0)
  end
end

function onFallbackSure(page)
  reasonPopupView:dismiss()
  local reason=getElementValue(page,"reasonID")
  local Parameters={
    NetSelector="NetData:",                
    URL="/client/cancelOrder.do",
    LuaNetCallBack="FallbackNetCallBack(page,{response})",
    LuaExceptionCallBack="FallbackExceptionCallBack(page)",
    RequestParams={phoneNum=phoneNum,channel="",IP="",orderSeq=orderNum,reason=reason,payType="1"},
    Method="POST",
    ResponseType="JSON",    
    Message="正在回退"
  }
  page:request(Parameters);
end

function FallbackNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 then
    for i=1,#fallbackData,1 do
      local itemData=fallbackData[i]
      if timeValue == itemData["TradeTime"] then
        local entityData={}
        local entity=fallbackListView:getEntity();
        entity:getTemplateData():remove(i-1);
  
        entityData[i]={subEntityValues={imsiID=itemData["IMSI"],mobileID=itemData["PhoneNum"],nameID=itemData["RealName"],order_numID=itemData["OrderSeq"],business_nameID=itemData["BusinessType"],actualID=(itemData["TotalPrice"]/100).."元",acceptID=itemData["Operator"],timeID=itemData["TradeTime"],validateID=itemData["LoginType"],remarkID="备注："..itemData["Memo"],isExitID=itemData["IsReturn"],btn_fallbackID={selected="false"}}}
        fallbackListView:upDateEntity(entityData)  
      end
    end  
    page:showToast("回退成功")
  else  
    page:showToast(response["resultMsg"])
  end
end

function FallbackExceptionCallBack(page)
  page:showToast("网络异常！")
  page:hideProgreesDialog()
end

local pageNo=1
function onFlush(page)
  if listType == "today" then
    local Parameters={
      NetSelector="NetData:",                
      URL="/client/checkOrderByPageNo.do",
      LuaNetCallBack="onFlushNetCallBack(page,{response})",
      LuaExceptionCallBack="onFlushExceptionCallBack(page)",
      RequestParams={pageNo=pageNo+1},
      Method="POST",
      ResponseType="JSON",    
      Message="正在加载"
    }
    page:request(Parameters);
  elseif listType == "search" then
    fallbackListView:onRefreshComplete()
  end
end

function onFlushNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 then
    pageNo=pageNo+1
    local data=response["response"]["data"]
    fallbackData=data
    local entityData={}
    for i=1,#data,1 do
      local itemData=data[i]
      if itemData["IsReturn"] == "2" then
        entityData[i]={subEntityValues={imsiID=itemData["IMSI"],mobileID=itemData["PhoneNum"],nameID=itemData["RealName"],order_numID=itemData["OrderSeq"],business_nameID=itemData["BusinessType"],actualID=(itemData["TotalPrice"]/100).."元",acceptID=itemData["Operator"],timeID=itemData["TradeTime"],validateID=itemData["LoginType"],remarkID="备注："..itemData["Memo"],isExitID=itemData["IsReturn"]},attributes={selected="true"}}
      else
        entityData[i]={subEntityValues={imsiID=itemData["IMSI"],mobileID=itemData["PhoneNum"],nameID=itemData["RealName"],order_numID=itemData["OrderSeq"],business_nameID=itemData["BusinessType"],actualID=(itemData["TotalPrice"]/100).."元",acceptID=itemData["Operator"],timeID=itemData["TradeTime"],validateID=itemData["LoginType"],remarkID="备注："..itemData["Memo"],isExitID=itemData["IsReturn"]},attributes={selected="false"}}
      end
    end
    fallbackListView:upDateEntity(entityData)  
    fallbackListView:onRefreshComplete()
  else  
    page:showToast(response["resultMsg"])
  end  
end

function onFlushExceptionCallBack(page)
  page:showToast("网络异常！")
  fallbackListView:onRefreshComplete()
end

function goBack(page)
  page:setCustomAnimations("right")
  page:goBack()  
end

function onBackPressed(page)
  goBack(page)
end