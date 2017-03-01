require ("public")
local userInfoSP
local mobileNum --客户电话号码
local posButton
local popupListView
local errorPopupView --资格校验错误弹框
local imagePopupView
local reasonPopupView
local picturePath=""
local prestoreData
local isRecom
local commodityId

function PageInit(page)
  userInfoSP=page:getSharedPreferences("NEWUSERINDO")
  popupListView=page:getElementById("pop_listView")
  errorPopupView=page:getElementById("pop_errorID")
  imagePopupView=page:getElementById("image_popupID")
  reasonPopupView=page:getElementById("reason_popupID")
  posButton=page:getElementById("btn_posID")
  isRecom=page:getArguments():getString("isRecommend")
  commodityId=page:getArguments():getString("commodityId")
  customerDetail(page)
  onPrestore(page)
end


-----^_^.^_^.^_^.^_^.^_^-------------小左边-------------^_^.^_^.^_^.^_^.^_^-----

--获取账户信息
function customerDetail(page)
  mobileNum=page:getActivity():decrypt(userInfoSP:getString("mobile",""))
  local customerBrand=page:getActivity():decrypt(userInfoSP:getString("brand",""))
  local customerRealName=page:getActivity():decrypt(userInfoSP:getString("realName",""))
  local customerStopOn=page:getActivity():decrypt(userInfoSP:getString("stopOn",""))
  local customerMenu=page:getActivity():decrypt(userInfoSP:getString("menu",""))
  
  setElementValue(page,"customer_mobileID",mobileNum)
  setElementValue(page,"customer_brandID",customerBrand)
  
  if customerRealName == "000" or customerRealName == "1" then
    setElementValue(page,"customer_realNameID","已实名登记")
  else
    setElementValue(page,"customer_realNameID","未实名登记")
  end
  
  if customerStopOn == "0" then
    setElementValue(page,"customer_stopOnID","已停机")
  elseif customerStopOn == "1" then
    setElementValue(page,"customer_stopOnID","正使用")
  end
  resetTimeUi(page)    
  setElementValue(page,"customer_menuID",customerMenu)
end

--获取策略列表
function onPrestore(page)
  local Parameters={
    NetSelector="NetData:",                
    URL="/storedFee/openStoredFee2.do",
    LuaNetCallBack="PrestoreNetCallBack(page,{response})",
    LuaExceptionCallBack="PrestoreExceptionCallBack(page)",
    RequestParams={},
    Method="POST",
    ResponseType="JSON",     
    Message="正在加载"
  }
  page:request(Parameters);
end

local mealList="" --所有的套餐类型数据
local businessList="" --所有的业务类型数据
local moneyList="" --所有的预存金额数据
function PrestoreNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 then
    local data=response["response"]["data"]
    prestoreData=data
    local listView=page:getElementById("strategy_listID")
    listView:getEntity():clearTemplateData()
    local entityData={}
    for i=1,#data,1 do
      mealList=mealList..data[i]["PackageType"]..","
      businessList=businessList..data[i]["BusinessType"]..","
      page:log("businessList=="..businessList)
      moneyList=moneyList..data[i]["Amount"]..","
      entityData[i]={subEntityValues={strategy_ID=data[i]["PackageMode"],effectType_ID=data[i]["EffectType"],explainID=data[i]["Instruction"]},attributes={selected="false"}}
    end
    listView:upDateEntity(entityData)
  else
    page:showToast(response["resultMsg"])
  end
end

function PrestoreExceptionCallBack(page)
  page:showToast("网络异常！")
  page:hideProgreesDialog()
end


-----^_^.^_^.^_^.^_^.^_^-------------步骤1-------------^_^.^_^.^_^.^_^.^_^-----

--调起套餐类型or承诺月份or预存金额的选项框
local spinnerList --下拉列表选项
local spinnerType --套餐类型meal,承诺月份month,预存金额money
local unit
function onSelected(page,this,type)
  spinnerType=type
  if type == "meal" then
    unit="元"
    spinnerList=page:getActivity():getSpinnerList(mealList)
    setElementValue(page,"spinner_typeID","选择套餐类型")
  elseif type == "business" then
    unit=""
    spinnerList=page:getActivity():getSpinnerLists(businessList)
    setElementValue(page,"spinner_typeID","选择业务类型")
    page:log("spinnerList=="..spinnerList)
  elseif type == "money" then
    unit="元"
    spinnerList=page:getActivity():getSpinnerList(moneyList)
    setElementValue(page,"spinner_typeID","选择预存金额")
  end
  
  local spinnerList1=LuaRemove(spinnerList," ")
  local spinnerList2=string.sub(spinnerList1,2,string.len(spinnerList1)-1)
  local spinner=split(spinnerList2,",")
  
  local listView=page:getElementById("selected_listID")
  listView:getEntity():clearTemplateData()
  local entityData={}
  for i=1,#spinner+1,1 do
    if i == 1 then
      entityData[i]={subEntityValues={spinner_nameID=""}}
    else
      entityData[i]={subEntityValues={spinner_nameID=spinner[i-1]..unit}}
    end
  end  
  listView:upDateEntity(entityData)
  
  popupListView:showInFragment("bottom",0,0)
end

--下拉列表选择
function onSpinnerSelected(page,this)
  popupListView:dismiss()
  if spinnerType == "meal" then
    if this:getElementById("spinner_nameID"):getValue() == "" then
      setElementValue(page,"mealID","选择套餐类型")
    else
      setElementValue(page,"mealID",this:getElementById("spinner_nameID"):getValue())
    end
  elseif spinnerType == "business" then
    if this:getElementById("spinner_nameID"):getValue() == "" then
      setElementValue(page,"businessID","选择业务类型")
    else
      setElementValue(page,"businessID",this:getElementById("spinner_nameID"):getValue())
    end
  elseif spinnerType == "money" then
    if this:getElementById("spinner_nameID"):getValue() == "" then
      setElementValue(page,"moneyID","选择预存金额")
    else
      setElementValue(page,"moneyID",this:getElementById("spinner_nameID"):getValue())
    end
  end
  
  resetTimeUi(page)
  onConditionScreening(page)  
  onScreen(page)
end

--搜索
function onSearch(page)
  local searchWord=getElementValue(page,"search_wordID")
  if searchWord == "" or searchWord == nil then
    page:showToast("请输入关键字")
  return
  end
  resetTimeUi(page)
  onConditionScreening(page)
  onScreen(page)  
end

--套餐类型and承诺月份and预存金额的筛选条件判断
local isMeal
local isMonth
local isMoney
local selectedMeal
local selectedMonth
local selectedMoney
function onConditionScreening(page)
  if getElementValue(page,"mealID") == "选择套餐类型" then
    isMeal=false
  else
    isMeal=true
    selectedMeal=string.sub(getElementValue(page,"mealID"),0,string.len(getElementValue(page,"mealID"))-3)
  end  
  if getElementValue(page,"businessID") == "选择业务类型" then
    isMonth=false
  else
    isMonth=true
    selectedMonth=getElementValue(page,"businessID")
  end
  
  if getElementValue(page,"moneyID") == "选择预存金额" then
    isMoney=false
  else
    isMoney=true
    selectedMoney=string.sub(getElementValue(page,"moneyID"),0,string.len(getElementValue(page,"moneyID"))-3)
   
  end
end

--筛选条件，更新策略列表
local strategyID --选择的策略对应的ID
function onScreen(page)
  strategyID=""
  local searchWord=getElementValue(page,"search_wordID")
  local j=0
  local listView=page:getElementById("strategy_listID")
  listView:getEntity():clearTemplateData()
  local entityData={}  
  for i=1,#prestoreData,1 do
    if isMeal == false and isMonth == false and isMoney == false and searchWord == "" then
      entityData[i]={subEntityValues={strategy_ID=prestoreData[i]["PackageMode"],effectType_ID=prestoreData[i]["EffectType"],explainID=prestoreData[i]["Instruction"]},attributes={selected="false"}}
      j=j+1
    elseif isMeal == false and isMonth == false and isMoney == false and searchWord ~= "" then
      if string.find(prestoreData[i]["Instruction"],searchWord) ~= nil then
        entityData[i]={subEntityValues={strategy_ID=prestoreData[i]["PackageMode"],effectType_ID=prestoreData[i]["EffectType"],explainID=prestoreData[i]["Instruction"]},attributes={selected="false"}}
        j=j+1
      end  
    elseif isMeal == false and isMonth == false and isMoney == true and searchWord == "" then
      if prestoreData[i]["Amount"] == selectedMoney then
        entityData[i]={subEntityValues={strategy_ID=prestoreData[i]["PackageMode"],effectType_ID=prestoreData[i]["EffectType"],explainID=prestoreData[i]["Instruction"]},attributes={selected="false"}}
        j=j+1
      end  
    elseif isMeal == false and isMonth == false and isMoney == true and searchWord ~= "" then
      if prestoreData[i]["Amount"] == selectedMoney and string.find(prestoreData[i]["Instruction"],searchWord) ~= nil then
        entityData[i]={subEntityValues={strategy_ID=prestoreData[i]["PackageMode"],effectType_ID=prestoreData[i]["EffectType"],explainID=prestoreData[i]["Instruction"]},attributes={selected="false"}}
        j=j+1
      end  
    elseif isMeal == false and isMonth == true and isMoney == false and searchWord == "" then
      if prestoreData[i]["BusinessType"] == selectedMonth then
        entityData[i]={subEntityValues={strategy_ID=prestoreData[i]["PackageMode"],effectType_ID=prestoreData[i]["EffectType"],explainID=prestoreData[i]["Instruction"]},attributes={selected="false"}}
        j=j+1
      end  
    elseif isMeal == false and isMonth == true and isMoney == false and searchWord ~= "" then
      if prestoreData[i]["BusinessType"] == selectedMonth and string.find(prestoreData[i]["Instruction"],searchWord) ~= nil then
        entityData[i]={subEntityValues={strategy_ID=prestoreData[i]["PackageMode"],effectType_ID=prestoreData[i]["EffectType"],explainID=prestoreData[i]["Instruction"]},attributes={selected="false"}}
        j=j+1
      end  
    elseif isMeal == false and isMonth == true and isMoney == true and searchWord == "" then
      if prestoreData[i]["BusinessType"] == selectedMonth and prestoreData[i]["Amount"] == selectedMoney then
        entityData[i]={subEntityValues={strategy_ID=prestoreData[i]["PackageMode"],effectType_ID=prestoreData[i]["EffectType"],explainID=prestoreData[i]["Instruction"]},attributes={selected="false"}}
        j=j+1
      end  
    elseif isMeal == false and isMonth == true and isMoney == true and searchWord ~= "" then
      if prestoreData[i]["BusinessType"] == selectedMonth and prestoreData[i]["Amount"] == selectedMoney and string.find(prestoreData[i]["Instruction"],searchWord) ~= nil then
        entityData[i]={subEntityValues={strategy_ID=prestoreData[i]["PackageMode"],effectType_ID=prestoreData[i]["EffectType"],explainID=prestoreData[i]["Instruction"]},attributes={selected="false"}}
        j=j+1
      end
    elseif isMeal == true and isMonth == false and isMoney == false and searchWord == "" then
      if prestoreData[i]["PackageType"] == selectedMeal then
        entityData[i]={subEntityValues={strategy_ID=prestoreData[i]["PackageMode"],effectType_ID=prestoreData[i]["EffectType"],explainID=prestoreData[i]["Instruction"]},attributes={selected="false"}}
        j=j+1
      end  
    elseif isMeal == true and isMonth == false and isMoney == false and searchWord ~= "" then
      if prestoreData[i]["PackageType"] == selectedMeal and string.find(prestoreData[i]["Instruction"],searchWord) ~= nil then
        entityData[i]={subEntityValues={strategy_ID=prestoreData[i]["PackageMode"],effectType_ID=prestoreData[i]["EffectType"],explainID=prestoreData[i]["Instruction"]},attributes={selected="false"}}
        j=j+1
      end  
    elseif isMeal == true and isMonth == false and isMoney == true and searchWord == "" then
      if prestoreData[i]["PackageType"] == selectedMeal and prestoreData[i]["Amount"] == selectedMoney then
        entityData[i]={subEntityValues={strategy_ID=prestoreData[i]["PackageMode"],effectType_ID=prestoreData[i]["EffectType"],explainID=prestoreData[i]["Instruction"]},attributes={selected="false"}}
        j=j+1
      end  
    elseif isMeal == true and isMonth == false and isMoney == true and searchWord ~= "" then
      if prestoreData[i]["PackageType"] == selectedMeal and prestoreData[i]["Amount"] == selectedMoney and string.find(prestoreData[i]["Instruction"],searchWord) ~= nil then
        entityData[i]={subEntityValues={strategy_ID=prestoreData[i]["PackageMode"],effectType_ID=prestoreData[i]["EffectType"],explainID=prestoreData[i]["Instruction"]},attributes={selected="false"}}
        j=j+1
      end  
    elseif isMeal == true and isMonth == true and isMoney == false and searchWord == "" then
      if prestoreData[i]["PackageType"] == selectedMeal and prestoreData[i]["BusinessType"] == selectedMonth then
        entityData[i]={subEntityValues={strategy_ID=prestoreData[i]["PackageMode"],effectType_ID=prestoreData[i]["EffectType"],explainID=prestoreData[i]["Instruction"]},attributes={selected="false"}}
        j=j+1
      end  
    elseif isMeal == true and isMonth == true and isMoney == false and searchWord ~= "" then
      if prestoreData[i]["PackageType"] == selectedMeal and prestoreData[i]["BusinessType"] == selectedMonth and string.find(prestoreData[i]["Instruction"],searchWord) ~= nil then
        entityData[i]={subEntityValues={strategy_ID=prestoreData[i]["PackageMode"],effectType_ID=prestoreData[i]["EffectType"],explainID=prestoreData[i]["Instruction"]},attributes={selected="false"}}
        j=j+1
      end  
    elseif isMeal == true and isMonth == true and isMoney == true and searchWord == "" then
      if prestoreData[i]["PackageType"] == selectedMeal and prestoreData[i]["BusinessType"] == selectedMonth and prestoreData[i]["Amount"] == selectedMoney then
        entityData[i]={subEntityValues={strategy_ID=prestoreData[i]["PackageMode"],effectType_ID=prestoreData[i]["EffectType"],explainID=prestoreData[i]["Instruction"]},attributes={selected="false"}}
        j=j+1
      end  
    elseif isMeal == true and isMonth == true and isMoney == true and searchWord ~= "" then
      if prestoreData[i]["PackageType"] == selectedMeal and prestoreData[i]["BusinessType"] == selectedMonth and prestoreData[i]["Amount"] == selectedMoney and string.find(prestoreData[i]["Instruction"],searchWord) ~= nil then
        entityData[i]={subEntityValues={strategy_ID=prestoreData[i]["PackageMode"],effectType_ID=prestoreData[i]["EffectType"],explainID=prestoreData[i]["Instruction"]},attributes={selected="false"}}
        j=j+1
      end  
    end
  end
  listView:upDateEntity(entityData)
  
  if j > 0 then
    setElementHidden(page,"strategy_listID",false)
  elseif j == 0 then
    setElementHidden(page,"strategy_listID",true)
  end
end

--选择生效时间
local lastStart
local startTime --选择的生效时间，立即生效=2，下月生效=3，自定义=0
local bookingFlag --是否预约订购，否0，是1，立即与下月生效传0，自定义传1
local bookingTime --预约订购时间，立即与下月生效传空，自定义传年月日000000
local startTimeFlag = true
function onStartSelected(page,this,type)
  if startTimeFlag then
  lastStart = page:getElementById("default_timeID")
  startTimeFlag = false
  end
  if lastStart ~= nil and lastStart ~= this then
    lastStart:setSelected(false)
  end
  lastStart=this
  this:setSelected(true)
   
  if type == "now" then
    startTime="2"
    bookingFlag="0"
    bookingTime=""
    setElementValue(page,"effective_timeID",os.date("%Y-%m-%d").." 00:00:00")
    setElementValue(page,"selected_timeID",this:getValue())
  elseif type == "next" then
    startTime="3"
    bookingFlag="0"
    bookingTime=""
    setElementValue(page,"effective_timeID","nextMonth")
    setElementValue(page,"selected_timeID",this:getValue())
  elseif type == "freedom" then
    startTime="0"
    bookingFlag="0"
    setElementValue(page,"selected_timeID",this:getValue())
    --page:getActivity():DateTimePicker()
  end
end

function DatePickerResult(page)
  local dateTime=page:getActivity():getDateTime()
    bookingTime=string.sub(dateTime,0,4)..string.sub(dateTime,6,7)..string.sub(dateTime,9,10).."000000"
    startTime="0"
    bookingFlag="1"
    setElementValue(page,"effective_timeID",dateTime.." 00:00:00")
    setElementValue(page,"selected_timeID",dateTime)
end

--选择策略
local strategyName --选择策略的名称
local nextStrategy
local effectTypeID
function onMealSelected(page,this)
  strategyID=this:getElementById("strategy_ID"):getValue()
  effectTypeID=this:getElementById("effectType_ID"):getValue()
  strategyName=this:getElementById("explainID"):getValue()
  if nextStrategy ~= nil and nextStrategy ~= this then
    nextStrategy:setSelected(false)
  end
  nextStrategy=this
  this:setSelected(true) 
  page:log("生效方式effectTypeID=="..effectTypeID)
  resetTimeUi(page)
  resetTime(page)
end

--根据选中的策略,来重新设置生效时间.
function resetTime(page)
  local effectLen=string.len(effectTypeID)
  if effectLen == 1 then
     setElementHidden(page,"btn_nextMonthID",true)  
     setElementHidden(page,"btn_startNowID",true)  
     return
  end
  if effectLen == 5 then
     return
  end
  if effectLen == 3 then
     local effect=split(effectTypeID,",")
     local oneNum = effect[1]
     local twoNum = effect[2]
     local TimeNum = oneNum+twoNum
     page:log("生效时间的数字和===="..TimeNum)
     if TimeNum == 2 then
        setElementHidden(page,"btn_nextMonthID",true)  
     elseif TimeNum == 3 then
        setElementHidden(page,"btn_startNowID",true) 
     elseif TimeNum == 5 then   
        setElementHidden(page,"default_timeID",true) 
     end
  end
end

function resetTimeUi(page)
   setElementHidden(page,"default_timeID",false)  
   setElementHidden(page,"btn_nextMonthID",false)  
   setElementHidden(page,"btn_startNowID",false)  
end

--下一步
function onNextStep(page)
  local selectedTime=getElementValue(page,"selected_timeID")
  if selectedTime == "" or selectedTime == nil then
    page:showToast("请选择生效时间")
  return
  end
  
  if strategyID == "" or strategyID == nil then
    page:showToast("请选择策略")
  return
  end

  local Parameters={
    NetSelector="NetData:",                
    URL="/storedFee/storedFeeOrderCheck.do",
    LuaNetCallBack="CheckNetCallBack(page,{response})",
    LuaExceptionCallBack="CheckExceptionCallBack(page)",
    RequestParams={phoneNum=mobileNum,channel="",custID="",custType="1",opType="0",prodId=strategyID,effType=startTime,bookingFlag=bookingFlag,bookingTime=bookingTime},
    Method="POST",
    ResponseType="JSON",     
    Message="正在校验"
  }
  page:request(Parameters);
end

local orderSeq
function CheckNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  if response["resultCode"] == 0 then
    local data=response["response"]["data"]
    setElementHidden(page,"group_applyID",true)    
    setElementHidden(page,"group_apply2ID",false)

    orderSeq=data["OrderSeq"]
    setElementValue(page,"pay_nameID",strategyName)
    setElementValue(page,"pay_discountID","￥"..data["DeratePrice"]/100)
    setElementValue(page,"pay_actualID","￥"..data["Fee"]/100)
    onPaymentSelected(page,posButton,"pos")
    
    onStartImageResult(page)
  else
    setElementValue(page,"error_wordID",response["resultMsg"])
    errorPopupView:showInFragment("center",0,0)
  end
end

function CheckExceptionCallBack(page)
  page:showToast("网络异常！")
  page:hideProgreesDialog()
end

function onErrorSure(page)
  errorPopupView:dismiss()
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


-----^_^.^_^.^_^.^_^.^_^-------------步骤2-------------^_^.^_^.^_^.^_^.^_^-----

--返回上一步
function onLastBack(page)
  setElementHidden(page,"group_applyID",false)
  setElementHidden(page,"group_apply2ID",true)
end

--选择POS机or现金
local nextPay
local paySelected --支付方式选择，POS机=pos，现金=cash
function onPaymentSelected(page,this,type)
  if nextPay ~= nil and nextPay ~= this then
    nextPay:setSelected(false)
  end
  nextPay=this
  this:setSelected(true)
  
  paySelected=type
  if type == "pos" then
    setElementHidden(page,"group_posID",false)
  elseif type == "cash" then
    setElementHidden(page,"group_posID",true)
  end
end

--业务单详情
function onProtocol(page)
  local signatureURL=getElementValue(page,"signature_urlID")
  local effectiveTime=getElementValue(page,"effective_timeID")
  local discount=string.sub(getElementValue(page,"pay_discountID"),4,string.len(getElementValue(page,"pay_discountID")))
  local actual=string.sub(getElementValue(page,"pay_actualID"),4,string.len(getElementValue(page,"pay_actualID")))
  local bundle=page:initBundle()
    bundle:putString("protocol","prestore")
    bundle:putString("signatureURL",signatureURL)
    bundle:putString("discount",discount)
    bundle:putString("actual",actual)
    bundle:putString("effectiveTime",effectiveTime)
    page:setCustomAnimations("left")
    page:nextPage("res://protocolPage.xml",false,bundle) 
end

function onPageResume(page)
  local signaturePath=page:getArguments():getString("signaturePath")
  if signaturePath ~= getElementValue(page,"signature_urlID") and signaturePath ~= nil then
    setElementValue(page,"signature_urlID",signaturePath)
    setElementValue(page,"signatureID","local://"..signaturePath)
  end
end

--拍照
function onPicture(page,this)
  local isPhotograph=this:getElementById("picture_typeID"):getValue()
  if isPhotograph == "photograph" then
    page:getActivity():photograph(mobileNum,"PayCharge")
  elseif isPhotograph == "looking" then
    onBigImage(page)
  end
end

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
  setElementEnable(page,"btn_submitID",true)
  
  local path=split(picturePath,",")
  local size=table.getn(path)
  local postFilePaths=getImagePostFilePaths(page,signaturePath,path,size)  
  local remark=getElementValue(page,"remarkID")
  local Parameters={
    NetSelector="NetData:",                
    URL="/client/imgsUpload.do",
    LuaNetCallBack="PictureSubmitNetCallBack(page,{response})",
    LuaExceptionCallBack="PictureSubmitExceptionCallBack(page)",
    RequestParams={phoneNum=mobileNum,businessType="预存送",remark=remark},
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
    setElementEnable(page,"btn_submitID",false)
    page:showToast("图片上传失败")
  end
end

function PictureSubmitExceptionCallBack(page)
  setElementEnable(page,"btn_submitID",false)
  page:showToast("网络异常！")
  page:hideProgreesDialog()
end

function onBusinessHandle(page)
  local fee=getElementValue(page,"pay_actualID")
  local newFee=string.sub(fee,4,string.len(fee))  
  
  local payType
  local paySeq
  if paySelected == "pos" then
    payType="5"
    paySeq=getElementValue(page,"pos_numID")
  elseif paySelected == "cash" then
    payType="1"
    paySeq=""
  end

  page:getActivity():showProgreesDialog("正在办理")
  local Parameters={
    NetSelector="NetData:",                
    URL="/storedFee/storedFeeCommitOrder.do",
    LuaNetCallBack="BusinessHandleNetCallBack(page,{response})",
    LuaExceptionCallBack="BusinessHandleExceptionCallBack(page)",
    RequestParams={phoneNum=mobileNum,channel="",orderSeq=orderSeq,payType=payType,fee=""..newFee*100,payMode="",payOrg="",paySeq=paySeq,custId="",custType="1",isRecommend=isRecom,cospCode=commodityId},           
    Method="POST",
    ResponseType="JSON",    
--    Message="正在办理"
  }
  page:request(Parameters);
end

function BusinessHandleNetCallBack(page,response)
  page:log("*************************"..response["resultCode"])
  page:log(response["response"])
  page:getActivity():hideProgreesDialog()
  if response["resultCode"] == 0 then
    setElementEnable(page,"btn_submitID",false)
    setElementHidden(page,"group_apply2ID",true)
    setElementHidden(page,"group_passID",false)
    --倒计时
    onCountDown(page,"btn_prestoreID")
    setElementValue(page,"order_NumID",response["response"]["data"]["OrderSeq"])
  else
    setElementValue(page,"fail_wordID",response["resultMsg"])
    setElementHidden(page,"group_apply2ID",true)
    setElementHidden(page,"group_failID",false)
  end
end

function BusinessHandleExceptionCallBack(page)
  setElementEnable(page,"btn_submitID",false)
  page:showToast("网络异常！")
  page:getActivity():hideProgreesDialog()
end


-----^_^.^_^.^_^.^_^.^_^-------------成功or失败-------------^_^.^_^.^_^.^_^.^_^-----

--一键回退
function onFallback(page)
  reasonPopupView:showInFragment("center",0,0)
end

function onFallbackSure(page)
  reasonPopupView:dismiss()
  local orderNum=getElementValue(page,"order_NumID")
  local reason=getElementValue(page,"reasonID")
  local Parameters={
    NetSelector="NetData:",                
    URL="/client/cancelOrder.do",
    LuaNetCallBack="FallbackNetCallBack(page,{response})",
    LuaExceptionCallBack="FallbackExceptionCallBack(page)",
    RequestParams={phoneNum=mobileNum,channel="",IP="",orderSeq=orderNum,reason=reason,payType="1"},
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
    setElementHidden(page,"group_applyID",false)
    setElementHidden(page,"group_passID",true)
    page:showToast("回退成功")
  else
    page:showToast(response["resultMsg"])
  end
end

function FallbackExceptionCallBack(page)
  page:showToast("网络异常！")
  page:hideProgreesDialog()
end

--本次服务已完成
function onCustomerQuit(page)
  onCustomerLoginQuit(page)
end

--重新进行
function onAgain(page)
  setElementEnable(page,"btn_submitID",false)
  setElementHidden(page,"group_apply2ID",false)
  setElementHidden(page,"group_failID",true)
end

function goBack(page)
  page:setCustomAnimations("right")
  page:goBack()  
end

function onBackPressed(page)
  goBack(page)
end