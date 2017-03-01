require ("public")
local userInfoSP
local alreadyPopupView --已办业务取消弹框
local imagePopupView
local mobileNum --客户电话号码
local recommendButton
local inquiryButton
local month6Button --账单信息最后一个月按钮
local feeButton --账单信息-费用信息按钮
local firstLogin
local btnAlreadyID

function PageInit(page)
  userInfoSP=page:getSharedPreferences("NEWUSERINDO")
  alreadyPopupView=page:getElementById("already_popupID")
  imagePopupView=page:getElementById("image_popupID")
  recommendButton=page:getElementById("btn_recommendID")
  inquiryButton=page:getElementById("btn_inquiryID")
  month6Button=page:getElementById("month6ID")
  feeButton=page:getElementById("btn_feeID")
  btnAlreadyID=page:getElementById("btn_alreadyID")
  
  mobileNum=page:getActivity():decrypt(userInfoSP:getString("phoneNum",""))
  setElementValue(page,"customer_mobileID",mobileNum)
  
  local customerBrand=page:getActivity():decrypt(userInfoSP:getString("brand",""))
  
  
  if customerBrand == "" then
    firstLogin=true
    customerDetail(page)
  else
    firstLogin=false
    local customerRealName=page:getActivity():decrypt(userInfoSP:getString("realName",""))
    local customerMenu=page:getActivity():decrypt(userInfoSP:getString("menu",""))
    local customerStopOn=page:getActivity():decrypt(userInfoSP:getString("stopOn",""))
    local customerCardType=page:getActivity():decrypt(userInfoSP:getString("cardType",""))
    local customerBalance=page:getActivity():decrypt(userInfoSP:getString("balance",""))
    local customerUSIMCard=page:getActivity():decrypt(userInfoSP:getString("usimCard",""))
    local customerUserName=page:getActivity():decrypt(userInfoSP:getString("userName",""))
    local customerUserNum=page:getActivity():decrypt(userInfoSP:getString("userNum",""))
    local customerUserAddress=page:getActivity():decrypt(userInfoSP:getString("userAddress",""))    
    local customerVoiceLength=page:getActivity():decrypt(userInfoSP:getString("voiceLength",""))
    local customerVoiceSurplus=page:getActivity():decrypt(userInfoSP:getString("voiceSurplus",""))
    local customerFlowLength=page:getActivity():decrypt(userInfoSP:getString("flowLength",""))
    local customerFlowSurplus=page:getActivity():decrypt(userInfoSP:getString("flowSurplus",""))    
    local customerBroadband=page:getActivity():decrypt(userInfoSP:getString("broadband",""))    

    setElementValue(page,"customer_brandID",customerBrand)

    if customerUserName ~= "" then
      setElementValue(page,"user_nameID",string.sub(customerUserName,0,3).."＊＊")
    end
    
    if string.find(customerUSIMCard,"USIM") ~= nil then
      setElementValue(page,"isUSIMID","res://icon_circle_yes.png")
    elseif string.find(customerUSIMCard,"USIM卡") == "深圳通-nano卡" then
      setElementValue(page,"isUSIMID","res://icon_circle_yes.png")
      setElementValue(page,"usim_wordID","USIM卡(4G深圳通)")
    elseif string.find(customerUSIMCard,"USIM卡") == "手机通宝RF-SIM卡" then
      setElementValue(page,"isUSIMID","res://icon_circle_no.png")
      setElementValue(page,"usim_wordID","USIM卡(3G深圳通)")
    else
      setElementValue(page,"isUSIMID","res://icon_circle_no.png")
    end    
    
    if customerMenu == "" then
      setElementValue(page,"is4GID","res://icon_circle_no.png")
      setElementValue(page,"menuID","尚未办理4G套餐")
    else
      setElementValue(page,"is4GID","res://icon_circle_yes.png")
      setElementValue(page,"menuID",customerMenu)
    end
    
    if customerRealName == "000" or customerRealName == "1" then
      setElementValue(page,"isRealNameID","res://icon_circle_yes.png")
      if customerUserNum ~= "" then
        setElementValue(page,"card_numID",page:getActivity():getHideInfo(customerUserNum,5,11))    
        setElementHidden(page,"card_numID",false)
      else
        setElementHidden(page,"card_numID",true)
      end
      if customerUserAddress ~= "" then
        setElementValue(page,"card_adressID",page:getActivity():getHideInfo(customerUserAddress,6,12))    
        setElementHidden(page,"card_adressID",false)
      else
        setElementHidden(page,"card_adressID",true)
      end
      if customerRealName == "000" then
        setElementValue(page,"real_nameID","已身份确认")
        setElementHidden(page,"btn_realNameID",true)
      elseif customerRealName == "1" then
        setElementValue(page,"real_nameID","已实名登记")
--        setElementValue(page,"btn_realNameID","二次实名")
--        setElementHidden(page,"btn_realNameID",false)
        setElementValue(page,"btn_realNameID","实名登记")
        setElementHidden(page,"btn_realNameID",true)
      end
    else
      setElementValue(page,"btn_realNameID","实名登记")
      setElementHidden(page,"btn_realNameID",false)
      setElementValue(page,"isRealNameID","res://icon_circle_no.png")
      setElementValue(page,"real_nameID","未实名登记")
      setElementHidden(page,"card_numID",true)
      setElementHidden(page,"card_adressID",true)
    end
    
    if customerBroadband == "yes" then
      setElementValue(page,"isBroadbandID","res://icon_circle_yes.png")
      setElementValue(page,"broad_bandID","家宽客户")
    elseif customerBroadband == "no" then
      setElementValue(page,"isBroadbandID","res://icon_circle_no.png")
      setElementValue(page,"broad_bandID","非家宽客户")
    end    
    
    if customerStopOn == "0" then
      setElementValue(page,"stop_onID","已停机")
      setElementValue(page,"btn_stopOnID","申请开机")
    elseif customerStopOn == "1" then
      setElementValue(page,"stop_onID","正使用")
      setElementValue(page,"btn_stopOnID","申请停机")
    end
        
    setElementValue(page,"balanceID",customerBalance)
    
    if customerVoiceSurplus == "" then
      setElementValue(page,"minute_lengthID","0,0")
      setElementValue(page,"surplus_minuteID","0分钟") 
    else
      setElementValue(page,"minute_lengthID",customerVoiceLength)
      setElementValue(page,"surplus_minuteID",customerVoiceSurplus) 
    end
    
    if customerFlowSurplus == "" then
      setElementValue(page,"flow_lengthID","0,0")
      setElementValue(page,"surplus_flowID","0M")           
    else
      setElementValue(page,"flow_lengthID",customerFlowLength)
      setElementValue(page,"surplus_flowID",customerFlowSurplus)           
    end
    
   -- onLeftButton(page,recommendButton,"recommend")
    --已办优惠
   onLeftButton(page,btnAlreadyID,"already")
   --onAlready(page)
   --setElementHidden(page,"group_alreadyID",false) 
   
  end
end

function onPageResume(page)
  local customerRealName=page:getActivity():decrypt(userInfoSP:getString("realName",""))
  local customerStopOn=page:getActivity():decrypt(userInfoSP:getString("stopOn",""))
  local customerBalance=page:getActivity():decrypt(userInfoSP:getString("balance",""))
  local customerUserName=page:getActivity():decrypt(userInfoSP:getString("userName",""))
  local customerUserNum=page:getActivity():decrypt(userInfoSP:getString("userNum",""))
  local customerUserAddress=page:getActivity():decrypt(userInfoSP:getString("userAddress",""))

  if customerUserName ~= "" then
    setElementValue(page,"user_nameID",string.sub(customerUserName,0,3).."＊＊")
  end
     
  if customerRealName == "000" or customerRealName == "1" then
    setElementValue(page,"isRealNameID","res://icon_circle_yes.png")
    if customerUserNum ~= "" then
      setElementValue(page,"card_numID",page:getActivity():getHideInfo(customerUserNum,5,11))    
      setElementHidden(page,"card_numID",false)
    else
      setElementHidden(page,"card_numID",true)
    end
    if customerUserAddress ~= "" then
      setElementValue(page,"card_adressID",page:getActivity():getHideInfo(customerUserAddress,6,12))    
      setElementHidden(page,"card_adressID",false)    
    else
      setElementHidden(page,"card_adressID",true)    
    end
    if customerRealName == "000" then
      setElementValue(page,"real_nameID","已身份确认")
      setElementHidden(page,"btn_realNameID",true)
    elseif customerRealName == "1" then
      setElementValue(page,"real_nameID","已实名登记")
--      setElementValue(page,"btn_realNameID","二次实名")
--      setElementHidden(page,"btn_realNameID",false)
      setElementValue(page,"btn_realNameID","实名登记")
      setElementHidden(page,"btn_realNameID",true)      
    end
  else
    setElementValue(page,"btn_realNameID","实名登记")
    setElementHidden(page,"btn_realNameID",false)
    setElementValue(page,"isRealNameID","res://icon_circle_no.png")
    setElementValue(page,"real_nameID","未实名登记")
    setElementHidden(page,"card_numID",true)
    setElementHidden(page,"card_adressID",true)    
  end  

  if customerStopOn == "0" then
    setElementValue(page,"stop_onID","已停机")
    setElementValue(page,"btn_stopOnID","申请开机")
  elseif customerStopOn == "1" then
    setElementValue(page,"stop_onID","正使用")
    setElementValue(page,"btn_stopOnID","申请停机")
  end
  setElementValue(page,"balanceID",customerBalance)
  
  local signaturePath=page:getArguments():getString("signaturePath")
  if signaturePath ~= getElementValue(page,"signature_urlID") and signaturePath ~= nil then
    setElementValue(page,"signature_urlID",signaturePath)
    setElementValue(page,"signatureID","local://"..signaturePath)
  end  
end


-----^_^.^_^.^_^.^_^.^_^-------------小左边-------------^_^.^_^.^_^.^_^.^_^-----

--获取账户信息
function customerDetail(page)
  local Parameters={
    NetSelector="NetData:",                
    URL="/client/personalMsg.do",
    LuaNetCallBack="CustomerDetailNetCallBack(page,{response})",
    LuaExceptionCallBack="ExceptionCallBack(page)",
    RequestParams={phoneNum=mobileNum,channel=""},
    Method="POST",
    ResponseType="JSON",     
    Message="正在加载"
  }
  page:request(Parameters);
end

function CustomerDetailNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 then
    onMealInquiry(page)
    onLeftButton(page,recommendButton,"recommend")
        
    local data=response["response"]["data"]
    setElementValue(page,"customer_brandID",data["Brand"])
    
    if data["UserName"] ~= "" then
      setElementValue(page,"user_nameID",string.sub(data["UserName"],0,3).."＊＊")
    end
    
    --是否USIN卡
    if string.find(data["SimCardType"],"USIM") ~= nil then
      setElementValue(page,"isUSIMID","res://icon_circle_yes.png")
    elseif data["SimCardType"] == "深圳通-nano卡" then
      setElementValue(page,"isUSIMID","res://icon_circle_yes.png")
      setElementValue(page,"usim_wordID","USIM卡(4G深圳通)")
    elseif data["SimCardType"] == "手机通宝RF-SIM卡" then
      setElementValue(page,"isUSIMID","res://icon_circle_no.png")
      setElementValue(page,"usim_wordID","USIM卡(3G深圳通)")
    else
      setElementValue(page,"isUSIMID","res://icon_circle_no.png")
    end
    
    --是否4G套餐
    if data["PackageTypeName"] == "" then
      setElementValue(page,"is4GID","res://icon_circle_no.png")
      setElementValue(page,"menuID","尚未办理4G套餐")
    else
      setElementValue(page,"is4GID","res://icon_circle_yes.png")
      setElementValue(page,"menuID",data["PackageTypeName"])
    end
    
    --是否身份确认、实名登记
    if data["RealInfoCode"] == "000" or data["RealInfoCode"] == "1" then
      setElementValue(page,"isRealNameID","res://icon_circle_yes.png")
      if data["CertNo"] ~= "" then
        setElementValue(page,"card_numID",page:getActivity():getHideInfo(data["CertNo"],5,11))    
        setElementHidden(page,"card_numID",false)
      else
        setElementHidden(page,"card_numID",true)
      end
      if data["Address"] ~= "" then
        setElementValue(page,"card_adressID",page:getActivity():getHideInfo(data["Address"],6,12))    
        setElementHidden(page,"card_adressID",false)      
      else
        setElementHidden(page,"card_adressID",true)      
      end
      if data["RealInfoCode"] == "000" then
        setElementValue(page,"real_nameID","已身份确认")
        setElementHidden(page,"btn_realNameID",true)
      elseif data["RealInfoCode"] == "1" then
        setElementValue(page,"real_nameID","已实名登记")
--        setElementValue(page,"btn_realNameID","二次实名")
--        setElementHidden(page,"btn_realNameID",false)
        setElementValue(page,"btn_realNameID","实名登记")
        setElementHidden(page,"btn_realNameID",true)        
      end
    else
      setElementValue(page,"btn_realNameID","实名登记")
      setElementHidden(page,"btn_realNameID",false)
      setElementValue(page,"isRealNameID","res://icon_circle_no.png")
      setElementValue(page,"real_nameID","未实名登记")
      setElementHidden(page,"card_numID",true)
      setElementHidden(page,"card_adressID",true)      
    end
    
    if data["IsHandled"] == "yes" then
      setElementValue(page,"isBroadbandID","res://icon_circle_yes.png")
      setElementValue(page,"broad_bandID","家宽客户")
    elseif data["IsHandled"] == "no" then
      setElementValue(page,"isBroadbandID","res://icon_circle_no.png")
      setElementValue(page,"broad_bandID","非家宽客户")
    end
    
    --账户状态
    if data["Status"] == "0" then
      setElementValue(page,"stop_onID","已停机")
      setElementValue(page,"btn_stopOnID","申请开机")
    elseif data["Status"] == "1" then
      setElementValue(page,"stop_onID","正使用")
      setElementValue(page,"btn_stopOnID","申请停机")
    end
    
    --余额    
    setElementValue(page,"balanceID",data["UsableFee"])
    
   
  else
    page:showToast(response["resultMsg"])
  end
  --已办优惠
   onLeftButton(page,btnAlreadyID,"already")
end

function ExceptionCallBack(page)
  page:showToast("网络异常！")
  page:hideProgreesDialog()
end

local nextView
local mealNum
function onLeftButton(page,this,type)
  if nextView ~= nil and nextView ~= this then
    nextView:setSelected(false)
  end
  nextView=this
  this:setSelected(true)

  setElementHidden(page,"group_menuID",true)  
  setElementHidden(page,"group_recommendID",true)  
  setElementHidden(page,"group_alreadyID",true)  
  setElementHidden(page,"group_consultID",true)
  
  if type == "menu" then
    if firstLogin == false then
      onMealInquiry(page)
    end
    mealNum=0
    onMonthSelected(page,month6Button,"6")
    onMealUse(page,inquiryButton,"inquiry")
    setElementHidden(page,"group_menuID",false)  
  elseif type == "recommend" then
    local isFlowYear=page:getActivity():decrypt(userInfoSP:getString("isFlowYear",""))
    onBroadbandList(page)
--    onFlowYear(page)
    onRecommend(page)
    --setElementHidden(page,"group_recommendID",false)  
  elseif type == "already" then
    onAlready(page)
    setElementHidden(page,"group_alreadyID",false)  
  elseif type == "consult" then
    setElementHidden(page,"group_consultID",false)
  end
end

function on4GMeal(page)
local customerMobile=page:getActivity():decrypt(userInfoSP:getString("mobile",""))
local phoneNum=page:getActivity():decrypt(userInfoSP:getString("phoneNum",""))
  if customerMobile =="" or customerMobile ~= phoneNum then
  	local bundle=page:initBundle()
    bundle:putString("customService","customService")
    page:setCustomAnimations("right")
    page:goBack(bundle)
 
   else
   local bundle=page:initBundle()
    bundle:putString("4GMeal","4GMeal")
    page:setCustomAnimations("right")
    page:goBack(bundle)
end
end

function onNextService(page,this,url)
  page:setCustomAnimations("left")
  page:nextPage(url,false,nil)
end


-----^_^.^_^.^_^.^_^.^_^-------------主套餐-------------^_^.^_^.^_^.^_^.^_^-----

--主套餐
local nextMeal
function onMealUse(page,this,type)
  if nextMeal ~= nil and nextMeal ~= this then
    nextMeal:setSelected(false)
  end
  nextMeal=this
  this:setSelected(true)
  
  setElementHidden(page,"group_inquiryID",true)  
  setElementHidden(page,"group_billID",true)
  if type == "inquiry" then
    setElementHidden(page,"group_inquiryID",false)  
  elseif type == "bill" then
    setElementHidden(page,"group_billID",false)
  end
end

--套餐查询
function onMealInquiry(page)
  page:getActivity():showProgreesDialog("正在加载")
  local Parameters={
    NetSelector="NetData:",                
    URL="/client/packageInfoQuery.do",
    LuaNetCallBack="MealInquiryNetCallBack(page,{response})",
    LuaExceptionCallBack="MealInquiryExceptionCallBack(page)",
    RequestParams={phoneNum=mobileNum,channel="",resourceType="0"},
    Method="POST",
    ResponseType="JSON",    
--    Message="正在加载"
  }
  page:request(Parameters);
end

function MealInquiryNetCallBack(page,response)
  page:log("*************************套餐查询"..response["resultCode"])
  page:log(response["response"])
  page:getActivity():hideProgreesDialog()
  local data=response["response"]["data"]
  if response["resultCode"] == 0 then
    setElementHidden(page,"group_inquiryNotID",true)
    setElementHidden(page,"group_inquiryYesID",false)
      
    local customerMenu=page:getActivity():decrypt(userInfoSP:getString("menu",""))
    --语音
    if data["voicesurplusTotal"] == "" then
      setElementHidden(page,"group_voiceID",true)
      setElementValue(page,"minute_lengthID","0,0")
      setElementValue(page,"surplus_minuteID","0分钟")
      local edit=userInfoSP:edit()
        edit:putString("voiceSurplus",page:getActivity():encrypt(""))
        edit:putString("voiceLength",page:getActivity():encrypt(""))
        edit:commit()       
    else
      setElementValue(page,"minute_lengthID",data["voicesurplusTotal"]..","..(data["gnvoicetotal"]+data["snvoicetotal"]))
      setElementValue(page,"surplus_minuteID",data["voicesurplusTotal"].."分钟")
      local edit=userInfoSP:edit()
        edit:putString("voiceSurplus",page:getActivity():encrypt(data["voicesurplusTotal"].."分钟"))
        edit:putString("voiceLength",page:getActivity():encrypt(data["voicesurplusTotal"]..","..(data["gnvoicetotal"]+data["snvoicetotal"])))
        edit:commit()       
      
      setElementHidden(page,"group_voiceID",false)
      setElementValue(page,"voice_surplusID",data["voicesurplusTotal"].."分钟")
      setElementValue(page,"voice_nameID",customerMenu)
      if data["gnvoicetotal"] == "" or data["gnvoicetotal"] == "0" then
        setElementHidden(page,"voice_countryID",true)
      else
        setElementValue(page,"voice_surplus1ID",data["gnvoicesurplus"].."分钟")
        setElementValue(page,"voice_sum1ID",data["gnvoicetotal"].."分钟")
        setElementValue(page,"voice_lengtn1ID",data["gnvoicesurplus"]..","..data["gnvoicetotal"])
      end
      
      if data["snvoicetotal"] == "" or data["snvoicetotal"] == "0" then
        setElementHidden(page,"voice_provinceID",true)
      else
        setElementValue(page,"voice_surplus2ID",data["snvoicesurplus"].."分钟")
        setElementValue(page,"voice_sum2ID",data["snvoicetotal"].."分钟")
        setElementValue(page,"voice_lengtn2ID",data["snvoicesurplus"]..","..data["snvoicetotal"])
      end
    end
    
    --短信
    if data["msgtotal"] == "" then
      setElementHidden(page,"group_smsID",true)
    else
      setElementHidden(page,"group_smsID",false)
      setElementValue(page,"sms_surplusID",data["msgsurplus"].."条")
      setElementValue(page,"sms_nameID",customerMenu)
      setElementValue(page,"sms_surplus1ID",data["msgsurplus"].."条")
      setElementValue(page,"sms_sum1ID",data["msgtotal"].."条")
      setElementValue(page,"sms_lengtn1ID",data["msgsurplus"]..","..data["msgtotal"])
    end
    
    --流量
    if data["fluxEntier"] == "" then
      setElementValue(page,"flow_lengthID","0,0")
      setElementValue(page,"surplus_flowID","0M") 
      local edit=userInfoSP:edit()
        edit:putString("flowSurplus",page:getActivity():encrypt(""))
        edit:putString("flowLength",page:getActivity():encrypt(""))
        edit:commit()           
    else
      setElementValue(page,"flow_lengthID",data["fluxWhole"]..","..data["fluxEntier"])
      setElementValue(page,"surplus_flowID",math.modf(data["fluxWhole"]/1024).."M")
      local edit=userInfoSP:edit()
        edit:putString("flowSurplus",page:getActivity():encrypt(math.modf(data["fluxWhole"]/1024).."M"))
        edit:putString("flowLength",page:getActivity():encrypt(data["fluxWhole"]..","..data["fluxEntier"]))
        edit:commit()      
            
      setElementValue(page,"flow_surplusID",math.modf(data["fluxWhole"]/1024).."M")
      local listView=page:getElementById("flow_listID")
      listView:getEntity():getTemplateData():clear()
      local entityData={}
      local entityData1={}
      local entityData2={}
      local k=1
      for j=1,#data["fluxList"],1 do
        if data["fluxList"][j]["gnllTotal"] ~= "0" then
          entityData1[k]={subEntityValues={flow_name1ID="国内通用套餐",flow_surplus1ID=math.modf(data["fluxList"][j]["gnllSup"]/1024).."M",flow_sum1ID=math.modf(data["fluxList"][j]["gnllTotal"]/1024).."M",flow_lengtn1ID=data["fluxList"][j]["gnllSup"]..","..data["fluxList"][j]["gnllTotal"]}}
        end  
        if data["fluxList"][j]["snllTotal"] ~= "0" then
          entityData1[k+1]={subEntityValues={flow_name1ID="省内通用套餐",flow_surplus1ID=math.modf(data["fluxList"][j]["snllSup"]/1024).."M",flow_sum1ID=math.modf(data["fluxList"][j]["snllTotal"]/1024).."M",flow_lengtn1ID=data["fluxList"][j]["snllSup"]..","..data["fluxList"][j]["snllTotal"]}}
        end
        entityData2[j]={entityData1[k],entityData1[k+1]}
        k=k+2
      end
      
      for i=1,#data["fluxList"],1 do
        entityData[i]={subEntityValues={flow_nameID=data["fluxList"][i]["ProdName"],flow_list1ID=entityData2[i]}}
      end
      listView:upDateEntity(entityData)    
    end
  else
    setElementHidden(page,"group_inquiryNotID",false)
    setElementHidden(page,"group_inquiryYesID",true)  
    setElementValue(page,"minute_lengthID","0,0")
    setElementValue(page,"surplus_minuteID","0分钟") 
    setElementValue(page,"flow_lengthID","0,0")
    setElementValue(page,"surplus_flowID","0M")           
    page:showToast(response["resultMsg"])
  end
end

function MealInquiryExceptionCallBack(page)
  page:showToast("网络异常！")
  page:getActivity():hideProgreesDialog()
--  page:hideProgreesDialog()
end

--账单信息
local checkDate --查询日期YYYYMM
local checkNumber --查询的月份位于第几个，1-6
local nextMonth
local requestParams
function onMonthSelected(page,this,id)
  if nextMonth ~= nil and nextMonth ~= this then
    nextMonth:setSelected(false)
  end
  nextMonth=this
  this:setSelected(true)
  
  local monthTotal=page:getElementById("month"..id.."SumID"):getValue()
  if mealNum > 0 then
    if tonumber(monthTotal) == 0 then
      setElementHidden(page,"group_notBillDetailID",false)
      setElementHidden(page,"group_billDetailID",true)
    return
    end
  end
  setElementHidden(page,"group_notBillDetailID",true)
  setElementHidden(page,"group_billDetailID",false)

  local brand=getElementValue(page,"customer_brandID")
  local selectedMonth=string.sub(this:getValue(),0,string.len(this:getValue())-3)
  local lastMonth=string.sub(month6Button:getValue(),0,string.len(month6Button:getValue())-3)
  if this:getValue() == "" then
    checkDate=""
    requestParams={phoneNum=mobileNum,channel="",brandType=brand,month=checkDate}
  else
    if tonumber(selectedMonth) > tonumber(lastMonth) and tonumber(selectedMonth) > 9 then
      checkDate=(os.date("%Y")-1)..selectedMonth
    elseif tonumber(selectedMonth) > tonumber(lastMonth) and tonumber(selectedMonth) < 10 then
      checkDate=(os.date("%Y")-1).."0"..selectedMonth
    elseif tonumber(selectedMonth) < tonumber(lastMonth) and tonumber(selectedMonth) > 9 then
      checkDate=os.date("%Y")..selectedMonth
    elseif tonumber(selectedMonth) < tonumber(lastMonth) and tonumber(selectedMonth) < 10 then
      checkDate=os.date("%Y").."0"..selectedMonth
    elseif tonumber(selectedMonth) == tonumber(lastMonth) and tonumber(selectedMonth) > 9 then
      checkDate=os.date("%Y")..selectedMonth
    elseif tonumber(selectedMonth) == tonumber(lastMonth) and tonumber(selectedMonth) < 10 then
      checkDate=os.date("%Y").."0"..selectedMonth
    end
    requestParams={phoneNum=mobileNum,channel="",brandType=brand,month=""..tonumber(checkDate)}
  end
  
  checkNumber=id
  local Parameters={
    NetSelector="NetData:",                
    URL="/client/getBillingCenterRealtimeInfo.do",
    LuaNetCallBack="MealBillNetCallBack(page,{response})",
    LuaExceptionCallBack="MealBillExceptionCallBack(page)",
    RequestParams=requestParams,
    Method="POST",
    ResponseType="JSON",    
    Message="正在加载"
  }
  page:request(Parameters);  
end

local BillData
function MealBillNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 then
    local data=response["response"]["data"][1]
    BillData=data
    
    if mealNum == 0 then
      setElementValue(page,"month1SumID",data["month1Total"])
      setElementValue(page,"month1ID",data["month1"])
      setElementValue(page,"month2SumID",data["month2Total"])
      setElementValue(page,"month2ID",data["month2"])
      setElementValue(page,"month3SumID",data["month3Total"])
      setElementValue(page,"month3ID",data["month3"])
      setElementValue(page,"month4SumID",data["month4Total"])
      setElementValue(page,"month4ID",data["month4"])
      setElementValue(page,"month5SumID",data["month5Total"])
      setElementValue(page,"month5ID",data["month5"])
      setElementValue(page,"month6SumID",data["month6Total"])
      setElementValue(page,"month6ID",data["month6"])
      mealNum=mealNum+1
    end
    
    local menuPercentage=string.sub(data["menuPercentage"],0,string.len(data["menuPercentage"])-1)
    local voicePercentage=string.sub(data["voicePercentage"],0,string.len(data["voicePercentage"])-1)
    local netPercentage=string.sub(data["netPercentage"],0,string.len(data["netPercentage"])-1)
    local otherPercentage=string.sub(data["otherPercentage"],0,string.len(data["otherPercentage"])-1)
    local pieChartView=page:getElementById("pie_chartID")
    pieChartView:setPer(menuPercentage,voicePercentage,netPercentage,otherPercentage)    
    
    local selectedMonth=string.sub(data["month"..checkNumber],0,string.len(data["month"..checkNumber])-3)
    local selectedMonthDate=page:getActivity():getDay(tonumber(os.date("%Y")),tonumber(selectedMonth))
    setElementValue(page,"charging_cycleID","计费周期："..selectedMonth.."月1日-"..selectedMonth.."月"..selectedMonthDate.."日")
    
    setElementValue(page,"fee_sumID",data["TotalBalance"])
    setElementValue(page,"menu_percentageID",data["menuPercentage"])
    setElementValue(page,"voice_percentageID",data["voicePercentage"])
    setElementValue(page,"net_percentageID",data["netPercentage"])
    setElementValue(page,"other_percentageID",data["otherPercentage"])
    
    setElementValue(page,"start_balanceID",data["startBalance"])
    setElementValue(page,"month_incomeID",data["monthIncome"])
    setElementValue(page,"end_balanceID",data["endBalance"])
    
    onFeeList(page)
    onAccountList(page)
    onPointList(page)
    onSelectedCheck(page,feeButton,"fee")
  else
    page:showToast(response["resultMsg"])
  end
end

function MealBillExceptionCallBack(page)
  page:showToast("网络异常！")
  page:hideProgreesDialog()
end

function onFeeList(page)
  local feeListView=page:getElementById("fee_listID")
  feeListView:getEntity():getTemplateData():clear()
  local entityData={}
  local entityData2={}
  local entityData3={}
  local entityData4={}
  local entityData5={}
  for i=1,#BillData["feeList"],1 do
    local secondFeeData=BillData["feeList"][i]["secondFeeList"]
    entityData3[i]={}
    if secondFeeData ~= "" then
      for j=1,#secondFeeData,1 do
        local thirdFeeData=secondFeeData[j]["thirdFeeList"]
        entityData5[j]={}
        if thirdFeeData ~= "" then
          for k=1,#secondFeeData[j]["thirdFeeList"],1 do
            entityData4[k]={subEntityValues={thirdFee_nameID=thirdFeeData[k]["thirdFeeName"],thirdFee_priceID=thirdFeeData[k]["thirdFeePrice"]}}
            table.insert(entityData5[j],entityData4[k])
          end
        end
        entityData2[j]={subEntityValues={secondFee_nameID=secondFeeData[j]["secondFeeName"],secondFee_priceID=secondFeeData[j]["secondFeePrice"],thirdFee_listID=entityData5[j]}}
        table.insert(entityData3[i],entityData2[j])
      end
    end
    entityData[i]={subEntityValues={fee_nameID=BillData["feeList"][i]["feeName"],fee_priceID=BillData["feeList"][i]["feePrice"],secondFee_listID=entityData3[i]}}
  end
  feeListView:upDateEntity(entityData)
end

function onAccountList(page)
  local accountListView=page:getElementById("account_listID")
  accountListView:getEntity():getTemplateData():clear()
  local entityData={}
  local entityData2={}
  local entityData3={}
  local entityData4={}
  local entityData5={}    
  for i=1,#BillData["accountList"],1 do
    local secondAccountData=BillData["accountList"][i]["secondAccountList"]
    entityData3[i]={}
    if secondAccountData ~= "" then
      for j=1,#secondAccountData,1 do
        local thirdAccountData=secondAccountData[j]["thirdAccountList"]
        entityData5[j]={}
        if thirdAccountData ~= "" then
          for k=1,#secondAccountData[j]["thirdAccountList"],1 do
            entityData4[k]={subEntityValues={thirdAccount_nameID=thirdAccountData[k]["thirdAccountName"],thirdAccount_priceID=thirdAccountData[k]["thirdAccountPrice"]}}
            table.insert(entityData5[j],entityData4[k])
          end
        end
        entityData2[j]={subEntityValues={secondAccount_nameID=secondAccountData[j]["secondAccountName"],secondAccount_priceID=secondAccountData[j]["secondAccountPrice"],thirdAccount_listID=entityData5[j]}}
        table.insert(entityData3[i],entityData2[j])
      end
    end
    entityData[i]={subEntityValues={account_nameID=BillData["accountList"][i]["accountName"],account_priceID=BillData["accountList"][i]["accountPrice"],secondAccount_listID=entityData3[i]}}
  end
  accountListView:upDateEntity(entityData)
end

function onPointList(page)
  setElementValue(page,"point_explainID",BillData["pointDescID"])
  local pointListView=page:getElementById("point_listID")
  pointListView:getEntity():getTemplateData():clear()
  local entityData={}
  for i=1,#BillData["pointList"],1 do
    entityData[i]={subEntityValues={point_nameID=BillData["pointList"][i]["pointName"],point_priceID=BillData["pointList"][i]["pointPrice"]}}
  end
  pointListView:upDateEntity(entityData)
end

--费用信息or账户信息or积分信息
local nextCheck
function onSelectedCheck(page,this,type)
  if nextCheck ~= nil and nextCheck ~= this then
    nextCheck:setSelected(false)
  end
  nextCheck=this
  this:setSelected(true)

  setElementHidden(page,"group_feeID",true)
  setElementHidden(page,"group_accountID",true)
  setElementHidden(page,"group_pointID",true)
  if type == "fee" then
    setElementHidden(page,"group_feeID",false)
  elseif type == "account" then
    setElementHidden(page,"group_accountID",false)
  elseif type == "point" then
    setElementHidden(page,"group_pointID",false)
  end
end

--近6个月消费趋势
function onTrend(page)
  local bundle=page:initBundle()
    bundle:putString("month1",getElementValue(page,"month1ID"))
    bundle:putString("month2",getElementValue(page,"month2ID"))
    bundle:putString("month3",getElementValue(page,"month3ID"))
    bundle:putString("month4",getElementValue(page,"month4ID"))
    bundle:putString("month5",getElementValue(page,"month5ID"))
    bundle:putString("month6",getElementValue(page,"month6ID"))
    bundle:putString("month1Sum",getElementValue(page,"month1SumID"))
    bundle:putString("month2Sum",getElementValue(page,"month2SumID"))
    bundle:putString("month3Sum",getElementValue(page,"month3SumID"))
    bundle:putString("month4Sum",getElementValue(page,"month4SumID"))
    bundle:putString("month5Sum",getElementValue(page,"month5SumID"))
    bundle:putString("month6Sum",getElementValue(page,"month6SumID"))
    bundle:putString("monthNum",checkNumber)
    page:setCustomAnimations("left")
    page:nextPage("res://consumptionTrendPage.xml",false,bundle)
end


-----^_^.^_^.^_^.^_^.^_^-------------推荐优惠及业务-------------^_^.^_^.^_^.^_^.^_^-----

--推荐-家庭宽带
function onBroadbandList(page)
  page:getActivity():showProgreesDialog("正在加载")
  local Parameters={
    NetSelector="NetData:",                
    URL="/client/broadbandMsg.do",
    LuaNetCallBack="BroadbandListNetCallBack(page,{response})",
    LuaExceptionCallBack="BroadbandListExceptionCallBack(page)",
    RequestParams={},
    Method="POST",
    ResponseType="JSON",    
--    Message="正在加载"
  }
  page:request(Parameters);
end

local broadbandListNum
function BroadbandListNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  page:getActivity():hideProgreesDialog()
  if response["resultCode"] == 0 then
    local data=response["response"]["data"]
    broadbandListNum=#data
    onIsRecommend(page)
    if #data == 0 then
      setElementHidden(page,"group_broadbandListID",true)
    else
      setElementHidden(page,"group_broadbandListID",false)
      local listView=page:getElementById("broadband_listID")
      listView:getEntity():getTemplateData():clear()
      local entityData={}
      for i=1,3,1 do
        entityData[i]={subEntityValues={broadband_oldID=data[i]["PackageMode"],broadband_ID=data[i]["CospCode"],broadband_speedID=data[i]["Speedtype"],broadband_freeID=data[i]["NeedPay"],broadband_moneyID=data[i]["Amount"],broadband_nameID=data[i]["Instruction"]}}
      end
      listView:upDateEntity(entityData)
    end    
  else  
    page:showToast(response["resultMsg"])
  end
end

function BroadbandListExceptionCallBack(page)
  page:showToast("网络异常！")
  page:getActivity():hideProgreesDialog()
end

--覆盖查询
function onCoverQuery(page)
  local bundle=page:initBundle()
    bundle:putString("entrance","service")  
    page:setCustomAnimations("left")
    page:nextPage("res://coverQueryPage.xml",false,bundle)
end

--家宽立即办理
function onBroadbandNowDeal(page,this)
  local broadbandOldID=this:getRootView():getElementById("broadband_oldID"):getValue()
  local broadbandID=this:getRootView():getElementById("broadband_ID"):getValue()
  local broadbandSpeed=this:getRootView():getElementById("broadband_speedID"):getValue()
  local broadbandFree=this:getRootView():getElementById("broadband_freeID"):getValue()
  local broadbandMoney=this:getRootView():getElementById("broadband_moneyID"):getValue()
  local broadbandName=this:getRootView():getElementById("broadband_nameID"):getValue()
  local bundle=page:initBundle()
    bundle:putString("dealEntrance","service")  
    bundle:putString("broadbandOldID",broadbandOldID)  
    bundle:putString("broadbandID",broadbandID)  
    bundle:putString("broadbandSpeed",broadbandSpeed)  
    bundle:putString("broadbandFree",broadbandFree)  
    bundle:putString("broadbandMoney",broadbandMoney)  
    bundle:putString("broadbandName",broadbandName)  
    page:setCustomAnimations("left")
    page:nextPage("res://broadbandPage.xml",false,bundle)
end

--推荐-流量年包
function onFlowYear(page)
  page:getActivity():showProgreesDialog("正在加载")
  local Parameters={
    NetSelector="NetData:",                
    URL="/fluxPackage/fluxTargetNum.do",
    LuaNetCallBack="onFlowYearNetCallBack(page,{response})",
    LuaExceptionCallBack="onFlowYearExceptionCallBack(page)",
    RequestParams={phoneNum=mobileNum},
    Method="POST",
    ResponseType="JSON",    
--    Message="正在加载"
  }
  page:request(Parameters);
end

local flowYearListNum
function onFlowYearNetCallBack(page,response)
  page:log("*************************流量年包"..response["resultCode"])
  page:log(response["response"])
  page:getActivity():hideProgreesDialog()
  if response["resultCode"] == 0 or response["resultCode"] == 222 then
    local data=response["response"]["data"]
    flowYearListNum=#data
    onIsRecommend(page)
    if #data == 0 then
      setElementHidden(page,"group_flowYearListID",true)
    else
      setElementHidden(page,"group_flowYearListID",false)
    end
    
    if response["resultCode"] == 0 then
      local listView=page:getElementById("flowYear_listID")
      listView:getEntity():getTemplateData():clear()
      local entityData={}
      for i=1,#data,1 do
        entityData[i]={subEntityValues={flowYear_ID=data[i]["ProdCode"],flowYear_priceID=data[i]["Price"],flowYear_nameID=data[i]["Instruction"]}}
      end
      listView:upDateEntity(entityData)
      
      local edit=userInfoSP:edit()
        edit:putString("isFlowYear",page:getActivity():encrypt("true"))
        edit:commit()      
    elseif response["resultCode"] == 222 then
      local edit=userInfoSP:edit()
        edit:putString("isFlowYear",page:getActivity():encrypt("false"))
        edit:commit()      
    end
  else  
    page:showToast(response["resultMsg"])
  end
end

function onFlowYearExceptionCallBack(page)
  page:showToast("网络异常！")
  page:getActivity():hideProgreesDialog()
end

local flowYearID
function onFlowYearNowDeal(page,this)
  flowYearID=this:getRootView():getElementById("flowYear_ID"):getValue()
  local Parameters={
    NetSelector="NetData:",                
    URL="/fluxPackage/orderCheck.do",
    LuaNetCallBack="onFlowYearNowDealNetCallBack(page,{response})",
    LuaExceptionCallBack="onFlowYearNowDealExceptionCallBack(page)",
    RequestParams={phoneNum=mobileNum,channel="",custID="",custType="1",opType="0",prodId=flowYearID,effType="0",bookingFlag="0",bookingTime=""},
    Method="POST",
    ResponseType="JSON",    
    Message="正在校验"
  }
  page:request(Parameters);
end

function onFlowYearNowDealNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 then
    local data=response["response"]["data"]
    local bundle=page:initBundle()
      bundle:putString("entrance","service")  
      bundle:putString("planID",flowYearID)  
      bundle:putString("orderNum",data["OrderSeq"])  
      bundle:putString("discount",data["DeratePrice"]/100)  
      bundle:putString("actual",data["Fee"])  
      page:setCustomAnimations("left")
      page:nextPage("res://flowYearPage.xml",false,bundle)    
  else  
    page:showToast(response["message"])
  end
end

function onFlowYearNowDealExceptionCallBack(page)
  page:showToast("网络异常！")
  page:hideProgreesDialog()
end

--推荐-其他业务
function onRecommend(page)
  page:getActivity():showProgreesDialog("正在加载")
  local Parameters={
    NetSelector="NetData:",                
    URL="/client/jzyx.do",
    LuaNetCallBack="RecommendNetCallBack(page,{response})",
    LuaExceptionCallBack="RecommendExceptionCallBack(page)",
    RequestParams={phoneNum=mobileNum,channel=""},
    Method="POST",
    ResponseType="JSON",    
--    Message="正在加载"
  }
  page:request(Parameters);
end

local recommendListNum
function RecommendNetCallBack(page,response)
  page:log("*************************"..response["code"])
  page:log(response["response"])
  page:getActivity():hideProgreesDialog()
  if response["code"] == 200 then
    local data=response["response"]["data"]
    recommendListNum=#data
    onIsRecommend(page)
    if #data == 0 then
      setElementHidden(page,"group_recommendListID",true)
    else
      setElementHidden(page,"group_recommendListID",false)    
      local listView=page:getElementById("recommend_listID")
      listView:getEntity():getTemplateData():clear()
      local entityData={}
      for i=1,#data,1 do
        if data[i]["couldHandle"] == "0" then
          entityData[i]={subEntityValues={can_dealID=data[i]["couldHandle"],recommend_ID="",recommend_typeID=data[i]["businessType"],recommend_nameID=data[i]["act_name"]},attributes={selected="false"}}
        elseif data[i]["couldHandle"] == "1" then
          entityData[i]={subEntityValues={can_dealID=data[i]["couldHandle"],recommend_ID=data[i]["commodities"][1]["commodity_code"],recommend_typeID=data[i]["businessType"],recommend_nameID=data[i]["act_name"]},attributes={selected="true"}}
        end
      end
      listView:upDateEntity(entityData)    
    end
  else  
    page:showToast(response["message"])
  end
end

function RecommendExceptionCallBack(page)
  page:showToast("网络异常！")
  page:getActivity():hideProgreesDialog()
--  page:hideProgreesDialog()
end

function onIsRecommend(page)
--  if broadbandListNum == 0 and recommendListNum == 0 and flowYearListNum == 0 then
  if broadbandListNum == 0 and recommendListNum == 0 then
    setElementHidden(page,"group_notRecommendID",false)
    setElementHidden(page,"group_haveRecommendID",true)
  else
    setElementHidden(page,"group_notRecommendID",true)
    setElementHidden(page,"group_haveRecommendID",false)
  end
end

--立即办理
local recommendID
local recommendType
local recommendName
function onNowDeal(page,this)
  local canDeal=this:getParent():getElementById("can_dealID"):getValue()
  if canDeal == "1" then
    recommendID=this:getParent():getElementById("recommend_ID"):getValue()
    recommendType=this:getParent():getElementById("recommend_typeID"):getValue()
    recommendName=this:getParent():getElementById("recommend_nameID"):getValue()
    setElementValue(page,"selected_recommendID",recommendName)
    
    setElementHidden(page,"group_applyID",true)
    setElementHidden(page,"group_apply2ID",false)
    onStartImageResult(page)
  end
end

function onStartImageResult(page)
  local listView=page:getElementById("picture_listID")
  listView:getEntity():getTemplateData():clear()
  local entityData={}
  for i=1,1,1 do
    entityData[i]={subEntityValues={picture_typeID="photograph",pictureID="res://icon_picture_add.png"}}
  end
  listView:upDateEntity(entityData)
end

--返回上一步
function onLastBack(page)
  setElementHidden(page,"group_applyID",false)
  setElementHidden(page,"group_apply2ID",true)
end

--业务单详情
function onProtocol(page)
  local signatureURL=getElementValue(page,"signature_urlID")
  local bundle=page:initBundle()
    bundle:putString("protocol","")
    bundle:putString("signatureURL",signatureURL)
    page:setCustomAnimations("left")
    page:nextPage("res://protocolPage.xml",false,bundle) 
end

--拍照
function onPicture(page,this)
  local isPhotograph=this:getElementById("picture_typeID"):getValue()
  if isPhotograph == "photograph" then
    page:getActivity():photograph(mobileNum,"Broadband")
  elseif isPhotograph == "looking" then
    onBigImage(page)
  end
end

local picturePath=""
function ImageResult(page)
  local path=page:getActivity():getPhotoName()
  picturePath=picturePath..path..","
  local url=split(picturePath,",")
  local pictureSize=table.getn(url)
  local listView=page:getElementById("picture_listID")
  listView:getEntity():getTemplateData():clear()
  local entityData={}
  for i=1,pictureSize,1 do
    if i < pictureSize then
      entityData[i]={subEntityValues={picture_typeID="looking",pictureID="local://"..url[i]}}
    elseif i == pictureSize and pictureSize < 6 then
      entityData[i]={subEntityValues={picture_typeID="photograph",pictureID="res://icon_picture_add.png"}}
    end
  end
  listView:upDateEntity(entityData)
end

--查看大图
function onBigImage(page)
  local path=split(picturePath,",")
  local pathSize=table.getn(path)
  local listView=page:getElementById("look_imageID")
  listView:getEntity():getTemplateData():clear()
  local entityData={}
  for i=1,pathSize-1,1 do
    entityData[i]={subEntityValues={big_imageID="local://"..path[i]}}
  end
  listView:upDateEntity(entityData)
  imagePopupView:showInFragment("center",0,0)
end

function onPictureDismiss(page,this)
  imagePopupView:dismiss()
end

--提交
function onPictureSubmit(page)
  local signaturePath=page:getArguments():getString("signaturePath")
  if signaturePath == "" or signaturePath == nil then
    page:showToast("请先签名")
    return
  end
  
  local path=split(picturePath,",")
  local size=table.getn(path)
  local postFilePaths=getImagePostFilePaths(page,signaturePath,path,size)  
  local remark=getElementValue(page,"remarkID")
  local Parameters={
    NetSelector="NetData:",                
    URL="/client/imgsUpload.do",
    LuaNetCallBack="PictureSubmitNetCallBack(page,{response})",
    LuaExceptionCallBack="PictureSubmitExceptionCallBack(page)",
    RequestParams={phoneNum=mobileNum,businessType="推荐优惠及业务",remark=remark},
    PostFilePaths=postFilePaths,
    Method="FORM",
    ResponseType="JSON",    
    Message="正在提交"
  }
  page:request(Parameters);
end

function PictureSubmitNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 then
    onBusinessHandle(page)
  else
    page:showToast("图片上传失败")
  end
end

function PictureSubmitExceptionCallBack(page)
  page:showToast("网络异常！")
  page:hideProgreesDialog()
end

function onBusinessHandle(page,this)
  page:getActivity():showProgreesDialog("正在办理")
  local Parameters={
    NetSelector="NetData:",                
    URL="/client/handleOrder.do",
    LuaNetCallBack="BusinessHandleNetCallBack(page,{response})",
    LuaExceptionCallBack="BusinessHandleExceptionCallBack(page)",
    RequestParams={orderType="推荐",phoneNum=mobileNum,channel="",IP="",commodityCode=recommendID,totalPrice="",payMode="",commodityName=recommendName,isDistribution="",protocolType="",recommendNumber="",memo="",operatorid="",consigneeName="",consigneeMobileNo="",consigneeTel="",address="",postalCode="",deliveryTime="",certType="",certNum="",custName="",certAddr="",linkPhone="",custAddress="",postCode=""},
    Method="POST",
    ResponseType="JSON",    
    Message="正在办理"
  }
  page:request(Parameters);
end

function BusinessHandleNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  page:getActivity():hideProgreesDialog()
  if response["resultCode"] == 0 then
    setElementHidden(page,"group_apply2ID",true)
    setElementHidden(page,"group_passID",false)    
  else
    setElementValue(page,"fail_wordID",response["resultMsg"])
    setElementHidden(page,"group_apply2ID",true)
    setElementHidden(page,"group_failID",false)    
  end
end

function BusinessHandleExceptionCallBack(page)
  page:showToast("网络异常！")
  page:getActivity():hideProgreesDialog()
--  page:hideProgreesDialog()
end

--返回推荐优惠及业务
function recommendBack(page)
  setElementHidden(page,"group_applyID",false)
  setElementHidden(page,"group_passID",true)
end

--重新进行
function onAgain(page)
  setElementHidden(page,"group_apply2ID",false)
  setElementHidden(page,"group_failID",true)
end


-----^_^.^_^.^_^.^_^.^_^-------------已办优惠及业务-------------^_^.^_^.^_^.^_^.^_^-----

function onAlready(page)
  local Parameters={
    NetSelector="NetData:",                
    URL="/client/qrySubsProds.do",
    LuaNetCallBack="AlreadyNetCallBack(page,{response})",
    LuaExceptionCallBack="AlreadyExceptionCallBack(page)",
    RequestParams={phoneNum=mobileNum,channel="",IP="",prodCode="",queryType="0"},
    Method="POST",
    ResponseType="JSON",    
    Message="正在加载"
  }
  page:request(Parameters);
end

local alreadyData
function AlreadyNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 then
    local data=response["response"]["data"]
    alreadyData=data
    local moneySum=0
    local listView=page:getElementById("already_listID")
    listView:getEntity():getTemplateData():clear()
    local entityData={}
    for i=1,#data,1 do
      moneySum=moneySum+(data[i]["SalePrice"]/100)
      entityData[i]={subEntityValues={already_nameID=data[i]["ProdName"],already_timeID=string.sub(data[i]["BeginTime"],0,4).."-"..string.sub(data[i]["BeginTime"],5,6).."-"..string.sub(data[i]["BeginTime"],7,8).."至"..string.sub(data[i]["EndTime"],0,4).."-"..string.sub(data[i]["EndTime"],5,6).."-"..string.sub(data[i]["EndTime"],7,8),already_moneyID=(data[i]["SalePrice"]/100).."元",already_ID=data[i]["ProdID"]}}
    end
    listView:upDateEntity(entityData)
     
    setElementValue(page,"already_numberID",#data.."个")
    setElementValue(page,"already_priceID",moneySum..".00元")
  else  
    page:showToast(response["resultMsg"])
  end
end

function AlreadyExceptionCallBack(page)
  page:showToast("网络异常！")
  page:hideProgreesDialog()
end

--取消业务
local alreadyNum
function onBusinessCancle(page,this)
  alreadyNum=this:getRootView():getElementById("already_ID"):getValue()
  alreadyPopupView:showInFragment("center",0,0)
end

function onAlreadyCancle(page)
  alreadyPopupView:dismiss()
end

function onAlreadySure(page)
  alreadyPopupView:dismiss()
  local customerBrand=page:getActivity():decrypt(userInfoSP:getString("brand",""))
  local Parameters={
    NetSelector="NetData:",                
    URL="/client/cancelOrHandleBusiness.do",
    LuaNetCallBack="AlreadySureNetCallBack(page,{response})",
    LuaExceptionCallBack="AlreadySureExceptionCallBack(page)",
    RequestParams={phoneNum=mobileNum,channel="",IP="",serviceCode=alreadyNum,operateType="0",effectiveType="",pType=customerBrand},
    Method="POST",
    ResponseType="JSON",    
    Message="正在取消"
  }
  page:request(Parameters);
end

function AlreadySureNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 then
    page:showToast("业务取消成功")
    local moneySum=0
    local listView=page:getElementById("already_listID")
    listView:getEntity():getTemplateData():clear()
    local entityData={}
    for i=1,#alreadyData,1 do
      if alreadyNum ~= alreadyData[i]["ProdID"] then
        moneySum=moneySum+(alreadyData[i]["SalePrice"]/100)
        entityData[i]={subEntityValues={already_nameID=alreadyData[i]["ProdName"],already_timeID=string.sub(alreadyData[i]["BeginTime"],0,4).."-"..string.sub(alreadyData[i]["BeginTime"],5,6).."-"..string.sub(alreadyData[i]["BeginTime"],7,8).."至"..string.sub(alreadyData[i]["EndTime"],0,4).."-"..string.sub(alreadyData[i]["EndTime"],5,6).."-"..string.sub(alreadyData[i]["EndTime"],7,8),already_moneyID=(alreadyData[i]["SalePrice"]/100).."元",already_ID=alreadyData[i]["ProdID"]}}
      end      
    end
    listView:upDateEntity(entityData)
     
    setElementValue(page,"already_numberID",(#alreadyData-1).."个")
    setElementValue(page,"already_priceID",moneySum..".00元")    
  else  
    page:showToast(response["resultMsg"])
  end
end

function AlreadySureExceptionCallBack(page)
  page:showToast("网络异常！")
  page:hideProgreesDialog()
end


-----^_^.^_^.^_^.^_^.^_^-------------咨询信息记录-------------^_^.^_^.^_^.^_^.^_^-----

local consultItem=""
function onNewConsult(page,this,type)
  local consult=this:isSelected()
  if consult then
    this:setSelected(false)
    consultItem = string.gsub(consultItem,type..",","")
  else
    this:setSelected(true)
    consultItem=consultItem..type..","
  end
end

--提交
function onConsultSubmit(page)
  local customerUserName=page:getActivity():decrypt(userInfoSP:getString("userName",""))
  local supplementValue=getElementValue(page,"consult_supplementID")
  local Parameters={
    NetSelector="NetData:",                
    URL="/client/consulting.do",
    LuaNetCallBack="ConsultSubmitNetCallBack(page,{response})",
    LuaExceptionCallBack="ConsultSubmitExceptionCallBack(page)",
    RequestParams={phoneNum=mobileNum,realName=customerUserName,message=consultItem,supplement=supplementValue},
    Method="POST",
    ResponseType="JSON",    
    Message="正在提交"
  }
  page:request(Parameters);
end

function ConsultSubmitNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 then
    page:showToast("提交成功")
    page:getElementById("consult_supplementID"):clearText()
  else  
    page:showToast(response["resultMsg"])
  end
end

function ConsultSubmitExceptionCallBack(page)
  page:showToast("网络异常！")
  page:hideProgreesDialog()
end

function goBack(page)
  local bundle=page:initBundle()
  bundle:putString("leftButton","other")
  page:setCustomAnimations("right")
  page:goBack(bundle)  
end

function onBackPressed(page)
  goBack(page)
end