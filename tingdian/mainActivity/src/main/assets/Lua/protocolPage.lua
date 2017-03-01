require ("public")
local userInfoSP
local protocolButton
local businessButton
local protocolDetail

function PageInit(page)
  userInfoSP=page:getSharedPreferences("NEWUSERINDO")
  protocolButton=page:getElementById("btn_protocolID")
  businessButton=page:getElementById("btn_businessID")
  
  onProtocolType(page,protocolButton,"1")
    
  local protocol=page:getArguments():getString("protocol")
  local signatureURL=page:getArguments():getString("signatureURL")
  if signatureURL == "" or signatureURL == nil then
    setElementHidden(page,"signatureID",true)
    setElementHidden(page,"btn_signatureID",false)  
  else
    setElementValue(page,"signatureID","local://"..signatureURL)
    setElementValue(page,"signature_urlID",signatureURL)
    setElementHidden(page,"signatureID",false)
    setElementHidden(page,"btn_signatureID",true)  
  end
  
  if protocol == "removeRealName" then
    onRemoveRealName(page)
  elseif protocol == "realName" then
    onRealName(page)
  elseif protocol == "stopOn" then
    onStopOn(page)
  elseif protocol == "payment" then
    onPayment(page)
  elseif protocol == "replacementCard" then
    onReplacementCard(page)
  elseif protocol == "resetPassword" then
    onResetPassword(page)
  elseif protocol == "prestore" then
    onPrestore(page)
  elseif protocol == "openAccount" then
    onOpenAccount(page)
  elseif protocol == "broadband" then
    onBroadband(page)
  elseif protocol == "4G" then
    on4GMeal(page)
  elseif protocol == "flowYear" then
    onFlowYear(page)
  end
end

function onNextStep(page)
  onProtocolType(page,businessButton,"2")
end

local nextProtocol
function onProtocolType(page,this,type)
  if nextProtocol ~= nil and nextProtocol ~= this then
    nextProtocol:setSelected(false)
  end
  nextProtocol=this
  this:setSelected(true)

  setElementHidden(page,"group_protocolID",true)
  setElementHidden(page,"group_businessID",true)
  if type == "1" then
    setElementHidden(page,"group_protocolID",false)
  elseif type == "2" then
    setElementHidden(page,"group_businessID",false)
  end
end

--退登记
function onRemoveRealName(page)
  local Parameters={
    NetSelector="NetData:",                
    URL="/client/regstatusmodifyBill.do",
    LuaNetCallBack="onRemoveRealNameNetCallBack(page,{response})",
    LuaExceptionCallBack="onRemoveRealNameExceptionCallBack(page)",
    RequestParams={},
    Method="POST",
    ResponseType="JSON",    
    Message="正在加载"
  }
  page:request(Parameters);
end

function onRemoveRealNameNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 then
    local data=response["response"]["data"]
    setElementValue(page,"timeID",data["HandleDate"])
    setElementValue(page,"mobileID","手机号码："..data["PhoneNum"])
    setElementValue(page,"nameID","客户姓名/名称：")
    
    local businessType=page:getArguments():getString("businessType")
    local listView=page:getElementById("detail_listID")
    listView:getEntity():getTemplateData():clear()
    local entityData={}
    entityData[1]={subEntityValues={detailID="品牌："..data["Brand"]}}
    entityData[2]={subEntityValues={detailID="业务类型："..data["BusinessType"]}}
    entityData[3]={subEntityValues={detailID="优惠费用：￥"..data["BargainsFee"]}}
    entityData[4]={subEntityValues={detailID="实收费用：￥"..data["LastFee"]}}
    entityData[5]={subEntityValues={detailID="鉴权类型："..data["LoginType"]}}
    entityData[6]={subEntityValues={detailID="操作员ID："..data["Operator"]}}
    entityData[7]={subEntityValues={detailID="操作员渠道："..data["ChannelNo"]}}
    listView:upDateEntity(entityData)    
  else  
    page:showToast(response["resultMsg"])
  end
end

function onRemoveRealNameExceptionCallBack(page)
  page:showToast("网络异常！")
  page:hideProgreesDialog()
end

--实名登记
function onRealName(page)
  local businessType=page:getArguments():getString("businessType")
  local newClientName=page:getArguments():getString("newName")
  local newClientAddress=page:getArguments():getString("newAddress")
  local newClientCardNo=page:getArguments():getString("newCardNum")
  local newClientCardType=page:getArguments():getString("newCardType")
  local newUsageName=page:getArguments():getString("userName")
  local newUsageAddress=page:getArguments():getString("userAddress")
  local newUsageCardNo=page:getArguments():getString("userCardNum")
  local newUsageCardType=page:getArguments():getString("userCardType")
  local handleManCardType=page:getArguments():getString("managerCardType")
  local handleMan=page:getArguments():getString("managerName")
  local handleManCardNo=page:getArguments():getString("managerCardNum")
  local handleManCardAddr=page:getArguments():getString("managerAddress")
  local Parameters={
    NetSelector="NetData:",                
    URL="/businessbill/resignBill.do",
    LuaNetCallBack="RealNameNetCallBack(page,{response})",
    LuaExceptionCallBack="RealNameExceptionCallBack(page)",
    RequestParams={businessType=businessType,newClientName=newClientName,newClientAddress=newClientAddress,newClientCardNo=newClientCardNo,newClientCardType=newClientCardType,newUsageName=newUsageName,newUsageAddress=newUsageAddress,newUsageCardNo=newUsageCardNo,newUsageCardType=newUsageCardType,handleMan=handleMan,handleManCardType=handleManCardType,handleManCardNo=handleManCardNo,handleManCardAddr=handleManCardAddr},
    Method="POST",
    ResponseType="JSON",    
    Message="正在加载"
  }
  page:request(Parameters);
end

function RealNameNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 then
    local data=response["response"]["data"]
    local businessType=page:getArguments():getString("businessType")
    setElementValue(page,"timeID",data["HandleDate"])
    setElementValue(page,"mobileID","手机号码："..data["PhoneNum"])
    setElementValue(page,"nameID","客户姓名/名称："..data["UserName"])
    
    local listView=page:getElementById("detail_listID")
    listView:getEntity():getTemplateData():clear()
    local entityData={}
    if businessType == "realNameResignPrivate" then
      entityData[1]={subEntityValues={detailID="品牌："..data["Brand"]}}
      entityData[2]={subEntityValues={detailID="证件类型："..data["CertType"]}}
      entityData[3]={subEntityValues={detailID="经办人："..data["HandleMan"]}}
      entityData[4]={subEntityValues={detailID="证件号码："..data["CertNo"]}}
      entityData[5]={subEntityValues={detailID="业务类型："..data["BusinessType"]}}
      entityData[6]={subEntityValues={detailID="优惠费用：￥"..data["BargainsFee"]}}
      entityData[7]={subEntityValues={detailID="实收费用：￥"..data["LastFee"]}}
      entityData[8]={subEntityValues={detailID="鉴权类型："..data["LoginType"]}}
      entityData[9]={subEntityValues={detailID="变更信息："}}
      entityData[10]={subEntityValues={detailID="新客户名称："..data["NewClientName"]}}
      entityData[11]={subEntityValues={detailID="新客户地址："..data["NewClientAddress"]}}
      entityData[12]={subEntityValues={detailID="新客户证件类型："..data["NewClientCardType"]}}
      entityData[13]={subEntityValues={detailID="新客户证件号码："..data["NewClientCardNum"]}}
      entityData[14]={subEntityValues={detailID="操作员ID："..data["Operator"]}}
      entityData[15]={subEntityValues={detailID="操作员渠道："..data["ChannelNo"]}}      
    elseif businessType == "realNameResignPublic" then
      entityData[1]={subEntityValues={detailID="品牌："..data["Brand"]}}
      entityData[2]={subEntityValues={detailID="证件类型："..data["CertType"]}}
      entityData[3]={subEntityValues={detailID="经办人："..data["HandleMan"]}}
      entityData[4]={subEntityValues={detailID="证件号码："..data["CertNo"]}}
      entityData[5]={subEntityValues={detailID="业务类型："..data["BusinessType"]}}
      entityData[6]={subEntityValues={detailID="优惠费用：￥"..data["BargainsFee"]}}
      entityData[7]={subEntityValues={detailID="实收费用：￥"..data["LastFee"]}}
      entityData[8]={subEntityValues={detailID="鉴权类型："..data["LoginType"]}}
      entityData[9]={subEntityValues={detailID="变更信息："}}
      entityData[10]={subEntityValues={detailID="新客户名称："..data["NewClientName"]}}
      entityData[11]={subEntityValues={detailID="新客户地址："..data["NewClientAddress"]}}
      entityData[12]={subEntityValues={detailID="新客户证件类型："..data["NewClientCardType"]}}
      entityData[13]={subEntityValues={detailID="新客户证件号码："..data["NewClientCardNum"]}}
      entityData[14]={subEntityValues={detailID="新使用人姓名："..data["NewUsageName"]}}
      entityData[17]={subEntityValues={detailID="新使用人证件地址："..data["NewUsageAddress"]}}
      entityData[16]={subEntityValues={detailID="新使用人证件类型："..data["NewUsageCardType"]}}
      entityData[15]={subEntityValues={detailID="新使用人证件号码："..data["NewUsageCardNum"]}}
      entityData[18]={subEntityValues={detailID="操作员ID："..data["Operator"]}}
      entityData[19]={subEntityValues={detailID="操作员渠道："..data["ChannelNo"]}}      
    end
    listView:upDateEntity(entityData)    
  else  
    page:showToast(response["resultMsg"])
  end
end

function RealNameExceptionCallBack(page)
  page:showToast("网络异常！")
  page:hideProgreesDialog()
end

--停开机
function onStopOn(page)
  local businessType=page:getArguments():getString("businessType")
  local Parameters={
    NetSelector="NetData:",                
    URL="/businessbill/modifyBill.do",
    LuaNetCallBack="StopOnNetCallBack(page,{response})",
    LuaExceptionCallBack="StopOnExceptionCallBack(page)",
    RequestParams={businessType=businessType},
    Method="POST",
    ResponseType="JSON",    
    Message="正在加载"
  }
  page:request(Parameters);
end

function StopOnNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 then
    local data=response["response"]["data"]
    setElementValue(page,"timeID",data["HandleDate"])
    setElementValue(page,"mobileID","手机号码："..data["PhoneNum"])
    setElementValue(page,"nameID","客户姓名/名称："..data["UserName"])
    
    local listView=page:getElementById("detail_listID")
    listView:getEntity():getTemplateData():clear()
    local entityData={}
    entityData[1]={subEntityValues={detailID="证件类型："..data["CertType"]}}
    entityData[2]={subEntityValues={detailID="证件号码："..data["CertNo"]}}
    entityData[3]={subEntityValues={detailID="品牌："..data["Brand"]}}
    entityData[4]={subEntityValues={detailID="业务类型："..data["BusinessType"]}}
    entityData[5]={subEntityValues={detailID="优惠费用：￥"..data["BargainsFee"]}}
    entityData[6]={subEntityValues={detailID="实收费用：￥"..data["LastFee"]}}
    entityData[7]={subEntityValues={detailID="鉴权类型："..data["LoginType"]}}
    entityData[8]={subEntityValues={detailID="操作员ID："..data["Operator"]}}
    entityData[9]={subEntityValues={detailID="操作员渠道："..data["ChannelNo"]}}
    listView:upDateEntity(entityData)
    
    protocolDetail=data["HandleDate"].."^"..
    "SZGT[201406]PY-(05)".."^"..
    "手机号码："..data["PhoneNum"].."^"..
    "客户姓名/名称："..data["UserName"].."^"..   
    "证件类型："..data["CertType"].."^"..   
    "证件号码："..data["CertNo"].."^"..   
    "品牌："..data["Brand"].."^"..   
    "业务类型："..data["BusinessType"].."^"..   
    "优惠费用："..data["BargainsFee"].."^"..   
    "实收费用："..data["LastFee"].."^"..   
    "鉴权类型："..data["LoginType"].."^"..   
    "操作员ID："..data["Operator"].."^"..   
    "操作员渠道："..data["ChannelNo"]  
  else  
    page:showToast(response["resultMsg"])
  end
end

function StopOnExceptionCallBack(page)
  page:showToast("网络异常！")
  page:hideProgreesDialog()
end

--缴费
function onPayment(page)
  local actual=page:getArguments():getString("actual")
  local Parameters={
    NetSelector="NetData:",                
    URL="/recharge/rechargeBill.do",
    LuaNetCallBack="PaymentNetCallBack(page,{response})",
    LuaExceptionCallBack="PaymentExceptionCallBack(page)",
    RequestParams={bargainsFee="0.00",lastFee=actual},
    Method="POST",
    ResponseType="JSON",    
    Message="正在加载"
  }
  page:request(Parameters);
end

function PaymentNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 then
    local data=response["response"]["data"]
    setElementValue(page,"timeID",data["HandleDate"])
    setElementValue(page,"mobileID","手机号码："..data["PhoneNum"])
    setElementValue(page,"nameID","客户姓名/名称："..data["UserName"])
    
    local listView=page:getElementById("detail_listID")
    listView:getEntity():getTemplateData():clear()
    local entityData={}
    entityData[1]={subEntityValues={detailID="优惠费用：￥"..data["BargainsFee"]}}
    entityData[2]={subEntityValues={detailID="实收费用：￥"..data["LastFee"]}}
    entityData[3]={subEntityValues={detailID="操作员ID："..data["Operator"]}}
    entityData[4]={subEntityValues={detailID="操作员渠道："..data["ChannelNo"]}}
    listView:upDateEntity(entityData)    
  else  
    page:showToast(response["resultMsg"])
  end
end

function PaymentExceptionCallBack(page)
  page:showToast("网络异常！")
  page:hideProgreesDialog()
end

--补换卡
function onReplacementCard(page)
  local discount=page:getArguments():getString("discount")
  local actual=page:getArguments():getString("actual")
  local businessType=page:getArguments():getString("businessType")
  local sim=page:getArguments():getString("sim")
  local Parameters={
    NetSelector="NetData:",                
    URL="/backup/backupCardBill.do",
    LuaNetCallBack="ReplacementCardNetCallBack(page,{response})",
    LuaExceptionCallBack="ReplacementCardExceptionCallBack(page)",
    RequestParams={bargainsFee=discount,lastFee=actual,businessType=businessType,newSimNum=sim},
    Method="POST",
    ResponseType="JSON",    
    Message="正在加载"
  }
  page:request(Parameters);
end

function ReplacementCardNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 then
    local data=response["response"]["data"]
    setElementValue(page,"timeID",data["HandleDate"])
    setElementValue(page,"mobileID","手机号码："..data["PhoneNum"])
    setElementValue(page,"nameID","客户姓名/名称："..data["UserName"])
    
    local listView=page:getElementById("detail_listID")
    listView:getEntity():getTemplateData():clear()
    local entityData={}
    entityData[1]={subEntityValues={detailID="品牌："..data["Brand"]}}
    entityData[2]={subEntityValues={detailID="证件类型："..data["CertType"]}}
    entityData[3]={subEntityValues={detailID="经办人："..data["HandleMan"]}}
    entityData[4]={subEntityValues={detailID="证件号码："..data["CertNo"]}}
    entityData[5]={subEntityValues={detailID="业务类型："..data["BusinessType"]}}
    entityData[6]={subEntityValues={detailID="鉴权类型："..data["LoginType"]}}
    entityData[7]={subEntityValues={detailID="优惠费用：￥"..data["BargainsFee"]}}
    entityData[8]={subEntityValues={detailID="实收费用：￥"..data["LastFee"]}}
    entityData[9]={subEntityValues={detailID="旧SIM卡号："..data["OldSimNum"]}}
    entityData[10]={subEntityValues={detailID="新SIM卡号："..data["NewSimNum"]}}
    entityData[11]={subEntityValues={detailID="温馨提示："..data["Tips"]}}
    entityData[12]={subEntityValues={detailID="操作员ID："..data["Operator"]}}
    entityData[13]={subEntityValues={detailID="操作员渠道："..data["ChannelNo"]}}
    listView:upDateEntity(entityData)    
  else  
    page:showToast(response["resultMsg"])
  end
end

function ReplacementCardExceptionCallBack(page)
  page:showToast("网络异常！")
  page:hideProgreesDialog()
end

--密码重置
function onResetPassword(page)
  local mobileNum=page:getArguments():getString("mobileNum")
  local businessType=page:getArguments():getString("businessType")
  local resetConditions=page:getArguments():getString("resetCondition")
  local Parameters={
    NetSelector="NetData:",                
    URL="/resetPassword/passwordResetBill.do",
    LuaNetCallBack="ResetPasswordNetCallBack(page,{response})",
    LuaExceptionCallBack="ResetPasswordExceptionCallBack(page)",
    RequestParams={businessType=businessType,phoneNum=mobileNum,resetConditions=resetConditions},
    Method="POST",
    ResponseType="JSON",    
    Message="正在加载"
  }
  page:request(Parameters);
end

function ResetPasswordNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 then
    local data=response["response"]["data"]
    setElementValue(page,"timeID",data["HandleDate"])
    setElementValue(page,"mobileID","手机号码："..data["PhoneNum"])
    setElementValue(page,"nameID","客户姓名/名称："..data["UserName"])
    
    local listView=page:getElementById("detail_listID")
    listView:getEntity():getTemplateData():clear()
    local entityData={}
    entityData[1]={subEntityValues={detailID="品牌："..data["Brand"]}}
    entityData[2]={subEntityValues={detailID="证件类型："..data["CertType"]}}
    entityData[3]={subEntityValues={detailID="证件号码："..data["CertNo"]}}
    entityData[4]={subEntityValues={detailID="经办人："..data["HandleMan"]}}
    entityData[5]={subEntityValues={detailID="业务类型："..data["BusinessType"]}}
    entityData[6]={subEntityValues={detailID="优惠费用：￥"..data["BargainsFee"]}}
    entityData[7]={subEntityValues={detailID="实收费用：￥"..data["LastFee"]}}
    entityData[8]={subEntityValues={detailID="鉴权类型："..data["LoginType"]}}
    entityData[9]={subEntityValues={detailID="重置方式："..data["ResetStyle"]}}
    entityData[10]={subEntityValues={detailID="重置条件："..data["ResetConditions"]}}
    entityData[11]={subEntityValues={detailID="操作员ID："..data["Operator"]}}
    entityData[12]={subEntityValues={detailID="操作员渠道："..data["ChannelNo"]}}
    entityData[13]={subEntityValues={detailID="温馨提示："..data["Tips"]}}
    listView:upDateEntity(entityData)    
  else  
    page:showToast(response["resultMsg"])
  end
end

function ResetPasswordExceptionCallBack(page)
  page:showToast("网络异常！")
  page:hideProgreesDialog()
end

--预存送
function onPrestore(page)
  local discount=page:getArguments():getString("discount")
  local actual=page:getArguments():getString("actual")
  local effectiveTime=page:getArguments():getString("effectiveTime")
  local Parameters={
    NetSelector="NetData:",                
    URL="/storedFee/storedFeeBill2.do",
    LuaNetCallBack="PrestoreNetCallBack(page,{response})",
    LuaExceptionCallBack="PrestoreExceptionCallBack(page)",
    RequestParams={bargainsFee=discount,lastFee=actual,effectiveTime=effectiveTime},
    Method="POST",
    ResponseType="JSON",    
    Message="正在加载"
  }
  page:request(Parameters);
end

function PrestoreNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 then
    local data=response["response"]["data"]
    setElementValue(page,"timeID",data["HandleDate"])
    setElementValue(page,"mobileID","手机号码："..data["PhoneNum"])
    setElementValue(page,"nameID","客户姓名/名称："..data["UserName"])
    
    local listView=page:getElementById("detail_listID")
    listView:getEntity():getTemplateData():clear()
    local entityData={}
    entityData[1]={subEntityValues={detailID="品牌："..data["Brand"]}}
    entityData[2]={subEntityValues={detailID="证件类型："..data["CertType"]}}
    entityData[3]={subEntityValues={detailID="经办人："..data["HandleMan"]}}
    entityData[4]={subEntityValues={detailID="证件号码："..data["CertNo"]}}
    entityData[5]={subEntityValues={detailID="业务类型："..data["BusinessType"]}}
    entityData[6]={subEntityValues={detailID="优惠费用：￥"..data["BargainsFee"]}}
    entityData[7]={subEntityValues={detailID="实收费用：￥"..data["LastFee"]}}
    entityData[8]={subEntityValues={detailID="鉴权类型："..data["LoginType"]}}
    entityData[9]={subEntityValues={detailID="营销方案："..data["MarketingPlan"]}}
    entityData[10]={subEntityValues={detailID="生效时间："..data["EffectiveTime"]}}
    entityData[11]={subEntityValues={detailID="操作员ID："..data["Operator"]}}
    entityData[12]={subEntityValues={detailID="操作员渠道："..data["ChannelNo"]}}
    listView:upDateEntity(entityData)    
  else  
    page:showToast(response["resultMsg"])
  end
end

function PrestoreExceptionCallBack(page)
  page:showToast("网络异常！")
  page:hideProgreesDialog()
end

--全球通开户
function onOpenAccount(page)
  local selectedMobile=page:getArguments():getString("selectedMobile")
  local discount=page:getArguments():getString("discount")
  local actual=page:getArguments():getString("actual")
  local model=page:getArguments():getString("model")
  local Parameters={
    NetSelector="NetData:",                
    URL="/account/openAccountBill.do",
    LuaNetCallBack="OpenAccountNetCallBack(page,{response})",
    LuaExceptionCallBack="OpenAccountExceptionCallBack(page)",
    RequestParams={phoneNum=selectedMobile,bargainsFee=discount..".00",lastFee=actual..".00",payMode=model},
    Method="POST",
    ResponseType="JSON",    
    Message="正在加载"
  }
  page:request(Parameters);
end

function OpenAccountNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 then
    local data=response["response"]["data"]
    setElementValue(page,"timeID",data["HandleDate"])
    setElementValue(page,"mobileID","手机号码："..data["PhoneNum"])
    setElementValue(page,"nameID","客户姓名/名称："..data["UserName"])
    
    local listView=page:getElementById("detail_listID")
    listView:getEntity():getTemplateData():clear()
    local entityData={}
    entityData[1]={subEntityValues={detailID="品牌："..data["Brand"]}}
    entityData[2]={subEntityValues={detailID="证件类型："..data["CertType"]}}
    entityData[3]={subEntityValues={detailID="经办人："..data["HandleMan"]}}
    entityData[4]={subEntityValues={detailID="证件号码："..data["CertNo"]}}
    entityData[5]={subEntityValues={detailID="业务类型："..data["BusinessType"]}}
    entityData[6]={subEntityValues={detailID="优惠费用：￥"..data["BargainsFee"]}}
    entityData[7]={subEntityValues={detailID="实收费用：￥"..data["LastFee"]}}
    entityData[8]={subEntityValues={detailID="鉴权类型："..data["LoginType"]}}
    entityData[9]={subEntityValues={detailID="付费模式："..data["PayMode"]}}
    entityData[10]={subEntityValues={detailID="邮政编码："..data["PostCode"]}}
    entityData[11]={subEntityValues={detailID="使用人："..data["UseMan"]}}
    entityData[12]={subEntityValues={detailID="联系人："..data["LinkMan"]}}
    entityData[13]={subEntityValues={detailID="联系电话："..data["LinkNum"]}}
    entityData[14]={subEntityValues={detailID="通讯地址："..data["LinkAddress"]}}
    entityData[15]={subEntityValues={detailID="订购了产品："..data["ProductInfo"]}}
    entityData[16]={subEntityValues={detailID="操作员ID："..data["Operator"]}}
    entityData[17]={subEntityValues={detailID="操作员渠道："..data["ChannelNo"]}}      
    listView:upDateEntity(entityData)
    
    protocolDetail=data["HandleDate"].."^"..
    "SZGT[201406]PY-(05)".."^"..    
    "手机号码："..data["PhoneNum"].."^"..
    "客户姓名/名称："..data["UserName"].."^"..   
    "品牌："..data["Brand"].."^"..   
    "证件类型："..data["CertType"].."^"..   
    "经办人："..data["HandleMan"].."^"..   
    "证件号码："..data["CertNo"].."^"..   
    "业务类型："..data["BusinessType"].."^"..   
    "优惠费用："..data["BargainsFee"].."^"..   
    "实收费用："..data["LastFee"].."^"..   
    "鉴权类型："..data["LoginType"].."^"..   
    "付费模式："..data["PayMode"].."^"..   
    "邮政编码："..data["PostCode"].."^"..   
    "使用人："..data["UseMan"].."^"..   
    "联系人："..data["LinkMan"].."^"..   
    "联系电话："..data["LinkNum"].."^"..   
    "通讯地址："..data["LinkAddress"].."^"..   
    "订购了产品："..data["ProductInfo"].."^"..   
    "操作员ID："..data["Operator"].."^"..   
    "操作员渠道："..data["ChannelNo"]        
  else  
    page:showToast(response["resultMsg"])
  end
end

function OpenAccountExceptionCallBack(page)
  page:showToast("网络异常！")
  page:hideProgreesDialog()
end

--家庭宽带
function onBroadband(page)
  local discount=page:getArguments():getString("discount")
  local actual=page:getArguments():getString("actual")
  local homeBroadbandType=page:getArguments():getString("homeBroadbandType")
  local prodID=page:getArguments():getString("planID")
  page:log("prodID==="..prodID)
  local Parameters={
    NetSelector="NetData:",                
    URL="/broadband/broadbandBill2.do",
    LuaNetCallBack="BroadbandNetCallBack(page,{response})",
    LuaExceptionCallBack="BroadbandExceptionCallBack(page)",
    RequestParams={bargainsFee=discount,lastFee=actual..".00",homeBroadbandType=homeBroadbandType,prodID=prodID,cospID=prodID},
    Method="POST",
    ResponseType="JSON",    
    Message="正在加载"
  }
  page:request(Parameters);
end

function BroadbandNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 then
    local data=response["response"]["data"]
    local homeBroadbandType=page:getArguments():getString("homeBroadbandType")
    setElementValue(page,"timeID",data["HandleDate"])
    setElementValue(page,"mobileID","手机号码："..data["PhoneNum"])
    setElementValue(page,"nameID","客户姓名/名称："..data["UserName"])
    
    local listView=page:getElementById("detail_listID")
    listView:getEntity():getTemplateData():clear()
    local entityData={}
    entityData[1]={subEntityValues={detailID="证件类型："..data["CertType"]}}
    entityData[2]={subEntityValues={detailID="证件号码："..data["CertNo"]}}
    entityData[3]={subEntityValues={detailID="经办人："..data["HandleMan"]}}
    entityData[4]={subEntityValues={detailID="品牌："..data["Brand"]}}
    entityData[5]={subEntityValues={detailID="业务类型："..data["BusinessType"]}}
    entityData[6]={subEntityValues={detailID="上网账号："..data["BroadbandAccount"]}}
    entityData[7]={subEntityValues={detailID="鉴权类型："..data["LoginType"]}}
    entityData[8]={subEntityValues={detailID="优惠费用：￥"..data["BargainsFee"]}}
    entityData[9]={subEntityValues={detailID="实收费用：￥"..data["LastFee"]}}
    entityData[10]={subEntityValues={detailID="营销方案："..data["MarketingPlan"]}}
    entityData[11]={subEntityValues={detailID="生效时间："..data["EffectiveTime"]}}
    if homeBroadbandType == "2" then
      entityData[12]={subEntityValues={detailID="修改了产品："..data["ChangeProduct"]}}
      entityData[13]={subEntityValues={detailID="操作员ID："..data["Operator"]}}
      entityData[14]={subEntityValues={detailID="操作员渠道："..data["ChannelNo"]}}
    elseif homeBroadbandType == "1" then
      entityData[12]={subEntityValues={detailID="操作员ID："..data["Operator"]}}
      entityData[13]={subEntityValues={detailID="操作员渠道："..data["ChannelNo"]}}
    end
    listView:upDateEntity(entityData)    
  else  
    page:showToast(response["resultMsg"])
  end
end

function BroadbandExceptionCallBack(page)
  page:showToast("网络异常！")
  page:hideProgreesDialog()
end

--4G套餐
function on4GMeal(page)
  local businessType=page:getArguments():getString("businessType")
  local discount=page:getArguments():getString("discount")
  local actual=page:getArguments():getString("actual")
  local commodityCode=page:getArguments():getString("commodityCode")
  local Parameters={
    NetSelector="NetData:",                
    URL="/client/packageHandleBill.do",
    LuaNetCallBack="MealNetCallBack(page,{response})",
    LuaExceptionCallBack="MealExceptionCallBack(page)",
    RequestParams={businessType=businessType,bargainsFee=discount..".00",lastFee=actual..".00",commodityCode=commodityCode},
    Method="POST",
    ResponseType="JSON",    
    Message="正在加载"
  }
  page:request(Parameters);
end

function MealNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 then
    local data=response["response"]["data"]
    setElementValue(page,"timeID",data["HandleDate"])
    setElementValue(page,"mobileID","手机号码："..data["PhoneNum"])
    setElementValue(page,"nameID","客户姓名/名称："..data["UserName"])
    
    local businessType=page:getArguments():getString("businessType")
    local listView=page:getElementById("detail_listID")
    listView:getEntity():getTemplateData():clear()
    local entityData={}
    entityData[1]={subEntityValues={detailID="证件类型："..data["CertType"]}}
    entityData[2]={subEntityValues={detailID="证件号码："..data["CertNo"]}}
    entityData[3]={subEntityValues={detailID="经办人："..data["HandleMan"]}}
    entityData[4]={subEntityValues={detailID="品牌："..data["Brand"]}}
    entityData[5]={subEntityValues={detailID="业务类型："..data["BusinessType"]}}
    entityData[6]={subEntityValues={detailID="优惠费用：￥"..data["BargainsFee"]}}
    entityData[7]={subEntityValues={detailID="实收费用：￥"..data["LastFee"]}}
    entityData[8]={subEntityValues={detailID="鉴权类型："..data["LoginType"]}}
    entityData[9]={subEntityValues={detailID="订购了产品："..data["ProductInfo"]}}
    entityData[10]={subEntityValues={detailID="生效时间："..data["EffectiveTime"]}}
    entityData[11]={subEntityValues={detailID="操作员ID："..data["Operator"]}}
    entityData[12]={subEntityValues={detailID="操作员渠道："..data["ChannelNo"]}}
    listView:upDateEntity(entityData)    
  else  
    page:showToast(response["resultMsg"])
  end
end

function MealExceptionCallBack(page)
  page:showToast("网络异常！")
  page:hideProgreesDialog()
end

--流量年包
function onFlowYear(page)
  local planID=page:getArguments():getString("planID")
  local plan2ID=page:getArguments():getString("plan2ID")
  local Parameters={
    NetSelector="NetData:",                
    URL="/fluxPackage/fluxYearPackageBill2.do",
    LuaNetCallBack="onFlowYearNetCallBack(page,{response})",
    LuaExceptionCallBack="onFlowYearExceptionCallBack(page)",
    RequestParams={bargainsFee="0.00",lastFee="0.00",prodCode=plan2ID,cospCode=planID},
    Method="POST",
    ResponseType="JSON",    
    Message="正在加载"
  }
  page:request(Parameters);
end

function onFlowYearNetCallBack(page,response)
  page:log("*************************流量年包"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 then
    local data=response["response"]["data"]
    setElementValue(page,"timeID",data["HandleDate"])
    setElementValue(page,"mobileID","手机号码："..data["PhoneNum"])
    setElementValue(page,"nameID","客户姓名/名称："..data["UserName"])
    
    local businessType=page:getArguments():getString("businessType")
    local listView=page:getElementById("detail_listID")
    listView:getEntity():getTemplateData():clear()
    local entityData={}
    entityData[1]={subEntityValues={detailID="证件类型："..data["CertType"]}}
    entityData[2]={subEntityValues={detailID="证件号码："..data["CertNo"]}}
    entityData[3]={subEntityValues={detailID="经办人："..data["HandleMan"]}}
    entityData[4]={subEntityValues={detailID="品牌："..data["Brand"]}}
    entityData[5]={subEntityValues={detailID="业务类型："..data["BusinessType"]}}
    entityData[6]={subEntityValues={detailID="优惠费用：￥"..data["BargainsFee"]}}
    entityData[7]={subEntityValues={detailID="实收费用：￥"..data["LastFee"]}}
    entityData[8]={subEntityValues={detailID="营销方案："..data["MarketingPlan"]}}
    entityData[9]={subEntityValues={detailID="生效时间："..data["EffectiveTime"]}}
    entityData[10]={subEntityValues={detailID="操作员ID："..data["Operator"]}}
    entityData[11]={subEntityValues={detailID="操作员渠道："..data["ChannelNo"]}}
    listView:upDateEntity(entityData)    
  else  
    page:showToast(response["resultMsg"])
  end
end

function onFlowYearExceptionCallBack(page)
  page:showToast("网络异常！")
  page:hideProgreesDialog()
end

--点击签名
local word="确认已阅读并同意合约详情、《中国移动通讯集团广东有限公司电子渠道商品销售及服务电子协议》及其他相关说明的所有条款"
function onSignature(page)
  local mobile=string.sub(getElementValue(page,"mobileID"),16,string.len(getElementValue(page,"mobileID")))
  local protocol=page:getArguments():getString("protocol")
  
  if protocol == "realName" then
    local businessType=page:getArguments():getString("businessType")
    if businessType == "realNameResignPrivate" then
      page:getActivity():onWriteSignature(word,mobile,"ResignPrivate")
    elseif businessType == "realNameResignPublic" then
      page:getActivity():onWriteSignature(word,mobile,"ResignPublic")
    end
    
  elseif protocol == "stopOn" then
    local businessType=page:getArguments():getString("businessType")
    if businessType == "stopMachine" then
      page:getActivity():onWriteSignature(word,mobile,"StopMachine")
    elseif businessType == "startMachine" then
      page:getActivity():onWriteSignature(word,mobile,"OpenMachine")
    end
    
  elseif protocol == "payment" then
    page:getActivity():onWriteSignature(word,mobile,"PayCharge")
    
  elseif protocol == "replacementCard" then
    local businessType=page:getArguments():getString("businessType")
    if businessType == "0" then
      page:getActivity():onWriteSignature(word,mobile,"BackupCard")
    elseif businessType == "1" then
      page:getActivity():onWriteSignature(word,mobile,"ExchangeCard")
    end
    
  elseif protocol == "resetPassword" then
    page:getActivity():onWriteSignature(word,mobile,"PasswordReset")
    
  elseif protocol == "prestore" then
    page:getActivity():onWriteSignature(word,mobile,"StoreFee")
    
  elseif protocol == "openAccount" then
    page:getActivity():onWriteSignature(word,mobile,"OpenAccount")
    
  elseif protocol == "broadband" then
    page:getActivity():onWriteSignature(word,mobile,"Broadband")
    
  elseif protocol == "4G" then
    local businessType=page:getArguments():getString("businessType")
    if businessType == "fourG" then
      page:getActivity():onWriteSignature(word,mobile,"FourGHandle")
    elseif businessType == "fluxPackage" then
      page:getActivity():onWriteSignature(word,mobile,"fluxPackage")
    elseif businessType == "overlayPackage" then
      page:getActivity():onWriteSignature(word,mobile,"overlayPackage")
    elseif businessType == "fluxKing" then
      page:getActivity():onWriteSignature(word,mobile,"fluxKing")
    end
  elseif protocol == "removeRealName" then
    page:getActivity():onWriteSignature(word,mobile,"UnsubscribeRealName")
  elseif protocol == "flowYear" then
    page:getActivity():onWriteSignature(word,mobile,"FluxYearPackage")
  end  
end

function WriteSignatureResult(page)
  local path=page:getActivity():getSignatureName()
  setElementValue(page,"signatureID","local://"..path)
  setElementValue(page,"signature_urlID",path)
  setElementHidden(page,"signatureID",false)
  setElementHidden(page,"btn_signatureID",true)
end

--确定
function onSure(page)
  local signatureURL=getElementValue(page,"signature_urlID")
  local bundle=page:initBundle()
    bundle:putString("signaturePath",signatureURL)
    bundle:putString("protocolDetail",protocolDetail)
    page:setCustomAnimations("right")
    page:goBack(bundle)
end

function goBack(page)
  page:setCustomAnimations("right")
  page:goBack()  
end

function onBackPressed(page)
  goBack(page)
end